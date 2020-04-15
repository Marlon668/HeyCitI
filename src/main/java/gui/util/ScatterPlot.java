package gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ScatterPlot extends JFrame {
    public ScatterPlot(String var1,HashMap<Mote,HashMap<Integer, HashMap<Integer, Result>>> results) {
        super(var1);
        JPanel var2 = createPanel(results);
        var2.setPreferredSize(new java.awt.Dimension(1000, 500));
        this.setContentPane(var2);
    }

    private static JFreeChart createChart(XYDataset var0) {
        JFreeChart var1 = ChartFactory.createScatterPlot("Scatter Plot", "Distance", "Air-Quality", var0, PlotOrientation.VERTICAL, true, true, false);
        XYPlot var2 = (XYPlot)var1.getPlot();
        var2.setDomainCrosshairVisible(true);
        var2.setDomainCrosshairLockedOnData(true);
        var2.setRangeCrosshairVisible(true);
        var2.setRangeCrosshairLockedOnData(true);
        var2.setDomainZeroBaselineVisible(true);
        var2.setRangeZeroBaselineVisible(true);
        var2.setDomainPannable(true);
        var2.setRangePannable(true);
        NumberAxis var3 = (NumberAxis)var2.getDomainAxis();
        var3.setAutoRangeIncludesZero(false);
        return var1;
    }

    public static JPanel createPanel(HashMap<Mote,HashMap<Integer, HashMap<Integer, Result>>> results) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Map.Entry<Mote,HashMap<Integer, HashMap<Integer, Result>>> moteEntry : results.entrySet()) {
            XYSeries series1 = new XYSeries("Mote : "+  moteEntry.getKey().getEUI());
            for (Map.Entry<Integer, HashMap<Integer, Result>> moteEntry2 :moteEntry.getValue().entrySet()){
                for (Map.Entry<Integer, Result> moteEntry3 :moteEntry2.getValue().entrySet()){
                    Result result = moteEntry3.getValue();
                    series1.add(result.getDistance(), result.getAirQuality());
                }
            }
            dataset.addSeries(series1);
        }

        JFreeChart var0 = createChart(dataset);
        ChartPanel panel = new ChartPanel(var0);
        panel.setLayout(new FlowLayout((FlowLayout.RIGHT)));
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

}
