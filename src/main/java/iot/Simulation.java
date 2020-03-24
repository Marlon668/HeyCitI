package iot;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.SensorEnvironment;
import application.pollution.PollutionGrid;
import application.pollution.PollutionMonitor;
import application.routing.KAStarRouter;
import application.routing.RouteEvaluator;
import application.routing.RoutingApplication;
import application.routing.heuristic.DistanceHeuristic;
import application.routing.heuristic.SimplePollutionHeuristic;
import be.kuleuven.cs.som.annotate.Basic;
import datagenerator.SensorDataGenerator;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import selfadaptation.instrumentation.MoteEffector;
import util.Pair;
import util.Path;
import util.TimeHelper;

import javax.sound.midi.SysexMessage;
import javax.swing.*;
import java.lang.ref.WeakReference;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;

/**
 * A class representing a simulation.
 */
public class Simulation {

    // region fields
    /**
     * The InputProfile used in the simulation.
     */
    private InputProfile inputProfile;
    /**
     * The Environment used in th simulation.
     */
    private WeakReference<Environment> environment;
    /**
     * The GenericFeedbackLoop used in the simulation.
     */
    private GenericFeedbackLoop approach;

    /**
     * A condition which determines if the simulation should continue (should return {@code false} when the simulation is finished).
     */
    private Predicate<Environment> continueSimulation;

    private boolean finished;

    /**
     * Intermediate parameters used during simulation
     */
    private Map<Mote, Integer> wayPointMap;
    private Map<Mote, LocalTime> timeMap;
    private RouteEvaluator routeEvaluator;
    private RoutingApplication routingApplication;
    private Map<Long,List<Double>> information;
    private Integer time;
    private PollutionGrid pollutionGrid;
    private LocalTime startTime;
    private SensorEnvironment sensorEnvironment;
    private PollutionEnvironment pollutionEnvironment;
    private HashMap<Mote,List<Long>> buffer;
    private HashMap<Mote,Boolean> synhronisedError;
    private SimulationRunner simulationRunner;
    private int amountUsers;
    private boolean lastMote;
    private HashMap<Mote,Integer> changed;

    /**
     * sets the pollutiongrid
     * @param pollutionGrid the pollutiongrid to be set
     */
    public void setPollutionGrid(PollutionGrid pollutionGrid) {
        this.pollutionGrid = pollutionGrid;
    }

    public void setSensorEnvironment(SensorEnvironment sensorEnvironment)
    {
        this.sensorEnvironment = sensorEnvironment;
    }

    public void setPollutionEnvironment(PollutionEnvironment pollutionEnvironment)
    {
        this.pollutionEnvironment = pollutionEnvironment;
    }
    // endregion

    // region constructors

    public Simulation(PollutionGrid pollutionGrid, SensorEnvironment sensorEnvironment, PollutionEnvironment pollutionEnvironment, SimulationRunner simulationRunner) {
        this.routeEvaluator = new RouteEvaluator(pollutionGrid,sensorEnvironment);
        setPollutionGrid(pollutionGrid);
        setSensorEnvironment(sensorEnvironment);
        this.finished = false;
        setPollutionEnvironment(pollutionEnvironment);
        this.simulationRunner = simulationRunner;
    }


    // endregion

    // region getter/setters

    /**
     * Gets the Environment used in th simulation.
     * @return The Environment used in the simulation.
     */
    @Basic
    public Environment getEnvironment() {
        return environment.get();
    }
    /**
     * Sets the Environment used in th simulation.
     * @param environment  The Environment to use in the simulation.
     */
    @Basic
    public void setEnvironment(WeakReference<Environment> environment) {
        this.environment = environment;
        this.routeEvaluator.setEnvironment(environment);
    }
    @Basic
    public void setRoutingApplication(RoutingApplication routingApplication)
    {
        this.routingApplication = routingApplication;
    }

    @Basic
    public Map<Long,List<Double>> getInformation(){
        return this.information;
    }

    @Basic
    public void setInformation() {
        this.information = new HashMap<>();
    }



    /**
     * Gets the InputProfile used in th simulation.
     * @return The InputProfile used in the simulation.
     */
    @Basic
    public Optional<InputProfile> getInputProfile() {
        return Optional.ofNullable(inputProfile);
    }
    /**
     * Sets the InputProfile used in th simulation.
     * @param inputProfile  The InputProfile to use in the simulation.
     */
    @Basic
    public void setInputProfile(InputProfile inputProfile) {
        this.inputProfile = inputProfile;
    }

    /**
     * Gets the GenericFeedbackLoop used in th simulation.
     * @return The GenericFeedbackLoop used in the simulation.
     */
    @Basic
    public GenericFeedbackLoop getAdaptationAlgorithm() {
        return approach;
    }
    /**
     * Sets the GenericFeedbackLoop used in th simulation.
     * @param approach  The GenericFeedbackLoop to use in the simulation.
     */
    @Basic
    public void setAdaptationAlgorithm(GenericFeedbackLoop approach) {
        this.approach = approach;
        routingApplication.setBufferSize(inputProfile.getBuffersize());
    }


    public GenericFeedbackLoop getApproach() {
        return approach;
    }
    /**
     * Sets the GenericFeedbackLoop.
     * @param approach The GenericFeedbackLoop to set.
     */
    @Basic
    void setApproach(GenericFeedbackLoop approach) {
        if (getApproach()!= null) {
            getApproach().stop();
        }
        this.approach = approach;
        routingApplication.setBufferSize(inputProfile.getBuffersize());

        getApproach().start();
    }
    // endregion


    /**
     * Gets the probability with which a mote should be active from the input profile of the current simulation.
     * If no probability is specified, the probability is set to one.
     * Then it performs a pseudo-random choice and sets the mote to active/inactive for the next run, based on that probability.
     */
    private void setupMotesActivationStatus() {
        List<Mote> motes = this.getEnvironment().getMotes();
        Set<Integer> moteProbabilities = this.inputProfile.getProbabilitiesForMotesKeys();
        for (int i = 0; i < motes.size(); i++) {
            Mote mote = motes.get(i);
            double activityProbability = 1;
            if (moteProbabilities.contains(i))
                activityProbability = this.inputProfile.getProbabilityForMote(i);
            if (Math.random() >= 1 - activityProbability)
                mote.enable(true);
        }
    }

    /**
     * Check if all motes have arrived at their destination.
     * @return True if the motes are at their destinations.
     */
    private boolean areAllMotesAtDestination() {
        return this.getEnvironment().getMotes().stream()
            .allMatch(m -> !m.isEnabled() || m.isArrivedToDestination());
    }


    public void initialiseMote(){
        changed = new HashMap<>();
        for(Mote mote : environment.get().getMotes()){
            if(mote instanceof UserMote){
                simulateIdealSituation((UserMote) mote);
            }
            changed.put(mote,0);
        }
    }



    public void simulateIdealSituation (UserMote mote) {
        time = 0;
        routingApplication.calculateRoutingAdaptations(mote, Objects.requireNonNull(environment.get()),sensorEnvironment);

        routingApplication.reset();
        pollutionEnvironment.clear();

        System.out.println("size : " + mote.getChangesPath().size() );
        mote.getChangesPath().entrySet().stream()
            .forEach(entry -> {
                List<Long> wayPoints = new ArrayList<>();
                for(GeoPosition wayPoint : entry.getValue()){
                    long id = environment.get().getGraph().getClosestWayPoint(wayPoint);
                    wayPoints.add(id);
                }
                System.out.println("WayPoint : " + environment.get().getGraph().getClosestWayPoint(entry.getKey())+ " /n changedPath : "  + wayPoints);
            });
    }


    /**
     * Simulate a single step in the simulator.
     */
    public synchronized void simulateStep(PollutionEnvironment Pollenvironment) {
        //noinspection SimplifyStreamApiCallChains
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> !(mote.isArrivedToDestination()))
            .map(mote -> { mote.consumePackets();return mote;})
            //DON'T replace with peek because the filtered mote after this line will not do the consume packet
            .filter(mote ->  mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            //.filter(mote -> !(mote instanceof UserMote) && mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(environment.get().getClock().getTime().isAfter(startTime)) {
                    timeMap.put(mote, this.getEnvironment().getClock().getTime());
                    if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                        if(mote instanceof UserMote)
                        {
                            this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));

                            routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));

                        }
                        else{
                            this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        }


                    } else {
                        int index = wayPointMap.get(mote);
                        if(!(mote instanceof UserMote) || getApproach().getName() != "Air Quality")
                        {
                            wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                        }

                        else {

                            if(mote.getPath().getWayPoints().size() == wayPointMap.get(mote)+2 && !this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote)+1)).equals(mote.getPosInt())) {
                                wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                                //this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                                //routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                            }
                            else {
                                if (!(routingApplication.getRoute(mote).size() == 0)) {
                                    if (!(pathNotContainsDoubles(mote.getPath().getWayPoints()))) {
                                        deleteDouble(mote);
                                        wayPointMap.put(mote, mote.getPath().getWayPoints().size() - 1);
                                        if (this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                                            List<GeoPosition> path = mote.getPath().getWayPoints();
                                            List<Long> path3 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path3.add(id);
                                                }
                                            );
                                            int sizeOrignialPath = path3.size();
                                            int indexPath = 0;
                                            long idW = environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath));
                                            while (mote.getPath().getWayPoints().get(wayPointMap.get(mote)).equals(routingApplication.getRoute(mote).get(indexPath)) && this.getEnvironment().getMapHelper().toMapCoordinate(routingApplication.getRoute(mote).get(indexPath)).equals(mote.getPosInt())) {
                                                indexPath += 1;
                                                idW = environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath));
                                            }
                                            System.out.println("IndexP : " + wayPointMap.get(mote));
                                            System.out.println("Position : " + environment.get().getGraph().getClosestWayPoint(environment.get().getMapHelper().toGeoPosition(mote.getPosInt())));
                                            System.out.println("Position : " + environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath)));
                                            path.add(routingApplication.getRoute(mote).get(indexPath));
                                            wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                                            System.out.println("Index : " + indexPath);
                                            List<Long> path2 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path2.add(id);
                                                }
                                            );
                                            System.out.println("Eui" + mote.getEUI() + " : " + path2);
                                            System.out.println("Waypoint : " + environment.get().getGraph().getClosestWayPoint(mote.getPath().getWayPoints().get(wayPointMap.get(mote))));
                                            synhronisedError.put(mote, true);
                                            //this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));

                                            //routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                                        }
                                    } else {
                                        if (inputProfile.getSynchronisation() == 0) {
                                            List<GeoPosition> path = mote.getPath().getWayPoints();
                                            List<Long> path3 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path3.add(id);
                                                }
                                            );
                                            int sizeOrignialPath = path3.size();
                                            int indexPath = 0;
                                            long idW = environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath));
                                            while (mote.getPath().getWayPoints().get(wayPointMap.get(mote)).equals(routingApplication.getRoute(mote).get(indexPath)) && this.getEnvironment().getMapHelper().toMapCoordinate(routingApplication.getRoute(mote).get(indexPath)).equals(mote.getPosInt())) {
                                                indexPath += 1;
                                                idW = environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath));
                                            }
                                            System.out.println("IndexP : " + wayPointMap.get(mote));
                                            System.out.println("Position : " + environment.get().getGraph().getClosestWayPoint(environment.get().getMapHelper().toGeoPosition(mote.getPosInt())));
                                            System.out.println("Position : " + environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath)));
                                            path.add(routingApplication.getRoute(mote).get(indexPath));
                                            wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                                            System.out.println("Index : " + indexPath);
                                            List<Long> path2 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path2.add(id);
                                                }
                                            );
                                            System.out.println("Eui" + mote.getEUI() + " : " + path2);
                                            System.out.println("Waypoint : " + environment.get().getGraph().getClosestWayPoint(mote.getPath().getWayPoints().get(wayPointMap.get(mote))));
                                            //this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));

                                            //routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                                            //synhronisedError.put(mote, true);

                                        } else {
                                            if (environment.get().getGraph().getClosestWayPoint(((UserMote) mote).getDestination()) == (environment.get().getGraph().getClosestWayPoint(environment.get().getMapHelper().toGeoPosition(mote.getPosInt())))) {
                                                ((UserMote) mote).setArrived();
                                            } else {
                                                setupSingleRun(true);
                                            }

                                        }
                                       // }
                                    }
                                }

                                    else {
                                        // There is a fault in the setup => redo
                                        //if(amountUsers == 0) {
                                        setupSingleRun(true);
                                        //}
                                    }
                                }

                            }
                        }


                    }

            });
        if(!(getApproach() == null) && getApproach().getName()=="Get Information" && environment.get().getClock().getTime().isAfter(startTime)) {
            if (time % 3000 == 0) {
                updateInformation();
            }
            time += 1;
        }

        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
        time += 1;

        //amount = 0;
    }

    private void deleteDouble(Mote mote) {
        List<Long> path2 = new ArrayList<>();
        mote.getPath().getWayPoints().forEach(
            waypoint -> {
                long id = getEnvironment().getGraph().getClosestWayPoint(waypoint);
                path2.add(id);
            }
        );
        for(int i=0;i<path2.size()-1;i++)
        {
            if (path2.get(i+1).equals(path2.get(i)))
            {
                mote.getPath().getWayPoints().remove(i);
            }
        }
    }


    private void updateInformation()
    {
        environment.get().getGraph().getConnections().entrySet().stream()
            .forEach(entry -> {
                    Double cost = routeEvaluator.getCostConnection(entry.getValue());
                    if(information.containsKey(entry.getKey())){
                        information.get(entry.getKey()).add(information.get(entry.getKey()).size() - 1, cost);
                        information.put(entry.getKey(), information.get(entry.getKey()));
                    }
                    else
                    {
                        List<Double> list = new ArrayList<>();
                        list.add(0,cost);
                        information.put(entry.getKey(),list);
                    }
                }
            );


    }

    public boolean isFinished() {
        if(!this.continueSimulation.test(this.getEnvironment()) && this.finished == false) {
            if (simulationRunner.getMultipleRun()) {
                    this.finished = true;
                    HashMap<Mote, Pair<Long, Long>> time = new HashMap<>();
                    HashMap<Mote, Pair<Double, Integer>> result = new HashMap<>();
                    for (Mote mote : getEnvironment().getMotes()) {
                        if (mote instanceof UserMote) {
                            double resultRoute = routeEvaluator.getTotalCostPath(mote.getEUI());
                            int amountAdaptations = routingApplication.getAmountAdaptations().get(mote.getEUI());
                            result.put(mote, new Pair(resultRoute, amountAdaptations));
                            long averageTime = routingApplication.getAverageTimeForDecisionPerMote().get(mote.getEUI()).getRight() / routingApplication.getAverageTimeForDecisionPerMote().get(mote.getEUI()).getLeft();
                            time.put(mote, new Pair(averageTime, routingApplication.getMaxTime().get(mote.getEUI())));
                        }
                    }
                    simulationRunner.multipleRun(time, result);
                }
             else {
                String message = "Evaluation of the path \n";
                for (Mote mote : getEnvironment().getMotes()) {
                    if (mote instanceof UserMote) {
                        if (getApproach() != null && getApproach().getName() == "Air Quality") {
                            long averageTime = routingApplication.getAverageTimeForDecisionPerMote().get(mote.getEUI()).getRight() / routingApplication.getAverageTimeForDecisionPerMote().get(mote.getEUI()).getLeft();
                            message += "EUI: " + mote.getEUI() + "  :    " + routeEvaluator.getTotalCostPath(mote.getEUI()) + "\n" +
                                " , Amount adaptations: " + routingApplication.getAmountAdaptations().get(mote.getEUI()) + " , AverageTimeDecision: " + averageTime +
                                " Max Time" + routingApplication.getMaxTime().get(mote.getEUI()) + "\n";
                            if (synhronisedError.get(mote)) {
                                message += "Synchronisation issue has occurred for mote + " + mote.getEUI();
                            }

                        } else {
                            message += "EUI: " + mote.getEUI() + "  :    " + routeEvaluator.getTotalCostPath(mote.getEUI()) + "\n";
                        }

                    }
                }
                System.out.println("Time: " + time/1000);
                JOptionPane.showMessageDialog(null, message, "Results", JOptionPane.INFORMATION_MESSAGE);
            }

        }

        if(!(getApproach() == null) && getApproach().getName()=="Get Information")
        {
            if(time/1000 >= 3000)
            {
                String message2 = "Finished \nPress on save to write the information about the evolution of the values for all connections on a xml-file";
                JOptionPane.showMessageDialog(null, message2, "Information gathered", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        else {
            return !this.continueSimulation.test(this.getEnvironment());
        }
        return false;

    }

    private boolean pathNotContainsDoubles(List<GeoPosition> positions)
    {
        List<Long> path2 = new ArrayList<>();
        positions.forEach(
            waypoint -> {
                long id = getEnvironment().getGraph().getClosestWayPoint(waypoint);
                path2.add(id);
            }
        );
        for(int i=0;i<path2.size()-1;i++)
        {
            if (path2.get(i+1).equals(path2.get(i)))
            {
                return false;
            }
        }
        return true;
    }


    private synchronized void setupSimulation(Predicate<Environment> pred) {
        this.amountUsers = 0;
        for(Mote mote : environment.get().getMotes()){
            if(mote instanceof UserMote){
                amountUsers +=1;
            }
        }
        startTime = LocalTime.of(0,0,40);
        this.finished = false;
        this.buffer = new HashMap<>();
        this.time = 0;

        routingApplication.setBufferSize(inputProfile.getBuffersize());

        this.wayPointMap = new HashMap<>();
        this.timeMap = new HashMap<>();
        this.routeEvaluator.reset();
        this.synhronisedError = new HashMap<>();


        setupMotesActivationStatus();

        this.getEnvironment().getGateways().forEach(Gateway::reset);

        this.getEnvironment().getMotes().forEach(mote -> {
            // Reset all the sensors of the mote
            if(mote instanceof UserMote) {
                synhronisedError.put(mote, false);
            }
            mote.getSensors().stream()
                .map(MoteSensor::getSensorDataGenerator)
                .forEach(SensorDataGenerator::reset);

            mote.reset();

            timeMap.put(mote, this.getEnvironment().getClock().getTime());
            wayPointMap.put(mote,0);
            buffer.put(mote,new ArrayList<>());

            // Add initial triggers to the clock for mote data transmissions (transmit sensor readings)
            this.getEnvironment().getClock().addTrigger(LocalTime.ofSecondOfDay(mote.getStartSendingOffset()), () -> {
                mote.sendToGateWay(
                    mote.getSensors().stream()
                        .flatMap(s -> s.getValueAsList(mote.getPosInt(), this.getEnvironment().getClock().getTime()).stream())
                        .toArray(Byte[]::new),
                    new HashMap<>());
                return this.getEnvironment().getClock().getTime().plusSeconds(mote.getPeriodSendingPacket());
            });
        });

        this.continueSimulation = pred;

    }

    //void setupSingleRun(boolean shouldResetHistory) {
    //    if (shouldResetHistory) {
    //        this.getEnvironment().resetHistory();
    //    }
//
   //     this.setupSimulation((env) -> !areAllMotesAtDestination());
    //}

    void setupSingleRun(boolean shouldResetHistory) {
        if (shouldResetHistory) {
            this.getEnvironment().resetHistory();
        }

        this.setupSimulation((env) -> !areAllMotesAtDestination());
    }

    void setupTimedRun() {
        this.getEnvironment().resetHistory();

        var finalTime = this.getEnvironment().getClock().getTime()
            .plus(inputProfile.getSimulationDuration(), inputProfile.getTimeUnit());
        this.setupSimulation((env) -> env.getClock().getTime().isBefore(finalTime));
    }

    public void simulateStepRun(PollutionEnvironment Pollenvironment) {
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote -> !(mote instanceof UserMote))
            .map(mote -> { mote.consumePackets(); return mote;}) //DON'T replace with peek because the filtered mote after this line will not do the consume packet
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                timeMap.put(mote, this.getEnvironment().getClock().getTime());
                if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                    this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                } else {wayPointMap.put(mote, wayPointMap.get(mote) + 1);}
            });
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> mote instanceof UserMote)
            .map(mote -> { ((UserMote) mote).changePath(); return mote;}) //DON'T replace with peek because the filtered mote after this line will not do the consume packet
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(((UserMote) mote).hasChanged() && changed.get(mote) >1){
                    wayPointMap.put(mote,1);
                }
                timeMap.put(mote, this.getEnvironment().getClock().getTime());
                if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                    this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                } else {
                    changed.put(mote,changed.get(mote)+1);
                    wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                }
            });
        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
    }

    public boolean RunisFinished() {
        return !this.continueSimulation.test(this.getEnvironment());
    }

}
