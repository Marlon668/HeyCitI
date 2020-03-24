package iot;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import selfadaptation.adaptationgoals.AdaptationGoal;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * A class representing parameters of the adaptation algorithm
 */
public class Parameters {

    /**
     * The default buffer size used by the adaptation algorithm
     */
    private static final int DEFAULT_BUFFERSIZE = 1;



    /**
     * The default evaluationvalue for evaluate two paths
     */
    private static final double DEFAULT_BETTER_PATH = 0.95;


    /**
     * The default OF redoing simulation to get result without synchronisation issue
     */
    private static final int DEFAULT_SYNCHRONISATION = 0;

    /**
     * The default OF redoing simulation to get result without synchronisation issue
     */
    private static final int DEFAULT_AMOUNTRUNS = 1;

    /**
     * The default OF setup first al the changes of the path of the mote
     */
    private static final int DEFAULT_SETUP = 0;

    /**
     * The source Document of the parameters.
     */
    private Document xmlSource;

    /**
     * buffer size used by the adaptation algorithm
     */
    private int buffersize;

    /**
     * Amount of runs
     */
    private int amountRuns;

    /**
     * Boolean to decide if we must redo the simulation to get result without synchronisation issue
     * 0 = false
     * 1 = true
     */
    private int synchronisation;

    /**
     * Boolean to decide if we must calculate first all the changes of the path of the mote
     * 0 = false
     * 1 = true
     */
    private int setupFirst;

    /**
     * betterpath value used to compare two paths
     */
    private double betterpath;

    /**
     * @return synchronisation: boolean to decide if we must redo the simulation
     */
    public int getSynchronisation(){
        return this.synchronisation;
    }

    /**
     * Set the synchronisation boolean: boolean to decide if we must redo the simulation
     * @param synchronisation = Boolean to decide if we must redo the simulation
     *         0 = false
     *         1 = true
     */
    public void setSynchronisation(int synchronisation){
        this.synchronisation = synchronisation;
    }

    /**
     * @return setupFirst boolean to decide if we must first calculate all the changes of the path
     */
    public int getSetupFirst(){
        return this.setupFirst;
    }

    /**
     * Set the setupFirst boolean: boolean to decide if we must calculate first all the changes of the path of the mote
     * @param setupFirst= Boolean to decide if we must calculate first all the changes of the path of the mote
     *         0 = false
     *         1 = true
     */
    public void setSetupFirst(int setupFirst){
        this.setupFirst = setupFirst;
    }

    /**
     * sets the amount of runs
     * @param amountRuns: amount of runs
     */

    public void setAmountRuns(int amountRuns){
        this.amountRuns = amountRuns;
    }

    /**
     * @return amountRuns : set the amount of runs
     */
    public int getAmountRuns(){
        return this.amountRuns;
    }

    /**
     * Generates InputProfile with a given qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
     * regionProbabilities, xmlSource and gui.
     * @param xmlSource The source of the InputProfile.
     */
    public Parameters(Element xmlSource) {
        this(xmlSource,DEFAULT_BUFFERSIZE,DEFAULT_BETTER_PATH,DEFAULT_SYNCHRONISATION,DEFAULT_AMOUNTRUNS,DEFAULT_SETUP);
    }


    /**
     * Generates InputProfile with a given qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
     * regionProbabilities, xmlSource and gui.
     * @param xmlSource The source of the InputProfile.
     * @param buffersize The buffer size used by the adaptation algorithm
     * @param betterpath Value used to compare two paths
     * @param synchronisation boolean to decide wether or not we must redo the simulation when a simulation error has occurred
     * @param amountRuns the amount of runs of simulation
     * @param setupFirst boolean to decide if we must calculate first the changes of the path of the mote
     */
    public Parameters(Element xmlSource,int buffersize, double betterpath, int synchronisation, int amountRuns, int setupFirst) {
        this.buffersize = buffersize;
        this.betterpath = betterpath;
        this.synchronisation = synchronisation;
        this.amountRuns = amountRuns;
        this.setupFirst = setupFirst;
        Node node = xmlSource;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document newDocument = builder.newDocument();
        Node importedNode = newDocument.importNode(node, true);
        newDocument.appendChild(importedNode);
        this.xmlSource = newDocument;
    }


    /**
     * returns the buffersize
     * @return buffersize
     */
    public int getBuffersize()
    {
        return buffersize;
    }

    /**
     * Sets the buffersize
     * @param bufferSize The buffersize to set for the adaptationalgorithm
     */
    public void setBufferSize(int bufferSize)
    {
        this.buffersize = bufferSize;
    }

    /**
     * returns the better path value
     * @return better path value to compare two paths in the adaptation algorithm
     */
    public double getBetterpath()
    {
        return betterpath;
    }

    /**
     * returns the better path value
     * @param betterpath value to compare two paths in the adaptation algorithm
     */
    public void setBetterpath(double betterpath)
    {
        this.betterpath = betterpath;
    }

    /**
     * returns the xml source.
     * @return The xml source.
     */
    public Document getXmlSource() {
        return xmlSource;
    }

    /**
     * A function which updates the source file.
     */
    public void updateFile(File file) {
        Document doc = getXmlSource();
        for (int i =0 ; i<doc.getChildNodes().getLength();) {
            doc.removeChild(doc.getChildNodes().item(0));
        }

        Element parameterElement = doc.createElement("adaptationParameters");
        doc.appendChild(parameterElement);

        Element evaluationValue = doc.createElement("evaluationValue");
        evaluationValue.appendChild(doc.createTextNode(Double.toString(getBetterpath())));
        parameterElement.appendChild(evaluationValue);

        Element bufferSize = doc.createElement("bufferSize");
        bufferSize.appendChild(doc.createTextNode(Integer.toString(getBuffersize())));
        parameterElement.appendChild(bufferSize);

        Element synchronisationIssue = doc.createElement("synchronisedIssue");
        synchronisationIssue.appendChild(doc.createTextNode(Integer.toString(getSynchronisation())));
        parameterElement.appendChild(synchronisationIssue);

        Element amountRuns = doc.createElement("amountRuns");
        amountRuns.appendChild(doc.createTextNode(Integer.toString(getAmountRuns())));
        parameterElement.appendChild(amountRuns);

        Element setupFirst = doc.createElement("setupFirst");
        setupFirst.appendChild(doc.createTextNode(Integer.toString(getSetupFirst())));
        parameterElement.appendChild(setupFirst);

        SimulationRunner.getInstance(file).updateParametersFile();
    }

}
