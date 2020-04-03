package util.xml;

import gui.MainGUI;
import iot.InputProfile;
import iot.Parameters;
import iot.QualityOfService;
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

public class ParameterReader {
    public static Parameters readParameters(File file) {
        Parameters parameters = null;
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            Element parameterElement = doc.getDocumentElement();
            var adaptationParameters = parameterElement.getElementsByTagName("adaptationParameters");
            int bufferSizeHeight  = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "bufferSizeHeight"));
            int bufferSizeWidth  = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "bufferSizeWidth"));
            double evaluationValue = Double.parseDouble(XMLHelper.readChild(parameterElement, "evaluationValue"));
            int synchronisedIssue  = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "synchronisedIssue"));
            int amountRuns = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "amountRuns"));
            int setupFirst = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "setupFirst"));
            int removeConn = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "removeConnections"));
            int analysingMethod = (int) Double.parseDouble(XMLHelper.readChild(parameterElement, "analysingMethod"));
            parameters = new Parameters(parameterElement,bufferSizeHeight,bufferSizeWidth,evaluationValue,synchronisedIssue,amountRuns,setupFirst,removeConn,analysingMethod);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return parameters;
    }
}
