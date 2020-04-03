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
     * The default buffer size height used by the adaptation algorithm
     */
    private static final int DEFAULT_BUFFERSIZEHEIGHT = 1;

    /**
     * The default buffer size width used by the adaptation algorithm
     */
    private static final int DEFAULT_BUFFERSIZEHWIDTH = 1;


    /**
     * The default evaluationvalue for evaluate two paths
     */
    private static final double DEFAULT_BETTER_PATH = 1;


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
     * The default OF remove visited visualisation of visited connections
     */
    private static final int DEFAULT_REMOVECON = 0;

    /**
     * The default OF method of analysing paths used in the adaptation algorithm
     * In adaptation algorithm
     * 0 = using no analysing method
     * 1 = change path if new found path is a given percentage better than previous path
     * 2 = change path if pollution value of path has changer a given percentage
     */
    private static final int DEFAULT_ANALYSINGMETHOD = 0;

    /**
     * The source Document of the parameters.
     */
    private Document xmlSource;

    /**
     * The analysing method used in the adaptation algorithm
     *  0 = using no analysing method
     *  1 = change path if new found path is a given percentage better than previous path
     *  2 = change path if pollution value of path has changer a given percentage
     */
    private int analysingMethod;

    /**
     * buffer size height used by the adaptation algorithm
     * After how much steps we must decide whether or not changing the path
     */
    private int buffersizeHeight;

    /**
     * buffer size width used by the adaptation algorithm
     * How much paths could we save in each step in the adaptation algorithm
     */
    private int buffersizeWidth;

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
     * Boolean to decide if we must remove the visualisation of visited connections
     * 0 = false
     * 1 = true
     */
    private int removeConn;

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
     * @return synchronisation: boolean to decide if we reset the visualisation of visited connections
     */
    public int getRemoveConn(){
        return this.removeConn;
    }

    /**
     * @return gives the analysing method used in the adaptation algorithm
     * 0 = using no analysing method
     * 1 = change path if new found path is a given percentage better than previous path
     * 2 = change path if pollution value of path has changer a given percentage
     */
     public int getAnalysingMethod(){
         return analysingMethod;
     }

    /**
     * Sets the analysing method used in the adaptation algorithm
     * 0 = using no analysing method
     * 1 = change path if new found path is a given percentage better than previous path
     * 2 = change path if pollution value of path has changer a given percentage
     */
    public void setAnalysingMethod(int analysingMethod) {
        this.analysingMethod = analysingMethod;
    }

    /**
     * Set the remove connections boolean: boolean to decide if we must remove visualisation of visited connections
     * @param removeConn = Boolean to decide if we must remove visualisation of visited connections
     *         0 = false
     *         1 = true
     */
    public void setRemoveConn(int removeConn){
        this.removeConn = removeConn;
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
     */
    public Parameters() {
        this(null,DEFAULT_BUFFERSIZEHEIGHT,DEFAULT_BUFFERSIZEHWIDTH,DEFAULT_BETTER_PATH,DEFAULT_SYNCHRONISATION,DEFAULT_AMOUNTRUNS,DEFAULT_SETUP,DEFAULT_REMOVECON,DEFAULT_ANALYSINGMETHOD);
    }


    /**
     * Generates InputProfile with a given qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
     * regionProbabilities, xmlSource and gui.
     * @param xmlSource The source of the InputProfile.
     * @param buffersizeHeight The buffer size height used by the adaptation algorithm
     * @param buffersizeWidth The buffer size width used by the adaptation algorithm
     * @param betterpath Value used to compare two paths
     * @param synchronisation boolean to decide wether or not we must redo the simulation when a simulation error has occurred
     * @param amountRuns the amount of runs of simulation
     * @param setupFirst boolean to decide if we must calculate first the changes of the path of the mote
     * @param analysingMethod method used to analyse methods in the adaptation algorithm
     */
    public Parameters(Element xmlSource,int buffersizeHeight,int buffersizeWidth, double betterpath, int synchronisation, int amountRuns, int setupFirst, int removeConn, int analysingMethod) {
        this.buffersizeHeight = buffersizeHeight;
        this.buffersizeWidth = buffersizeWidth;
        this.betterpath = betterpath;
        this.synchronisation = synchronisation;
        this.amountRuns = amountRuns;
        this.setupFirst = setupFirst;
        this.removeConn = removeConn;
        this.analysingMethod = analysingMethod;
        if(xmlSource != null) {
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
    }


    /**
     * returns the buffersize height
     * @return buffersize height
     */
    public int getBuffersizeHeight()
    {
        return buffersizeHeight;
    }

    /**
     * returns the buffersize width
     * @return buffersize width
     */
    public int getBuffersizeWidth()
    {
        return buffersizeWidth;
    }

    /**
     * Sets the buffersize height
     * @param bufferSizeHeight heightThe buffersize height to set for the adaptation algorithm
     */
    public void setBufferSizeHeight(int bufferSizeHeight)
    {
        this.buffersizeHeight = bufferSizeHeight;
    }

    /**
     * Sets the buffersize width
     * @param buffersizeWidth The buffersize width to set for the adaptation algorithm
     */
    public void setBuffersizeWidth(int buffersizeWidth)
    {
        this.buffersizeWidth = buffersizeWidth;
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

        Element bufferSizeHeight = doc.createElement("bufferSizeHeight");
        bufferSizeHeight.appendChild(doc.createTextNode(Integer.toString(getBuffersizeHeight())));
        parameterElement.appendChild(bufferSizeHeight);

        Element bufferSizeWidth = doc.createElement("bufferSizeWidth");
        bufferSizeWidth.appendChild(doc.createTextNode(Integer.toString(getBuffersizeWidth())));
        parameterElement.appendChild(bufferSizeWidth);

        Element synchronisationIssue = doc.createElement("synchronisedIssue");
        synchronisationIssue.appendChild(doc.createTextNode(Integer.toString(getSynchronisation())));
        parameterElement.appendChild(synchronisationIssue);

        Element amountRuns = doc.createElement("amountRuns");
        amountRuns.appendChild(doc.createTextNode(Integer.toString(getAmountRuns())));
        parameterElement.appendChild(amountRuns);

        Element setupFirst = doc.createElement("setupFirst");
        setupFirst.appendChild(doc.createTextNode(Integer.toString(getSetupFirst())));
        parameterElement.appendChild(setupFirst);

        Element removeCon = doc.createElement("removeConnections");
        removeCon.appendChild(doc.createTextNode(Integer.toString(getRemoveConn())));
        parameterElement.appendChild(removeCon);

        Element analysingMethod = doc.createElement("analysingMethod");
        analysingMethod.appendChild(doc.createTextNode(Integer.toString(getAnalysingMethod())));
        parameterElement.appendChild(analysingMethod);

        SimulationRunner.getInstance(file).updateParametersFile();
    }

}
