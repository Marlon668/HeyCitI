package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import gui.MainGUI;
import iot.Result;
import iot.networkentity.Mote;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import util.Pair;


public class LinePlot2 extends JFrame {
    public LinePlot2 (String var1, HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>> adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes, ArrayList<Mote> motes, int index) {
        super(var1);
        JPanel var2 = createPanel(visualiseRun,adaptationPoints,alternativeRoutes,motes,index);
        RefineryUtilities.centerFrameOnScreen(this);
        var2.setPreferredSize(new java.awt.Dimension(1000, 500));
        this.setContentPane(var2);
    }

    public static JPanel createPanel(HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>> adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes,ArrayList<Mote> motes,int index) {
        JFreeChart var0 = createChart(createDataset(visualiseRun,adaptationPoints,alternativeRoutes,motes,index),motes,index);
        ChartPanel panel = new ChartPanel(var0);
        panel.setLayout(new FlowLayout((FlowLayout.RIGHT)));
        panel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != 0) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot2 linePlot = new LinePlot2(
                        "Mote : " + motes.get(index - 1).getEUI(), visualiseRun,adaptationPoints,alternativeRoutes, motes, index - 1);
                    linePlot.pack();
                    linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    RefineryUtilities.centerFrameOnScreen(linePlot);
                    linePlot.setVisible(true);
                }
                }
        }));
        panel.add(new JButton(new AbstractAction("Next\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != motes.size() - 1) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot2 linePlot = new LinePlot2(
                        "Mote : " + motes.get(index + 1).getEUI(), visualiseRun,adaptationPoints,alternativeRoutes, motes, index + 1);
                    linePlot.pack();
                    linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    RefineryUtilities.centerFrameOnScreen(linePlot);
                    linePlot.setVisible(true);
                }

            }
        }));
        panel.add(new JButton(new AbstractAction("Save\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save output");
                fc.setFileFilter(new FileNameExtensionFilter("pdf output", "pdf"));

                File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                file = new File(file.getParent());
                fc.setCurrentDirectory(file);

                int returnVal = fc.showSaveDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = GUIUtil.getOutputFile(fc.getSelectedFile(), "pdf");
                    PDFDocument pdfDoc = new PDFDocument();
                    pdfDoc.setTitle("Line plot");
                    Page page = pdfDoc.createPage(new Rectangle(panel.getWidth(), panel.getHeight()));
                    PDFGraphics2D g2 = page.getGraphics2D();
                    var0.draw(g2, new Rectangle(0, 0, panel.getWidth(), panel.getHeight()));
                    pdfDoc.writeToFile(file);
                }

            }
        }));
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    private static XYDataset createDataset(HashMap<Mote, List<Pair<Double,Double>>> visualiseRun,HashMap<Mote,List<Double>>adaptationPoints,HashMap<Mote,List<Pair<Pair<Double,Double>,Pair<Double,Double>>>> alternativeRoutes, ArrayList<Mote> motes, int index) {
            Mote mote = motes.get(index);
            XYSeriesCollection dataset = new XYSeriesCollection();
            List<Pair<Double,Double>> resultsMote = visualiseRun.get(mote);
            XYSeries series1 = new XYSeries("Path");
            for (Pair<Double,Double> moteEntry2 : resultsMote) {
                series1.add(moteEntry2.getRight(),moteEntry2.getLeft());
            }
            dataset.addSeries(series1);
            List<Double>adaptation= adaptationPoints.get(mote);
            int i = 0;
            for(double adaptationPoint : adaptation){
                series1 = new XYSeries(i);
                series1.add(adaptationPoint,0.0);
                series1.add(adaptationPoint,5.0);
                dataset.addSeries(series1);
                i++;
            }
            int j = 1000;
            for(Pair<Pair<Double,Double>,Pair<Double,Double>> adaptationPoint : alternativeRoutes.get(mote)) {
                series1 = new XYSeries(j);
                series1.add(adaptationPoint.getLeft().getRight(),adaptationPoint.getLeft().getLeft());
                series1.add(adaptationPoint.getRight().getRight(),adaptationPoint.getRight().getLeft());
                dataset.addSeries(series1);
                j++;
            }
            return dataset;
        }

    private static JFreeChart createChart(XYDataset var0, ArrayList<Mote> motes, int index) {
        JFreeChart var1 = ChartFactory.createXYLineChart("Mote : " + motes.get(index).getEUI(), "Distance (meter)", "Air Quality", var0, PlotOrientation.VERTICAL, false, true, false);
        XYPlot var2 = (XYPlot) var1.getPlot();
        var2.setDomainPannable(true);
        var2.setRangePannable(true);
        var2.setDomainZeroBaselineVisible(true);
        var2.setRangeZeroBaselineVisible(true);
        XYLineAndShapeRenderer var3 = (XYLineAndShapeRenderer) var2.getRenderer();
        var3.setDefaultShapesVisible(true);
        var3.setDefaultShapesFilled(true);

        var3.setDrawOutlines(true);
        NumberAxis var4 = (NumberAxis) var2.getRangeAxis();
        var4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return var1;
    }
}

