package application.routing.heuristic;

import EnvironmentAPI.PollutionEnvironment;
import EnvironmentAPI.SensorEnvironment;
import application.pollution.PollutionGrid;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.MapHelper;

import java.util.Map;


/**
 * A simple routing heuristic which also takes the pollution over a given connection into account.
 */
public class SimplePollutionHeuristic implements RoutingHeuristic {
    private final PollutionGrid pollutionGrid;
    private final SensorEnvironment env;

    public SimplePollutionHeuristic(PollutionGrid pollutionGrid, SensorEnvironment env) {
        this.pollutionGrid = pollutionGrid;
        this.env = env;
    }

    @Override
    public double calculateCost(HeuristicEntry entry) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

        double pollutionValue = this.env.getDataBetweenPoints(begin,end, 0.1);

        // The lower the pollution level, the better the heuristic
        return pollutionValue*pollutionValue*pollutionValue*MapHelper.distance(begin,end)*10;
    }

    @Override
    public double calculateCost(HeuristicEntry entry,long startTime) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

        double pollutionValue = this.env.getDataBetweenPointsFromTime(begin,end,startTime,0.1);

        // The lower the pollution level, the better the heuristic
        return pollutionValue*pollutionValue*pollutionValue*MapHelper.distance(begin,end)*10;
    }

    @Override
    public double calculateCostConnection(Connection connection, GraphStructure graph)
    {
        GeoPosition begin = graph.getWayPoint(connection.getFrom());
        GeoPosition end = graph.getWayPoint(connection.getTo());

        double pollutionValue = this.env.getDataBetweenPoints(begin,end, 0.1);

        // The lower the pollution level, the better the heuristic
        return pollutionValue*pollutionValue*pollutionValue*MapHelper.distance(begin,end)*10;
    }

    @Override
    public double calculateCostBetweenTwoNeighbours(GeoPosition begin, GeoPosition end)
    {
        double pollutionValue = this.env.getDataBetweenPoints(begin,end, 0.1);

        // The lower the pollution level, the better the heuristic
        return pollutionValue*pollutionValue*pollutionValue*MapHelper.distance(begin,end)*10;

    }
}
