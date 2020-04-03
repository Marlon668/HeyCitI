package application.routing;

import EnvironmentAPI.SensorEnvironment;
import application.Application;
import iot.Environment;
import iot.mqtt.Topics;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.List;
import java.util.Map;

public abstract class RoutingApplication extends Application implements Cloneable {

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
     * returns het buffersizeHeight used by the adaptation algorithm
     * @return bufferSizeHeight used in the adaptation algorithm
     */
    public abstract int getBufferSizeHeight();

    /**
     * returns het buffersizeWidth used by the adaptation algorithm
     * @return bufferSizeWidth used in the adaptation algorithm
     */
    public abstract int getBufferSizeWidth();

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
    public abstract void clean();

    /**
     * Get a stored route for a specific mote.
     * @param mote The mote from which the cached route is requested.
     * @return The route as a list of geo coordinates.
     */
    public abstract List<GeoPosition> getRoute(Mote mote);

    /**
     * Sets begintime back on 31380 ms
     */
    public abstract void reset();
}
