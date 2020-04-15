package application.routing;

import application.routing.heuristic.RoutingHeuristic;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.Pair;
import util.Path;

import java.util.List;

public interface PathFinder {
    /**
     * Retrieve a path from a given starting position to an end destination using a graph with available connections.
     * @param graph The graph containing all the connections.
     * @param begin The starting position.
     * @param end The destination position.
     * @return A list of positions containing the path to the destination
     * @throws RuntimeException When no path existed between the starting and ending position.
     */
    Pair<Double,List<GeoPosition>> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end);

    /**
     * Retrieve the K best path from a given starting position to an end destination using a graph with available connections.
     * @param graph The graph containing all the connections.
     * @param begin The starting position.
     * @param end The destination position.
     * @param amountBestPaths the number of best paths from a given starting position to an end destination using a graph with available connections
     * @return A list of positions containing the path to the destination
     * @throws RuntimeException When no path existed between the starting and ending position.
     */
    List<Pair<Double,List<GeoPosition>>> retrieveKPaths(GraphStructure graph, GeoPosition begin, GeoPosition end,Integer amountBestPaths);
    RoutingHeuristic getHeuristic();
}
