package util.xml;

import iot.Result;
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

public class ResultReaderForBoxPlot {
    public static HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Double>>>> readResultsAirQuality(File file, SimulationRunner simulationRunner) throws ParserConfigurationException {
        HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Double>>>> results = new HashMap<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element resultElement = doc.getDocumentElement();
            Element airQuality = (Element) resultElement.getElementsByTagName("valuesAirQuality").item(0);
            var resultList = airQuality.getElementsByTagName("mote");
            for (int i = 0; i < resultList.getLength(); i++) {
                Element moteElement = (Element) resultList.item(i);
                long moteEUI = Long.parseLong(moteElement.getFirstChild().getNodeValue());
                System.out.println("Mote : " + moteEUI);
                for (Mote mote : simulationRunner.getEnvironment().getMotes()) {
                    if (mote.getEUI() == moteEUI) {
                        System.out.println("Mote : " + moteEUI);
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
                                var airQualityElement = widthElement.getElementsByTagName("AirQuality");
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

    public static HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Integer>>>> readResultsAdaptation(File file, SimulationRunner simulationRunner) throws ParserConfigurationException {
        HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Integer>>>> results = new HashMap<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element resultElement = doc.getDocumentElement();
            Element adaptation = (Element) resultElement.getElementsByTagName("valuesAdaptations").item(0);
            var resultList = adaptation.getElementsByTagName("mote");
            for (int i = 0; i < resultList.getLength(); i++) {
                Element moteElement = (Element) resultList.item(i);
                long moteEUI = Long.parseLong(moteElement.getFirstChild().getNodeValue());
                System.out.println("Mote : " + moteEUI);
                for (Mote mote : simulationRunner.getEnvironment().getMotes()) {
                    if (mote.getEUI() == moteEUI) {
                        System.out.println("Mote : " + moteEUI);
                        var height = moteElement.getElementsByTagName("BufferSizeHeight");
                        HashMap<Integer, HashMap<Integer, List<Integer>>> results1 = new HashMap<>();
                        for (int j = 0; j < height.getLength(); j++) {
                            Element heighthElement = (Element) height.item(j);
                            int bufferHeight = Integer.parseInt(heighthElement.getFirstChild().getNodeValue());
                            var width = heighthElement.getElementsByTagName("BufferSizeWidth");
                            HashMap<Integer, List<Integer>> results2 = new HashMap<>();
                            for (int z = 0; z < width.getLength(); z++) {
                                Element widthElement = (Element) width.item(z);
                                int bufferWidth = Integer.parseInt(widthElement.getFirstChild().getNodeValue());
                                List<Integer> resultsAdaptations = new ArrayList<>();
                                var adpatationElement = widthElement.getElementsByTagName("AmountAdaptations");
                                for(int t=0;t<adpatationElement.getLength();t++) {
                                    Element adaptationElementi = (Element) adpatationElement.item(t);
                                    int adaptationValue = Integer.parseInt(adaptationElementi.getFirstChild().getNodeValue());
                                    resultsAdaptations.add(adaptationValue);
                                }
                                System.out.println("Size : " +  resultsAdaptations.size());
                                results2.put(bufferWidth,resultsAdaptations);
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
