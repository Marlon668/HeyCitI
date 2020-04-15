package util.xml;

import gui.MainGUI;
import iot.*;
import iot.networkentity.Mote;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import selfadaptation.adaptationgoals.AdaptationGoal;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ResultReader {
    public static HashMap<Mote, HashMap<Integer, HashMap<Integer, Result>>> readResults(File file, SimulationRunner simulationRunner) throws ParserConfigurationException {
        HashMap<Mote, HashMap<Integer, HashMap<Integer, Result>>> results = new HashMap<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element resultElement = doc.getDocumentElement();
            var resultList = resultElement.getElementsByTagName("mote");
            for (int i = 0; i < resultList.getLength(); i++) {
                Element moteElement = (Element) resultList.item(i);
                long moteEUI = Long.parseLong(moteElement.getFirstChild().getNodeValue());
                System.out.println("Mote : " + moteEUI);
                for (Mote mote : simulationRunner.getEnvironment().getMotes()) {
                    if (mote.getEUI() == moteEUI) {
                        System.out.println("Mote : " + moteEUI);
                        var width = moteElement.getElementsByTagName("BufferSizeWidth");
                        HashMap<Integer, HashMap<Integer, Result>> results1 = new HashMap<>();
                        for (int j = 0; j < width.getLength(); j++) {
                            Element widthElement = (Element) width.item(j);
                            int bufferWidth = Integer.parseInt(widthElement.getFirstChild().getNodeValue());
                            var hight = widthElement.getElementsByTagName("BufferSizeHight");
                            HashMap<Integer, Result> results2 = new HashMap<>();
                            for (int z = 0; z < hight.getLength(); z++) {
                                Element hightElement = (Element) hight.item(z);
                                int bufferHight = Integer.parseInt(hightElement.getFirstChild().getNodeValue());
                                Element result = (Element) hightElement.getElementsByTagName("Result").item(0);
                                double airQuality = Double.parseDouble(XMLHelper.readChild(result, "AirQuality"));
                                int adaptations = (int) Double.parseDouble(XMLHelper.readChild(result, "AmountAdaptations"));
                                double distance = Double.parseDouble(XMLHelper.readChild(result, "Distance"));
                                Result resultExperiment = new Result(airQuality, adaptations, distance);
                                results2.put(bufferHight, resultExperiment);
                            }
                            results1.put(bufferWidth, results2);
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
