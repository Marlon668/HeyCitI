package util.xml;

import iot.SimulationRunner;
import iot.networkentity.Mote;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultReaderForBoxPlot3 {
    public static HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Double>>>> readResultsDifference(File file, SimulationRunner simulationRunner) throws ParserConfigurationException {
        HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Double>>>> results = new HashMap<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element resultElement = doc.getDocumentElement();
            Element airQuality = (Element) resultElement.getElementsByTagName("valuesDifferenceConnections").item(0);
            var resultList = airQuality.getElementsByTagName("mote");
            for (int i = 0; i < resultList.getLength(); i++) {
                Element moteElement = (Element) resultList.item(i);
                long moteEUI = Long.parseLong(moteElement.getFirstChild().getNodeValue());
                for (Mote mote : simulationRunner.getEnvironment().getMotes()) {
                    if (mote.getEUI() == moteEUI) {
                        var height = moteElement.getElementsByTagName("BufferSizeHeight");
                        HashMap<Integer, HashMap<Integer, List<Double>>> results1 = new HashMap<>();
                        for (int j = 0; j < height.getLength(); j++) {
                            Element heighthElement = (Element) height.item(j);
                            int bufferHeight = Integer.parseInt(heighthElement.getFirstChild().getNodeValue());
                            var width = heighthElement.getElementsByTagName("BufferSizeWidth");
                            HashMap<Integer, List<Double>> results2 = new HashMap<>();
                            for (int z = 0; z < width.getLength(); z++) {
                                Element widthElement = (Element) width.item(z);
                                int bufferWidth = Integer.parseInt(widthElement.getFirstChild().getNodeValue());
                                List<Double> resultsAirquality = new ArrayList<>();
                                var airQualityElement = widthElement.getElementsByTagName("DifferenceConnections");
                                for(int t=0;t<airQualityElement.getLength();t++) {
                                    Element airQualityElementi = (Element) airQualityElement.item(t);
                                    double airQualityValue = Double.parseDouble(airQualityElementi.getFirstChild().getNodeValue());
                                    resultsAirquality.add(airQualityValue);
                                }
                                results2.put(bufferWidth,resultsAirquality);
                            }
                            results1.put(bufferHeight, results2);
                        }
                        results.put(mote, results1);
                    }
                }

            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}
