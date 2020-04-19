package iot.networkentity;

import application.routing.KAStarRouter;
import application.routing.heuristic.SimplePollutionHeuristic;
import datagenerator.SensorDataGenerator;
import iot.Environment;
import iot.GlobalClock;
import iot.SimulationRunner;
import iot.lora.BasicFrameHeader;
import iot.lora.LoraWanPacket;
import iot.lora.MacCommand;
import iot.lora.MessageType;
import iot.strategy.consume.AddPositionToPath;
import org.jxmapviewer.viewer.GeoPosition;
import selfadaptation.instrumentation.MoteEffector;
import util.Converter;
import util.MapHelper;
import util.Path;

import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class UserMote extends Mote {

    // Distance in km
    public static final double DISTANCE_THRESHOLD_ROUNDING_ERROR = 0.001;
    private boolean arrived;

    // the user mote can ask for a path only if this property is true
    private boolean isActive = false;
    private GeoPosition destination;
    private final LocalTime whenAskPath = LocalTime.of(0, 0, 15);
    private boolean alreadyRequested;
    // boolean that gives an indication that we use an adaptation algorithm to change the path
    private boolean adaptation;
    // HashMap containing all the adaptations of the path of a mote, calculated before running the simulation
    // used to visualise properly the routing of motes
    private HashMap<GeoPosition,List<List<GeoPosition>>> changesPath ;
    // Boolean that determines if we had changed the path used in the simulation without network option 2
    private boolean hasChanged;
    private Color color;

    public void setChangesPath(HashMap<GeoPosition,List<List<GeoPosition>>> changesPath){
        this.changesPath = changesPath;
    }


    public void setArrived(){
        arrived = true;
    }

    UserMote(long DevEUI, int xPos, int yPos, int transmissionPower, int SF,
             List<MoteSensor> moteSensors, int energyLevel, Path path, double movementSpeed,
             int startMovementOffset, int periodSendingPacket, int startSendingOffset, GeoPosition destination, Environment environment) {
        super(DevEUI, xPos, yPos, transmissionPower, SF, moteSensors, energyLevel, path, movementSpeed, startMovementOffset, periodSendingPacket, startSendingOffset, environment);
        this.destination = destination;
        this.adaptation = false;
        this.initialize();
        hasChanged = false;
        int R = (int) (Math.random( )*256);
        int G = (int)(Math.random( )*256);
        int B= (int)(Math.random( )*256);
        this.color = new Color(R, G, B);

    }

    @Override
    protected LoraWanPacket composePacket(Byte[] data, Map<MacCommand, Byte[]> macCommands) {
        if(isAdaptation()) {
            // change this when there is no adaptation => no communication

            GlobalClock clock = this.getEnvironment().getClock();

            if (isActive() && !alreadyRequested && whenAskPath.isBefore(clock.getTime())) {
                alreadyRequested = true;
                byte[] payload = new byte[17];
                payload[0] = MessageType.REQUEST_PATH.getCode();
                System.arraycopy(getGPSSensor().generateData(getPosInt(), clock.getTime()), 0, payload, 1, 8);
                System.arraycopy(Converter.toByteArray(destination), 0, payload, 9, 8);
                return new LoraWanPacket(getEUI(), getApplicationEUI(), payload,
                    new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>(macCommands.keySet()));
            }
            return LoraWanPacket.createEmptyPacket(getEUI(), getApplicationEUI());
        }
        else
        {
            return null;
        }
    }

    @Override
    public void consumePackets()
    {
        if(isAdaptation())
        {
            super.consumePackets();
        }
    }

    @Override
    public void setPos(double xPos, double yPos) {
        super.setPos(xPos, yPos);

        if (isAdaptation()) {
            Environment environment = this.getEnvironment();

            if (isActive()) {
                if (getPath().isEmpty()) {
                    throw new IllegalStateException("I don't have any path to follow...I can't move:(");
                }
                var path = getPath();
                var wayPoints = path.getWayPoints();
                //if I don't the path to the destination and I am at the penultimate position of the path

                if (path.getDestination().isPresent() &&    //at least the path has one point
                    MapHelper.distance(path.getDestination().get(), destination) > DISTANCE_THRESHOLD_ROUNDING_ERROR &&
                    wayPoints.size() > 1 &&
                    environment.getMapHelper().toMapCoordinate(wayPoints.get(wayPoints.size()-1)).equals(getPosInt())) {
                    //require new part of path
                    askNewPartOfPath();
                }
            }
        }
    }


    public void setAdaptation(){
        this.adaptation = true;
    }

    public boolean isAdaptation(){
        return this.adaptation;
    }

    public void resetAdaptation(){this.adaptation = false;
    }

    public void askNewPartOfPath() {
        if (getPath().getDestination().isEmpty()) {
            throw new IllegalStateException("You can't require new part of path without a previous one");
        }
        byte[] payload= new byte[9];
        payload[0] = MessageType.REQUEST_UPDATE_PATH.getCode();
        System.arraycopy(Converter.toByteArray(getPath().getDestination().get()), 0, payload, 1, 8);
        if(getEnvironment().getGateways().size()>0) {
            sendToGateWay(new LoraWanPacket(getEUI(), getApplicationEUI(), payload,
                new BasicFrameHeader().setFCnt(incrementFrameCounter()), new LinkedList<>()));
        }

        var clock = this.getEnvironment().getClock();
        var oldDestination = getPath().getDestination();
        clock.addTriggerOneShot(clock.getTime().plusSeconds(10), () -> {
            if (oldDestination.equals(getPath().getDestination())) {
                askNewPartOfPath();
            }
        });
    }

    private SensorDataGenerator getGPSSensor() {
        return getSensors().stream().filter(s -> s.equals(MoteSensor.GPS)).findFirst().orElseThrow().getSensorDataGenerator();
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Setting active a userMote means also set not active all this other userMote
     * @param active true to set active, false otherwise
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    public GeoPosition getDestination() {
        return this.destination;
    }

    public void setDestination(GeoPosition destination) {
        this.destination = destination;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && isActive();
    }

    @Override
    public boolean isArrivedToDestination() {
        return this.getPosInt().equals(this.getEnvironment().getMapHelper().toMapCoordinate(destination)) || arrived;
    }


    @Override
    protected void initialize() {
        super.initialize();
        this.arrived = false;
        if(isAdaptation()) {
            setPath(new Path(List.of(this.getEnvironment().getMapHelper().toGeoPosition(this.getPosInt())),
                this.getEnvironment().getGraph()));
            this.alreadyRequested = false;
            consumePacketStrategies.add(new AddPositionToPath());
        }
    }

    /**
     * Function to change the path of the mote if the simualtion option is without network option 2
     */
    public void changePath(){
        GeoPosition setPosition = null;
        for (GeoPosition positionToChange : changesPath.keySet()) {
            if (MapHelper.distance(getEnvironment().getMapHelper().toGeoPosition(getPosInt()), positionToChange) < 0.002) {
                setPath(changesPath.get(positionToChange).get(0));
                setPosition = positionToChange;
                this.hasChanged = true;
                break;
            }
        }
        if(setPosition != null) {
            if (changesPath.get(setPosition).size() == 1) {
                changesPath.remove(setPosition);
            } else {
                changesPath.get(setPosition).remove(0);
            }
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
