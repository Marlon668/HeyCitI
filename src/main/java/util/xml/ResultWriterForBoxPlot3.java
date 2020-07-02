package util.xml;

import iot.SimulationRunner;
import iot.networkentity.Mote;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultWriterForBoxPlot3 {

    public static void saveResultsToFile(HashMap<Mote, HashMap<Integer, HashMap<Integer, List<Double>>>> differenceConnections, File file) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            // root element
            Element rootElement = doc.createElement("experimentalData");
            doc.appendChild(rootElement);
            Element valuesForAirQuality = doc.createElement("valuesDifferenceConnections");
            for (Map.Entry<Mote,HashMap<Integer, HashMap<Integer, List<Double>>>> moteEntry : differenceConnections.entrySet()) {
                Element moteElement = doc.createElement("mote");
                moteElement.appendChild(doc.createTextNode(Long.toString(moteEntry.getKey().getEUI())));
                for (Map.Entry<Integer, HashMap<Integer, List<Double>>> moteEntry2 : moteEntry.getValue().entrySet()) {
                    Element height = doc.createElement("BufferSizeHeight");
                    height.appendChild(doc.createTextNode(Integer.toString(moteEntry2.getKey())));
                    for (Map.Entry<Integer, List<Double>> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Element width = doc.createElement("BufferSizeWidth");
                        width.appendChild(doc.createTextNode(Integer.toString(moteEntry3.getKey())));
                        for(double differenceValue : moteEntry3.getValue()){
                            Element connectionElement = doc.createElement("DifferenceConnections");
                            connectionElement.appendChild(doc.createTextNode(Double.toString(differenceValue)));
                            width.appendChild(connectionElement);
                        }
                        height.appendChild(width);
                    }
                    moteElement.appendChild(height);
                }
                valuesForAirQuality.appendChild(moteElement);
            }
            rootElement.appendChild(valuesForAirQuality);
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
