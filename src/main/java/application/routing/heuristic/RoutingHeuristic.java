package application.routing.heuristic;

import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;


/**
 * An interface used to specify routing heuristics.
 */
public interface RoutingHeuristic {


    /**
     * Calculates a the heuristic value of a path on the given entry.
     * @param entry The entry for the heuristic function on which the heuristic value is calculated.
     * @return A double according to the heuristic (the lower the better).
     */
    double calculateCost(HeuristicEntry entry);

    /**
     * Function to calculate the accumulated of a connection
     * @param connection connection to calculate the heuristic value of
     * @param graph graph for getting begin and end point of a connection
     * @return A double according to the heuristic (the lower the better) of the connection.
     */
    double calculateCostConnection(Connection connection, GraphStructure graph);

    /**
     * Function to calculate the accumulated cost for a connection given its begin - and
     * and end point
     * @param begin begin point of the connection
     * @param end end point of the connection
     * @return A double according to the heuristic (the lower the better) of the connection.
     */
    double calculateCostBetweenTwoNeighbours(GeoPosition begin, GeoPosition end);

    /**
     * Calculates a heuristic value based on the given entry for a given time
     * @param entry The entry for the heuristic function on which the heuristic value is calculated.
     * @param startTime the time upon which we calculate the heuristic value on the given entry
     * @return A double according to the heuristic (the lower the better).
     */
    double calculateCost(HeuristicEntry entry,long startTime);

    /**
     * Data class used to store data to calculate a heuristic value associated with that data.
     */
    class HeuristicEntry {
        public GraphStructure graph;
        public Connection connection;
        public GeoPosition destination;

        public HeuristicEntry(GraphStructure graph, Connection connection, GeoPosition destination) {
            this.graph = graph;
            this.connection = connection;
            this.destination = destination;
        }
    }
}
