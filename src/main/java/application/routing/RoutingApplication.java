package application.routing;

import EnvironmentAPI.SensorEnvironment;
import application.Application;
import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.mqtt.Topics;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that determines adaptations for a path for every usermote used in the loaded configuration
 * @author Marlon
 */
public abstract class RoutingApplication extends Application implements Cloneable {

    protected HashMap<Mote,List<Pair<Double,Double>>> visualiseRun;
    protected HashMap<Mote,List<Double>> adaptationPoints;
    protected HashMap<Mote,Double>airQualityRun;
    protected HashMap<Mote,Double> distances;
    protected HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoute;
    /**
     Sets the buffersizeWidth used by this application
     How much paths could we save in each step in the adaptation algorithm
     The amount of best paths that we must search for by using the K-A* algorithm
     **/
    protected int bufferSizeHeight;

    /**
     buffer size width used by the adaptation algorithm
     * How much paths could we save in each step in the adaptation algorithm
     **/
    protected int bufferSizeWidth;

    /**
     * Gives a hashmap containing the values of alternative routes used in the visualise run graph
     * @return a hashmap containing the values of alternative routes used in the visualise run graph
     */
    public HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> getAlternativeRoute(){
        return alternativeRoute;
    }

    /**
     * Gives a hashmap containing a list of values of the waypoints of the path of a mote, whereby the value is based on the heuristic
     * used in the adaptation algorithm. This hashmap is used in the visualise run graph
     * @return a hashmap containing a list of values of the waypoints of the path of a mote, whereby the value is based on the heuristic
     * used in the adaptation algorithm
     */
    public HashMap<Mote,List<Pair<Double,Double>>> getVisualiseRun(){
        return visualiseRun;
    }

    /**
     * Gives a hashmap containing a list of distances where the geopositions upon which there is happened an adaptation on the path
     * This hashmap is used in the viasulise run graph to visualise the adaptation points
     * @return hashmap containing a list of distances where the geopositions upon which there is happened an adaptation on the path
     */
    public HashMap<Mote,List<Double>> getAdaptationPoints(){
        return adaptationPoints;
    }

    /**
     * Gives average time needed to make all decisions for every mote
     * @return hashmap containing average decision time for every mote
     */
    public abstract Map<Long, Pair<Integer, Long>> getAverageTimeForDecisionPerMote();

    /**
     * Gives the amount of adaptations made for every mote
     * @return hashmap containing amount of adaptations for every mote
     */
    public abstract Map<Long, Integer> getAmountAdaptations();

    /**
     * Sets the buffersizeHeight used by this application
     * After how much steps we must decide whether or not changing the path
     *
     * @param bufferSizeHeight: buffersizeHeight used by this application
     */
    public abstract void setBufferSizeHeight(int bufferSizeHeight);

    /**
     * Sets the buffersizeWidth used by this application
     * How much paths could we save in each step in the adaptation algorithm
     * The amount of best paths that we must search for by using the K-A* algorithm
     *
     * @param bufferSizeWidth: buffersizeWidth used by this application
     */
    public abstract void setBufferSizeWidth(int bufferSizeWidth);

    /**
     * Returns maximum time needed to make a decision to make whether or not to change the path
     * @return Hashmap where the maximum time to mak
     */
    public abstract Map<Long, Long> getMaxTime();

    public RoutingApplication(PathFinder pathFinder, Environment environment) {
        super(List.of(Topics.getNetServerToApp("+", "+")));
    }

    /**
     * Handles a route request without sending packets
     * It gives directly the next part of the route to the mote
     *
     * @param mote Usermote where we would send a next part of the route to it
     * @param environment the environment that we use for the simulation
     */
    public abstract void handleRouteRequestWithoutNetwork(UserMote mote, Environment environment, SensorEnvironment sensorEnvironment);

    /**
     * Calculate all the routingApplications of every mote in the environment before simulating
     * It saves a hashmap where the keys are all the geopositions where there happens an adaptation of the path
     * and the values is the changed path
     *
     * @param environment the environment where we would do the simulation
     * @param sensorEnvironment the sensorenvironment that we would use for the simulation
     */
    public abstract void calculateRoutingAdaptations(Environment environment, SensorEnvironment sensorEnvironment);

    /**
     * Clean the cached routes and mote positions.
     */
    public abstract void clean(int delete);

    public abstract void handleRouteRequestWithoutNetworkForRun(UserMote mote, Environment environment, SensorEnvironment sensorEnvironment);

    /**
     * Handles a route request without sending packets
     * It gives directly the next part of the route to the mote
     *
     * @param mote Usermote where we would send a next part of the route to it
     * @param environment the environment that we use for the simulation
     */
    protected abstract List<GeoPosition> handleRouteRequestWithoutNetwork2(UserMote mote, Environment environment, SensorEnvironment sensorEnvironment);

    /**
     * Handle a route request message by replying with (part of) the route to the requesting device.
     * @param message The message which contains the route request.
     */
    protected abstract  void handleRouteRequest(LoraWanPacket message);

    /**
     * Get a stored route for a specific mote.
     * @param mote The mote from which the cached route is requested.
     * @return The route as a list of geo coordinates.
     */
    public abstract List<GeoPosition> getRoute(Mote mote);

    /**
     * Sets begintime back on 40000 ms
     */
    public abstract void reset();
}
