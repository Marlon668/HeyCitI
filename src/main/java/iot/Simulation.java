package iot;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.SensorEnvironment;
import application.pollution.PollutionGrid;
import application.routing.RouteEvaluator;
import application.routing.RoutingApplication;
import application.routing.RoutingApplication1;
import be.kuleuven.cs.som.annotate.Basic;
import datagenerator.SensorDataGenerator;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import util.MapHelper;
import util.Pair;
import util.TimeHelper;

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
    private HashMap<Mote,Integer> changed;
    private HashMap<Mote,Double> distanceMote;
    private int steps;

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
        steps = 1;
    }

    public RouteEvaluator getRouteEvaluator(){
        return routeEvaluator;
    }

    public HashMap<Mote,Double> getDistanceMote(){
        return distanceMote;
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

    /**
     * Sets the Routing application used in the simulation
     * If there is already set up a routingapplication, the existing application will
     * be destructed
     * @param routingApplication routing application to be set
     */
    @Basic
    public void setRoutingApplication(RoutingApplication routingApplication)
    {
        if(this.routingApplication!=null){
            this.routingApplication.destruct();
        }
        this.routingApplication = routingApplication;
        routingApplication.setBufferSizeHeight(simulationRunner.getParameters().getBuffersizeHeight());
        routingApplication.setBufferSizeWidth(simulationRunner.getParameters().getBuffersizeWidth());
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
        getApproach().start();
    }

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


    /**
     * Initialise the configuration and motes if option 2 without network is chosen
     * The earlier routing application will be destructed, single run will be set up and
     * all the gateways in the environment will be moved. This is necessarilly because otherwise
     * there will be sent unusable packages to the motes causing possible errors
     * For network option 2,  every adaptation that will be made to the path of the mote will be determined
     * before simulation and the path belonging to the start position will be set as the start path of the mote
     */
    public void initialiseMote(){
        changed = new HashMap<>();
        // Removing of the gateWays
        int numberOfGateWays = environment.get().getGateways().size();
        for(int i=0;i<numberOfGateWays;i++){
            environment.get().getGateways().remove(0);
        }
        if(simulationRunner.getParameters().getSetupFirst()==2) {
            routingApplication.calculateRoutingAdaptations(Objects.requireNonNull(environment.get()),sensorEnvironment);
            for (Mote mote : environment.get().getMotes()) {
                if (mote instanceof UserMote) {
                    changed.put(mote, 0);
                    ((UserMote) mote).changePath();
                }
            }
        }
        setupSingleRun(true);

    }

    /**
     * Simulate a single step in the simulator but isn't visible
     * This run is intended for getting data to visualise the results of doing adaptations
     */
    public void simulateVisualiseRun(PollutionEnvironment Pollenvironment){
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> !(mote.isArrivedToDestination()))
            .filter(mote-> mote instanceof UserMote)
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(environment.get().getClock().getTime().isAfter(startTime)) {
                    timeMap.put(mote, this.getEnvironment().getClock().getTime());
                    if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                        this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                    } else {
                        routingApplication.handleRouteRequestWithoutNetworkForRun((UserMote) mote, environment.get(), sensorEnvironment);
                        addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        wayPointMap.put(mote, wayPointMap.get(mote) + 1);

                    }
                }
            });
        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
    }

    /**
     * Simulate a single step in the simulator but isn't visible
     * This run is intended for getting data to visualise the results of doing adaptations
     */
    public void simulateVisualiseRun2(PollutionEnvironment Pollenvironment){
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> !(mote.isArrivedToDestination()))
            .filter(mote-> mote instanceof UserMote)
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(environment.get().getClock().getTime().isAfter(startTime)) {
                    timeMap.put(mote, this.getEnvironment().getClock().getTime());
                    if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                        this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                    } else {
                        routingApplication.handleRouteRequestWithoutNetworkForRun2((UserMote) mote, environment.get(), sensorEnvironment);
                        addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        wayPointMap.put(mote, wayPointMap.get(mote) + 1);

                    }
                }
            });
        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
    }


    /**
     * Simulate a single step in the simulator.
     * @param Pollenvironment the pollution environment used in the environment
     */
    public synchronized void simulateStep(PollutionEnvironment Pollenvironment) {
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> !(mote.isArrivedToDestination()))
            .map(mote -> { mote.consumePackets();return mote;})
            //DON'T replace with peek because the filtered mote after this line will not do the consume packet
            .filter(mote ->  mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
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
                    }
                    else {
                        if(!(mote instanceof UserMote) ||(getApproach() == null || !getApproach().getName().equals("Air Quality")))
                        {
                            // Remove the visibility of already visited waypoints of the followed path
                            if(simulationRunner.getParameters().getRemoveConn()==1 && mote.getPath().getWayPoints().size()==3) {
                                mote.getPath().getWayPoints().remove(0);
                            }
                            else{
                                wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                            }
                            addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        }

                        else {
                            // Check if next waypoint of path is already available otherwise a synchronisation issue has occurred
                            if(mote.getPath().getWayPoints().size() == wayPointMap.get(mote)+2 && !this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote)+1)).equals(mote.getPosInt())) {
                                if(simulationRunner.getParameters().getRemoveConn()==1 && mote.getPath().getWayPoints().size()==3) {
                                    mote.getPath().getWayPoints().remove(0);
                                }
                                else{
                                    wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                                }
                                addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                            }
                            else {
                                // Check if routing application has already received call of mote to determine a path of the mote
                                // if this is the fact the simulation can last by getting next part of the path by looking in
                                // the routes buffer of the mote in the routing application, only if user has not selected the option
                                // to restart the simulation if a synchronisation issue has occurred
                                // otherwise simulation must be restarted
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
                                            int indexPath = 0;
                                            long idW = environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath));
                                            while (mote.getPath().getWayPoints().get(wayPointMap.get(mote)).equals(routingApplication.getRoute(mote).get(indexPath)) && this.getEnvironment().getMapHelper().toMapCoordinate(routingApplication.getRoute(mote).get(indexPath)).equals(mote.getPosInt())) {
                                                indexPath += 1;
                                                idW = environment.get().getGraph().getClosestWayPoint(routingApplication.getRoute(mote).get(indexPath));
                                            }
                                            path.add(routingApplication.getRoute(mote).get(indexPath));
                                            if(simulationRunner.getParameters().getRemoveConn()==1 && mote.getPath().getWayPoints().size()==3) {
                                                mote.getPath().getWayPoints().remove(0);
                                            }
                                            else{
                                                wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                                            }
                                            addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                                            List<Long> path2 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path2.add(id);
                                                }
                                            );
                                            synhronisedError.put(mote, true);
                                        }

                                    } else {
                                        if (simulationRunner.getParameters().getSynchronisation() == 0) {
                                            List<GeoPosition> path = mote.getPath().getWayPoints();
                                            List<Long> path3 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path3.add(id);
                                                }
                                            );
                                            int indexPath = 0;
                                            while (mote.getPath().getWayPoints().get(wayPointMap.get(mote)).equals(routingApplication.getRoute(mote).get(indexPath)) && this.getEnvironment().getMapHelper().toMapCoordinate(routingApplication.getRoute(mote).get(indexPath)).equals(mote.getPosInt())) {
                                                indexPath += 1;
                                            }
                                            path.add(routingApplication.getRoute(mote).get(indexPath));
                                            if(simulationRunner.getParameters().getRemoveConn()==1 && mote.getPath().getWayPoints().size()==3) {
                                                mote.getPath().getWayPoints().remove(0);
                                            }
                                            else{
                                                wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                                            }
                                            List<Long> path2 = new ArrayList<>();
                                            mote.getPath().getWayPoints().forEach(
                                                waypoint -> {
                                                    long id = environment.get().getGraph().getClosestWayPoint(waypoint);
                                                    path2.add(id);
                                                }
                                            );

                                        } else {
                                            if (environment.get().getGraph().getClosestWayPoint(((UserMote) mote).getDestination()) == (environment.get().getGraph().getClosestWayPoint(environment.get().getMapHelper().toGeoPosition(mote.getPosInt())))) {
                                                ((UserMote) mote).setArrived();
                                            } else {
                                                setupSingleRun(true);
                                            }

                                        }
                                    }
                                }

                                else {
                                    setupSingleRun(true);
                                }
                            }

                        }
                    }


                }

            });


        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
        time += 1;
    }

    /**
     * Simulating a simulation step
     * Used for simulating a simulationstep where every adaptation of every biker is determined before visualise the simulation run
     * @param Pollenvironment the pollution environment that is used in the environment
     */
    public void simulateStepRun2(PollutionEnvironment Pollenvironment) {
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote -> !(mote instanceof UserMote))
            .map(mote -> { mote.consumePackets(); return mote;}) //DON'T replace with peek because the filtered mote after this line will not do the consume packet
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(environment.get().getClock().getTime().isAfter(startTime)) {
                    timeMap.put(mote, this.getEnvironment().getClock().getTime());
                    if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                        this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                    } else {
                        wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                        addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                    }
                }
            });
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> mote instanceof UserMote)
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(environment.get().getClock().getTime().isAfter(startTime)) {
                    timeMap.put(mote, this.getEnvironment().getClock().getTime());
                    if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                        this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                    } else {
                        ((UserMote) mote).changePath();
                        //addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                        if(!(wayPointMap.get(mote) == mote.getPath().getWayPoints().size())) {
                            addDistance(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        }
                    }
                }
            });
        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
    }

    /**
     * Delete waypoints that come just after each other
     * This happens only if there is happened a synchronisation issue and the direct link to the routing application is used
     * instead of restarting the simulation
     * @param mote the mote where we must delete a waypoint such 2 same waypoints doesn't come just after each other
     */
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

    /**
     * Function to determine if the siulation is finished
     * @return true if all motes are  arrived to their destination
     */
    public boolean isFinished() {
        if(!this.continueSimulation.test(this.getEnvironment()) && !this.finished) {
            if (simulationRunner.getMultipleRun() && getApproach().getName()!="No Adaptation") {
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
                                " , Amount adaptations: " + routingApplication.getAmountAdaptations().get(mote.getEUI()) + " , Distance : " + distanceMote.get(mote) +  "\n , AverageTimeDecision: " + averageTime +
                                " Max Time" + routingApplication.getMaxTime().get(mote.getEUI()) + "\n";
                            if (synhronisedError.get(mote)) {
                                message += "Synchronisation issue has occurred for mote + " + mote.getEUI();
                            }

                        } else {
                            message += "EUI: " + mote.getEUI() + "  :    " + routeEvaluator.getTotalCostPath(mote.getEUI()) + "Distance : " + distanceMote.get(mote) + "\n";
                        }

                    }
                }
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
        return !this.continueSimulation.test(this.getEnvironment());

    }

    /**
     * Function to add the distance of a connection to the distance of the already completed path
     * @param mote the mote to add the distance of the connection to the distance of the already completed path
     * @param geoPosition the next geoposition where the mote should go to
     */
    private void addDistance(Mote mote,GeoPosition geoPosition){
        distanceMote.put(mote,distanceMote.get(mote)+ MapHelper.distance(Objects.requireNonNull(environment.get()).getMapHelper().toGeoPosition(mote.getPosInt()),geoPosition)*1000);
    }

    /**
     * Function to determine if 2 geopostions doesn't come after each other
     * @param positions list of geopositions to check for doubles
     * @return true if path contains doubles after each other, false otherwise
     */
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


    /**
     * Function for setting up a simulation
     * @param pred the stopping condition of the simulation (e.g. all motes are at their destination)
     */
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
        sensorEnvironment.getPoll().clear();


        this.wayPointMap = new HashMap<>();
        this.timeMap = new HashMap<>();
        this.routeEvaluator.reset();
        this.synhronisedError = new HashMap<>();
        this.distanceMote = new HashMap<>();


        setupMotesActivationStatus();

        this.getEnvironment().getGateways().forEach(Gateway::reset);

        this.getEnvironment().getMotes().forEach(mote -> {
            // Reset all the sensors of the mote
            if(mote instanceof UserMote) {
                synhronisedError.put(mote, false);
            }
            distanceMote.put(mote,0.0);
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

    /**
     * Function for setting up a single run
     * @param shouldResetHistory boolean to decide if we should reset the history of the environment
     *                           or of the routing application
     */
    void setupSingleRun(boolean shouldResetHistory) {
        if (shouldResetHistory) {
            this.getEnvironment().resetHistory();
            if(routingApplication != null) {
                routingApplication.clean(simulationRunner.getParameters().getSetupFirst());
                routingApplication.reset();
            }
        }

        this.setupSimulation((env) -> !areAllMotesAtDestination());
        simulationRunner.setupSimulationRunner();
    }

    /**
     * Function for doing a simulation without a network
     * @param Pollenvironment the pollutionenvironment that is used for the simulation
     */
    public void simulateStepRun(PollutionEnvironment Pollenvironment) {
        this.getEnvironment().getMotes().stream()
            .filter(Mote::isEnabled)
            .filter(mote-> !(mote.isArrivedToDestination()))
            .filter(mote-> mote instanceof UserMote)
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.getEnvironment().getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                if(environment.get().getClock().getTime().isAfter(startTime)) {
                    timeMap.put(mote, this.getEnvironment().getClock().getTime());
                    if (!this.getEnvironment().getMapHelper().toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                        this.getEnvironment().moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                        routeEvaluator.addCostConnectionOfMote(mote, wayPointMap.get(mote));
                    } else {
                        routingApplication.handleRouteRequestWithoutNetwork((UserMote) mote, environment.get(), sensorEnvironment);
                        if (simulationRunner.getParameters().getRemoveConn() == 1 && mote.getPath().getWayPoints().size() == 3) {
                            mote.getPath().getWayPoints().remove(0);
                        } else {
                            wayPointMap.put(mote, wayPointMap.get(mote) + 1);
                        }
                        addDistance(mote,mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                    }
                }
            });
        Pollenvironment.doStep(this.getEnvironment().getClock().getTime().toNanoOfDay(), this.getEnvironment());
        this.getEnvironment().getClock().tick(1);
    }

    /**
     * Function to decide if run for simulations for gathering results is finished
     * @return true if simulation is finished, false otherwise
     */
    public boolean RunIsFinishedForResults(){
        return !this.continueSimulation.test(this.getEnvironment());
    }

    /**
     * Function to decide if run is finished for runs without network
     * @return true if run is finished, false otherwise
     */
    public boolean RunisFinished() {
        if(!this.continueSimulation.test(this.getEnvironment()))
        {
            String message = "Evaluation of the path \n";
            for (Mote mote : getEnvironment().getMotes()) {
                if (mote instanceof UserMote) {
                    if (getApproach() != null && getApproach().getName() == "Air Quality") {
                        message += "EUI: " + mote.getEUI() + "  :    " + routeEvaluator.getTotalCostPath(mote.getEUI()) +
                            " , Amount adaptations: " + routingApplication.getAmountAdaptations().get(mote.getEUI())+ "Distance : " + distanceMote.get(mote) +
                            "\n";

                    } else {
                        message += "EUI: " + mote.getEUI() + "  :    " + routeEvaluator.getTotalCostPath(mote.getEUI()) + "Distance : " + distanceMote.get(mote) + "\n";
                    }

                }
            }
            JOptionPane.showMessageDialog(null, message, "Results", JOptionPane.INFORMATION_MESSAGE);
        }
        return !this.continueSimulation.test(this.getEnvironment());
    }

}
