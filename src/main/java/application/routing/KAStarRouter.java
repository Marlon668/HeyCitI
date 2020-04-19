package application.routing;

import application.routing.heuristic.RoutingHeuristic;
import application.routing.heuristic.RoutingHeuristic.HeuristicEntry;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import util.GraphStructure;
import util.MapHelper;
import util.Pair;

import java.awt.*;
import java.io.Console;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


/**
 * An class which implements the K-A* routing algorithm, assuming the used heuristic is consistent.
 */
public class KAStarRouter implements PathFinder {

    // The maximum amount of distance the closest waypoint should be to a given GeoPosition (in km)
    @SuppressWarnings("FieldCanBeLocal")
    private final double DISTANCE_THRESHOLD_POSITIONS = 0.05;

    // The heuristic used in the K-A* algorithm
    private RoutingHeuristic heuristic;

    public RoutingHeuristic getHeuristic ()
    {
        return this.heuristic;
    }


    public KAStarRouter(RoutingHeuristic heuristic) {
        this.heuristic = heuristic;
    }


    @Override
    public Pair<Double,List<GeoPosition>> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end){
        return retrieveKPaths(graph,begin,end,1).get(0);
    }

    @Override
    public List<Pair<Double,List<GeoPosition>>> retrieveKPaths(GraphStructure graph, GeoPosition begin, GeoPosition end, Integer amountBestPaths) {
        long beginWaypointId = graph.getClosestWayPointWithinRange(begin, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The mote position retrieved from the message is not located at a waypoint."));
        long endWaypointId = graph.getClosestWayPointWithinRange(end, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The destination position retrieved from the message is not located at a waypoint."));

        List<Pair<Double, List<GeoPosition>>> KBestPaths = new ArrayList<>();

        Set<Long> visitedConnections = new HashSet<>();

        HashMap<Long, Pair<Integer,HashSet<Long>>> amountConnectionUsed = new HashMap<>();

        Integer totalPaths = amountBestPaths;


        PriorityQueue<FringeEntry> fringe = new PriorityQueue<>();
        // Initialize the fringe by adding the first outgoing connections
        graph.getConnections().entrySet().stream()
            .filter(entry -> entry.getValue().getFrom() == beginWaypointId || entry.getValue().getTo() == beginWaypointId)
            .forEach(entry -> {
                double accumulatedCost = this.heuristic.calculateCost(new HeuristicEntry(graph, entry.getValue(), end));
                fringe.add(new FringeEntry(
                    List.of(entry.getKey()),
                    accumulatedCost,
                    accumulatedCost
                ));
                visitedConnections.add(entry.getKey());
                if (!amountConnectionUsed.containsKey(beginWaypointId)) {
                    amountConnectionUsed.put(beginWaypointId, new Pair(1,null));
                } else {
                    Integer amountConnections = amountConnectionUsed.get(beginWaypointId).getLeft() + 1;
                    amountConnectionUsed.put(beginWaypointId, new Pair(amountConnections,null));
                }
            });


        // Actual A* algorithm
        try {
            while (!fringe.isEmpty()) {
                FringeEntry current = fringe.poll();
                long lastWaypointId = graph.getConnection(current.getLastConnectionId()).getTo();

                // Are we at the destination?
                if (lastWaypointId == endWaypointId) {

                    Pair<Double, List<GeoPosition>> IBestPath = new Pair(current.heuristicValue, this.getPath(current.connections, graph, end));
                    KBestPaths.add(IBestPath);
                    amountBestPaths -= 1;
                    if (amountBestPaths == 0){
                        return KBestPaths;
                    }
                else{
                        DeleteConnectionsInGraph(current.connections, visitedConnections, amountConnectionUsed, graph);
                    }
                }

                if(!(lastWaypointId == endWaypointId))
                {
                    // Explore the different outgoing connections from the last connection in the list
                    // -> Add the new possible paths (together with their new heuristic values) to the fringe
                    Integer finalAmountBestPaths = amountBestPaths;
                    graph.getOutgoingConnectionsById(lastWaypointId).stream()
                        .filter(connId ->  !visitedConnections.contains(connId) || (amountConnectionUsed.get(graph.getConnection(connId).getFrom()) != null && amountConnectionUsed.get(graph.getConnection(connId).getFrom()).getRight() != null && amountConnectionUsed.get(graph.getConnection(connId).getFrom()).getRight().contains(graph.getConnection(connId).getTo())))// Filter out connections which we have already considered (since these were visited in a better path first)
                        .filter(connId ->   (finalAmountBestPaths == totalPaths) || !containsWaypoints(current.connections, connId, graph))
                        .forEach(connId -> {
                            List<Long> extendedPath = new ArrayList<>(current.connections);
                            if((amountConnectionUsed.get(graph.getConnection(connId).getFrom()) != null && amountConnectionUsed.get(graph.getConnection(connId).getFrom()).getRight() != null))
                            {
                                amountConnectionUsed.get(graph.getConnection(connId).getFrom()).getRight().remove(graph.getConnection(connId).getTo());
                            }
                            extendedPath.add(connId);

                            double accumulatedCost = current.accumulatedCost + this.heuristic.calculateCost(new HeuristicEntry(graph, graph.getConnection(connId), end));

                            fringe.add(new FringeEntry(extendedPath, accumulatedCost, accumulatedCost));

                            visitedConnections.add(connId);
                            if (amountConnectionUsed.get(lastWaypointId) == null) {
                                amountConnectionUsed.put(lastWaypointId, new Pair(1, null));
                            } else {
                                Integer amountConnections = amountConnectionUsed.get(lastWaypointId).getLeft() + 1;
                                amountConnectionUsed.put(lastWaypointId, new Pair(amountConnections,null));
                            }

                        });
                }
            }

            throw new RuntimeException(String.format("Could not find a path from {%s} to {%s}", begin.toString(), end.toString()));

        }
        catch(RuntimeException e)
        {

            return KBestPaths;
        }
    }

    /**
     * Function to decide path already contains the reversed connection
     * @param connections list of connections
     * @param connectionId connectionId to be checked if path doesn't contain this connection already
     * @param graph graph used to translate connectionId to connection
     * @return true if path already contains the connection, false otherwise
     */
    private boolean containsWaypoints(List<Long> connections,Long connectionId,GraphStructure graph)
    {
        for(Long conn: connections)
        {
            if(graph.getConnection(conn).getFrom() == graph.getConnection(connectionId).getTo())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Function for resetting the visitedConnections for searching the next best path
     * We decrease the amount of paths that used that waypoint in the hashmap amountconnections used,
     * till we remove a waypoint (amount of paths that uses this waypoint = 0). Then we set markers to
     * the waypoints located above in the path tree
     * @param connections deleted path
     * @param visitedConnections set of already visited connections that must be updated to allow multiple
     *                           best paths
     * @param amountConnectionsUsed hashmap used to update the visited Connections
     * @param graph graph that we are working in
     */
    private void DeleteConnectionsInGraph(List<Long> connections, Set<Long> visitedConnections,HashMap<Long,Pair<Integer,HashSet<Long>>> amountConnectionsUsed,GraphStructure graph) {
        long waypointFree = 0;
        for (int i = connections.size()-1;i>=0;i--){
                if(amountConnectionsUsed.get(graph.getConnection(connections.get(i)).getFrom()).getLeft() >1)
                {
                    long waypointId = graph.getConnection(connections.get(i)).getFrom();
                    Integer amountConnections = amountConnectionsUsed.get(waypointId).getLeft() - 1;
                    if(amountConnectionsUsed.get(waypointId).getRight()==null)
                    {
                        HashSet<Long>wayPointFree = new HashSet<>();
                        wayPointFree.add(waypointFree);
                        amountConnectionsUsed.put(waypointId,new Pair(amountConnections,wayPointFree));
                    }
                    else{
                        amountConnectionsUsed.put(waypointId,new Pair(amountConnections,waypointFree));
                    }
                    setFreeWaypointParent(graph,amountConnectionsUsed,connections,i);
                    i = 0;

                }
                else {
                        visitedConnections.remove(connections.get(i));
                        amountConnectionsUsed.remove(graph.getConnection(connections.get(i)).getFrom());
                        waypointFree = graph.getConnection(connections.get(i)).getFrom();
                }
            }
    }

    /**
     * Function for marking connections above the search tree that there is be one path re-availbale to the goal
     * Used in the deletion process
     * @param graph graph that we are working at
     * @param amountConnectionsUsed in this hashmap we must set for the waypoints above the deleted waypoint
     *                              a marker that there is a path till the deleted waypoint is re-available
     * @param connections the deletd path
     * @param i : from which waypoints may we start setting markers, starting from the end of the list
     */
    private void setFreeWaypointParent(GraphStructure graph, HashMap<Long, Pair<Integer, HashSet<Long>>> amountConnectionsUsed, List<Long> connections, int i) {
        for (int j = i;j>=0;j--){
            long waypointId = graph.getConnection(connections.get(j)).getFrom();
            if(amountConnectionsUsed.get(waypointId).getRight()==null)
            {
                HashSet<Long>wayPointFree = new HashSet<>();
                wayPointFree.add(graph.getConnection(connections.get(j)).getTo());
                amountConnectionsUsed.put(waypointId,new Pair(amountConnectionsUsed.get(waypointId).getLeft(),wayPointFree));
            }
            else{
                amountConnectionsUsed.get(waypointId).getRight().add(graph.getConnection(connections.get(j)).getTo());
            }
        }
    }

    /**
     * Convert a list of connection Ids to a list of the respective GeoPositions of those connections.
     * @param connectionIds The list of connections Ids which are used for the conversion.
     * @param graph The graph which contains all the connections and waypoints.
     * @return A list of GeoPositions which correspond to the connections in {@code connectionIds}.
     */
    private List<GeoPosition> getPath(List<Long> connectionIds, GraphStructure graph,GeoPosition end) {
        List<GeoPosition> points = connectionIds.stream()
            .map(o -> graph.getConnections().get(o).getFrom())
            .map(graph::getWayPoint)
            .collect(Collectors.toList());

        // Don't forget the final waypoint
        long lastWaypointId = graph.getConnections().get(connectionIds.get(connectionIds.size()-1)).getTo();
        points.add(graph.getWayPoint(lastWaypointId));


        return points;
    }


    /**
     * Class used in the priority queue, providing an order for the A* algorithm based on the
     * accumulated heuristic values for the evaluated path.
     */
    private static class FringeEntry implements Comparable<FringeEntry> {
        List<Long> connections;
        double heuristicValue;
        double accumulatedCost;

        FringeEntry(List<Long> connections, double heuristicValue,double accumulatedCost) {
            this.connections = connections;
            this.heuristicValue = accumulatedCost;
            this.accumulatedCost = accumulatedCost;
        }

        long getLastConnectionId() {
            return connections.get(connections.size() - 1);
        }

        @Override
        public int compareTo(@NotNull FringeEntry fringeEntry) {
            return Double.compare(this.heuristicValue, fringeEntry.heuristicValue);
        }
    }
}
