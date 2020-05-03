package iot;

import EnvironmentAPI.SensorEnvironment;
import EnvironmentAPI.util.EnvironmentReader;
import EnvironmentAPI.util.EnvironmentWriter;
import application.pollution.PollutionGrid;
import application.pollution.PollutionMonitor;
import application.routing.*;
import application.routing.heuristic.DistanceHeuristic;
import application.routing.heuristic.SimplePollutionHeuristic;
import gui.MainGUI;
import iot.mqtt.MQTTClientFactory;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.NetworkServer;
import iot.networkentity.UserMote;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import selfadaptation.feedbackloop.ReliableEfficientDistanceGateway;
import selfadaptation.feedbackloop.SignalBasedAdaptation;
import selfadaptation.instrumentation.MoteEffector;
import selfadaptation.instrumentation.MoteProbe;
import util.MutableInteger;
import util.Pair;
import util.Statistics;
import util.xml.*;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

/**
 * Class used for running a simulation
 * @author Marlon Saelens
 */

public class SimulationRunner {
    private static SimulationRunner instance = null;

    private List<InputProfile> inputProfiles;
    private List<GenericFeedbackLoop> algorithms;
    private QualityOfService QoS;
    private Parameters parameters;
    private File parameterFile;

    private final Simulation simulation;
    private Environment environment;
    private List<MoteProbe> moteProbe;
    private PollutionGrid pollutionGrid;

    private RoutingApplication routingApplication;
    private PollutionMonitor pollutionMonitor;
    public NetworkServer networkServer;
    private MutableInteger updateFrequency;
    private SimulationUpdateListener listener;
    private HashMap<Mote, Pair<Long, Long>> timeResults;
    private int amountRuns;
    private boolean multipleRun;
    private boolean couldLoadPollutionFile;
    // Results for visualising experiments
    private HashMap<Mote,HashMap<Integer,HashMap<Integer,Result>>> results;
    private HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> resultsBoxPlotAirQuality;
    private HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> resultsBoxPlotPredicatedAirQuality;
    private HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> resultsBoxPlotAdaptations;
    private HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> errorListConnections;
    private HashMap<Mote,List<Pair<Double,Double>>> visualiseRun;
    private HashMap<Mote,List<Double>> adaptationPoints;
    private HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoute;

    public HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> getAlternativeRoute(){
        return alternativeRoute;
    }

    public HashMap<Mote,List<Pair<Double,Double>>> getVisualiseRun(){
        return visualiseRun;
    }

    public HashMap<Mote,List<Double>> getAdaptationPoints(){
        return adaptationPoints;
    }

    private SensorEnvironment sensorEnvironment;

    public boolean getMultipleRun(){
        return  multipleRun;
    }

    public boolean isCouldLoadPollutionFile(){
        return couldLoadPollutionFile;
    }

    public HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> getResultsBoxPlotAirQuality(){
        return resultsBoxPlotAirQuality;
    }

    public HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> getResultsBoxPlotPredicatedAirQuality(){
        return resultsBoxPlotPredicatedAirQuality;
    }
    public HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> getResultsBoxPlotAdaptations(){
        return resultsBoxPlotAdaptations;
    }

    public HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Double>>>> getErrorListConnections(){
        return errorListConnections;
    }


    public Parameters getParameters(){
        return parameters;
    }

    public HashMap<Mote,HashMap<Integer,HashMap<Integer,Result>>> getResults(){
        return results;
    }

    public static SimulationRunner getInstance(File parameterFile) {
        if (instance == null) {
            instance = new SimulationRunner(parameterFile);
        }

        return instance;
    }

    public static SimulationRunner getInstance() {
        if (instance == null) {
            instance = new SimulationRunner();
        }

        return instance;
    }

    /**
     * Initialise a simulationRunner with certain parameters derived from a file
     * @param parameterfile the file to derive the parameters from
     */
    private SimulationRunner(File parameterfile) {
        this.multipleRun = false;
        QoS = new QualityOfService(new HashMap<>());
        QoS.putAdaptationGoal("reliableCommunication", new IntervalAdaptationGoal(0.0, 0.0));
        QoS.putAdaptationGoal("energyConsumption", new ThresholdAdaptationGoal(0.0));
        QoS.putAdaptationGoal("collisionBound", new ThresholdAdaptationGoal(0.0));

        this.sensorEnvironment = new SensorEnvironment();
        simulation = new Simulation(pollutionGrid,sensorEnvironment,sensorEnvironment.getPoll(),this);
        inputProfiles = loadInputProfiles();
        parameterFile = parameterfile;
        parameters = loadParameters(parameterfile);
        this.couldLoadPollutionFile = false;

        // Loading all the algorithms
        GenericFeedbackLoop noAdaptation = new GenericFeedbackLoop("No Adaptation") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        GenericFeedbackLoop airQuality = new GenericFeedbackLoop("Air Quality") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        GenericFeedbackLoop information = new GenericFeedbackLoop("Get Information") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        GenericFeedbackLoop bestPath = new GenericFeedbackLoop("Best Path") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        algorithms = new ArrayList<>();
        algorithms.add(noAdaptation);

        SignalBasedAdaptation signalBasedAdaptation = new SignalBasedAdaptation(QoS);
        algorithms.add(signalBasedAdaptation);

        ReliableEfficientDistanceGateway reliableEfficientDistanceGateway = new ReliableEfficientDistanceGateway();
        algorithms.add(reliableEfficientDistanceGateway);

        //Airquality airqualityAdaptation = new Airquality();
        algorithms.add(airQuality);
        algorithms.add(information);
        algorithms.add(bestPath);


        /*
         * Setting the mote probes
         */
        moteProbe = new LinkedList<>();
        List<MoteEffector> moteEffector = new LinkedList<>();
        for (int i = 0; i < algorithms.size(); i++) {
            moteProbe.add(new MoteProbe());
            moteEffector.add(new MoteEffector());
        }
        for (GenericFeedbackLoop feedbackLoop : algorithms) {
            feedbackLoop.setMoteProbe(moteProbe.get(algorithms.indexOf(feedbackLoop)));
            feedbackLoop.setMoteEffector(moteEffector.get(algorithms.indexOf(feedbackLoop)));
        }

        networkServer = new NetworkServer(MQTTClientFactory.getSingletonInstance());
        pollutionGrid = new PollutionGrid();
        environment = null;
    }

    private SimulationRunner() {
        this.multipleRun = false;
        QoS = new QualityOfService(new HashMap<>());
        QoS.putAdaptationGoal("reliableCommunication", new IntervalAdaptationGoal(0.0, 0.0));
        QoS.putAdaptationGoal("energyConsumption", new ThresholdAdaptationGoal(0.0));
        QoS.putAdaptationGoal("collisionBound", new ThresholdAdaptationGoal(0.0));

        this.sensorEnvironment = new SensorEnvironment();
        simulation = new Simulation(pollutionGrid,sensorEnvironment,sensorEnvironment.getPoll(),this);
        inputProfiles = loadInputProfiles();
        parameters = new Parameters();

        // Loading all the algorithms
        GenericFeedbackLoop noAdaptation = new GenericFeedbackLoop("No Adaptation") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        GenericFeedbackLoop airQuality = new GenericFeedbackLoop("Air Quality") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        GenericFeedbackLoop bestPath = new GenericFeedbackLoop("Best Path") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {


            }
        };

        algorithms = new ArrayList<>();
        algorithms.add(noAdaptation);

        SignalBasedAdaptation signalBasedAdaptation = new SignalBasedAdaptation(QoS);
        algorithms.add(signalBasedAdaptation);

        ReliableEfficientDistanceGateway reliableEfficientDistanceGateway = new ReliableEfficientDistanceGateway();
        algorithms.add(reliableEfficientDistanceGateway);
        algorithms.add(airQuality);
        algorithms.add(bestPath);


        /*
         * Setting the mote probes
         */
        moteProbe = new LinkedList<>();
        List<MoteEffector> moteEffector = new LinkedList<>();
        for (int i = 0; i < algorithms.size(); i++) {
            moteProbe.add(new MoteProbe());
            moteEffector.add(new MoteEffector());
        }
        for (GenericFeedbackLoop feedbackLoop : algorithms) {
            feedbackLoop.setMoteProbe(moteProbe.get(algorithms.indexOf(feedbackLoop)));
            feedbackLoop.setMoteEffector(moteEffector.get(algorithms.indexOf(feedbackLoop)));
        }

        networkServer = new NetworkServer(MQTTClientFactory.getSingletonInstance());
        pollutionGrid = new PollutionGrid();
        environment = null;
    }


    // region getters/setters

    public Environment getEnvironment() {
        return this.environment;
    }

    public List<InputProfile> getInputProfiles() {
        return inputProfiles;
    }

    public List<GenericFeedbackLoop> getAlgorithms() {
        return algorithms;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Map<Long,List<Double>> getInformation(){
        return simulation.getInformation();
    }

    public QualityOfService getQoS() {
        return QoS;
    }

    public RoutingApplication getRoutingApplication() {
        return routingApplication;
    }

    public final PollutionGrid getPollutionGrid() {
        return pollutionGrid;
    }


    public void setApproach(String name) {
        var selectedAlgorithm = algorithms.stream()
            .filter(o -> o.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("Could not load approach with name %s", name)));

        if(selectedAlgorithm.getName() != null && selectedAlgorithm.getName() == "Air Quality")
        {
            for(Mote mote : environment.getMotes())
            {
                if(mote instanceof UserMote)
                {
                    ((UserMote) mote).setAdaptation();
                }
            }
        }
        else{
            for(Mote mote : environment.getMotes())
            {
                if(mote instanceof UserMote)
                {
                    ((UserMote) mote).resetAdaptation();
                }
            }
        }

        simulation.setApproach(selectedAlgorithm);

        if(selectedAlgorithm.getName() == "Best Path")
        {
                for (Mote mote : environment.getMotes()) {

                    if (mote instanceof UserMote && ((UserMote) mote).isActive()) {
                        MoteEffector moteEffector = new MoteEffector();
                        BestPath bestpath = new BestPath(new SimplePollutionHeuristic(pollutionGrid,sensorEnvironment),sensorEnvironment);
                        moteEffector.bestPath(mote, bestpath, environment);
                    }

                }
        }
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void updateQoS(QualityOfService QoS) {
        this.QoS.updateAdaptationGoals(QoS);
    }

    // endregion


    // region setup simulations



    public void setupSingleRun() {
        this.setupSingleRun(true);
        // - Remove previous pollution measurements
        // destruct the earlier routing application
        this.routingApplication.destruct();
        // setting routing application dependent on the selected approach
        if(simulation.getApproach()!= null && simulation.getApproach().getName()=="Air Quality") {
            switch (parameters.getAnalysingMethod()) {
                case 0:
                    this.routingApplication = new RoutingApplication1(
                        new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid, sensorEnvironment)), getEnvironment());
                    simulation.setRoutingApplication(routingApplication);
                    break;
                case 1:
                    this.routingApplication = new RoutingApplication2(
                        new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid, sensorEnvironment)), getEnvironment(), parameters.getBetterpath());
                    simulation.setRoutingApplication(routingApplication);
                    break;
                case 2:
                    this.routingApplication = new RoutingApplication3(
                        new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid, sensorEnvironment)), getEnvironment(), parameters.getBetterpath());
                    simulation.setRoutingApplication(routingApplication);
                    break;
            }
        }
        else{
            this.routingApplication = new RoutingApplication1(
                new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid, sensorEnvironment)), getEnvironment());
            simulation.setRoutingApplication(routingApplication);
        }
        if(getParameters().getSetupFirst()==1){
            simulation.initialiseMote();
        }
        else{
            if(getParameters().getSetupFirst()==2){
                simulation.initialiseMote();
            }
        }

    }

    /**
     * Set the simulator up for a single (regular) run.
     * @param startFresh If true: reset the history stored in the {@link Statistics} class.
     */
    public void setupSingleRun(boolean startFresh) {
        synchronized (simulation) {
            simulation.setupSingleRun(startFresh);
        }
        this.setupSimulationRunner();
    }

    /**
     * Function for doing multiple runs after each other for getting a result about the average time
     * and worste time of doing a decision wether or not change the path
     * @param updateFrequency the frequency to visually update the path
     * @param listener listener used to update the visibility of the mote
     */
    public void multipleRun(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        this.amountRuns = getParameters().getAmountRuns();
        this.multipleRun = true;
        this.updateFrequency = updateFrequency;
        this.listener = listener;
        HashMap<Mote,Pair<Double,Integer>> result = new HashMap<>();
        multipleRun(this.timeResults,result);
    }

    /**
     * Function used for getting data for plotting a graphic to show the relationship between the
     * parameters of the adaptation method and the air quality/number of adaptations
     * @param minimumBufferSizeHeight buffersize height to start the iteration
     * @param maximumBufferSizeHeight buffersize height to stop the iteration
     * @param minimumBufferSizeWidth buffersize width to start the iteration
     * @param maximumBufferSizeWidth buffersiz width to end the iteration
     * For example minHeight:1,maxHeight2,minWidth:1,maxWidth:2, will gather information for height 1
     * width 1, height 1 width 2, height 2 width 1 and height 2 width 2
     */
    public void resultRun(int minimumBufferSizeHeight, int maximumBufferSizeHeight, int minimumBufferSizeWidth, int maximumBufferSizeWidth){
        results = new HashMap<>();
        int currenctBufferSizeHeight = minimumBufferSizeHeight;
        int currentBufferSizeWidth = minimumBufferSizeWidth;
        this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
        this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
        this.getParameters().setSetupFirst(0);
        // setting noAdaptation Method
        simulation.setApproach(algorithms.get(0));
        simulation.setAdaptationAlgorithm(algorithms.get(0));
        setupSingleRun();
        simulation.setupSingleRun(true);
        setupSimulationRunner();
        simulateCalculatedRunForResult();
        for(Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                HashMap<Integer, HashMap<Integer, Result>> results1 = new HashMap<>();
                HashMap<Integer, Result> results2 = new HashMap<>();
                Result result = new Result(simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI()), routingApplication.getAmountAdaptations().get(mote.getEUI()), simulation.getDistanceMote().get(mote));
                    while(currenctBufferSizeHeight<=maximumBufferSizeHeight) {
                    results2.put(currenctBufferSizeHeight, result);
                    currenctBufferSizeHeight +=1;
                    ArrayList<GeoPosition> path = new ArrayList<>();
                    path.add(environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt()));
                    mote.setPath(path);
                }
                results1.put(0, results2);
                results.put(mote,results1);
                currenctBufferSizeHeight = minimumBufferSizeHeight;
            }
        }
        currenctBufferSizeHeight = minimumBufferSizeHeight;
        this.getParameters().setSetupFirst(1);
        simulation.setApproach(algorithms.get(3));
        simulation.setAdaptationAlgorithm(algorithms.get(3));
        setupMultipleRun(true);
        simulateCalculatedRunForResult();
        for(Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                HashMap<Integer, HashMap<Integer, Result>> results1 = results.get(mote);
                HashMap<Integer,Result> results2 = new HashMap<>();
                Result result = new Result(simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI()),routingApplication.getAmountAdaptations().get(mote.getEUI()),simulation.getDistanceMote().get(mote));
                results2.put(currenctBufferSizeHeight,result);
                results1.put(currentBufferSizeWidth,results2);
                results.put(mote,results1);
            }
        }


        while(!(currenctBufferSizeHeight == maximumBufferSizeHeight && currentBufferSizeWidth == maximumBufferSizeWidth)){
            if(currenctBufferSizeHeight!=maximumBufferSizeHeight){
                currenctBufferSizeHeight +=1;
                this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
            }
            else{
                currentBufferSizeWidth += 1;
                currenctBufferSizeHeight = minimumBufferSizeHeight;
                this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
                this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
            }
            setupMultipleRun(true);
            simulateCalculatedRunForResult();
            for(Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    if (results.get(mote).containsKey(currentBufferSizeWidth)) {
                        HashMap<Integer, Result> results2 = results.get(mote).get(currentBufferSizeWidth);
                        Result result = new Result(simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI()), routingApplication.getAmountAdaptations().get(mote.getEUI()), simulation.getDistanceMote().get(mote));
                        results2.put(currenctBufferSizeHeight, result);
                    } else {
                        HashMap<Integer, HashMap<Integer, Result>> results1 = results.get(mote);
                        HashMap<Integer, Result> results2 = new HashMap<>();
                        Result result = new Result(simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI()), routingApplication.getAmountAdaptations().get(mote.getEUI()), simulation.getDistanceMote().get(mote));
                        results2.put(currenctBufferSizeHeight, result);
                        results1.put(currentBufferSizeWidth, results2);
                        results.put(mote, results1);
                    }

                }
            }
        }

    }

    /**
     * Function to gather information for visualising the utility of using an adpation method for
     * following the best path for one run with specific parameters
     * @param bufferSizeWidth buffersize Width used in the simulation
     * @param bufferSizeHeight buffersize height used in the simulation
     */
    public void visualiseRun(int bufferSizeWidth,int bufferSizeHeight){
        this.getParameters().setBufferSizeHeight(bufferSizeHeight);
        this.getParameters().setBuffersizeWidth(bufferSizeWidth);
        this.getParameters().setSetupFirst(1);
        this.getParameters().setAnalysingMethod(0);
        simulation.setApproach(algorithms.get(3));
        setupSingleRun();
        this.setupSimulationRunner();
        for (Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                ArrayList<GeoPosition> path = new ArrayList<>();
                path.add(environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt()));
                mote.setPath(path);
            }
        }
        int sizeGateWays = environment.getGateways().size();
        if (sizeGateWays > 0) {
            environment.getGateways().subList(0, sizeGateWays).clear();
        }
        simulation.initialiseMote();
        simulation.setupSingleRun(true);
        simulateVisualiseRun();
        visualiseRun = routingApplication.getVisualiseRun();
        adaptationPoints = routingApplication.getAdaptationPoints();
        alternativeRoute = routingApplication.getAlternativeRoute();
    }

    /**
     * Function for gathering information for multiple runs for some specific parameters used for
     * visualising them via a boxplot
     * This is used to visualise the diversity of the values for air quality and number of adaptations
     * for a certain amount of runs with noise added
     * @param amountRuns the amount of runs per parameter selection
     * @param configurationList the list of parameters to gather the information from
     */
    public void resultRunBoxPlot(int amountRuns, List<Pair<Integer,Integer>> configurationList){
        resultsBoxPlotAirQuality = new HashMap<>();
        resultsBoxPlotAdaptations = new HashMap<>();
        this.getParameters().setSetupFirst(0);
        // setting noAdaptation Method
        simulation.setApproach(algorithms.get(0));
        simulation.setAdaptationAlgorithm(algorithms.get(0));
        setupSingleRun();
        simulation.setupSingleRun(true);
        setupSimulationRunner();
        simulateCalculatedRunForResult();
        HashMap<Mote,List<Double>> resultSaver1 = new HashMap<>();
        for(Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                double airQuality = simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI());
                List<Double> resultAirQuality = new ArrayList<>();
                resultAirQuality.add(airQuality);
                resultSaver1.put(mote,resultAirQuality);
            }
        }
        for(int i=0;i<configurationList.size();i++){
            for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
                HashMap<Integer, HashMap<Integer, List<Double>>> results1a = new HashMap<>();
                HashMap<Integer, List<Double>> results2a = new HashMap<>();
                results2a.put(0,resultSaver1.get(moteEntry.getKey()));
                results1a.put(0,results2a);
                resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
            }
        }
        Pair<Integer,Integer> firstConfiguration = configurationList.get(0);
        int currenctBufferSizeHeight = firstConfiguration.getLeft();
        int currentBufferSizeWidth = firstConfiguration.getRight();
        configurationList.remove(0);
        this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
        this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
        this.getParameters().setSetupFirst(1);
        simulation.setApproach(algorithms.get(3));
        resultSaver1 = new HashMap<>();
        HashMap<Mote,List<Integer>> resultSaver2 = new HashMap<>();
        int experiment = 1;
        for (Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                ArrayList<GeoPosition> path = new ArrayList<>();
                path.add(environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt()));
                mote.setPath(path);
            }
        }
        while(experiment<= amountRuns) {
            setupSingleRun();
            simulation.setupSingleRun(true);
            setupSimulationRunner();
            simulateCalculatedRunForResult();
            for (Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    double airQuality = simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI());
                    int adaptations = routingApplication.getAmountAdaptations().get(mote.getEUI());
                    if(resultSaver1.get(mote)!=null) {
                        List<Double> resultAirQuality = resultSaver1.get(mote);
                        List<Integer> resultAdaptations = resultSaver2.get(mote);
                        resultAirQuality.add(airQuality);
                        resultAdaptations.add(adaptations);
                        resultSaver1.put(mote,resultAirQuality);
                        resultSaver2.put(mote,resultAdaptations);
                    }
                    else{
                        List<Double> resultAirQuality = new ArrayList<>();
                        List<Integer> resultAdaptations = new ArrayList<>();
                        resultAirQuality.add(airQuality);
                        resultAdaptations.add(adaptations);
                        resultSaver1.put(mote,resultAirQuality);
                        resultSaver2.put(mote,resultAdaptations);
                    }
                }
            }
            experiment += 1;
        }
        for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
            HashMap<Integer, HashMap<Integer, List<Double>>> results1a = resultsBoxPlotAirQuality.get(moteEntry.getKey());
            HashMap<Integer, HashMap<Integer, List<Integer>>> results1b = new HashMap<>();
            HashMap<Integer, List<Double>> results2a = new HashMap<>();
            HashMap<Integer, List<Integer>> results2b = new HashMap<>();
            results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
            results2b.put(currentBufferSizeWidth,resultSaver2.get(moteEntry.getKey()));
            results1a.put(currenctBufferSizeHeight,results2a);
            results1b.put(currenctBufferSizeHeight,results2b);
            resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
            resultsBoxPlotAdaptations.put(moteEntry.getKey(),results1b);
        }
        while(!(configurationList.isEmpty())){
            Pair<Integer,Integer> nextConfiguration = configurationList.get(0);
            currenctBufferSizeHeight = nextConfiguration.getLeft();
            currentBufferSizeWidth = nextConfiguration.getRight();
            configurationList.remove(0);
            this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
            this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
            resultSaver1 = new HashMap<>();
            resultSaver2 = new HashMap<>();
            experiment = 1;
            while(experiment<= amountRuns) {
                setupMultipleRun(true);
                simulateCalculatedRunForResult();
                for (Mote mote : environment.getMotes()) {
                    if (mote instanceof UserMote) {
                        double airQuality = simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI());
                        int adaptations = routingApplication.getAmountAdaptations().get(mote.getEUI());
                        if(resultSaver1.get(mote)!=null) {
                            List<Double> resultAirQuality = resultSaver1.get(mote);
                            List<Integer> resultAdaptations = resultSaver2.get(mote);
                            resultAirQuality.add(airQuality);
                            resultAdaptations.add(adaptations);
                            resultSaver1.put(mote,resultAirQuality);
                            resultSaver2.put(mote,resultAdaptations);
                        }
                        else{
                            List<Double> resultAirQuality = new ArrayList<>();
                            List<Integer> resultAdaptations = new ArrayList<>();
                            resultAirQuality.add(airQuality);
                            resultAdaptations.add(adaptations);
                            resultSaver1.put(mote,resultAirQuality);
                            resultSaver2.put(mote,resultAdaptations);
                        }
                    }
                }
                experiment += 1;
            }
            for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
                HashMap<Integer, HashMap<Integer, List<Double>>> results1a = resultsBoxPlotAirQuality.get(moteEntry.getKey());
                HashMap<Integer, HashMap<Integer, List<Integer>>> results1b = resultsBoxPlotAdaptations.get(moteEntry.getKey());
                if(results1a.get(currenctBufferSizeHeight)!=null) {
                    HashMap<Integer, List<Double>> results2a = results1a.get(currenctBufferSizeHeight);
                    HashMap<Integer, List<Integer>> results2b = results1b.get(currenctBufferSizeHeight);
                    results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
                    results2b.put(currentBufferSizeWidth,resultSaver2.get(moteEntry.getKey()));
                    results1a.put(currenctBufferSizeHeight,results2a);
                    results1b.put(currenctBufferSizeHeight,results2b);
                    resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
                    resultsBoxPlotAdaptations.put(moteEntry.getKey(),results1b);
                }
                else{
                    HashMap<Integer, List<Double>> results2a = new HashMap<>();
                    HashMap<Integer, List<Integer>> results2b = new HashMap<>();
                    results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
                    results2b.put(currentBufferSizeWidth,resultSaver2.get(moteEntry.getKey()));
                    results1a.put(currenctBufferSizeHeight,results2a);
                    results1b.put(currenctBufferSizeHeight,results2b);
                    resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
                    resultsBoxPlotAdaptations.put(moteEntry.getKey(),results1b);
                }

            }
        }

    }

    /**
     * Function for gathering information for multiple runs for some specific parameters used for
     * visualising them via a boxplot
     * This is used for visualising the diversity of effective values for the air quality for an amount of runs
     * and the averages of all predicted values for the air quality for the whole run for a certain
     * amount of runs and added noise
     * @param amountRuns the amount of runs per parameter selection
     * @param configurationList the list of parameters to gather the information from
     */
    public void resultRunBoxPlot2(int amountRuns, List<Pair<Integer,Integer>> configurationList){
        resultsBoxPlotAirQuality = new HashMap<>();
        resultsBoxPlotPredicatedAirQuality = new HashMap<>();
        Pair<Integer,Integer> firstConfiguration = configurationList.get(0);
        int currenctBufferSizeHeight = firstConfiguration.getLeft();
        int currentBufferSizeWidth = firstConfiguration.getRight();
        configurationList.remove(0);
        this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
        this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
        this.getParameters().setSetupFirst(1);
        simulation.setApproach(algorithms.get(3));
        HashMap<Mote,List<Double>> resultSaver1 = new HashMap<>();
        HashMap<Mote,List<Double>> resultSaver2 = new HashMap<>();
        int experiment = 1;
        for (Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                ArrayList<GeoPosition> path = new ArrayList<>();
                path.add(environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt()));
                mote.setPath(path);
            }
        }
        while(experiment<= amountRuns) {
            setupSingleRun();
            simulation.setupSingleRun(true);
            setupSimulationRunner();
            simulateVisualiseRun2();
            for (Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    double airQuality = simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI());
                    double prediction = routingApplication.getPredicted().get(mote)/routingApplication.getAmountWaypoints().get(mote);
                    if(resultSaver1.get(mote)!=null) {
                        List<Double> resultAirQuality = resultSaver1.get(mote);
                        List<Double> resultPredictions = resultSaver2.get(mote);
                        resultAirQuality.add(airQuality);
                        resultPredictions.add(prediction);
                        resultSaver1.put(mote,resultAirQuality);
                        resultSaver2.put(mote,resultPredictions);
                    }
                    else{
                        List<Double> resultAirQuality = new ArrayList<>();
                        List<Double> resultPredictions = new ArrayList<>();
                        resultAirQuality.add(airQuality);
                        resultPredictions.add(prediction);
                        resultSaver1.put(mote,resultAirQuality);
                        resultSaver2.put(mote,resultPredictions);
                    }
                }
            }
            experiment += 1;
        }
        for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
            HashMap<Integer, HashMap<Integer, List<Double>>> results1a = new HashMap<>();
            HashMap<Integer, HashMap<Integer, List<Double>>> results1b = new HashMap<>();
            HashMap<Integer, List<Double>> results2a = new HashMap<>();
            HashMap<Integer, List<Double>> results2b = new HashMap<>();
            results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
            results2b.put(currentBufferSizeWidth,resultSaver2.get(moteEntry.getKey()));
            results1a.put(currenctBufferSizeHeight,results2a);
            results1b.put(currenctBufferSizeHeight,results2b);
            resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
            resultsBoxPlotPredicatedAirQuality.put(moteEntry.getKey(),results1b);
        }
        while(!(configurationList.isEmpty())){
            Pair<Integer,Integer> nextConfiguration = configurationList.get(0);
            currenctBufferSizeHeight = nextConfiguration.getLeft();
            currentBufferSizeWidth = nextConfiguration.getRight();
            configurationList.remove(0);
            this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
            this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
            resultSaver1 = new HashMap<>();
            resultSaver2 = new HashMap<>();
            experiment = 1;
            while(experiment<= amountRuns) {
                setupMultipleRun(true);
                simulateVisualiseRun2();
                for (Mote mote : environment.getMotes()) {
                    if (mote instanceof UserMote) {
                        double airQuality = simulation.getRouteEvaluator().getTotalCostPath(mote.getEUI());
                        double prediction = routingApplication.getPredicted().get(mote)/routingApplication.getAmountWaypoints().get(mote);
                        if(resultSaver1.get(mote)!=null) {
                            List<Double> resultAirQuality = resultSaver1.get(mote);
                            List<Double> resultPredictions = resultSaver2.get(mote);
                            resultAirQuality.add(airQuality);
                            resultPredictions.add(prediction);
                            resultSaver1.put(mote,resultAirQuality);
                            resultSaver2.put(mote,resultPredictions);
                        }
                        else{
                            List<Double> resultAirQuality = new ArrayList<>();
                            List<Double> resultPredictions = new ArrayList<>();
                            resultAirQuality.add(airQuality);
                            resultPredictions.add(prediction);
                            resultSaver1.put(mote,resultAirQuality);
                            resultSaver2.put(mote,resultPredictions);
                        }
                    }
                }
                experiment += 1;
            }
            for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
                HashMap<Integer, HashMap<Integer, List<Double>>> results1a = resultsBoxPlotAirQuality.get(moteEntry.getKey());
                HashMap<Integer, HashMap<Integer, List<Double>>> results1b = resultsBoxPlotPredicatedAirQuality.get(moteEntry.getKey());
                if(results1a.get(currenctBufferSizeHeight)!=null) {
                    HashMap<Integer, List<Double>> results2a = results1a.get(currenctBufferSizeHeight);
                    HashMap<Integer, List<Double>> results2b = results1b.get(currenctBufferSizeHeight);
                    results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
                    results2b.put(currentBufferSizeWidth,resultSaver2.get(moteEntry.getKey()));
                    results1a.put(currenctBufferSizeHeight,results2a);
                    results1b.put(currenctBufferSizeHeight,results2b);
                    resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
                    resultsBoxPlotPredicatedAirQuality.put(moteEntry.getKey(),results1b);
                }
                else{
                    HashMap<Integer, List<Double>> results2a = new HashMap<>();
                    HashMap<Integer, List<Double>> results2b = new HashMap<>();
                    results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
                    results2b.put(currentBufferSizeWidth,resultSaver2.get(moteEntry.getKey()));
                    results1a.put(currenctBufferSizeHeight,results2a);
                    results1b.put(currenctBufferSizeHeight,results2b);
                    resultsBoxPlotAirQuality.put(moteEntry.getKey(),results1a);
                    resultsBoxPlotPredicatedAirQuality.put(moteEntry.getKey(),results1b);
                }

            }
        }

    }

    /**
     * Function for gathering information for multiple runs for some specific parameters used for
     * visualising them via a boxplot
     * This method is used for visualising the diversity of the average difference of the predicted cost of the next connection
     * to take by the mote calculated by the routing application and the effective cost
     * of that connection calculated by the route-evaluator for a certain amount of runs
     * @param amountRuns the amount of runs per parameter selection
     * @param configurationList the list of parameters to gather the information from
     */
    public void resultRunBoxPlot3(int amountRuns, List<Pair<Integer,Integer>> configurationList){
        errorListConnections = new HashMap<>();
        Pair<Integer,Integer> firstConfiguration = configurationList.get(0);
        int currenctBufferSizeHeight = firstConfiguration.getLeft();
        int currentBufferSizeWidth = firstConfiguration.getRight();
        configurationList.remove(0);
        this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
        this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
        this.getParameters().setSetupFirst(1);
        simulation.setApproach(algorithms.get(3));
        HashMap<Mote,List<Double>> resultSaver1 = new HashMap<>();
        int experiment = 1;
        for (Mote mote : environment.getMotes()) {
            if (mote instanceof UserMote) {
                ArrayList<GeoPosition> path = new ArrayList<>();
                path.add(environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt()));
                mote.setPath(path);
            }
        }
        while(experiment<= amountRuns) {
            setupSingleRun();
            simulation.setupSingleRun(true);
            setupSimulationRunner();
            simulateVisualiseRun();
            for (Mote mote : environment.getMotes()) {
                if (mote instanceof UserMote) {
                    List<Double> effectiveResultsConnections = simulation.getRouteEvaluator().getEffectiveValueConnection().get(mote);
                    List<Double> predictiveResults = routingApplication.getPredictiveWaypoints().get(mote);
                    List<Double> difference = new ArrayList<>();
                    for(int i=0;i<effectiveResultsConnections.size();i++){
                        difference.add(java.lang.Math.abs(predictiveResults.get(i)-effectiveResultsConnections.get(i)));
                    }
                    double averageDifference =0;
                    for(double differenceConn : difference){
                        averageDifference += differenceConn;
                    }
                    averageDifference = averageDifference/difference.size();
                    if(resultSaver1.get(mote)!=null) {
                        List<Double> costConnections = resultSaver1.get(mote);

                        costConnections.add(averageDifference);
                        resultSaver1.put(mote,costConnections);
                    }
                    else{
                        List<Double> costConnections = new ArrayList<>();
                        costConnections.add(averageDifference);
                        resultSaver1.put(mote,costConnections);
                    }
                }
            }
            experiment += 1;
        }
        for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
            HashMap<Integer, HashMap<Integer, List<Double>>> results1a = new HashMap<>();
            HashMap<Integer, List<Double>> results2a = new HashMap<>();
            results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
            results1a.put(currenctBufferSizeHeight,results2a);
            errorListConnections.put(moteEntry.getKey(),results1a);
        }
        while(!(configurationList.isEmpty())){
            Pair<Integer,Integer> nextConfiguration = configurationList.get(0);
            currenctBufferSizeHeight = nextConfiguration.getLeft();
            currentBufferSizeWidth = nextConfiguration.getRight();
            configurationList.remove(0);
            this.getParameters().setBufferSizeHeight(currenctBufferSizeHeight);
            this.getParameters().setBuffersizeWidth(currentBufferSizeWidth);
            resultSaver1 = new HashMap<>();
            experiment = 1;
            while(experiment<= amountRuns) {
                setupMultipleRun(true);
                simulateVisualiseRun();
                for (Mote mote : environment.getMotes()) {
                    if (mote instanceof UserMote) {
                        List<Double> effectiveResultsConnections = simulation.getRouteEvaluator().getEffectiveValueConnection().get(mote);
                        List<Double> predictiveResults = routingApplication.getPredictiveWaypoints().get(mote);
                        List<Double> difference = new ArrayList<>();
                        for(int i=0;i<=effectiveResultsConnections.size();i++){
                            difference.add(java.lang.Math.abs(predictiveResults.get(i)-effectiveResultsConnections.get(i)));
                        }
                        double averageDifference =0;
                        for(double differenceConn : difference){
                            averageDifference += differenceConn;
                        }
                        averageDifference = averageDifference/difference.size();
                        if(resultSaver1.get(mote)!=null) {
                            List<Double> costConnections = resultSaver1.get(mote);

                            costConnections.add(averageDifference);
                            resultSaver1.put(mote,costConnections);
                        }
                        else{
                            List<Double> costConnections = new ArrayList<>();
                            costConnections.add(averageDifference);
                            resultSaver1.put(mote,costConnections);
                        }
                    }
                }
                experiment += 1;
            }
            for (Map.Entry<Mote,List<Double>> moteEntry : resultSaver1.entrySet()) {
                HashMap<Integer, HashMap<Integer, List<Double>>> results1a = errorListConnections.get(moteEntry.getKey());
                if(results1a.get(currenctBufferSizeHeight)!=null) {
                    HashMap<Integer, List<Double>> results2a = results1a.get(currenctBufferSizeHeight);
                    results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
                    results1a.put(currenctBufferSizeHeight,results2a);
                    errorListConnections.put(moteEntry.getKey(),results1a);
                }
                else{
                    HashMap<Integer, List<Double>> results2a = new HashMap<>();
                    results2a.put(currentBufferSizeWidth,resultSaver1.get(moteEntry.getKey()));
                    results1a.put(currenctBufferSizeHeight,results2a);
                    errorListConnections.put(moteEntry.getKey(),results1a);
                }

            }
        }

    }

    /**
     * Function used for initialising the next run in the multiple run option
     * @param time timeresults of the earlier runs
     * @param result results about air-quality and amount of adaptations to show if
     *               we have done a certain amount of runs. These results have not to be updated because
     *               the air quality for multiple runs without noise is almost the same. For doing
     *               multiple runs with noise, there is an experimental option to do multiple runs
     *               after each other and visualise the results by a boxplot, see method resultRunBoxPlot
     */
    public void multipleRun(HashMap<Mote, Pair<Long,Long>> time,HashMap<Mote,Pair<Double,Integer>> result){
        if(amountRuns != 0) {
                setupMultipleRun(true);
                simulate(updateFrequency, listener);
                amountRuns = amountRuns -1;
                for(Mote mote : environment.getMotes()) {
                    Pair<Long, Long> timeMote = time.get(mote);
                    if (timeResults.get(mote) == null){
                        timeResults.put(mote,timeMote);
                    }
                    else{
                        long average = timeResults.get(mote).getLeft() + timeMote.getLeft();
                        long maxTime = timeResults.get(mote).getRight() + timeMote.getRight();
                        timeResults.put(mote,new Pair(average,maxTime));
                    }
                }
            }
        else{
            String message = "Evaluation of the path \n";
            for(Mote mote : environment.getMotes()){
                if(mote instanceof UserMote) {
                    long averageTime = time.get(mote).getLeft()/getParameters().getAmountRuns();
                    long maxTime = time.get(mote).getRight()/getParameters().getAmountRuns();
                    message += "EUI: " + mote.getEUI() + "  :    " + result.get(mote).getLeft()+ "\n" +
                        " , Amount adaptations: " + result.get(mote).getRight() + " , AverageTimeDecision: " + averageTime +
                        " Max Time" + maxTime + "\n";
                }
            }
            JOptionPane.showMessageDialog(null, message, "Results", JOptionPane.INFORMATION_MESSAGE);
            this.listener = null;
            this.updateFrequency = null;


        }
        }

    /**
     * Set the simulator up for MultipleRun
     * @param startFresh If true: reset the history stored in the {@link Statistics} class.
     */
    public void setupMultipleRun(boolean startFresh) {
        synchronized (simulation) {
            simulation.setupSingleRun(startFresh);
        }
        setupSingleRun();

        this.setupSimulationRunner();
    }

    /**
     * Setup of applications/servers/clients before each run.
     */
    public void setupSimulationRunner() {
        getEnvironmentAPI().getPoll().clear();
        timeResults = new HashMap<Mote, Pair<Long, Long>>();
        pollutionGrid.clean();
        sensorEnvironment.getPoll().clear();

        // Reset received transmissions in the networkServer
        this.networkServer.reset();
        if(simulation.getApproach()==null || simulation.getApproach().getName().equals("No Adaptation"))
        {
            for (Mote mote : environment.getMotes()) {

                if (mote instanceof UserMote && ((UserMote) mote).isActive()) {
                    MoteEffector moteEffector = new MoteEffector();
                    moteEffector.changePath(mote, new KAStarRouter(new DistanceHeuristic()), environment);
                }

            }
        }

    }

    // endregion


    // region simulations

    /**
     * Check if the simulation has finished, based on the ending condition.
     * @return True if the simulation has finished.
     */
    private boolean isSimulationFinished() {
        return simulation.isFinished();
    }

    /**
     * Simulate a whole run, until the simulation is finished.
     * Result of the run is not visible
     *
     **/
    public synchronized void simulateCalculatedRunForResult() {
        while (!simulation.RunIsFinishedForResults()) {
            simulation.simulateStepRun(getEnvironmentAPI().getPoll());
        }
    }

    /**
     * Simulate a whole run, until the simulation is finished.
     * Result of the run is not visible
     * Used for making the utility of adaptation visible
     */
    public synchronized void simulateVisualiseRun() {
        while (!simulation.RunIsFinishedForResults()) {
            simulation.simulateVisualiseRun(getEnvironmentAPI().getPoll());
        }
    }

    /**
     * Simulate a whole run, until the simulation is finished.
     * Result of the run is not visible
     * Used for making the utility of adaptation visible
     */
    public synchronized void simulateVisualiseRun2() {
        while (!simulation.RunIsFinishedForResults()) {
            simulation.simulateVisualiseRun2(getEnvironmentAPI().getPoll());
        }
    }



    /**
     * Simulate a whole run, until the simulation is finished.
     * @param updateFrequency The frequency of callback updates to the {@code listener} (expressed in once every x simulation steps).
     * @param listener The listener which receives the callbacks every x simulation steps.
     */
    public synchronized void simulateCalculatedRun(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        new Thread(() -> {
            long simulationStep = 0;
            while (!simulation.RunisFinished()) {
                //synchronized (this.simulation){
                this.simulation.simulateStepRun(getEnvironmentAPI().getPoll());
                //};
                // Visualize every x seconds
                if (simulationStep++ % (updateFrequency.intValue() * 1000) == 0) {
                    listener.update();
                }
            }

            // Restore the initial positions after the run
            listener.update();
            listener.onEnd();
        }).start();
    }

    /**
     * Simulate a whole run, until the simulation is finished.
     * @param updateFrequency The frequency of callback updates to the {@code listener} (expressed in once every x simulation steps).
     * @param listener The listener which receives the callbacks every x simulation steps.
     */
    public synchronized void simulateCalculatedRun2(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        new Thread(() -> {
            long simulationStep = 0;
            while (!simulation.RunisFinished()) {
                //synchronized (this.simulation){
                this.simulation.simulateStepRun2(getEnvironmentAPI().getPoll());
                //};
                // Visualize every x seconds
                if (simulationStep++ % (updateFrequency.intValue() * 1000) == 0) {
                    listener.update();
                }
            }

            // Restore the initial positions after the run
            listener.update();
            listener.onEnd();
        }).start();
    }


    /**
     * Simulate a whole run, until the simulation is finished.
     * @param updateFrequency The frequency of callback updates to the {@code listener} (expressed in once every x simulation steps).
     * @param listener The listener which receives the callbacks every x simulation steps.
     */
    public synchronized void simulate(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        new Thread(() -> {
            long simulationStep = 0;
            while (!this.isSimulationFinished()) {
                //synchronized (this.simulation){
                    this.simulation.simulateStep(getEnvironmentAPI().getPoll());
                //};
                // Visualize every x seconds
                if (simulationStep++ % (updateFrequency.intValue() * 1000) == 0) {
                    listener.update();
                }
            }

            // Restore the initial positions after the run
            listener.update();
            listener.onEnd();
        }).start();
    }



    @SuppressWarnings("unused")
    public void totalRun() {
        this.totalRun(o -> {});
    }

    /**
     * Simulate (and keep track of) multiple runs with the simulator.
     * @param fn A callback function which is invoked after every executed single run.
     */
    public void totalRun(@NotNull Consumer<Pair<Integer, Integer>> fn) {
        int nrOfRuns = simulation.getInputProfile()
            .orElseThrow(() -> new IllegalStateException("No input profile selected before running the simulation"))
            .getNumberOfRuns();
        setupSingleRun(true);

        new Thread(() -> {
            fn.accept(new Pair<>(0, nrOfRuns));

            for (int i = 0; i < nrOfRuns; i++) {

                while (!simulation.isFinished()) {
                    this.simulation.simulateStep(sensorEnvironment.getPoll());
                }

                fn.accept(new Pair<>(i+1, nrOfRuns));

                if (i != nrOfRuns - 1) {
                    this.getEnvironment().addRun();
                    setupSingleRun(false);
                }
            }
        }).start();
    }

    // endregion


    // region loading/saving/cleanup

    public void updateInputProfilesFile() {
        File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        file = new File(file.getParent() + "/inputProfiles/inputProfile.xml");
        updateInputProfilesFile(file);
    }


    public void updateParametersFile() {
        ParameterWriter.updateParameterFile(parameters,parameterFile);
    }

    public void updateInputProfilesFile(File file) {
        InputProfilesWriter.updateInputProfilesFile(inputProfiles, file);
    }

    private List<InputProfile> loadInputProfiles() {
        return InputProfilesReader.readInputProfiles();
    }

    private Parameters loadParameters(File file) {
        return ParameterReader.readParameters(file);
    }

    public void loadEnvironmentFromFile(File file){

        EnvironmentReader.loadEnvironment(file,this);
    }

    /**
     * Load a configuration from a provided xml file.
     * @param file The file with the configuration.
     */
    public void loadConfigurationFromFile(File file) {
        this.cleanupSimulation();
        ConfigurationReader.loadConfiguration(file, this);
        simulation.setEnvironment(new WeakReference<>(this.getEnvironment()));
        if(simulation.getApproach()!=null && simulation.getApproach().getName()=="Air Quality")
        {
            for(Mote mote : environment.getMotes())
            {
                if(mote instanceof UserMote)
                {
                    ((UserMote) mote).setAdaptation();
                }
            }
        }

        for (Gateway gateway : simulation.getEnvironment().getGateways()) {
            for (int i = 0; i < algorithms.size(); i++) {
                gateway.addSubscription(moteProbe.get(i));
            }
        }

        setupApplications();
    }

    /**
     * Load parameters from a provided xml file.
     * @param file The file with the parameters
     */
    public void loadParametersFromFile(File file) {
        parameters = ParameterReader.readParameters(file);
        parameterFile = file;
    }

    public File getParameterFile(){
        return parameterFile;
    }


    /**
     * Save a configuration to a file.
     * @param file The file to save to.
     */
    public void saveConfigurationToFile(File file) {
        ConfigurationWriter.saveConfigurationToFile(file, this);
    }

    public void saveResultsToFile(File file){
        ResultWriter.saveResultsToFile(results,file);
    }

    public void readResultsFromFile(File file) throws ParserConfigurationException {
        this.results = ResultReader.readResults(file,this);
    }

    public void saveResultsBoxPlotToFile(File file){
        ResultWriterForBoxPlot.saveResultsToFile(this,resultsBoxPlotAirQuality,resultsBoxPlotAdaptations,file);
    }

    public void readResultsBoxPlotFromFile(File file) throws ParserConfigurationException {
        this.resultsBoxPlotAirQuality = ResultReaderForBoxPlot.readResultsAirQuality(file,this);
        this.resultsBoxPlotAdaptations = ResultReaderForBoxPlot.readResultsAdaptation(file,this);
    }

    public void saveResultsBoxPlotToFile2(File file){
        ResultWriterForBoxPlot2.saveResultsToFile(this,resultsBoxPlotAirQuality,resultsBoxPlotPredicatedAirQuality,file);
    }

    public void readResultsBoxPlotFromFile2(File file) throws ParserConfigurationException {
        this.resultsBoxPlotAirQuality = ResultReaderForBoxPlot2.readResultsAirQuality(file,this);
        this.resultsBoxPlotPredicatedAirQuality = ResultReaderForBoxPlot2.readResultsPredictions(file,this);
    }

    public void saveResultsBoxPlotToFile3(File file){
        ResultWriterForBoxPlot2.saveResultsToFile(this,resultsBoxPlotAirQuality,resultsBoxPlotPredicatedAirQuality,file);
    }

    public void readResultsBoxPlotFromFile3(File file) throws ParserConfigurationException {
        this.resultsBoxPlotAirQuality = ResultReaderForBoxPlot2.readResultsAirQuality(file,this);
        this.resultsBoxPlotPredicatedAirQuality = ResultReaderForBoxPlot2.readResultsPredictions(file,this);
    }

    public void saveSimulationToFile(File file) {
        SimulationWriter.saveSimulationToFile(file, simulation);
    }

    public void savePollutionConfiguration(File file) {
        EnvironmentWriter.saveEnvironment(this.sensorEnvironment.getPoll().getSources(), this.sensorEnvironment.getSensors(), file);
    }


    /**
     * Method which is called when a new configuration will be opened.
     * Provides manual cleanup for old applications/servers/clients/...
     */
    public void cleanupSimulation() {
        this.multipleRun = false;
        if (this.pollutionMonitor != null) {
            this.pollutionMonitor.destruct();
        }
        if (this.routingApplication != null) {
            this.routingApplication.destruct();
        }

        this.networkServer.reconnect();
        this.environment = null;

    }

    /**
     * Initialize all applications used in the simulation.
     */
    public void setupApplications() {
        this.pollutionMonitor = new PollutionMonitor(this.getEnvironment(), this.pollutionGrid);
        this.routingApplication = new RoutingApplication1(
            new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid, sensorEnvironment)), getEnvironment());
        simulation.setPollutionGrid(this.getPollutionGrid());
        simulation.setSensorEnvironment(sensorEnvironment);
        for(Mote mote: environment.getMotes())
        {

           if (mote instanceof UserMote && ((UserMote) mote).isActive())
            {
                MoteEffector moteEffector = new MoteEffector();
                moteEffector.changePath(mote,new KAStarRouter(new DistanceHeuristic()),environment);
            }

        }
    }

    public SensorEnvironment getEnvironmentAPI() {
        return this.sensorEnvironment;
    }



    public void setSensorEnvironment(SensorEnvironment env) {
        this.sensorEnvironment = env;
    }

    // endregion
}
