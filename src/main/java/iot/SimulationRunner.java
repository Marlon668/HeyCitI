package iot;

import EnvironmentAPI.PollutionEnvironment;
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
import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

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
    private NetworkServer networkServer;
    private MutableInteger updateFrequency;
    private SimulationUpdateListener listener;
    private HashMap<Mote, Pair<Long, Long>> timeResults;
    private int amountRuns;
    private int amountUsers;
    private boolean multipleRun;

    private SensorEnvironment sensorEnvironment;

    public boolean getMultipleRun(){
        return  multipleRun;
    }

    //private RoutingApplication routingApplication2;

    private Boolean bestpathAvailable;


    public void activateBestpath()
    {
        bestpathAvailable = true;
    }

    public void deactivateBestpath()
    {
        bestpathAvailable = false;
    }


    public Parameters getParameters(){
        return parameters;
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
        bestpathAvailable = false;

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
        bestpathAvailable = false;
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
        //System.out.println(selectedAlgorithm.getName());

        if(!(selectedAlgorithm.getName()=="No Adaptation" || selectedAlgorithm.getName()=="Best Path"))
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
            //if(this.bestpathAvailable) {
                for (Mote mote : environment.getMotes()) {

                    //if (mote instanceof UserMote && ((UserMote) mote).isActive())
                    //{
                    //    MoteEffector moteEffector = new MoteEffector();
                    //    moteEffector.changePath(mote,new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid)),environment);
                    //}

                    if (mote instanceof UserMote && ((UserMote) mote).isActive()) {
                        MoteEffector moteEffector = new MoteEffector();
                        //BestPath bestpath = new BestPath(new SimplePollutionHeuristic(pollutionGrid,pollutionEnvironment),pollutionEnvironment);
                        //bestpath.setInformation(simulation.getInformation());
                        //moteEffector.changePath(mote,bestpath,environment);
                        //moteEffector.bestPath(mote, bestpath, environment);
                        //System.out.println(mote.get);
                    }

                }
            //}
            //else{
            //    String message = "No information about evolution of values of connections available in xml-file\n" +
            //        "To make this option available, do the following things\n" +
            //        "1. Gather information about values of connections by clicking \"Get Information\" in the simulation menu \n" +
            //        "2. Save the xml-file\n" +
            //        "3. Load the new xml-file\n" +
            //        "4. Select best-path algorithm in the simulation menu"
            //        ;
            //    JOptionPane.showMessageDialog(null, message, "Lack of information", JOptionPane.ERROR_MESSAGE);
            //}
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
        //if(getParameters().getSetupFirst()==1){
        //    simulation.initialiseMote();
        //}
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

    public void multipleRun(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        this.amountRuns = getParameters().getAmountRuns();
        this.multipleRun = true;
        this.updateFrequency = updateFrequency;
        this.listener = listener;
        HashMap<Mote,Pair<Double,Integer>> result = new HashMap<>();
        multipleRun(this.timeResults,result);


    }

    public void multipleRun(HashMap<Mote, Pair<Long,Long>> time,HashMap<Mote,Pair<Double,Integer>> result){
        System.out.println("i =  " + amountRuns);
        if(amountRuns != 0) {
                //this.setupSimulationRunner();;
                setupMultipleRun(true);
                simulate(updateFrequency, listener);
                amountRuns = amountRuns -1;
                System.out.println("i =  " + amountRuns);
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

        this.setupSimulationRunner();
    }

    /**
     * Set the simulator up for a single timed run.
     */
    public void setupTimedRun() {
        simulation.setupTimedRun();

        this.setupSimulationRunner();
    }

    /**
     * Set the simulator for multiple timed runs
     */
    //public void setupMultipleRuns(){
    //    i = 0;
    //    while(i== this.in)
//
//    }

    /**
     * Setup of applications/servers/clients before each run.
     */
    public void setupSimulationRunner() {
        // - Remove previous pollution measurements
        getEnvironmentAPI().getPoll().clear();
        timeResults = new HashMap<Mote, Pair<Long, Long>>();
        pollutionGrid.clean();
        routingApplication.clean();

        // Reset received transmissions in the networkServer
        this.networkServer.reset();
        if(simulation.getApproach() == null || simulation.getApproach().getName() == "No Adaptation")
        {
            System.out.println("ok");
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
        simulation.setInformation();

        ConfigurationReader.loadConfiguration(file, this);
        simulation.setEnvironment(new WeakReference<>(this.getEnvironment()));

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
        this.listener = null;
        this.updateFrequency = null;

    }

    /**
     * Initialize all applications used in the simulation.
     */
    public void setupApplications() {
        this.pollutionMonitor = new PollutionMonitor(this.getEnvironment(), this.pollutionGrid);
        this.routingApplication = new RoutingApplication(
           new KAStarRouter(new SimplePollutionHeuristic(pollutionGrid,sensorEnvironment)), getEnvironment());
        simulation.setRoutingApplication(routingApplication);
        simulation.setPollutionGrid(this.getPollutionGrid());
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
