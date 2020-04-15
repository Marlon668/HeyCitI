package iot;

/**
 * Class to represent a result of the simulation for certain parameters
 * @author Marlon
 */
public class Result {

    private double airQuality;
    private int amountAdaptations;
    private double distance;

    public Result(double airQuality, int amountAdaptations, double distance){
        this.airQuality = airQuality;
        this.amountAdaptations = amountAdaptations;
        this.distance = distance;
    }

    public double getAirQuality(){
        return airQuality;
    }

    public int getAmountAdaptations(){
        return amountAdaptations;
    }

    public double getDistance(){
        return distance;
    }


}
