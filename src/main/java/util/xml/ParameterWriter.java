package util.xml;

import iot.InputProfile;
import iot.Parameters;
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
import java.util.List;


public class ParameterWriter {

    public static void updateParameterFile(Parameters parameters, File file) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

            Element parametersElement = doc.getDocumentElement();

            for (int i = 0; i < parametersElement.getChildNodes().getLength(); ) {
               parametersElement.removeChild(parametersElement.getChildNodes().item(0));
            }
            Node importedNode = doc.importNode(parameters.getXmlSource().getDocumentElement(), true);
            parametersElement.appendChild(importedNode);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            e.printStackTrace();
        }

    }
}
