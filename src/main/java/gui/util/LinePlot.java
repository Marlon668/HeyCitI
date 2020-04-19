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

/**
 * Class used for plotting a line graphic representing data for air quality or amount of adaptations
 * for a certain run
 */

public class LinePlot extends JFrame {
    public LinePlot (String var1, HashMap<Mote,HashMap<Integer, HashMap<Integer, Result>>> results, ArrayList<Mote> motes, int index,int data) {
        super(var1);
        JPanel var2 = createPanel(results,motes,index,data);
        RefineryUtilities.centerFrameOnScreen(this);
        var2.setPreferredSize(new java.awt.Dimension(1000, 500));
        this.setContentPane(var2);
    }

    public static JPanel createPanel(HashMap<Mote,HashMap<Integer, HashMap<Integer, Result>>> results,ArrayList<Mote> motes,int index,int data) {
        JFreeChart var0 = createChart(createDataset(results,motes,index,data),motes,index,data);
        ChartPanel panel = new ChartPanel(var0);
        panel.setLayout(new FlowLayout((FlowLayout.RIGHT)));
        panel.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (data == 1) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot linePlot = new LinePlot(
                        "Mote : " + motes.get(index).getEUI(), results, motes, index, data-1);
                    linePlot.pack();
                    linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    RefineryUtilities.centerFrameOnScreen(linePlot);
                    linePlot.setVisible(true);
                } else {
                    if (index != 0) {
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                        window.dispose();
                        LinePlot linePlot = new LinePlot(
                            "Mote : " + motes.get(index - 1).getEUI(), results, motes, index - 1, data+1);
                        linePlot.pack();
                        linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        RefineryUtilities.centerFrameOnScreen(linePlot);
                        linePlot.setVisible(true);
                    }
                }
            }
        }));
        panel.add(new JButton(new AbstractAction("Next\u22b3") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (data == 0) {
                    Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    window.dispose();
                    LinePlot linePlot = new LinePlot(
                        "Mote : " + motes.get(index).getEUI(), results, motes, index, 1);
                    linePlot.pack();
                    linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    RefineryUtilities.centerFrameOnScreen(linePlot);
                    linePlot.setVisible(true);
                } else {
                    if (index != motes.size() - 1) {
                        Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                        window.dispose();
                        LinePlot linePlot = new LinePlot(
                            "Mote : " + motes.get(index + 1).getEUI(), results, motes, index + 1, 0);
                        linePlot.pack();
                        linePlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        RefineryUtilities.centerFrameOnScreen(linePlot);
                        linePlot.setVisible(true);
                    }
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

    /**
     * Translate data in a dataset to plot the boxplots
     * @param results a hashmap containing information about the air quality and a hashmap for different
     *                runs with different parameters (width,height) for a certain configurations
     * @param motes arraylist containing every mote of the configuration
     * @param index of which mote must we show a window containing boxplots to respresent its data
     * @param data 0 = showing air qualtiy, 1 = showing amount of adaptations
     */
    private static XYDataset createDataset(HashMap<Mote,HashMap<Integer, HashMap<Integer, Result>>> results,ArrayList<Mote> motes,int index, int data) {
        if(data==0) {
            double noAdaptationValue = 0.0;
            Mote mote = motes.get(index);
            XYSeriesCollection dataset = new XYSeriesCollection();
            HashMap<Integer, HashMap<Integer, Result>> resultsMote = results.get(mote);
            for (Map.Entry<Integer, HashMap<Integer, Result>> moteEntry2 : resultsMote.entrySet()) {
                if(moteEntry2.getKey()==0){
                    for (Map.Entry<Integer, Result> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Result result = moteEntry3.getValue();
                        noAdaptationValue = result.getAirQuality();
                    }
                }
                else{
                    XYSeries series1 = new XYSeries("Buffersize(Width) : " + moteEntry2.getKey());
                    for (Map.Entry<Integer, Result> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Result result = moteEntry3.getValue();
                        series1.add((double) moteEntry3.getKey(), result.getAirQuality()/noAdaptationValue);
                    }
                    dataset.addSeries(series1);
                }
            }

            return dataset;
        }
        else{
            Mote mote = motes.get(index);
            XYSeriesCollection dataset = new XYSeriesCollection();
            HashMap<Integer, HashMap<Integer, Result>> resultsMote = results.get(mote);
            for (Map.Entry<Integer, HashMap<Integer, Result>> moteEntry2 : resultsMote.entrySet()) {
                if(moteEntry2.getKey()!=0) {
                    XYSeries series1 = new XYSeries("Buffersize(Width) : " + moteEntry2.getKey());
                    for (Map.Entry<Integer, Result> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Result result = moteEntry3.getValue();
                        series1.add((double) moteEntry3.getKey(), result.getAmountAdaptations());
                    }
                    dataset.addSeries(series1);
                }
            }

            return dataset;
        }
    }

    private static JFreeChart createChart(XYDataset var0, ArrayList<Mote> motes, int index,int data) {
        if (data == 0) {
            JFreeChart var1 = ChartFactory.createXYLineChart("Mote : " + motes.get(index).getEUI(), "Buffersize (height)", "Air-quality", var0, PlotOrientation.VERTICAL, true, true, false);
            XYPlot var2 = (XYPlot) var1.getPlot();
            var2.setDomainPannable(true);
            var2.setRangePannable(true);
            var2.setDomainZeroBaselineVisible(true);
            var2.setRangeZeroBaselineVisible(true);
            var2.getRangeAxis().setRange(0.0,2.0);
            XYLineAndShapeRenderer var3 = (XYLineAndShapeRenderer) var2.getRenderer();
            var3.setBaseShapesVisible(true);
            var3.setBaseShapesFilled(true);

            var3.setDrawOutlines(true);
            NumberAxis var4 = (NumberAxis) var2.getRangeAxis();
            var4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            return var1;
        } else {
            JFreeChart var1 = ChartFactory.createXYLineChart("Mote : " + motes.get(index).getEUI(), "Buffersize (height)", "Number of Adaptations", var0, PlotOrientation.VERTICAL, true, true, false);
            XYPlot var2 = (XYPlot) var1.getPlot();
            var2.setDomainPannable(true);
            var2.setRangePannable(true);
            var2.setDomainZeroBaselineVisible(true);
            var2.setRangeZeroBaselineVisible(true);
            XYLineAndShapeRenderer var3 = (XYLineAndShapeRenderer) var2.getRenderer();
            var3.setBaseShapesVisible(true);
            var3.setBaseShapesFilled(true);

            var3.setDrawOutlines(true);
            NumberAxis var4 = (NumberAxis) var2.getRangeAxis();
            var4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            return var1;
        }
    }

}

