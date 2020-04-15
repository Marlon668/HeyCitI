package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import gui.util.RefineryUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class GraphsForResults extends JFrame {

    public GraphsForResults(String title) {
        super(title);
        JPanel chartPanel = createDemoPanel();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
        setContentPane(chartPanel);
    }

    private static JFreeChart createChart(final XYDataset dataset) {

        JFreeChart chart = ChartFactory.createXYLineChart(
            "Line Chart Demo: XYLineAndShapeRenderer",
            "X",
            "Y",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(){
            Stroke soild = new BasicStroke(2.0f);
            Stroke dashed =  new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f);
            @Override
            public Stroke getItemStroke(int row, int column) {
                return soild;
            }
        };

        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultStroke(new BasicStroke(3));
        plot.setRenderer(renderer);
        return chart;
    }

    public static JPanel createDemoPanel() {
        JFreeChart chart = createChart(createDataset());
        ChartPanel panel = new ChartPanel(chart);
        panel.setLayout(new FlowLayout((FlowLayout.RIGHT)));
        panel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                window.dispose();
                GraphsForResults demo = new GraphsForResults(
                    "Line plot");
                demo.pack();
                demo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
            }
        }));
        panel.add(new JButton(new AbstractAction("Next\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                window.dispose();
                GraphsForResults demo = new GraphsForResults(
                    "JFreeChart");
                demo.pack();
                demo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
            }
        }));
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public static void main(String[] args) {

        GraphsForResults demo = new GraphsForResults(
            "JFreeChart");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

    private static XYDataset createDataset() {

        XYSeries series1 = new XYSeries("First");
        series1.add(1.0, 1.0);
        series1.add(2.0, 4.0);
        series1.add(3.0, 3.0);
        series1.add(4.0, 5.0);
        series1.add(5.0, 5.0);
        series1.add(6.0, 7.0);
        series1.add(7.0, 7.0);
        series1.add(8.0, 8.0);

        XYSeries series2 = new XYSeries("Second");
        series2.add(1.0, 5.0);
        series2.add(2.0, 7.0);
        series2.add(3.0, 6.0);
        series2.add(4.0, 8.0);
        series2.add(5.0, 4.0);
        series2.add(6.0, 4.0);
        series2.add(7.0, 2.0);
        series2.add(8.0, 1.0);

        XYSeries series3 = new XYSeries("Third");
        series3.add(3.0, 4.0);
        series3.add(4.0, 3.0);
        series3.add(5.0, 2.0);
        series3.add(6.0, 3.0);
        series3.add(7.0, 6.0);
        series3.add(8.0, 3.0);
        series3.add(9.0, 4.0);
        series3.add(10.0, 3.0);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);

        return dataset;

    }


}
