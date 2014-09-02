package org.olat.lms.scorm.server.servermodels;

/**
 * RELOAD TOOLS Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir, Paul Sharples Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
 * the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software. THE SOFTWARE
 * IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. Project Management Contact: Oleg Liber Bolton
 * Institute of Higher Education Deane Road Bolton BL3 5AB UK e-mail: o.liber@bolton.ac.uk Technical Contact: Phillip Beauvoir e-mail: p.beauvoir@bolton.ac.uk Paul
 * Sharples e-mail: p.sharples@bolton.ac.uk Web: http://www.reload.ac.uk
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.olat.lms.scorm.ISettingsHandler;
import org.olat.system.exception.OLATRuntimeException;

import uk.ac.reload.jdom.XMLDocument;

/**
 * ScoDocument. A class that allows us to read in a CMI datamodel for a particular item, get at the datamodel and allow the javascript runtime model to access the
 * elements, read in the values to its model and also update back to the server model with any changes once the user has "browsed" it.
 * 
 * @author Paul Sharples
 */
public class ScoDocument extends XMLDocument {
    // var used to flag if the sco was "failed"
    private boolean isFailed = false;

    // some more vars used to keep the initial settings for a CMI_DataModel
    // so we can re-generate a "clean slate" easily, if a user has failed a sco
    private String _userId;
    private String _userName;
    private String _max_time_allowed;
    private String _time_limit_action;
    private String _data_from_lms;
    private String _mastery_score;
    private String _lesson_location;
    private String _lesson_mode;
    private String _credit_mode;

    /**
     * Some sco data elements that will appear in every sco data model This is split intot part_a and part_b so we can insert 'n' number of <objective> nodes if the sco
     * creates them during a session
     */
    public static String[] _cmivalues_a = { "cmi.core.student_id", "cmi.core.student_name", "cmi.core.lesson_location", "cmi.core.credit", "cmi.core.lesson_status",
            "cmi.core.entry", "cmi.core.score.raw", "cmi.core.score.max", "cmi.core.score.min", "cmi.core.total_time", "cmi.core.lesson_mode", "cmi.core.exit",
            "cmi.core.session_time", "cmi.suspend_data", "cmi.launch_data", "cmi.comments", "cmi.comments_from_lms", };

    /**
     * <objective> nodes go between part_a and part_b
     */
    public static String[] _cmivalues_b = { "cmi.student_data.mastery_score", "cmi.student_data.max_time_allowed", "cmi.student_data.time_limit_action",
            "cmi.student_preference.audio", "cmi.student_preference.language", "cmi.student_preference.speed", "cmi.student_preference.text", };

    // <interactions> nodes go after part_b

    // We will need to read the xml doc model and find out if it contains
    // items that were previously created by the sco, such as
    // objectives/interactions
    // Once we know this we can dynamically add those element together with the
    // the values that appear in every cmi model to create a complete list.
    // this array will contain ALL cmi element that appear in this sco model
    protected String[] _cmiValuesForThisDoc;

    // some static variables
    protected static final String OBJECTIVES_COUNT = "cmi.objectives._count";
    protected static final String INTERACTIONS_COUNT = "cmi.interactions._count";

    protected String _totalTimeHolder;
    private final ISettingsHandler settings;

    /**
     * Default constuctor
     */
    public ScoDocument(final ISettingsHandler settings) {
        this.settings = settings;
    }

    /**
     * A method to load into JDOM, a particular sco model given the item identifier.
     * 
     * @param scoID
     *            - the Item identifier for a sco
     */
    public void loadDocument(final String scoID) {
        /*
         * we need to figure out the file reference from the scoId. When a sco was originally imported an xmlfile representing the item was created, so we need to load
         * that document in. If it isn't there for some reason, then we've got a problem.
         */
        final File pathToNavFile = settings.getScoDataModelFile(scoID);
        // Make sure its there
        if (pathToNavFile.exists()) {
            try {
                super.loadDocument(pathToNavFile);

                final Element root = getDocument().getRootElement();

                // set these now, so they are easy to get to when we need them later.
                _userId = getElement(root, "cmi.core.student_id").getText();
                _userName = getElement(root, "cmi.core.student_name").getText();
                _lesson_location = getElement(root, "cmi.core.lesson_location").getText();
                _max_time_allowed = getElement(root, "cmi.student_data.max_time_allowed").getText();
                _time_limit_action = getElement(root, "cmi.student_data.time_limit_action").getText();
                _mastery_score = getElement(root, "cmi.student_data.mastery_score").getText();
                _data_from_lms = getElement(root, "cmi.launch_data").getText();
                _lesson_mode = getElement(root, "cmi.core.lesson_mode").getText();
                _credit_mode = getElement(root, "cmi.core.credit").getText();
                // ** If a user failed a sco, then they get a new clean slate of data
                // so here we will flag this...
                if (getElement(root, "cmi.core.lesson_status").getText().equals("failed")) {
                    // remember the total time.
                    _totalTimeHolder = getElement(root, "cmi.core.total_time").getText();
                    isFailed = true;
                } else {
                    isFailed = false;
                }
            } catch (final Exception ex) {
                throw new OLATRuntimeException(this.getClass(), "error: could not load sco model for " + scoID, ex);
            }
        } else {
            throw new OLATRuntimeException(this.getClass(), "error: could not find sco model for " + scoID, null);
        }
    }

    /**
     * getScoModel() - This method searches the CMI xml file that was created when a scorm package was imported. It first gets all part_a elements that appear in every
     * sco file. It then looks for any <objective> elements, then looks for part_b elements. Finally it then looks for any <interactions>. All of these values are added
     * to a vector (in the correct order). The vector is then copied into a 2_D array, the first index representing an element/value combination. The second index of this
     * 2-D array allows us to get at our name/value combinations (i.e. the second index only contains 2 elements 0 - being the element name, 1 being its value)
     * 
     * @return - a 2-D array of name value pairs.
     */
    public String[][] getScoModel() {
        // if a sco was previously failed, then generate a clean model first...
        if (isFailed) {
            try {
                setDocument(formatCleanScoModel());
                // need to set the total time here
                saveDocument();
            } catch (final IOException ex) {
                throw new OLATRuntimeException(this.getClass(), "Error: could not reset sco to its original state", ex);
            }
        }

        final Vector allElements = new Vector();
        // first add all part A cmi model components...
        for (int i = 0; i < _cmivalues_a.length; i++) {
            final String[] _cmicomponents = new String[2];
            _cmicomponents[0] = _cmivalues_a[i];
            _cmicomponents[1] = getElement(getDocument().getRootElement(), _cmivalues_a[i]).getText();
            allElements.add(_cmicomponents);
        }

        // next find if there are any objectives in the model
        final Element objectivesCount = getElement(getDocument().getRootElement(), OBJECTIVES_COUNT);
        if (objectivesCount != null) {
            final int noOfObjectives = Integer.parseInt(objectivesCount.getText());
            final String[] _cmiobjectivescount = new String[2];
            _cmiobjectivescount[0] = OBJECTIVES_COUNT;
            _cmiobjectivescount[1] = objectivesCount.getText();
            allElements.add(_cmiobjectivescount);
            if (noOfObjectives > 0) {
                allElements.addAll(getObjectivesNodesFromModel(noOfObjectives));
            }
        }

        // next add all part B cmi model components...
        for (int i = 0; i < _cmivalues_b.length; i++) {
            final String[] _cmicomponents = new String[2];
            _cmicomponents[0] = _cmivalues_b[i];
            _cmicomponents[1] = getElement(getDocument().getRootElement(), _cmivalues_b[i]).getText();
            allElements.add(_cmicomponents);
        }

        // next find if there are any interaction in the model
        final Element interactionsCount = getElement(getDocument().getRootElement(), INTERACTIONS_COUNT);
        if (interactionsCount != null) {
            final int noOfInteractions = Integer.parseInt(interactionsCount.getText());
            final String[] _cmiinteractionscount = new String[2];
            _cmiinteractionscount[0] = INTERACTIONS_COUNT;
            _cmiinteractionscount[1] = interactionsCount.getText();
            allElements.add(_cmiinteractionscount);
            if (noOfInteractions > 0) {
                allElements.addAll(getInteractionsNodesFromModel());
            }
        }

        // finally copy the vector into our _cmiValuesForThisDoc string array
        // above so a complete list for this sco is available
        final String[][] _cmiValuesForThisDoc = new String[allElements.size()][2];
        allElements.copyInto(_cmiValuesForThisDoc);
        // check for updates that may be needed in datamodel
        return doFinalPreUpdate(_cmiValuesForThisDoc, true);
    }

    /**
     * Method to get all interaction & children nodes from our JDOM datamodel doc
     * 
     * @return a vector containing a 2-d array of name/values for interactions
     */
    private Vector getInteractionsNodesFromModel() {
        final Vector interactionsElements = new Vector();
        final Element id = getElement(getDocument().getRootElement(), "cmi.interactions");
        final List interactionList = id.getChildren("interaction");
        final Iterator listElement = interactionList.iterator();
        while (listElement.hasNext()) {
            final Element anInteraction = (Element) listElement.next();
            final String interactionIndex = anInteraction.getAttributeValue("index");

            final String[] interactionIDNameValue = new String[2];
            interactionIDNameValue[0] = "cmi.interactions." + interactionIndex + ".id";
            interactionIDNameValue[1] = anInteraction.getChild("id").getText();
            interactionsElements.add(interactionIDNameValue);

            // objectives
            final Element interactionObjectives = anInteraction.getChild("objectives");
            if (interactionObjectives != null) {
                final List objectiveList = interactionObjectives.getChildren("objective");
                if (objectiveList != null) {
                    final Iterator objListElement = objectiveList.iterator();
                    while (objListElement.hasNext()) {
                        final Element anObjective = (Element) objListElement.next();
                        final String objectiveIndex = anObjective.getAttributeValue("index");
                        final String[] interactionObjectiveID = new String[2];
                        interactionObjectiveID[0] = "cmi.interactions." + interactionIndex + ".objectives." + objectiveIndex + ".id";
                        interactionObjectiveID[1] = anObjective.getChild("id").getText();
                        interactionsElements.add(interactionObjectiveID);
                    }
                }
            }

            final String[] interactionTimeNameValue = new String[2];
            interactionTimeNameValue[0] = "cmi.interactions." + interactionIndex + ".time";
            interactionTimeNameValue[1] = anInteraction.getChild("time").getText();
            interactionsElements.add(interactionTimeNameValue);

            final String[] interactionTypeNameValue = new String[2];
            interactionTypeNameValue[0] = "cmi.interactions." + interactionIndex + ".type";
            interactionTypeNameValue[1] = anInteraction.getChild("type").getText();
            interactionsElements.add(interactionTypeNameValue);

            // correct_responses

            final Element interactionCorrectResponses = anInteraction.getChild("correct_responses");
            if (interactionCorrectResponses != null) {
                final List correctResponseList = interactionCorrectResponses.getChildren("correct_response");
                if (correctResponseList != null) {
                    final Iterator correctResponseListElement = correctResponseList.iterator();
                    while (correctResponseListElement.hasNext()) {
                        final Element aCorrectResponse = (Element) correctResponseListElement.next();
                        final String correctResponseIndex = aCorrectResponse.getAttributeValue("index");
                        final String[] correctResponse = new String[2];
                        correctResponse[0] = "cmi.interactions." + interactionIndex + ".correct_responses." + correctResponseIndex + ".pattern";
                        correctResponse[1] = aCorrectResponse.getChild("pattern").getText();
                        interactionsElements.add(correctResponse);
                    }
                }
            }

            final String[] interactionWeightingNameValue = new String[2];
            interactionWeightingNameValue[0] = "cmi.interactions." + interactionIndex + ".weighting";
            interactionWeightingNameValue[1] = anInteraction.getChild("weighting").getText();
            interactionsElements.add(interactionWeightingNameValue);

            final String[] interactionStudentResponseNameValue = new String[2];
            interactionStudentResponseNameValue[0] = "cmi.interactions." + interactionIndex + ".student_response";
            interactionStudentResponseNameValue[1] = anInteraction.getChild("student_response").getText();
            interactionsElements.add(interactionStudentResponseNameValue);

            final String[] interactionResultNameValue = new String[2];
            interactionResultNameValue[0] = "cmi.interactions." + interactionIndex + ".result";
            interactionResultNameValue[1] = anInteraction.getChild("result").getText();
            interactionsElements.add(interactionResultNameValue);

            final String[] interactionLatencyNameValue = new String[2];
            interactionLatencyNameValue[0] = "cmi.interactions." + interactionIndex + ".latency";
            interactionLatencyNameValue[1] = anInteraction.getChild("latency").getText();
            interactionsElements.add(interactionLatencyNameValue);
        }
        return interactionsElements;
    }

    /**
     * getObjectivesNodesFromModel - searches the CMI xml model for a particular sco for <objective> elements, looping around "cmi.objectives.n" times where 'n' is the
     * number of objectives declared in the "cmi.objectives._count" element of the model. These element/values are stored as 2-D arrays and copied to a vector.
     * 
     * @param numberOfObjectives
     * @return objectives nodes
     * @returns a vector of obectives found in model
     */
    private Vector getObjectivesNodesFromModel(final int numberOfObjectives) {
        final Vector objectiveElements = new Vector();
        final Element id = getElement(getDocument().getRootElement(), "cmi.objectives");
        final List objectiveList = id.getChildren("objective");
        final Iterator listElement = objectiveList.iterator();
        while (listElement.hasNext()) {
            final Element anObjective = (Element) listElement.next();
            final String objectiveIndex = anObjective.getAttributeValue("index");
            final Element score = anObjective.getChild("score");

            final String[] objectiveIDNameValue = new String[2];
            objectiveIDNameValue[0] = "cmi.objectives." + objectiveIndex + ".id";
            objectiveIDNameValue[1] = anObjective.getChild("id").getText();
            objectiveElements.add(objectiveIDNameValue);

            final String[] objectiveScoreRaw = new String[2];
            objectiveScoreRaw[0] = "cmi.objectives." + objectiveIndex + ".score.raw";
            objectiveScoreRaw[1] = score.getChild("raw").getText();
            objectiveElements.add(objectiveScoreRaw);

            final String[] objectiveScoreMax = new String[2];
            objectiveScoreMax[0] = "cmi.objectives." + objectiveIndex + ".score.max";
            objectiveScoreMax[1] = score.getChild("max").getText();
            objectiveElements.add(objectiveScoreMax);

            final String[] objectiveScoreMin = new String[2];
            objectiveScoreMin[0] = "cmi.objectives." + objectiveIndex + ".score.min";
            objectiveScoreMin[1] = score.getChild("min").getText();
            objectiveElements.add(objectiveScoreMin);

            final String[] objectiveStatus = new String[2];
            objectiveStatus[0] = "cmi.objectives." + objectiveIndex + ".status";
            objectiveStatus[1] = anObjective.getChild("status").getText();
            objectiveElements.add(objectiveStatus);
        }
        final String[] objectiveCount = new String[2];
        objectiveCount[0] = OBJECTIVES_COUNT;
        objectiveCount[1] = String.valueOf(numberOfObjectives);
        objectiveElements.add(objectiveCount);
        return objectiveElements;
    }

    /**
     * getElement. A utility method which takes a JDOM element (usually root) and a "cmi" property name (i.e. "cmi.core.student_id") to search for as parameter. The
     * method spilts the cmi value into individual parts and searches down the tree until the correct JDOM element is found. If the element does not exist in the model,
     * then null is returned.
     * 
     * @param element
     * @param propertyName
     * @return the searched element
     */
    public Element getElement(final Element element, final String propertyName) {
        Element current = element;
        final String[] cmiBits = propertyName.split("\\.");
        for (int i = 1; i < cmiBits.length; i++) {
            final Element next = current.getChild(cmiBits[i]);
            if (next != null) {
                current = next;
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * Method to allow us to make changes to the model (if we need to) before it is saved back to disk & also changes to be made once loaded
     * 
     * @param scoElementsPreUpdate
     *            - 2D array of all name/value pairs
     * @param loadFlag
     * @return 2D Array of all name/value pairs
     */
    public String[][] doFinalPreUpdate(final String[][] scoElementsPreUpdate, final boolean loadFlag) {
        // Keying the elements into a map will make this easier
        final Map<String, String> keyedElements = new HashMap<String, String>();
        for (int i = 0; i < scoElementsPreUpdate.length; i++) {
            keyedElements.put(scoElementsPreUpdate[i][0], scoElementsPreUpdate[i][1]);
        }
        // ***********************************************************************
        // *** bug fix 2003-11-12
        // We need to check to see if theres anything in cmi.core.session_time
        // before
        // we call "addTimes"
        // first we'll update the total_time
        if (loadFlag == false) {
            if (keyedElements.containsKey("cmi.core.session_time") && !keyedElements.get("cmi.core.session_time").equals("")) {
                final String totalTime = ScoUtils.addTimes(keyedElements.get("cmi.core.total_time"), keyedElements.get("cmi.core.session_time"));
                keyedElements.put("cmi.core.total_time", totalTime);
            }
        }
        if (loadFlag == true) {
            String exitStatus = keyedElements.get("cmi.core.exit");
            // now update the entry for next time...
            String entryStatus = "";
            /*
             * Bugfix 2005-08-'8 Guido only "suspend" sets entryStatus to "resume" "logout" sets it to "". Not as written in the spec but the testsuite depends on it.
             */
            if (exitStatus.equals("suspend") || exitStatus.equals("logout")) {
                if (exitStatus.equals("suspend")) {
                    entryStatus = "resume";
                }
                if (exitStatus.equals("logout")) {
                    entryStatus = "";
                }
                exitStatus = "";
                keyedElements.put("cmi.core.entry", entryStatus);
                keyedElements.put("cmi.core.exit", exitStatus);
            }
        }
        // **********************************************************************
        // create a 2-d array to pass back
        final String[][] finalArray = new String[keyedElements.size()][2];
        // finally copy all the elements back into the 2d array and return..
        int i = 0;
        for (final Map.Entry<String, String> entry : keyedElements.entrySet()) {
            finalArray[i][0] = entry.getKey();
            finalArray[i][1] = entry.getValue();
            i++;
        }
        keyedElements.clear();
        return finalArray;
    }

    /**
     * doLmsCommit. This method takes the Javascript cmi model and attempts to update the CMI xml sco file. It takes the javascript model as a 2-D array of name/value
     * pairs, finds the element in the JDOM model, updates it with the new value and then writes the new model back to disk.
     * 
     * @param scoElementsPreUpdate
     */
    public void doLmsCommit(final String[][] scoElementsPreUpdate) {
        // System.out.println("reloadScoDocuement - dolmscommit is called");
        final String[][] scoElements = doFinalPreUpdate(scoElementsPreUpdate, false);
        final Vector objectives = new Vector();
        final Vector interactions = new Vector();
        int objectivesCount = 0;
        int interactionsCount = 0;

        for (int i = 0; i < scoElements.length; i++) {
            if (scoElements[i][0].startsWith("cmi.objectives")) {
                if (scoElements[i][0].equals("cmi.objectives._count")) {
                    objectivesCount = Integer.parseInt(scoElements[i][1]);
                } else {
                    objectives.add(scoElements[i]);
                }
            } else if (scoElements[i][0].startsWith("cmi.interactions")) {
                if (scoElements[i][0].equals("cmi.interactions._count")) {
                    interactionsCount = Integer.parseInt(scoElements[i][1]);
                } else {
                    interactions.add(scoElements[i]);
                }
            } else {
                final Element itemToChange = getElement(getDocument().getRootElement(), scoElements[i][0]);
                if (itemToChange != null) {
                    itemToChange.setText(scoElements[i][1]);
                }
            }
        }
        // update the objectives
        final String[][] objectivesArray = new String[objectives.size()][2];
        objectives.copyInto(objectivesArray);
        if (objectivesCount > 0) {
            dealWithSavingObjectives(objectivesCount, objectivesArray);
        }
        objectives.clear();
        // update the interactions
        final String[][] interactionsArray = new String[interactions.size()][2];
        interactions.copyInto(interactionsArray);
        if (interactionsCount > 0) {
            dealWithSavingInteractions(interactionsCount, interactionsArray);
        }
        interactions.clear();
        // finally commit this back to disk
        try {
            saveDocument();
        } catch (final IOException ex) {
            throw new OLATRuntimeException(this.getClass(), "Error: could not save sco model:", ex);
        }
    }

    /**
     * Method to clear a sco back to its original state - this would happen if the sco set lesson_status to "failed"
     * 
     * @return a new JDOM doc
     */
    public Document formatCleanScoModel() {
        final CMI_DataModel cleanData = new CMI_DataModel(_userId, _userName, _max_time_allowed, _time_limit_action, _data_from_lms, _mastery_score, _lesson_mode,
                _credit_mode);
        cleanData.buildFreshModel();
        final Document theModel = cleanData.getModel();
        cleanData.setDocument(theModel);
        return cleanData.getDocument();
    }

    /**
     * Method to update our cmi datamodel with interaction elements that were created at runtime by a sco. Before the sco is taken, the xml model will not contain any
     * <interaction> nodes. So the important job is to look at the values provided by the javascript model and ascertain if we need to create any new JDOM nodes. Once any
     * new nodes are created, we can then (using a hashtable) use keys to update each node in the document. If the node did not exist in the xml doc, then there would be
     * problems, possibly lost information.
     * 
     * @param count
     *            - the number of interactions (obtained from _count)
     * @param interactions
     *            - the 2d array of name/value pairs
     */
    public void dealWithSavingInteractions(final int count, final String[][] interactions) {
        // first find out how many interactions there are already
        final int interactionsCount = Integer.parseInt(getElement(getDocument().getRootElement(), "cmi.interactions._count").getText());

        // Keying the elements into a hashtable will make this easier
        final Hashtable keyedInteractions = new Hashtable();
        for (int i = 0; i < interactions.length; i++) {
            keyedInteractions.put(interactions[i][0], interactions[i][1]);
        }

        // if there are more elements in the javascript model, then we need
        // to create the same number of <interaction> nodes in our JDOM document...
        if (count > interactionsCount) {
            for (int i = interactionsCount; i < count; i++) {

                final int noOfObjectives = Integer.parseInt((String) keyedInteractions.get("cmi.interactions." + i + ".objectives._count"));
                final String interaction = (String) keyedInteractions.get("cmi.interactions." + i + ".correct_responses._count");
                // added fix because reload code depends on the
                // xx.correct_responses._count if objectives._count is set.
                int noOfCorrectResponses = 0;
                if (interaction != null) {
                    noOfCorrectResponses = Integer.parseInt(interaction);
                }
                createNewInteraction(i, noOfObjectives, noOfCorrectResponses);
            }
        }
        // ****** need to make sure existing ones have right amount of objectives &
        // correct_responses

        // now that the correct number of JDOM nodes exist in the model, we
        // can start to update each node with the data contained in our hashtable
        final Element interactionElement = getElement(getDocument().getRootElement(), "cmi.interactions");
        final List interactionList = interactionElement.getChildren("interaction");
        final Iterator listElement = interactionList.iterator();
        while (listElement.hasNext()) {
            final Element anInteraction = (Element) listElement.next();
            final String interactionIndex = anInteraction.getAttributeValue("index");
            // set the ID for this interaction
            anInteraction.getChild("id").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".id"));
            // next we need to update 'n' number of of <cmi.interactions.n.objective>
            // nodes
            final Element objectives = anInteraction.getChild("objectives");
            // get the list of <objective> nodes
            final List objList = objectives.getChildren("objective");
            final Iterator objListElement = objList.iterator();
            // now go thru all of the child <objective> nodes and update them
            while (objListElement.hasNext()) {
                final Element anObjective = (Element) objListElement.next();
                // find the correct index for this node
                final String objectiveIndex = anObjective.getAttributeValue("index");
                // now use that index to get the correct key from the hashtable
                // and update the correct JDOM node in our model
                anObjective.getChild("id").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".objectives." + objectiveIndex + ".id"));
            }
            // update the time
            anInteraction.getChild("time").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".time"));
            // update the type
            anInteraction.getChild("type").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".type"));

            // correct_responses
            final Element correct_responses = anInteraction.getChild("correct_responses");
            final List crList = correct_responses.getChildren("correct_response");
            final Iterator crListElement = crList.iterator();
            // now go thru all of the child <correct_response> nodes and update them
            while (crListElement.hasNext()) {
                final Element aCorrectResponse = (Element) crListElement.next();
                final String crIndex = aCorrectResponse.getAttributeValue("index");
                aCorrectResponse.getChild("pattern").setText(
                        (String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".correct_responses." + crIndex + ".pattern"));
            }

            anInteraction.getChild("weighting").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".weighting"));
            anInteraction.getChild("student_response").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".student_response"));
            anInteraction.getChild("result").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".result"));
            anInteraction.getChild("latency").setText((String) keyedInteractions.get("cmi.interactions." + interactionIndex + ".latency"));
        }
        // once finished, update the <_count> to reflect the changes
        getElement(getDocument().getRootElement(), "cmi.interactions._count").setText(count + "");
    }

    /**
     * Method to update our cmi datamodel with objective elements that were created at runtime by a sco. Before the sco is taken, the xml model will not contain any
     * <objective> nodes. So the important job is to look at the values provided by the javascript model and ascertain if we need to create any new JDOM nodes. Once any
     * new nodes are created, we can then (using a hashtable) use keys to update each node in the document. If the node did not exist in the xml doc, then there would be
     * problems, possibly lost information.
     * 
     * @param count
     *            - the number of objectives found in <_count>
     * @param objectives
     *            - the name/value pairs of objective info
     */
    public void dealWithSavingObjectives(final int count, final String[][] objectives) {
        // first find out how many objectives there are already
        final int objectivesCount = Integer.parseInt(getElement(getDocument().getRootElement(), "cmi.objectives._count").getText());

        // if there are more elements in the javascript model, then we need
        // to create the same number of <objective> nodes in our JDOM document...
        if (count > objectivesCount) {
            for (int i = objectivesCount; i < count; i++) {
                createNewObjective(i);
            }
        }
        // next update the elements...
        // Keying the elements into a hashtable will make this easier
        final Hashtable keyedObjectives = new Hashtable();
        for (int i = 0; i < objectives.length; i++) {
            keyedObjectives.put(objectives[i][0], objectives[i][1]);
        }
        final Element id = getElement(getDocument().getRootElement(), "cmi.objectives");
        final List objectiveList = id.getChildren("objective");
        final Iterator listElement = objectiveList.iterator();
        while (listElement.hasNext()) {
            final Element anObjective = (Element) listElement.next();
            final String objectiveIndex = anObjective.getAttributeValue("index");
            // set the ID for this objective
            anObjective.getChild("id").setText((String) keyedObjectives.get("cmi.objectives." + objectiveIndex + ".id"));
            // now move onto the score
            final Element score = anObjective.getChild("score");
            // next update the children of <score>
            score.getChild("raw").setText((String) keyedObjectives.get("cmi.objectives." + objectiveIndex + ".score.raw"));
            score.getChild("max").setText((String) keyedObjectives.get("cmi.objectives." + objectiveIndex + ".score.max"));
            score.getChild("min").setText((String) keyedObjectives.get("cmi.objectives." + objectiveIndex + ".score.min"));
            // finally update <status>
            anObjective.getChild("status").setText((String) keyedObjectives.get("cmi.objectives." + objectiveIndex + ".status"));
        }
        // once finished, update the <_count> to reflect the changes
        getElement(getDocument().getRootElement(), "cmi.objectives._count").setText(count + "");
    }

    /**
     * createNewInteraction() - Creates a new <interaction> element structure in the xml CMI datamodel for a particular sco.
     * 
     * @param index
     *            - the index for the <interaction> to be created
     * @param noOfObjectives
     *            - the number of objective nodes to create
     * @param noOfCorrectResponses
     *            - the number of correct_responses nodes to create
     */
    private void createNewInteraction(final int index, final int noOfObjectives, final int noOfCorrectResponses) {
        // first get hold of the <interactions> node
        final Element ints = getElement(getDocument().getRootElement(), "cmi.interactions");
        // and then add our <interaction index=""> node
        final Element interaction = new Element("interaction");
        interaction.setAttribute("index", index + "");
        ints.addContent(interaction);
        // add <id>
        final Element id = new Element("id");
        interaction.addContent(id);

        // add <objectives>
        final Element objectives = new Element("objectives");
        interaction.addContent(objectives);
        // add <_count> to <objectives>
        final Element _count = new Element("_count");
        _count.setText(noOfObjectives + "");
        objectives.addContent(_count);

        // add all the objectives needed
        for (int i = 0; i < noOfObjectives; i++) {
            final Element objectiveItem = new Element("objective");
            objectiveItem.setAttribute("index", i + "");
            final Element objID = new Element("id");
            objectiveItem.addContent(objID);
            // now add this lot to the <objectives> node
            objectives.addContent(objectiveItem);
        }

        // add <time>
        final Element time = new Element("time");
        interaction.addContent(time);
        // add <type>
        final Element type = new Element("type");
        interaction.addContent(type);

        // add <correct_responses>
        final Element correct_responses = new Element("correct_responses");
        interaction.addContent(correct_responses);
        // add <_count> to <correct_responses>
        final Element _countCR = new Element("_count");
        _countCR.setText(noOfCorrectResponses + "");
        correct_responses.addContent(_countCR);

        // add all the correct_responses needed
        for (int i = 0; i < noOfCorrectResponses; i++) {
            final Element correctResponseItem = new Element("correct_response");
            correctResponseItem.setAttribute("index", i + "");
            final Element pattern = new Element("pattern");
            correctResponseItem.addContent(pattern);
            // now add this lot to the <correct_responses> node
            correct_responses.addContent(correctResponseItem);
        }

        // add <weighting>
        final Element weighting = new Element("weighting");
        interaction.addContent(weighting);
        // add <student_response>
        final Element student_response = new Element("student_response");
        interaction.addContent(student_response);
        // add <result>
        final Element result = new Element("result");
        interaction.addContent(result);
        // add <latency>
        final Element latency = new Element("latency");
        interaction.addContent(latency);
    }

    /**
     * createNewObjective() - Creates a new <objective> element structure in the xml CMI datamodel for a particular sco.
     * 
     * @param index
     *            - the index for the <objective> to be created
     */
    private void createNewObjective(final int index) {
        // first get hold of the <objectives> node
        final Element objs = getElement(getDocument().getRootElement(), "cmi.objectives");
        // and then add our <objective index=""> node
        final Element objective = new Element("objective");
        objective.setAttribute("index", index + "");
        objs.addContent(objective);
        // add <id>
        final Element id = new Element("id");
        objective.addContent(id);
        // add <score>
        final Element score = new Element("score");
        objective.addContent(score);
        // add <_children>
        final Element _children = new Element("_children");
        _children.setText("raw,max,min");
        score.addContent(_children);
        // add <raw>
        final Element raw = new Element("raw");
        score.addContent(raw);
        // add <max>
        final Element max = new Element("max");
        score.addContent(max);
        // add <min>
        final Element min = new Element("min");
        score.addContent(min);
        // add <status>
        final Element status = new Element("status");
        objective.addContent(status);
    }
}
