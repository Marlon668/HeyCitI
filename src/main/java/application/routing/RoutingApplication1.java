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
import java.util.stream.Stream;

/**
 * Class to dedicate if
 */
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

    private LocalTime startTime;

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
        this.alternativeRoute = new HashMap<>();
        this.distances = new HashMap<>();
        this.adaptationPoints = new HashMap<>();
        this.visualiseRun = new HashMap<>();
        this.airQualityRun = new HashMap<>();
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
        startTime = LocalTime.of(0,0,40);
    }

    /**
     * Handles a route request without sending packets
     * It gives directly the next part of the route to the mote
     *
     * @param mote Usermote where we would send a next part of the route to it
     * @param environment the environment that we use for the simulation
     */
    public void handleRouteRequestWithoutNetworkForRun(UserMote mote, Environment environment, SensorEnvironment sensorEnvironment) {
        GeoPosition currentPosition= environment.getMapHelper().toGeoPosition(mote.getPosInt());
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
                        List<Pair<Double,Double>> visualisedResults = visualiseRun.get(mote);
                        Pair<Double,Double> lastPoint = visualisedResults.get(visualisedResults.size()-1);
                        double newAirQuality = airQualityRun.get(mote) + bestPath.getLeft();
                        List<Pair<Pair<Double,Double>,Pair<Double,Double>>> alternativeRoutesMote = alternativeRoute.get(mote);
                        Pair<Double,Double> newResult = new Pair<>(newAirQuality,distances.get(mote));
                        double newAccumulatedCost = 0;
                        GeoPosition lastWaypoint = null;
                        for(GeoPosition i : path)
                        {
                            if(lastWaypoint == null)
                            {
                                lastWaypoint = i;
                            }
                            else{
                                double accumulatedCostConnection = this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(lastWaypoint,i);
                                newAccumulatedCost = newAccumulatedCost + accumulatedCostConnection;
                                lastWaypoint = i;
                            }
                        }
                        Pair<Double,Double> oldResult = new Pair<>(newAccumulatedCost+airQualityRun.get(mote),distances.get(mote));
                        alternativeRoutesMote.add(new Pair<>(lastPoint,oldResult));
                        double newDistance = distances.get(mote) + MapHelper.distance(bestPath.getRight().get(0),bestPath.getRight().get(1))*1000;
                        adaptationPoints.get(mote).add(distances.get(mote));
                        distances.put(mote,newDistance);
                        visualiseRun.get(mote).add(newResult);
                        double accumalatedCostConnection = airQualityRun.get(mote) + this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(bestPath.getRight().get(0),bestPath.getRight().get(1));
                        airQualityRun.put(mote,accumalatedCostConnection);
                        this.routes.put(mote.getEUI(), bestPath);
                    }
                    else{
                        List<Pair<Double,Double>> visualisedResults = visualiseRun.get(mote);
                        Pair<Double,Double> lastPoint = visualisedResults.get(visualisedResults.size()-1);
                        double newAirQuality = airQualityRun.get(mote) + bestPath.getLeft();
                        Pair<Double,Double> newResult = new Pair<>(newAirQuality,distances.get(mote));
                        visualiseRun.get(mote).add(newResult);
                        double newDistance = distances.get(mote) + MapHelper.distance(bestPath.getRight().get(0),bestPath.getRight().get(1))*1000;
                        distances.put(mote,newDistance);
                        double accumalatedCostConnection = airQualityRun.get(mote) + this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(bestPath.getRight().get(0),bestPath.getRight().get(1));
                        airQualityRun.put(mote,accumalatedCostConnection);
                        this.routes.put(mote.getEUI(), bestPath);
                    }
                }
                else{
                    int amountAdaptations = getAmountAdaptations().get(mote.getEUI()) + 1;
                    getAmountAdaptations().put(mote.getEUI(), amountAdaptations);
                    double distanceLastMote = MapHelper.distance(bestPath.getRight().get(0),bestPath.getRight().get(1))*1000;
                    distances.put(mote,distanceLastMote);
                    List<Double> airValues = new ArrayList<>();
                    airValues.add(0.0);
                    adaptationPoints.put(mote,airValues);
                    List<Pair<Double,Double>> airQuality = new ArrayList<>();
                    Pair<Double,Double> resultsFinal = new Pair<>(bestPath.getLeft(),0.0);
                    airQuality.add(resultsFinal);
                    visualiseRun.put(mote,airQuality);
                    this.routes.put(mote.getEUI(),bestPath);
                    double accumalatedCostConnection = this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(bestPath.getRight().get(0),bestPath.getRight().get(1));
                    airQualityRun.put(mote,accumalatedCostConnection);
                    alternativeRoute.put(mote,new ArrayList<>());
                }
            }
            else {
                bestPath = this.routes.get(mote.getEUI());
                this.routes.put(mote.getEUI(), bestPath);
                List<Pair<Double,Double>> visualisedResults = visualiseRun.get(mote);
                Pair<Double,Double> lastPoint = visualisedResults.get(visualisedResults.size()-1);
                double newDistance = distances.get(mote) + (MapHelper.distance(bestPath.getRight().get(0),bestPath.getRight().get(1)))*1000;
                double newAirQuality = airQualityRun.get(mote) + bestPath.getLeft();
                Pair<Double,Double> newResult = new Pair<>(newAirQuality,distances.get(mote));
                visualiseRun.get(mote).add(newResult);
                distances.put(mote,newDistance);
                double accumalatedCostConnection = airQualityRun.get(mote) + this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(bestPath.getRight().get(0),bestPath.getRight().get(1));
                airQualityRun.put(mote,accumalatedCostConnection);
                this.routes.put(mote.getEUI(), bestPath);
            }
        } else {
            if (routes.get(mote.getEUI()) == null) {
                bestPath = this.pathFinder.retrievePath(graph,currentPosition, mote.getDestination());
                this.routes.put(mote.getEUI(), bestPath);
                int amountAdaptations = getAmountAdaptations().get(mote.getEUI()) + 1;
                getAmountAdaptations().put(mote.getEUI(), amountAdaptations);
                double distanceLastMote = MapHelper.distance(bestPath.getRight().get(0),bestPath.getRight().get(1))*1000;
                List<Double> airValues = new ArrayList<>();
                airValues.add(0.0);
                adaptationPoints.put(mote,airValues);
                List<Pair<Double,Double>> airQuality = new ArrayList<>();
                Pair<Double,Double> resultsFinal = new Pair<>(bestPath.getLeft(),0.0);
                distances.put(mote,distanceLastMote);
                airQuality.add(resultsFinal);
                visualiseRun.put(mote,airQuality);
                this.routes.put(mote.getEUI(),bestPath);
                double accumalatedCostConnection = this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(bestPath.getRight().get(0),bestPath.getRight().get(1));
                airQualityRun.put(mote,accumalatedCostConnection);
                alternativeRoute.put(mote,new ArrayList<>());
            } else {
                bestPath = this.routes.get(mote.getEUI());
                List<Pair<Double,Double>> visualisedResults = visualiseRun.get(mote);
                Pair<Double,Double> lastPoint = visualisedResults.get(visualisedResults.size()-1);
                double distance = distances.get(mote) + MapHelper.distance(bestPath.getRight().get(0),bestPath.getRight().get(1))*1000;
                double newAirQuality = airQualityRun.get(mote) + bestPath.getLeft();
                Pair<Double,Double> newResult = new Pair<>(newAirQuality,distances.get(mote));
                visualiseRun.get(mote).add(newResult);
                distances.put(mote,distance);
                double accumalatedCostConnection = airQualityRun.get(mote) + this.pathFinder.getHeuristic().calculateCostBetweenTwoNeighbours(bestPath.getRight().get(0),bestPath.getRight().get(1));
                this.airQualityRun.put(mote,accumalatedCostConnection);
                this.routes.put(mote.getEUI(), bestPath);
            }
        }
        Path motePath = mote.getPath();
        motePath.addPosition(bestPath.getRight().get(1));
        mote.setPath(motePath.getWayPoints());
        this.routes.get(mote.getEUI()).getRight().remove(0);
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
            GeoPosition currentPosition= environment.getMapHelper().toGeoPosition(mote.getPosInt());
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
     **/
    @Override
    public void calculateRoutingAdaptations(Environment environment, SensorEnvironment sensorEnvironment) {
        HashMap<Mote,HashMap<GeoPosition, List<List<GeoPosition>>>> changesPath = new HashMap<>();
        Map<Mote, Integer> wayPointMap = new HashMap<>();
        Map<Mote, LocalTime> timeMap = new HashMap<>();
        for(Mote mote : environment.getMotes()){
            if(mote instanceof UserMote) {
                changesPath.put(mote,new HashMap<>());
                wayPointMap.put(mote,0);
                timeMap.put(mote, environment.getClock().getTime());
            }
        }
        while(notAllMotesFinished(environment))
        {
            environment.getMotes().stream()
                .filter(Mote::isEnabled)
                .filter(mote-> !(mote.isArrivedToDestination()))
                .filter(mote -> mote instanceof UserMote)
                .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
                .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                    TimeHelper.nanoToMili(environment.getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
                .filter(mote -> TimeHelper.nanoToMili(environment.getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
                .forEach(mote -> {
                    if(environment.getClock().getTime().isAfter(startTime)) {
                        timeMap.put(mote, environment.getClock().getTime());
                        if (!environment.getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                            environment.moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        } else {
                            List<GeoPosition> adaptation = handleRouteRequestWithoutNetwork2((UserMote) mote, environment, sensorEnvironment);
                            if(adaptation!= null){
                                HashMap<GeoPosition, List<List<GeoPosition>>> changePath = changesPath.get(mote);
                                if(changePath.get(environment.getMapHelper().toGeoPosition(mote.getPosInt())) == null){
                                    List<List<GeoPosition>> paths = new ArrayList<>();
                                    paths.add(adaptation);
                                    changePath.put(environment.getMapHelper().toGeoPosition(mote.getPosInt()),paths);
                                }
                                else{
                                    List<List<GeoPosition>> paths = changePath.get(environment.getMapHelper().toGeoPosition(mote.getPosInt()));
                                    paths.add(adaptation);
                                    changePath.put(environment.getMapHelper().toGeoPosition(mote.getPosInt()),paths);
                                }
                            }
                            wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                            }
                        }
                    });
            sensorEnvironment.getPoll().doStep(environment.getClock().getTime().toNanoOfDay(), environment);
            environment.getClock().tick(1);
        }
        environment.getMotes().stream()
            .filter(mote-> mote instanceof UserMote)
            .forEach(mote->{
                ((UserMote) mote).setChangesPath(changesPath.get(mote));
            });
    }

    /**
     * Handles a route request without sending packets
     * It gives directly the next part of the route to the mote
     *
     * @param mote Usermote where we would send a next part of the route to it
     * @param environment the environment that we use for the simulation
     */
    @Override
    protected List<GeoPosition> handleRouteRequestWithoutNetwork2(UserMote mote, Environment environment, SensorEnvironment sensorEnvironment) {
        GeoPosition currentPosition= environment.getMapHelper().toGeoPosition(mote.getPosInt());
        GeoPosition endPosition = mote.getDestination();
        // Use the routing algorithm to calculate the K best paths for the mote
        List<Pair<Double,List<GeoPosition>>> routeMote = this.pathFinder.retrieveKPaths(graph,currentPosition,mote.getDestination(),bufferSizeWidth);
        putKBestPathsBuffers(mote.getEUI(), routeMote);
        Pair<Double, List<GeoPosition>> bestPath;
        List<GeoPosition> changedPath = new ArrayList<>();
        boolean send = false;
        if (bufferKBestPaths.get(mote.getEUI()).size() == bufferSizeHeight) {
            bestPath = takeBestPath(bufferKBestPaths.get(mote.getEUI()));
            bufferKBestPaths = new HashMap<>();
            if (bestPath.getRight().size() > 0) {
                if (routes.get(mote.getEUI()) != null) {
                    List<GeoPosition> path = routes.get(mote.getEUI()).getRight();
                    if (!(bestPath.getRight().equals(path))) {
                        int amountAdaptations = getAmountAdaptations().get(mote.getEUI()) + 1;
                        getAmountAdaptations().put(mote.getEUI(), amountAdaptations);
                        changedPath = Stream.concat(mote.getPath().getWayPoints().stream(),bestPath.getRight().stream()).collect(Collectors.toList());
                        send = true;
                    }
                }
                else{
                   changedPath = Stream.concat(mote.getPath().getWayPoints().stream(),bestPath.getRight().stream()).collect(Collectors.toList());
                    send = true;
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
                changedPath = Stream.concat(mote.getPath().getWayPoints().stream(),bestPath.getRight().stream()).collect(Collectors.toList());
                send = true;
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
        if(send){
            return changedPath;
        }
        else{
            return null;
        }
    }


    /**
     * Determines if all motes are finished
      * @param environment hash map that contains for every mote the current position of that mote
     * @return true if current position of mote is equal to its destination, false otherwise
     */
    private boolean notAllMotesFinished(Environment environment){
        for(Mote mote : environment.getMotes()){
            if(!(mote.isArrivedToDestination())){
                return true;
            }
        }
        return false;
    }


    /**
     * Handle a route request message by replying with (part of) the route to the requesting device.
     * @param message The message which contains the route request.
     */
    @Override
    protected void handleRouteRequest(LoraWanPacket message) {
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

    /**
     * Function to take the best path out of List of a list of possible best paths together with its cost
     * Every row of the list contains the k best paths calculated at a waypoint with the K-A* algorithm
     * First we select the most common paths out of this list of lists and than the path with the less
     * air quality calculated as an average of the cost all the common pahts, where paths calculated closer
     * to the current position of the mote, have a higher priority to be chosen.
     * @param bufferKBestPaths a list of lists of possible paths to chose from
     * @return the best path together with its cost.
     */
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
    public void clean(int delete) {
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.bufferKBestPaths = new HashMap<>();
        if(delete != 2) {
            amountAdaptations.keySet().stream().forEach(moteEui -> amountAdaptations.put(moteEui, 0));
            long time = 0;
            averageTimeForDecisionPerMote.keySet().stream().forEach(moteEUI -> averageTimeForDecisionPerMote.put(moteEUI, new Pair(0, time)));
            maxTime.keySet().stream().forEach(moteEUI -> maxTime.put(moteEUI, time));
        }
    }

    @Override
    public void reset(){
        startTime = LocalTime.of(0,0,40);
    }
}
