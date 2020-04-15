package util.xml;

import iot.Parameters;
import iot.Result;
import iot.Simulation;
import iot.SimulationRunner;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import org.jfree.data.xy.XYSeries;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultWriterForBoxPlot {

    public static void saveResultsToFile(SimulationRunner simulationRunner, HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Double>>>> airQuality, HashMap<Mote,HashMap<Integer,HashMap<Integer,List<Integer>>>> adaptations, File file) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            // root element
            Element rootElement = doc.createElement("experimentalData");
            doc.appendChild(rootElement);
            Element valuesForAirQuality = doc.createElement("valuesAirQuality");
            for (Map.Entry<Mote,HashMap<Integer, HashMap<Integer, List<Double>>>> moteEntry : airQuality.entrySet()) {
                Element moteElement = doc.createElement("mote");
                moteElement.appendChild(doc.createTextNode(Long.toString(moteEntry.getKey().getEUI())));
                for (Map.Entry<Integer, HashMap<Integer, List<Double>>> moteEntry2 : moteEntry.getValue().entrySet()) {
                    Element height = doc.createElement("BufferSizeHeight");
                    height.appendChild(doc.createTextNode(Integer.toString(moteEntry2.getKey())));
                    for (Map.Entry<Integer, List<Double>> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Element width = doc.createElement("BufferSizeWidth");
                        width.appendChild(doc.createTextNode(Integer.toString(moteEntry3.getKey())));
                        for(double airQualityValue : moteEntry3.getValue()){
                            Element airQualityElement = doc.createElement("AirQuality");
                            airQualityElement.appendChild(doc.createTextNode(Double.toString(airQualityValue)));
                            width.appendChild(airQualityElement);
                        }
                        height.appendChild(width);
                    }
                    moteElement.appendChild(height);
                }
                valuesForAirQuality.appendChild(moteElement);
            }
            Element valuesForAdaptations = doc.createElement("valuesAdaptations");
            for (Map.Entry<Mote,HashMap<Integer, HashMap<Integer, List<Integer>>>> moteEntry : adaptations.entrySet()) {
                Element moteElement = doc.createElement("mote");
                moteElement.appendChild(doc.createTextNode(Long.toString(moteEntry.getKey().getEUI())));
                for (Map.Entry<Integer, HashMap<Integer, List<Integer>>> moteEntry2 : moteEntry.getValue().entrySet()) {
                    Element height = doc.createElement("BufferSizeHeight");
                    height.appendChild(doc.createTextNode(Integer.toString(moteEntry2.getKey())));
                    for (Map.Entry<Integer, List<Integer>> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Element width = doc.createElement("BufferSizeWidth");
                        width.appendChild(doc.createTextNode(Integer.toString(moteEntry3.getKey())));
                        for(int adaptationValue : moteEntry3.getValue()){
                            Element adaptationElement = doc.createElement("AmountAdaptations");
                            adaptationElement.appendChild(doc.createTextNode(Integer.toString(adaptationValue)));
                            width.appendChild(adaptationElement);
                        }
                        height.appendChild(width);
                    }
                    moteElement.appendChild(height);
                }
                valuesForAdaptations.appendChild(moteElement);
            }
            Integer noise = simulationRunner.getEnvironmentAPI().getSensors().get(0).getNoiseRatio();
            Element noiseElement =  doc.createElement("NoiseRatio");
            noiseElement.appendChild(doc.createTextNode(Integer.toString(noise)));
            rootElement.appendChild(noiseElement);
            rootElement.appendChild(valuesForAirQuality);
            rootElement.appendChild(valuesForAdaptations);
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
