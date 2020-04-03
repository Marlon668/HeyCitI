package gui.util;

import EnvironmentAPI.SensorEnvironment;
import application.pollution.PollutionGrid;

import application.routing.RoutingApplication;
import application.routing.RoutingApplication1;
import gui.mapviewer.*;
import iot.Environment;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import util.GraphStructure;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompoundPainterBuilder {
    private List<Painter<JXMapViewer>> painters = new ArrayList<>();

    /**
     * Include painters for motes in the builder.
     * @param environment The environment in which the motes are stored.
     * @return The current object.
     */
    public CompoundPainterBuilder withMotes(Environment environment) {
        Map<MoteWayPoint, Integer> motes = GUIUtil.getMoteMap(environment);
        painters.add(new MotePainter<>().setWaypoints(motes.keySet()));
        painters.add(new NumberPainter<>(NumberPainter.Type.MOTE).setWaypoints(motes));
        return this;
    }

    /**
     * Include painters for gateways in the builder.
     * @param environment The environment in which the gateways are stored.
     * @return The current object.
     */
    public CompoundPainterBuilder withGateways(Environment environment) {
        Map<Waypoint, Integer> gateways = GUIUtil.getGatewayMap(environment);

        painters.add(new GatewayPainter<>().setWaypoints(gateways.keySet()));
        painters.add(new NumberPainter<>(NumberPainter.Type.GATEWAY).setWaypoints(gateways));
        return this;
    }

    /**
     * Include painters for all waypoints in the builder.
     * @param graph The graph which stores all the waypoints.
     * @param includeNumbers Boolean indicating if the Ids of the waypoints should also be painted.
     * @return The current object.
     */
    public CompoundPainterBuilder withWaypoints(GraphStructure graph, boolean includeNumbers) {
        var waypoints = graph.getWayPoints();

        painters.add(new WayPointPainter<>().setWaypoints(waypoints.values().stream()
            .map(DefaultWaypoint::new)
            .collect(Collectors.toSet()))
        );

        if (includeNumbers) {
            painters.add(new NumberPainter<>(NumberPainter.Type.WAYPOINT)
                .setWaypoints(waypoints.entrySet().stream()
                    .collect(Collectors.toMap(e -> new DefaultWaypoint(e.getValue()), e -> e.getKey().intValue())))
            );
        }
        return this;
    }

    /**
     * Include painters for all connections in the builder.
     * @param graph The graph which contains all the connections.
     * @return The current object.
     */
    public CompoundPainterBuilder withConnections(GraphStructure graph) {
        Color lineColor = GUISettings.CONNECTION_LINE_COLOR;
        int lineSize = GUISettings.CONNECTION_LINE_SIZE;

        graph.getConnections().values().forEach(c -> painters.add(new LinePainter(List.of(graph.getWayPoint(c.getFrom()), graph.getWayPoint(c.getTo())), lineColor, lineSize)));
        return this;
    }

    /**
     * Include painters for the borders of the environment in the builder.
     * @param environment The environment which has a bounded x and y value.
     * @return The current object.
     */
    public CompoundPainterBuilder withBorders(Environment environment) {
        painters.addAll(GUIUtil.getBorderPainters(environment));
        return this;
    }

    /**
     * Include painters for the paths of the motes in the builder.
     * @param environment The environment which contains all the motes.
     * @return The current object.
     */
    public CompoundPainterBuilder withMotePaths(Environment environment, GenericFeedbackLoop approach) {
        Color lineColor = GUISettings.MOTE_PATH_LINE_COLOR;
        int lineSize = GUISettings.MOTE_PATH_LINE_SIZE;


        for(Mote m : environment.getMotes())
            if(m instanceof  UserMote)
            {
                if(!(approach == null))
                {
                    if(approach.getName()=="Best Path")
                    {
                        System.out.println(approach.getName());
                        painters.add(new LinePainter(m.getPath().getWayPoints(), Color.MAGENTA, lineSize));
                    }
                    else{
                        painters.add(new LinePainter(m.getPath().getWayPoints(),((UserMote) m).getColor(), lineSize));
                    }
                }
                else{
                    //System.out.println("niet ok");
                    painters.add(new LinePainter(m.getPath().getWayPoints(), ((UserMote) m).getColor(), lineSize));
                }
            }
            else
            {
              painters.add(new LinePainter(m.getPath().getWayPoints(),Color.green,lineSize));
            }
        return this;
    }

    /**
     * Include a painter of a pollution grid in the builder.
     * @param environment The environment to which the pollution grid belongs.
     * @param pollutionGrid The pollution grid which should be painted.
     * @return The current object.
     */
    public CompoundPainterBuilder withPollutionGrid(Environment environment, PollutionGrid pollutionGrid) {
        painters.add(new PollutionGridPainter(environment, pollutionGrid));
        return this;
    }

    /**
     * Include a painter of a pollution grid in the builder.
     * @param environment The environment to which the pollution grid belongs.
     * @param pollution   The pollution environment which should be painted.
     * @return The current object.
     */
    public CompoundPainterBuilder withEnvironmentAPI(Environment environment, SensorEnvironment pollution) {
        painters.add(new PollutionEnvironmentPainter(environment, pollution));
        return this;
    }

    public CompoundPainterBuilder withEnvironmentAPISensor(Environment environment, SensorEnvironment pollution) {
        painters.add(new SensorEnvironmentPainter(environment, pollution));
        return this;
    }

    public CompoundPainterBuilder withSources(Environment environment, SensorEnvironment pollution) {
        painters.add(new SourcePainter(environment, pollution));
        return this;
    }

    public CompoundPainterBuilder withSensors(Environment environment, SensorEnvironment pollution) {
        painters.add(new SensorPainter(environment, pollution));
        return this;
    }

    /**
     * Include a painter for the stored routing path (at {@code routingApplication}) for the currently active user mote (if present)
     * @param environment The environment which contains the user mote.
     * @param routingApplication The routing application which stores the user mote's path.
     * @return The current object.
     */
    public CompoundPainterBuilder withRoutingPath(Environment environment, RoutingApplication routingApplication) {
        Color lineColor = GUISettings.ROUTING_PATH_LINE_COLOR;
        int lineSize = GUISettings.ROUTING_PATH_LINE_SIZE;

        // Optional painter of the complete path
        environment.getMotes().stream()
            .filter(m -> m instanceof UserMote && ((UserMote) m).isActive() && ((UserMote) m).isAdaptation())
            .forEach(m-> {
                painters.add(new LinePainter(routingApplication.getRoute(m), ((UserMote) m).getColor(), lineSize));
            });
            //.ifPresent(m -> painters.add(new LinePainter(routingApplication.getRoute(m), lineColor, lineSize)));
        return this;
    }


    /**
     * Build a {@link CompoundPainter} which has all the painters added to this builder.
     * @return A {@link CompoundPainter} with all the specified painters.
     */
    public CompoundPainter<JXMapViewer> build() {
        return new CompoundPainter<>(painters);
    }

    public CompoundPainterBuilder withSelected(Environment environment, String text) {
        painters.add(new SelectedPainter(environment, text));
        return this;
    }
}
