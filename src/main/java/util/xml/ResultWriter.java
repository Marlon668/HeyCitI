package util.xml;

import iot.Parameters;
import iot.Result;
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
import java.util.Map;

public class ResultWriter {

    public static void saveResultsToFile(HashMap<Mote, HashMap<Integer, HashMap<Integer, Result>>> results, File file) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            // root element
            Element rootElement = doc.createElement("experimentalData");
            doc.appendChild(rootElement);
            for (Map.Entry<Mote, HashMap<Integer, HashMap<Integer, Result>>> moteEntry : results.entrySet()) {
                Element moteElement = doc.createElement("mote");
                moteElement.appendChild(doc.createTextNode(Long.toString(moteEntry.getKey().getEUI())));
                for (Map.Entry<Integer, HashMap<Integer, Result>> moteEntry2 : moteEntry.getValue().entrySet()) {
                    Element width = doc.createElement("BufferSizeWidth");
                    width.appendChild(doc.createTextNode(Integer.toString(moteEntry2.getKey())));
                    for (Map.Entry<Integer, Result> moteEntry3 : moteEntry2.getValue().entrySet()) {
                        Element hight = doc.createElement("BufferSizeHight");
                        hight.appendChild(doc.createTextNode(Integer.toString(moteEntry3.getKey())));
                        Element result = doc.createElement("Result");
                        Element airQuality = doc.createElement("AirQuality");
                        airQuality.appendChild(doc.createTextNode(Double.toString(moteEntry3.getValue().getAirQuality())));
                        Element amountAdaptations = doc.createElement("AmountAdaptations");
                        amountAdaptations.appendChild(doc.createTextNode(Integer.toString(moteEntry3.getValue().getAmountAdaptations())));
                        Element distance = doc.createElement("Distance");
                        distance.appendChild(doc.createTextNode(Double.toString(moteEntry3.getValue().getDistance())));
                        result.appendChild(airQuality);
                        result.appendChild(amountAdaptations);
                        result.appendChild(distance);
                        hight.appendChild(result);
                        width.appendChild(hight);
                    }
                    moteElement.appendChild(width);
                }
                rootElement.appendChild(moteElement);
            }
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

