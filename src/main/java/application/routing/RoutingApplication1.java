package application.routing;

import EnvironmentAPI.SensorEnvironment;
import application.Application;
import application.routing.heuristic.SimplePollutionHeuristic;
import be.kuleuven.cs.som.annotate.Model;
import iot.Environment;
import iot.GlobalClock;
import iot.SimulationRunner;
import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.lora.MessageType;
import iot.mqtt.BasicMqttMessage;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import selfadaptation.instrumentation.FeedbackLoopGatewayBuffer;
import selfadaptation.instrumentation.MoteProbe;
import util.*;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoutingApplication1 extends RoutingApplication implements Cloneable {
    // The routes stored per device
    private Map<Long, Pair<Double, List<GeoPosition>>> routes;

    // The last recorded positions of the requesting user motes
    private Map<Long, GeoPosition> lastPositions;

    // The graph with waypoints and connections
    private GraphStructure graph;

    // The route finding algorithm that is used to handle routing requests
    private PathFinder pathFinder;

    private MoteProbe moteProbe;

    private RouteAnalyser pathAnalyser;

    /**
     Sets the buffersizeWidth used by this application
     How much paths could we save in each step in the adaptation algorithm
     The amount of best paths that we must search for by using the K-A* algorithm
     **/
    private int bufferSizeHeight;

    /**
     buffer size width used by the adaptation algorithm
     * How much paths could we save in each step in the adaptation algorithm
     **/
    private int bufferSizeWidth;

    private long beginTime;

    private Map<Long, Integer> amountAdaptations;

    private Map<Long, Long> maxTime;

    private Map<Long, Pair<Integer, Long>> averageTimeForDecisionPerMote;

    @Override
    public Map<Long, Pair<Integer, Long>> getAverageTimeForDecisionPerMote() {
        return averageTimeForDecisionPerMote;
    }

    @Override
    public Map<Long, Integer> getAmountAdaptations() {
        return this.amountAdaptations;
    }

    /**
     * Sets the buffersizeHeight used by this application
     *
     * @param bufferSizeHeight: buffersizeHeight used by this application
     */
    @Override
    public void setBufferSizeHeight(int bufferSizeHeight) {
        this.bufferSizeHeight = bufferSizeHeight;
    }

    /**
     * Sets the buffersizeWidth used by this application
     *
     * @param bufferSizeWidth: buffersizeWidth used by this application
     */
    @Override
    public void setBufferSizeWidth(int bufferSizeWidth) {
        this.bufferSizeWidth = bufferSizeWidth;
    }

    @Override
    public int getBufferSizeHeight() {
        return this.bufferSizeHeight;
    }

    @Override
    public int getBufferSizeWidth() {
        return this.bufferSizeWidth;
    }

    /**
     * A HashMap representing the buffers for the approach.
     */
    @Model
    private Map<Long, List<List<Pair<Double, List<GeoPosition>>>>> bufferKBestPaths;

    /**
     * Returns the algorithm buffers.
     *
     * @return The algorithm buffers.
     */
    @Model
    private Map<Long, List<List<Pair<Double, List<GeoPosition>>>>> getBufferKBestPaths() {
        return this.bufferKBestPaths;
    }

    /**
     * Puts an KBestPathBuffer under the KBestPathBuffers under a path.
     *
     * @param kPaths The k best paths according with its accumulated cost
     */
    @Model
    private void putKBestPathsBuffers(long moteID, List<Pair<Double, List<GeoPosition>>> kPaths) {
        if (this.bufferKBestPaths.get(moteID) == null) {
            this.bufferKBestPaths.put(moteID, new ArrayList<List<Pair<Double, List<GeoPosition>>>>());
        }
        this.bufferKBestPaths.get(moteID).add(kPaths);
    }



    @Override
    public Map<Long, Long> getMaxTime() {
        return this.maxTime;
    }

    public RoutingApplication1(PathFinder pathFinder, Environment environment) {
        super(pathFinder,environment);
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.graph = environment.getGraph();
        this.pathFinder = pathFinder;
        bufferKBestPaths = new HashMap<>();
        this.moteProbe = new MoteProbe();
        this.amountAdaptations = new HashMap<>();
        this.averageTimeForDecisionPerMote = new HashMap<>();
        this.maxTime = new HashMap<>();
        for (Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                long time = 0;
                amountAdaptations.put(mote.getEUI(), 0);
                averageTimeForDecisionPerMote.put(mote.getEUI(), new Pair(0, time));
                maxTime.put(mote.getEUI(), time);

            }
        }
        beginTime = 31380L;
    }

    /**
     * Handles a route request without sending packets
     * It gives directly the next part of the route to the mote
     *
     * @param mote Usermote where we would send a next part of the route to it
     * @param environment the environment that we use for the simulation
     */
    @Override
    public void handleRouteRequestWithoutNetwork(UserMote mote, Environment environment, SensorEnvironment sensorEnvironment) {
            System.out.println("Time: " + environment.getClock().getTime().toNanoOfDay());
            GeoPosition currentPosition= environment.getMapHelper().toGeoPosition(mote.getPosInt());
            //System.out.println("Time " + moteStartTime);
            GeoPosition endPosition = mote.getDestination();
            // Use the routing algorithm to calculate the K best paths for the mote
            List<Pair<Double,List<GeoPosition>>> routeMote = this.pathFinder.retrieveKPaths(graph,currentPosition,mote.getDestination(),bufferSizeWidth);
            putKBestPathsBuffers(mote.getEUI(), routeMote);
            Pair<Double, List<GeoPosition>> bestPath;
            if (bufferKBestPaths.get(mote.getEUI()).size() == bufferSizeHeight) {
                bestPath = takeBestPath(bufferKBestPaths.get(mote.getEUI()));
                bufferKBestPaths = new HashMap<>();
                if (bestPath.getRight().size() > 0) {
                    if (routes.get(mote.getEUI()) != null) {
                        List<GeoPosition> path = routes.get(mote.getEUI()).getRight();
                        if (!(bestPath.getRight().equals(path))) {
                            int amountAdaptations = getAmountAdaptations().get(mote.getEUI()) + 1;
                            getAmountAdaptations().put(mote.getEUI(), amountAdaptations);

                        }
                    }
                    this.routes.put(mote.getEUI(), bestPath);
                }
                else {
                    bestPath = this.routes.get(mote.getEUI());
                    this.routes.put(mote.getEUI(), bestPath);
                }
            } else {
                if (routes.get(mote.getEUI()) == null) {
                    bestPath = this.pathFinder.retrievePath(graph,currentPosition, mote.getDestination());
                    this.routes.put(mote.getEUI(), bestPath);
                } else {
                    bestPath = this.routes.get(mote.getEUI());
                    if (bestPath.getRight().size() > 1) {
                        this.routes.put(mote.getEUI(), bestPath);
                    }
                }
            }
            Path motePath = mote.getPath();
            motePath.addPosition(bestPath.getRight().get(1));
            mote.setPath(motePath.getWayPoints());
            this.routes.get(mote.getEUI()).getRight().remove(0);
    }

    /**
     * Calculate all the routingApplications of every mote in the environment before simulating
     * It saves a hashmap where the keys are all the geopositions where there happens an adaptation of the path
     * and the values is the changed path
     *
     * @param environment the environment where we would do the simulation
     * @param sensorEnvironment the sensorenvironment that we would use for the simulation
     */
    @Override
    public void calculateRoutingAdaptations(Environment environment, SensorEnvironment sensorEnvironment) {
        HashMap<Mote,HashMap<GeoPosition, List<GeoPosition>>> changesPath = new HashMap<>();
        long currentTime = 2000L;
        HashMap<UserMote,GeoPosition> currentPositionMote = new HashMap<>();
        HashMap<UserMote,Long> currentTimeMote = new HashMap<>();
        for(Mote mote : environment.getMotes()){
            if(mote instanceof UserMote) {
                GeoPosition currentPosition = environment.getMapHelper().toGeoPosition(mote.getPosInt());
                currentPositionMote.put((UserMote) mote, currentPosition);
                currentTimeMote.put((UserMote) mote, currentTime);
                changesPath.put(mote,new HashMap<>());
            }
        }
        long i = 0;
        while (i <= currentTime) {
            sensorEnvironment.getPoll().doStep(i*1000000,sensorEnvironment.getSensors().get(0).getEnvironment());
            i++;
        }
        while(notAllMotesFinished(currentPositionMote))
            {
                long finalCurrentTime = currentTime;
                environment.getMotes().stream()
                .filter(mote -> mote instanceof UserMote)
                .filter(mote -> currentTimeMote.get(mote).equals(finalCurrentTime))
                .filter(mote -> !(currentPositionMote.get(mote).equals(((UserMote) mote).getDestination())))
                .forEach(mote -> {
                    GeoPosition endPosition = ((UserMote) mote).getDestination();
                    // Use the routing algorithm to calculate the K best paths for the mote
                    List<Pair<Double, List<GeoPosition>>> routeMote = this.pathFinder.retrieveKPaths(graph, currentPositionMote.get(mote), endPosition, bufferSizeWidth, finalCurrentTime / 1000, mote.getMovementSpeed());
                    putKBestPathsBuffers(mote.getEUI(), routeMote);
                    Pair<Double, List<GeoPosition>> bestPath;
                    if (bufferKBestPaths.get(mote.getEUI()).size() == bufferSizeHeight) {
                        bestPath = takeBestPath(bufferKBestPaths.get(mote.getEUI()));
                        bufferKBestPaths = new HashMap<>();
                        if (bestPath.getRight().size() > 0) {
                            if (routes.get(mote.getEUI()) != null) {
                                List<GeoPosition> path = routes.get(mote.getEUI()).getRight();
                                if (!(bestPath.getRight().equals(path))) {
                                    int amountAdaptations = getAmountAdaptations().get(mote.getEUI()) + 1;
                                    getAmountAdaptations().put(mote.getEUI(), amountAdaptations);
                                    List<GeoPosition> route = new ArrayList<GeoPosition>(bestPath.getRight());
                                    HashMap<GeoPosition, List<GeoPosition>> changePath = changesPath.get(mote);
                                    changePath.put(currentPositionMote.get(mote), route);
                                    changesPath.put(mote, changePath);
                                    this.routes.put(mote.getEUI(), bestPath);
                                }
                            }
                            else{
                                this.routes.put(mote.getEUI(), bestPath);
                                List<GeoPosition> route = new ArrayList<GeoPosition>(bestPath.getRight());
                                HashMap<GeoPosition, List<GeoPosition>> changePath = changesPath.get(mote);
                                changePath.put(currentPositionMote.get(mote), route);
                                changesPath.put(mote, changePath);
                                System.out.println(route);
                            }

                        } else {
                            bestPath = this.routes.get(mote.getEUI());
                            this.routes.put(mote.getEUI(), bestPath);
                        }
                    } else {
                        if (routes.get(mote.getEUI()) == null) {
                            bestPath = this.pathFinder.retrieveKPaths(graph, currentPositionMote.get(mote), endPosition, bufferSizeWidth, finalCurrentTime / 1000, mote.getMovementSpeed()).get(0);
                            this.routes.put(mote.getEUI(), bestPath);
                            List<GeoPosition> route = new ArrayList<GeoPosition>(bestPath.getRight());
                            HashMap<GeoPosition, List<GeoPosition>> changePath = changesPath.get(mote);
                            changePath.put(currentPositionMote.get(mote), route);
                            changesPath.put(mote, changePath);
                        } else {
                            bestPath = this.routes.get(mote.getEUI());
                            if (bestPath.getRight().size() > 1) {
                                this.routes.put(mote.getEUI(), bestPath);
                            }
                        }
                    }
                    this.routes.get(mote.getEUI()).getRight().remove(0);
                    Double distance = MapHelper.distance(currentPositionMote.get(mote), this.routes.get(mote.getEUI()).getRight().get(0));
                    //System.out.println("Distance "  + distance);
                    long time = currentTimeMote.get(mote);
                    time += distance * 1000 / mote.getMovementSpeed() * 1000;
                    currentPositionMote.put((UserMote) mote, this.routes.get(mote.getEUI()).getRight().get(0));
                    currentTimeMote.put((UserMote) mote,time);

                });
                sensorEnvironment.getPoll().doStep(beginTime*1000000,sensorEnvironment.getSensors().get(0).getEnvironment());
                currentTime ++;

            }
            environment.getMotes().stream()
                .filter(mote-> mote instanceof UserMote)
                .forEach(mote->{
                    ((UserMote) mote).setChangesPath(changesPath.get(mote));
                });

        }


    /**
     * Determines if all motes are finished
      * @param positionsMote hash map that contains for every mote the current position of that mote
     * @return true if current position of mote is equal to its destination, false otherwise
     */
    private boolean notAllMotesFinished(HashMap<UserMote,GeoPosition> positionsMote){
        for (UserMote mote : positionsMote.keySet()) {
            if (!(positionsMote.get(mote).equals(mote.getDestination())))
                return true;
        }
        return false;
    }


    /**
     * Handle a route request message by replying with (part of) the route to the requesting device.
     * @param message The message which contains the route request.
     */
    private void handleRouteRequest(LoraWanPacket message) {
        long startTime = System.nanoTime();
        var body = Arrays.stream(Converter.toObjectType(message.getPayload()))
            .skip(1) // Skip the first byte since this indicates the message type
            .collect(Collectors.toList());
        long deviceEUI = message.getSenderEUI();

        GeoPosition motePosition;
        GeoPosition destinationPosition;

        if (!lastPositions.containsKey(deviceEUI)) {
            // This is the first request the mote has made for a route
            //  -> both the current position as well as the destination of the mote are transmitted
            byte[] rawPositions = new byte[16];
            IntStream.range(0, 16).forEach(i -> rawPositions[i] = body.get(i));

            ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);
            motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
            destinationPosition = new GeoPosition(byteBuffer.getFloat(8), byteBuffer.getFloat(12));
        } else {
            // The mote has already sent the initial request
            //  -> only the current position of the mote is transmitted (the destination has been stored already)
            byte[] rawPositions = new byte[8];
            IntStream.range(0, 8).forEach(i -> rawPositions[i] = body.get(i));
            ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);

            motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
            destinationPosition = routes.get(deviceEUI).getRight().get(routes.get(deviceEUI).getRight().size()-1);
        }

        // Use the routing algorithm to calculate the K best paths for the mote
        //List<GeoPosition> routeMote=this.pathFinder.retrievePath(graph,motePosition,destinationPosition);
        List<Pair<Double,List<GeoPosition>>> routeMote = this.pathFinder.retrieveKPaths(graph, motePosition, destinationPosition,bufferSizeWidth);
        putKBestPathsBuffers(deviceEUI,routeMote);
        Pair<Double,List<GeoPosition>> bestPath;
        if(bufferKBestPaths.get(deviceEUI).size()==bufferSizeHeight) {
            bestPath = takeBestPath(bufferKBestPaths.get(deviceEUI));
            bufferKBestPaths = new HashMap<>();
            if(bestPath.getRight().size()>0) {
                if(routes.get(deviceEUI)!=null) {
                    List<GeoPosition> path = routes.get(deviceEUI).getRight();
                    if (!(bestPath.getRight().equals(path))) {
                        int amountAdaptations = getAmountAdaptations().get(deviceEUI) + 1;
                        getAmountAdaptations().put(deviceEUI,amountAdaptations);
                    }
                }
                this.routes.put(deviceEUI, bestPath);
            }
            else {
                bestPath = this.routes.get(deviceEUI);
                this.routes.put(deviceEUI, bestPath);
            }
        }
        else {
            if (routes.get(deviceEUI) == null) {
                bestPath = this.pathFinder.retrievePath(graph,motePosition, destinationPosition);
                this.routes.put(deviceEUI, bestPath);
            } else {
                bestPath = this.routes.get(deviceEUI);
                if(bestPath.getRight().size()>1) {
                    this.routes.put(deviceEUI, bestPath);
                }
            }
        }
        long endTime = System.nanoTime();
        // time we needed to determine next part of the route
        long elapsedSeconds = endTime - startTime;
        elapsedSeconds = elapsedSeconds + averageTimeForDecisionPerMote.get(deviceEUI).getRight();
        // update eventually the maximum time
        if (maxTime.get(deviceEUI)<elapsedSeconds){
            maxTime.put(deviceEUI,elapsedSeconds);
        }
        // update amount of determinations
        int step = averageTimeForDecisionPerMote.get(deviceEUI).getLeft() + 1;
        // save amount of determinations together with total cost in time of all determinations till now of the mote
        averageTimeForDecisionPerMote.put(deviceEUI,new Pair(step,elapsedSeconds));
        // Compose the reply packet: up to 24 bytes for now, which can store 1 geoposition (in float)
        int amtPositions = Math.min(bestPath.getRight().size() - 1, 1);
        ByteBuffer payloadRaw = ByteBuffer.allocate(8 * amtPositions);

        for (GeoPosition pos : bestPath.getRight().subList(1, amtPositions+1)) {
            payloadRaw.putFloat((float) pos.getLatitude());
            payloadRaw.putFloat((float) pos.getLongitude());
        }

        List<Byte> payload = new ArrayList<>();
        for (byte b : payloadRaw.array()) {
            payload.add(b);
        }

        // Update the position of the mote if it has changed since the previous time
        if (!lastPositions.containsKey(deviceEUI) || !lastPositions.get(deviceEUI).equals(motePosition)) {
            lastPositions.put(deviceEUI, motePosition);
        }

        // Send the reply (via MQTT) to the requesting device
        BasicMqttMessage routeMessage = new BasicMqttMessage(payload);
        this.mqttClient.publish(Topics.getAppToNetServer(message.getReceiverEUI(), deviceEUI), routeMessage);
        // Remove first way point of the route
        this.routes.get(deviceEUI).getRight().remove(0);
    }

    private Pair<Double,List<GeoPosition>> takeBestPath(List<List<Pair<Double, List<GeoPosition>>>> bufferKBestPaths) {
        List<Pair<Double,List<GeoPosition>>> lastMeasure = bufferKBestPaths.get(bufferKBestPaths.size()-1);
        HashMap<Integer,Pair<Integer,Double>> average = new HashMap<>();
        for(int k = 0;k<= lastMeasure.size()-1;k++){
            Pair<Double,List<GeoPosition>> path = lastMeasure.get(k);
            average.put(k,new  Pair(1,path.getLeft()));
            for (int i =0;i <= bufferKBestPaths.size()-2;i++)
            {
                for (int j = 0; j <= bufferKBestPaths.get(i).size()-1; j++)
                {
                    if(containsList(bufferKBestPaths.get(i).get(j).getRight(),path.getRight()))
                    {
                        //System.out.println(k);
                        int amount = average.get(k).getLeft() + 1;
                        double accumulatedCost = average.get(k).getRight() + bufferKBestPaths.get(i).get(j).getLeft();
                        average.put(k,new Pair(amount,accumulatedCost));
                        j = 10; //there couldn't be two same routes for the same measurement
                    }

                }
            }
        }
        Iterator it = average.entrySet().iterator();
        List<Pair<Integer,Double>> PathsIdWithMostAppearances = new ArrayList<>();
        int mostappearances = 0;
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            int iD = (Integer)pair.getKey();
            Pair<Integer,Double> value = (Pair<Integer, Double>)pair.getValue();
            if (value.getLeft()>mostappearances)
            {
                mostappearances = value.getLeft();
                PathsIdWithMostAppearances = new ArrayList<Pair<Integer,Double>>();
                Pair<Integer,Double>IdWithPath = new Pair(iD,value.getRight());
                PathsIdWithMostAppearances.add(IdWithPath);
            }
            else if (value.getLeft()==mostappearances)
            {
                Pair<Integer,Double>IdWithPath = new Pair(iD,value.getRight());
                PathsIdWithMostAppearances.add(IdWithPath);
            }
            it.remove();
        }
        double bestAverage = 1000000000;
        Pair<Double,List<GeoPosition>> bestRoute = lastMeasure.get(0);
        for (Pair<Integer,Double> o:PathsIdWithMostAppearances) {
            double averageCost = o.getRight()/mostappearances;
            if(averageCost < bestAverage)
            {
                bestAverage = averageCost;
                int index = o.getLeft();
                bestRoute = lastMeasure.get(index);
            }
        }
        return bestRoute;

    }

    /**
     * Determines if route is inserted in a greater, previous route
     * @param list the given list where we would determine if the given sublist is a sublist of this list
     * @param sublist the list where we would determine if it a sublist of the given list
     * @return true if sublist is a sublist of the list, false otherwise
     */
    private boolean containsList(List<GeoPosition> list, List<GeoPosition> sublist) {
        return Collections.indexOfSubList(list, sublist) != -1;
    }


    /**
     * Get a stored route for a specific mote.
     * @param mote The mote from which the cached route is requested.
     * @return The route as a list of geo coordinates.
     */
    @Override
    public List<GeoPosition> getRoute(Mote mote) {
        if (routes.containsKey(mote.getEUI())) {
            return routes.get(mote.getEUI()).getRight();
        }
        return new ArrayList<>();
    }

    @Override
    public void consumePackets(String topicFilter, TransmissionWrapper transmission) {
        if(active) {
            var message = transmission.getTransmission().getContent();
            // Only handle packets with a route request
            var messageType = message.getPayload()[0];
            if (messageType == MessageType.REQUEST_PATH.getCode() || messageType == MessageType.REQUEST_UPDATE_PATH.getCode()) {
                handleRouteRequest(message);
            }
        }
    }

    /**
     * Clean the cached routes and mote positions.
     */
    @Override
    public void clean() {
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.bufferKBestPaths = new HashMap<>();
        amountAdaptations.keySet().stream().forEach(moteEui -> amountAdaptations.put(moteEui,0));
        long time = 0;
        averageTimeForDecisionPerMote.keySet().stream().forEach(moteEUI -> averageTimeForDecisionPerMote.put(moteEUI,new Pair(0,time)));
        maxTime.keySet().stream().forEach(moteEUI -> maxTime.put(moteEUI,time));
    }

    @Override
    public void reset(){
        beginTime = 31380L;
    }
}
