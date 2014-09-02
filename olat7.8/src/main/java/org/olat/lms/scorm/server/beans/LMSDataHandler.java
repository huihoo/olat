package org.olat.lms.scorm.server.beans;

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
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.lms.scorm.ISettingsHandler;
import org.olat.lms.scorm.manager.ScormManager;
import org.olat.lms.scorm.server.sequence.SequenceManager;
import org.olat.lms.scorm.server.servermodels.SequencerModel;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * A handler class which takes an input beans data and decides how to act. It will update a given items state and model and will return the new items data if the user had
 * clicked "next/prev" or an item on the tree.
 * 
 * @author Paul Sharples
 */
public class LMSDataHandler {

    private static final Logger log = LoggerHelper.getLogger();

    protected ScormManager theCourse;

    protected LMSDataFormBean _inputBean;

    protected String status;

    protected boolean isSco = false;

    protected boolean isUpdating = false;

    protected boolean isItemCompleted = false;

    protected boolean isCourseCompleted = false;

    protected boolean hasPrerequisites = false;

    protected String itemId;

    protected String[][] cmiStrings;

    /**
     * Constructor takes an input bean as argument
     * 
     * @param theCourse
     *            an instance to a scorm manager
     * @param inputBean
     */
    public LMSDataHandler(final ScormManager theCourse, final LMSDataFormBean inputBean, final ISettingsHandler settings) {
        _inputBean = inputBean;
        this.theCourse = theCourse;
    }

    /**
     * A method to decide what kind of action to take given the information contained within the bean
     * 
     * @return LMSResultsBean
     */
    public LMSResultsBean getResultsBean() {
        final String lmsAction = _inputBean.getLmsAction();
        if (lmsAction != null) {
            // the user wants a new item - nothing to persist just get the data
            if (lmsAction.equals("get")) {
                return getCMIData(_inputBean.getItemID());
            }
            // the user wants to commit a model and may or may not want the next item
            else if (lmsAction.equals("update")) {
                return updateCMIData(_inputBean.getItemID());
            }
            // the package has been launched for the first time, so find the first
            // item to launch
            else if (lmsAction.equals("boot")) {
                return getCMIData(Integer.toString(getSequenceFromId(findFirstItemToLaunch())));
            }
        }
        return null;
    }

    /**
     * A method to update any CMI info given in the input bean
     * 
     * @param itemIndex
     * @return LMSResultsBean
     */
    public LMSResultsBean updateCMIData(final String itemIndex) {
        itemId = findItemFromIndex(Integer.parseInt(itemIndex));
        // if this is a sco
        if (isItemSco(itemId)) {
            isSco = true;

            if (_inputBean.getDataAsMap() != null) {
                cmiStrings = convertTo2dArray(_inputBean.getDataAsMap());
                updateClientModel(itemId, cmiStrings);
            } else {
                // get the cmi data string into correct format
                cmiStrings = formatCmiDataResults(_inputBean.getData());
                updateClientModel(itemId, cmiStrings);
            }
            // cmiStrings = formatCmiDataResults("");
            // next update the model with the results from browser.
            // updateClientModel(itemId, cmiStrings);
            // update prerequisite table
            addtoPrereqTable(itemId, status, true);
            // If an LMSFinish() was made by the sco then we do not need to update
            // anything client side
            String nextAction = _inputBean.getNextAction();
            // String nextAction = "";
            if (nextAction.equals("none") && !getAutoNav()) {
                isUpdating = false;
            }
            // ELSE IF THE USER HAD CLICKED NEXT/PREV OR A TREE NODE THEN WE NEED TO
            // GET NEXT ITEMs DATA
            else {
                if (getAutoNav()) {
                    // need nextAction to point to next item...
                    nextAction = Integer.toString(getSequenceFromId(findFirstItemToLaunch()));
                }
                isUpdating = true;
                // now get and return new sco
                return getCMIData(nextAction);
            }
        }
        // must be asset
        else {
            isSco = false;
            isUpdating = false;
        }
        // return the updated dataModel
        return new LMSResultsBean(itemId, Boolean.toString(isSco), cmiStrings, Boolean.toString(isUpdating), getPreReqStrings(), Boolean.toString(isItemCompleted),
                Boolean.toString(isCourseCompleted), Boolean.toString(hasPrerequisites));
    }

    /**
     * @param dataAsMap
     * @return a 2d String array
     */
    private String[][] convertTo2dArray(final Map dataAsMap) {
        final String[][] cmiData = new String[dataAsMap.size()][2];
        int j = 0;
        for (final Iterator it = dataAsMap.keySet().iterator(); it.hasNext(); j++) {
            final Object l = it.next();
            final Object r = dataAsMap.get(l);
            cmiData[j][0] = (String) l;
            cmiData[j][1] = (String) r;
            if (l.toString().equals("cmi.core.lesson_status")) {
                status = r.toString();
            }

        }
        return cmiData;
    }

    /**
     * A method to get a new item, based on the values given from the input bean
     * 
     * @param itemIndex
     * @return LMSResultsBean
     */
    public LMSResultsBean getCMIData(final String itemIndex) {
        // HAS THIS COURSE BEEN COMPLETED
        if (itemIndex.equals(Integer.toString(SequenceManager.COURSE_COMPLETED_VALUE))) {
            isCourseCompleted = true;
            generatePrereqBean();
        }
        // COURSE NOT COMPLETED SO CHECK THIS ITEM
        else {
            itemId = findItemFromIndex(Integer.parseInt(itemIndex));
            // has this particular item been completed?
            if (hasItemBeenCompleted(itemId)) {
                isItemCompleted = true;
            }
            // does this item have prerequisites?
            else if (!checkItemsPrerequisites(itemId)) {
                hasPrerequisites = true;
                generatePrereqBean();
            }
            // WE CAN LAUNCH THIS ITEM
            else {
                // if this is a sco
                if (isItemSco(itemId)) {
                    // load this scos model
                    loadinModel(itemId);
                    // get it
                    cmiStrings = getScoModel(itemId);
                    isSco = true;
                    isUpdating = true;
                }
                // must be asset
                else {
                    // Update status of this ASSET to completed
                    addtoPrereqTable(itemId, SequencerModel.ITEM_COMPLETED, true);
                    isSco = false;
                    isUpdating = false;
                }
            }
        }
        return new LMSResultsBean(itemIndex, Boolean.toString(isSco), cmiStrings, Boolean.toString(isUpdating), getPreReqStrings(), Boolean.toString(isItemCompleted),
                Boolean.toString(isCourseCompleted), Boolean.toString(hasPrerequisites));
    }

    /**
     * Format the string containing the cmi data into a nice 2d array.
     * 
     * @param cmiString
     * @return
     */
    protected String[][] formatCmiDataResults(final String cmiString) {
        final String[] cmiBits = cmiString.split("\\^r\\@l\\@ad\\^");
        final String[][] cmiComponents = new String[cmiBits.length][2];

        for (int i = 0; i < cmiBits.length; i++) {
            final String[] cmiNameValue = cmiBits[i].split("\\~r\\@l\\@ad\\~");
            cmiComponents[i][0] = cmiNameValue[0];
            if (cmiNameValue[0].equals("cmi.core.lesson_status")) {
                status = cmiNameValue[1];
            }
            if (cmiNameValue.length > 1) {
                cmiComponents[i][1] = cmiNameValue[1];
            } else {
                cmiComponents[i][1] = "";
            }
            if (log.isDebugEnabled()) {
                log.debug("name: " + cmiComponents[i][0] + "   value:" + cmiComponents[i][1]);
            }
        }
        return cmiComponents;
    }

    protected String[][] getPreReqStrings() {
        final String[][] pTable = getPackageStatus();
        return pTable;
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param index
     * @return
     */
    protected String findItemFromIndex(final int index) {
        return theCourse.getSequence().findItemFromIndex(index);
    }

    protected String[][] getPackageStatus() {
        return theCourse.getSequence().getPackageStatus();
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param item
     * @return
     */
    protected String getTitle(final String item) {
        return theCourse.getSequence().getItem(item).getTitle();
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param item
     * @return
     */
    protected boolean isItemSco(final String item) {
        return theCourse.getSequence().isItemSco(item);
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @return
     */
    protected String findFirstItemToLaunch() {
        return theCourse.getSequence().findFirstItemToLaunch();
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param item
     * @return
     */
    protected int getSequenceFromId(final String item) {
        return theCourse.getSequence().getSequenceFromId(item);
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param item
     * @return
     */
    protected boolean hasItemBeenCompleted(final String item) {
        return theCourse.getSequence().hasItemBeenCompleted(item);
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param item
     * @return
     */
    protected boolean checkItemsPrerequisites(final String item) {
        return theCourse.getSequence().checkItemsPrerequisites(item);
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param itemId
     */
    protected void loadinModel(final String itemId) {
        theCourse.getSequence().getItem(itemId).loadInModel();
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param itemId
     * @return
     */
    protected String[][] getScoModel(final String itemId) {
        return theCourse.getSequence().getItem(itemId).getScoModel();
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param itemId
     * @param itemStatus
     * @param save
     */
    protected void addtoPrereqTable(final String itemId, final String itemStatus, final boolean save) {
        theCourse.getSequence().addtoPrereqTable(itemId, itemStatus, true);
    }

    /**
     * Wrapper method from SequenceManager
     * 
     * @param itemId
     * @param cmiStrings
     */
    protected void updateClientModel(final String itemId, final String[][] cmiStrings) {
        theCourse.getSequence().getItem(itemId).updateClientModel(cmiStrings);
    }

    protected boolean getAutoNav() {
        return theCourse.isAutoProgressionEnabled();
    }

    protected void generatePrereqBean() {
        // not needed
        // HttpSession session = ScormLaunch.getHttpSession();
        // StatusBean sb = new StatusBean(getPreReqStrings());
        // System.out.println("from generatePrereqBean()");
        // String[][] strArr = getPreReqStrings();
        // if(strArr != null){
        // for(int i=0;i<strArr.length;i++){
        // System.out.println("");
        // for(int j=0;j<strArr[i].length;j++){
        // System.out.print(strArr[i][j] + " : ");
        // }
        //
        // }
        // }
        // System.out.println("from generatePrereqBean() end");
        // session.setAttribute("prereqBean", sb);
    }
}
