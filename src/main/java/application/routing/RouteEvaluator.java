package application.routing;

import EnvironmentAPI.SensorEnvironment;
import application.pollution.PollutionGrid;
import application.routing.heuristic.RoutingHeuristic;
import application.routing.heuristic.SimplePollutionHeuristic;
import iot.Environment;
import iot.networkentity.Mote;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import util.Connection;
import util.Pair;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Class used to evaluate a path followed by a mote for a simulation
 */
public class RouteEvaluator extends Evaluator {

    private Map<Long,Double> totalAccumulatedCost;
    private Map<Long,Long> firstVisitedWaypointByMote;
    private Map<Long, Connection> lastUsedConnectionByMote;
    private RoutingHeuristic heuristic;
    private WeakReference<Environment> environment;
    private Map<Long, Pair<Integer,Connection>> amountConnectionUsed;
    private Map<Long,Double> lastMeasurementOfMote;
    private int counter;
    private SensorEnvironment sensorEnvironment;
    private HashMap<Mote,List<Double>> measures;
    private HashMap<Mote,List<Double>> lastMeasures;
    private HashMap<Mote,List<Double>> effectiveValueConnection;

    public RouteEvaluator(PollutionGrid pollutionGrid, SensorEnvironment pollutionEnvironment){
        super();
        this.firstVisitedWaypointByMote = new HashMap<Long,Long>();
        this.lastUsedConnectionByMote = new HashMap<Long,Connection>();
        this.totalAccumulatedCost = new HashMap<Long,Double>();
        this.heuristic = new SimplePollutionHeuristic(pollutionGrid,pollutionEnvironment);
        this.amountConnectionUsed = new HashMap<>();
        this.lastMeasurementOfMote = new HashMap<>();
        this.sensorEnvironment = pollutionEnvironment;
        this.measures = new HashMap<>();
        this.lastMeasures = new HashMap<>();
        this.effectiveValueConnection = new HashMap<>();
        counter = 0;
    }

    public HashMap<Mote,List<Double>> getEffectiveValueConnection(){
        return effectiveValueConnection;
    }

    public void initialise(Mote mote){
        if(mote.getPosInt().equals(mote.getOriginalPosInt())) {
            List<Double>costList = new ArrayList<>();
            double cost = (sensorEnvironment.getDataFromSensors(environment.get().getMapHelper().toGeoPosition(mote.getOriginalPosInt())));
            cost = cost * cost * cost;
            System.out.println(cost);
            costList.add(cost);
            lastMeasures.put(mote,costList);
            costList = new ArrayList<>();
            measures.put(mote,costList);
        }
    }
    public void addConnectionOfMote2(Mote mote, Pair<Integer,Integer> positionMote,boolean added){
        if(added){
                counter = 0;
                List<Double>costList = lastMeasures.get(mote);
                double cost = (sensorEnvironment.getDataFromSensors(environment.get().getMapHelper().toGeoPosition(positionMote)));
                cost = cost * cost * cost;
                System.out.println(cost);
                costList.add(cost);
                int numberOfMeasures = costList.size();
                double costConnection = costList.stream().mapToDouble(measure -> measure).sum();
                costConnection = costConnection/numberOfMeasures;
                measures.get(mote).add(costConnection);
                lastMeasures.put(mote,new ArrayList<>());
                counter += 1;
            }
        else {
            if (counter != 10000) {
                counter += 1;
            } else {
                double cost = sensorEnvironment.getDataFromSensors(environment.get().getMapHelper().toGeoPosition(positionMote));
                cost = cost*cost*cost;
                System.out.println(cost);
                lastMeasures.get(mote).add(cost);
                counter =0;
            }
        }
    }

    public void setEnvironment(WeakReference<Environment> environment){
        this.environment = environment;
    }

    /**
     *
     * @param mote
     * @param positionMoteInWaypoint
     */
    public void addCostConnectionOfMote(Mote mote, int positionMoteInWaypoint)
    {
        if(firstVisitedWaypointByMote.get(mote.getEUI())==null)
        {
            firstVisitedWaypointByMote.put(mote.getEUI(), Objects.requireNonNull(environment.get()).getGraph().getClosestWayPoint(mote.getPath().getWayPoints().get(0)));
            this.totalAccumulatedCost.put(mote.getEUI(),0.0);
            this.effectiveValueConnection.put(mote,new ArrayList<>());
        }
        if (lastUsedConnectionByMote.get(mote.getEUI()) == null) {
            Connection usedConnection = new Connection(firstVisitedWaypointByMote.get(mote.getEUI()), Objects.requireNonNull(environment.get()).getGraph().getClosestWayPoint(mote.getPath().getWayPoints().get(positionMoteInWaypoint)));
            amountConnectionUsed.put(mote.getEUI(),new Pair<>(1,usedConnection));
            lastUsedConnectionByMote.put(mote.getEUI(),usedConnection);
            this.lastMeasurementOfMote.put(mote.getEUI(),this.heuristic.calculateCostConnection(usedConnection, Objects.requireNonNull(environment.get()).getGraph()));
        }
        if(!(lastUsedConnectionByMote.get(mote.getEUI()).getTo() == Objects.requireNonNull(environment.get()).getGraph().getClosestWayPoint(mote.getPath().getWayPoints().get(positionMoteInWaypoint))))
        {
            List<Double>currentResults = effectiveValueConnection.get(mote);
            currentResults.add(lastMeasurementOfMote.get(mote.getEUI())/amountConnectionUsed.get(mote.getEUI()).getLeft());
            this.effectiveValueConnection.put(mote,currentResults);
            double accumulatedCost = this.totalAccumulatedCost.get(mote.getEUI()) + lastMeasurementOfMote.get(mote.getEUI())/amountConnectionUsed.get(mote.getEUI()).getLeft();
            this.totalAccumulatedCost.put(mote.getEUI(),accumulatedCost);
            Connection usedConnection = new Connection(lastUsedConnectionByMote.get(mote.getEUI()).getTo(), Objects.requireNonNull(environment.get()).getGraph().getClosestWayPoint(mote.getPath().getWayPoints().get(positionMoteInWaypoint)));
            amountConnectionUsed.put(mote.getEUI(),new Pair<>(1,usedConnection));
            lastUsedConnectionByMote.put(mote.getEUI(),usedConnection);
            this.lastMeasurementOfMote.put(mote.getEUI(),this.heuristic.calculateCostConnection(usedConnection, Objects.requireNonNull(environment.get()).getGraph()));
        }
        else{
            int amountUsed = amountConnectionUsed.get(mote.getEUI()).getLeft() + 1;
            amountConnectionUsed.put(mote.getEUI(),new Pair<>(amountUsed,amountConnectionUsed.get(mote.getEUI()).getRight()));
            lastMeasurementOfMote.put(mote.getEUI(),lastMeasurementOfMote.get(mote.getEUI())+this.heuristic.calculateCostConnection(lastUsedConnectionByMote.get(mote.getEUI()), Objects.requireNonNull(environment.get()).getGraph()));
        }
    }

    public Double getTotalCostPath(Mote mote, double distance){
        //measures.get(mote).forEach(measure->{
        //    System.out.println("Cost + " + measure);
        //});
        double cost = measures.get(mote).stream().mapToDouble(measure -> measure).sum();
        return cost*distance/100;
    }


    public Double getTotalCostPath(long moteId)
    {
        double accumulatedCost = this.totalAccumulatedCost.get(moteId) + lastMeasurementOfMote.get(moteId)/amountConnectionUsed.get(moteId).getLeft();
        this.totalAccumulatedCost.put(moteId,accumulatedCost);
        return totalAccumulatedCost.get(moteId) ;
    }

    public Double getCostConnection(Connection connection)
    {
        return heuristic.calculateCostConnection(connection, Objects.requireNonNull(environment.get()).getGraph());
    }

    public void reset(){
        this.firstVisitedWaypointByMote = new HashMap<Long,Long>();
        this.lastUsedConnectionByMote = new HashMap<Long,Connection>();
        this.totalAccumulatedCost = new HashMap<Long,Double>();
    }
}
