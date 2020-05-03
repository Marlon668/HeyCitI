package application.routing;

import application.routing.heuristic.RoutingHeuristic;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.util.List;

/**
 * Class used to analyse if a path is eventually better than another path
 * @author Marlon Saelens
 */
public class RouteAnalyser extends Analyser{

    private double threshold;

    /**
     * Sets the threshold used for evaluating paths
     * @param threshold: threshold used for evaluating paths
     */
    public void settreshold(double threshold)
    {
        this.threshold = threshold;
    }

    private RoutingHeuristic heuristic;

    public RouteAnalyser(RoutingHeuristic heuristic, double threshold) {
        super();
        this.heuristic = heuristic;
        settreshold(threshold);
    }



    public boolean isBetterPath(Pair<Double, List<GeoPosition>> route1, Pair<Double,List<GeoPosition>> route2) {
        if (!(route1 == null)) {
            Double accumulatedCost1 = calculateHeuristicPath(route1.getRight());
            return !(threshold * accumulatedCost1 > route2.getLeft());
        } else {
            return false;
        }
    }

    public Double calculateHeuristicPath(List<GeoPosition> route)
    {
        double newAccumulatedCost = 0;
        GeoPosition lastWaypoint = null;
        if(route.size()==0 || route.size()==1)
        {
            return 0.0;
        }
        for(GeoPosition i : route)
        {
            if(lastWaypoint == null)
            {
                lastWaypoint = i;
            }
            else{
                double accumulatedCostConnection = heuristic.calculateCostBetweenTwoNeighbours(lastWaypoint,i);
                newAccumulatedCost = newAccumulatedCost + accumulatedCostConnection;
                lastWaypoint = i;
            }
        }
        return newAccumulatedCost;
    }

    public boolean hasChangedEnough(Pair<Double,List<GeoPosition>> route1)
    {
        double newAccumulatedCost = 0;
        GeoPosition lastWaypoint = null;
        for(GeoPosition i : route1.getRight())
        {
            if(lastWaypoint == null)
            {
                lastWaypoint = i;
            }
            else{
                double accumulatedCostConnection = heuristic.calculateCostBetweenTwoNeighbours(lastWaypoint,i);
                newAccumulatedCost = newAccumulatedCost + accumulatedCostConnection;
                lastWaypoint = i;
            }
        }
        return threshold *newAccumulatedCost >= route1.getLeft();
    }
}
