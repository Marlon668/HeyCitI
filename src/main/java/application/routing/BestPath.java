package application.routing;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.SensorEnvironment;
import application.routing.heuristic.RoutingHeuristic;
import iot.Environment;
import iot.networkentity.UserMote;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.MapHelper;
import util.Pair;

import java.util.*;
import java.util.stream.Collectors;


/**
 * An class which implements the A* routing algorithm, assuming the used heuristic is consistent.
 */
public class BestPath{

    // The maximum amount of distance the closest waypoint should be to a given GeoPosition (in km)
    @SuppressWarnings("FieldCanBeLocal")
    private final double DISTANCE_THRESHOLD_POSITIONS = 0.05;
    private SensorEnvironment pollutionEnvironment;


    private Map<Long,List<Double>> information;

    public void setInformation(Map<Long,List<Double>> information)
    {
        this.information = information;
    }

    public BestPath(RoutingHeuristic heuristic, SensorEnvironment pollutionEnvironment) {
        this.pollutionEnvironment = pollutionEnvironment;

    }






    public Pair<Double,List<GeoPosition>>  retrievePath(Environment environment, UserMote mote) {
        GraphStructure graph = environment.getGraph();
        GeoPosition begin = environment.getMapHelper().toGeoPosition(mote.getOriginalPosInt());
        GeoPosition end = mote.getDestination();
        double veloctiy = mote.getMovementSpeed();
        long beginWaypointId = graph.getClosestWayPointWithinRange(begin, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The mote position retrieved from the message is not located at a waypoint."));
        long endWaypointId = graph.getClosestWayPointWithinRange(end, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The destination position retrieved from the message is not located at a waypoint."));
        PriorityQueue<FringeEntry> fringe = new PriorityQueue<>();
        // Initialize the fringe by adding the first outgoing connections
        graph.getConnections().entrySet().stream()
            .filter(entry -> entry.getValue().getFrom() == beginWaypointId)
            .forEach(entry -> {
                Pair<Double, Long> value = calculateCostConnection(graph,31820,veloctiy,entry.getKey());
                fringe.add(new FringeEntry(
                    List.of(entry.getKey()),value.getRight(),value.getLeft()));
            });


        // Actual A* algorithm
        while (!fringe.isEmpty()) {
            FringeEntry current = fringe.poll();
            long lastWaypointId = graph.getConnection(current.getLastConnectionId()).getTo();
            // Are we at the destination?
            if (lastWaypointId == endWaypointId) {
                return new Pair(0,this.getPath(current.connections, graph));
            }


            // Explore the different outgoing connections from the last connection in the list
            // -> Add the new possible paths (together with their new heuristic values) to the fringe
            graph.getOutgoingConnectionsById(lastWaypointId).stream()
                .filter(connId ->!containsWaypoints(current.connections, connId, graph))
                .forEach(connId -> {
                    List<Long> extendedPath = new ArrayList<>(current.connections);
                    extendedPath.add(connId);
                    Pair<Double, Long> value = calculateCostConnection(graph,current.time,veloctiy,connId);
                    double newHeuristicValue = current.heuristicValue + value.getLeft();
                    fringe.add(new FringeEntry(extendedPath,value.getRight(), newHeuristicValue));
                });
        }

        throw new RuntimeException(String.format("Could not find a path from {%s} to {%s}", begin.toString(), end.toString()));
    }

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
     * Convert a list of connection Ids to a list of the respective GeoPositions of those connections.
     * @param connectionIds The list of connections Ids which are used for the conversion.
     * @param graph The graph which contains all the connections and waypoints.
     * @return A list of GeoPositions which correspond to the connections in {@code connectionIds}.
     */
    private List<GeoPosition> getPath(List<Long> connectionIds, GraphStructure graph) {
        List<GeoPosition> points = connectionIds.stream()
            .map(o -> graph.getConnections().get(o).getFrom())
            .map(graph::getWayPoint)
            .collect(Collectors.toList());

        // Don't forget the final waypoint
        long lastWaypointId = graph.getConnections().get(connectionIds.get(connectionIds.size()-1)).getTo();
        points.add(graph.getWayPoint(lastWaypointId));

        return points;
    }

    private Pair<Double, Long> calculateCostConnection(GraphStructure graph, long time, double velocity, Long connectionId)
    {
        Connection connection = graph.getConnection(connectionId);
        GeoPosition begin = graph.getWayPoint(connection.getFrom());
        GeoPosition end = graph.getWayPoint(connection.getTo());
        double distance = MapHelper.distance(begin,end)*1000;
        int period = 0;
        long endTime = (long)(distance/velocity)*1000 + time;
        double airValue = pollutionEnvironment.getDataBetweenPointsFromTime(begin,end, time,endTime,velocity,0.1);
        return new Pair<Double, Long>(airValue*distance/1000,endTime);
    }


    /**
     * Class used in the priority queue, providing an order for the Best Path algorithm based on the
     * accumulated heuristic values for the evaluated path.
     */
    private static class FringeEntry implements Comparable<FringeEntry> {
        List<Long> connections;
        long time;
        double heuristicValue;

        FringeEntry(List<Long> connections,long time, double heuristicValue) {
            this.connections = connections;
            this.time = time;
            this.heuristicValue = heuristicValue;
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
