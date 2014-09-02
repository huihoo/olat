/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.ims.qti.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.olat.lms.ims.qti.editor.QTIEditHelperEBL;
import org.olat.system.commons.CodeHelper;

/**
 * @author rkulow
 */
public class Section implements QTIObject {

    // Assesment Attributes
    private String ident = null; // required, max 256 chars
    private String title = null; // optional, max 256 chars

    // Elements
    private QTIObject qticomment = null; // occurs 0 ore 1 time
    private QTIObject duration = null; // occurs 0 ore 1 time
    private List qtimetadatas = null; // occurs 0 to many
    private List sectionprecondition = null; // occurs 0 to many
    private List sectionpostcondition = null; // occurs 0 to many
    private String objectives = null; // occurs 0 to many, 1st if available is processed
    private List rubrics = null; // occurs 0 to many
    private List sectioncontrols = new ArrayList(); // occurs 0 to many
    private QTIObject presentation_material = null; // occurs 0 to 1 time
    private QTIObject outcomes_processing = null; // ?
    private QTIObject sectionproc_extension = null; // occurs 0 to 1 time
    private List sectionfeedbacks = new ArrayList(); // occurs 0 to many
    private SelectionOrdering selection_ordering = null; // ?
    private QTIObject reference = null; // occurs 0 to 1 time
    private List sections = null; // occurs 0 to 1 time ( sections and section_references)
    private List<Item> items = new ArrayList<Item>(); // occurs 0 to many (items and item_references)
    private boolean alienItems = false;

    public Section() {
        setIdent(String.valueOf(CodeHelper.getRAMUniqueID()));
        setTitle("New Section");
        getSectioncontrols().add(new Control());
        setSelection_ordering(new SelectionOrdering());
    }

    /**
	 */
    @Override
    public void addToElement(final Element root) {
        // SECTION
        final Element section = root.addElement("section");
        section.addAttribute("ident", this.ident);
        section.addAttribute("title", this.title);

        // DURATION
        final QTIObject obj_duration = this.getDuration();
        if (obj_duration != null) {
            obj_duration.addToElement(section);
        }

        // OBJECTIVES
        QTIEditHelperEBL.addObjectives(section, objectives);

        // SECTIONCONTROL
        for (final Iterator i = this.sectioncontrols.iterator(); i.hasNext();) {
            final QTIObject obj = (QTIObject) i.next();
            if (obj != null) {
                obj.addToElement(section);
            }
        }

        // OUTCOMES_PROCESSING
        final QTIObject obj_outcomes_processing = this.getOutcomes_processing();
        if (obj_outcomes_processing != null) {
            obj_outcomes_processing.addToElement(section);
        }

        // SECTIONFEEDBACK
        for (final Iterator i = this.sectionfeedbacks.iterator(); i.hasNext();) {
            final QTIObject obj = (QTIObject) i.next();
            if (obj != null) {
                obj.addToElement(section);
            }
        }

        // SELECTION ORDERING
        final SelectionOrdering selectionOrdering = this.getSelection_ordering();
        if (selectionOrdering != null) {
            selectionOrdering.addToElement(section);
        }

        // ITEMS
        for (final Iterator i = this.items.iterator(); i.hasNext();) {
            final QTIObject obj = (QTIObject) i.next();
            if (obj != null) {
                obj.addToElement(section);
            }
        }

    }

    public boolean checkAlienItems() {
        alienItems = false;
        for (final Iterator iter = items.iterator(); iter.hasNext();) {
            final Item element = (Item) iter.next();
            alienItems = alienItems || element.isAlient();
        }
        return alienItems;
    }

    public boolean hasAlienItems() {
        return alienItems;
    }

    /**
     * Returns the duration.
     * 
     * @return QTIObject
     */
    public QTIObject getDuration() {
        return duration;
    }

    /**
     * Returns the ident.
     * 
     * @return String
     */
    public String getIdent() {
        return ident;
    }

    /**
     * Returns the items.
     * 
     * @return List
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Returns the objectives.
     * 
     * @return List
     */
    public String getObjectives() {
        return objectives;
    }

    /**
     * Returns the outcomes_processing.
     * 
     * @return QTIObject
     */
    public QTIObject getOutcomes_processing() {
        return outcomes_processing;
    }

    /**
     * Returns the presentation_material.
     * 
     * @return QTIObject
     */
    public QTIObject getPresentation_material() {
        return presentation_material;
    }

    /**
     * Returns the qticomment.
     * 
     * @return QTIObject
     */
    public QTIObject getQticomment() {
        return qticomment;
    }

    /**
     * Returns the qtimetadatas.
     * 
     * @return List
     */
    public List getQtimetadatas() {
        return qtimetadatas;
    }

    /**
     * Returns the reference.
     * 
     * @return QTIObject
     */
    public QTIObject getReference() {
        return reference;
    }

    /**
     * Returns the rubrics.
     * 
     * @return List
     */
    public List getRubrics() {
        return rubrics;
    }

    /**
     * Returns the sectioncontrols.
     * 
     * @return List
     */
    public List getSectioncontrols() {
        return sectioncontrols;
    }

    /**
     * Returns the sectionfeedbacks.
     * 
     * @return List
     */
    public List getSectionfeedbacks() {
        return sectionfeedbacks;
    }

    /**
     * Returns the sectionpostcondition.
     * 
     * @return List
     */
    public List getSectionpostcondition() {
        return sectionpostcondition;
    }

    /**
     * Returns the sectionprecondition.
     * 
     * @return List
     */
    public List getSectionprecondition() {
        return sectionprecondition;
    }

    /**
     * Returns the sectionproc_extension.
     * 
     * @return QTIObject
     */
    public QTIObject getSectionproc_extension() {
        return sectionproc_extension;
    }

    /**
     * Returns the sections.
     * 
     * @return List
     */
    public List getSections() {
        return sections;
    }

    /**
     * Returns the selection_ordering.
     * 
     * @return SelectionOrdering
     */
    public SelectionOrdering getSelection_ordering() {
        return selection_ordering;
    }

    /**
     * Returns the title.
     * 
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the duration.
     * 
     * @param duration
     *            The duration to set
     */
    public void setDuration(final QTIObject duration) {
        this.duration = duration;
    }

    /**
     * Sets the ident.
     * 
     * @param ident
     *            The ident to set
     */
    public void setIdent(final String ident) {
        this.ident = ident;
    }

    /**
     * Sets the items.
     * 
     * @param items
     *            The items to set
     */
    public void setItems(final List items) {
        this.items = items;
        checkAlienItems();
    }

    /**
     * Sets the objectives.
     * 
     * @param objectives
     *            The objectives to set
     */
    public void setObjectives(final String objectives) {
        this.objectives = objectives;
    }

    /**
     * Sets the outcomes_processing.
     * 
     * @param outcomes_processing
     *            The outcomes_processing to set
     */
    public void setOutcomes_processing(final QTIObject outcomes_processing) {
        this.outcomes_processing = outcomes_processing;
    }

    /**
     * Sets the presentation_material.
     * 
     * @param presentation_material
     *            The presentation_material to set
     */
    public void setPresentation_material(final QTIObject presentation_material) {
        this.presentation_material = presentation_material;
    }

    /**
     * Sets the qticomment.
     * 
     * @param qticomment
     *            The qticomment to set
     */
    public void setQticomment(final QTIObject qticomment) {
        this.qticomment = qticomment;
    }

    /**
     * Sets the qtimetadatas.
     * 
     * @param qtimetadatas
     *            The qtimetadatas to set
     */
    public void setQtimetadatas(final List qtimetadatas) {
        this.qtimetadatas = qtimetadatas;
    }

    /**
     * Sets the reference.
     * 
     * @param reference
     *            The reference to set
     */
    public void setReference(final QTIObject reference) {
        this.reference = reference;
    }

    /**
     * Sets the rubrics.
     * 
     * @param rubrics
     *            The rubrics to set
     */
    public void setRubrics(final List rubrics) {
        this.rubrics = rubrics;
    }

    /**
     * Sets the sectioncontrols.
     * 
     * @param sectioncontrols
     *            The sectioncontrols to set
     */
    public void setSectioncontrols(final List sectioncontrols) {
        this.sectioncontrols = sectioncontrols;
    }

    /**
     * Sets the sectionfeedbacks.
     * 
     * @param sectionfeedbacks
     *            The sectionfeedbacks to set
     */
    public void setSectionfeedbacks(final List sectionfeedbacks) {
        this.sectionfeedbacks = sectionfeedbacks;
    }

    /**
     * Sets the sectionpostcondition.
     * 
     * @param sectionpostcondition
     *            The sectionpostcondition to set
     */
    public void setSectionpostcondition(final List sectionpostcondition) {
        this.sectionpostcondition = sectionpostcondition;
    }

    /**
     * Sets the sectionprecondition.
     * 
     * @param sectionprecondition
     *            The sectionprecondition to set
     */
    public void setSectionprecondition(final List sectionprecondition) {
        this.sectionprecondition = sectionprecondition;
    }

    /**
     * Sets the sectionproc_extension.
     * 
     * @param sectionproc_extension
     *            The sectionproc_extension to set
     */
    public void setSectionproc_extension(final QTIObject sectionproc_extension) {
        this.sectionproc_extension = sectionproc_extension;
    }

    /**
     * Sets the sections.
     * 
     * @param sections
     *            The sections to set
     */
    public void setSections(final List sections) {
        this.sections = sections;
    }

    /**
     * Sets the selection_ordering.
     * 
     * @param selection_ordering
     *            The selection_ordering to set
     */
    public void setSelection_ordering(final SelectionOrdering selection_ordering) {
        this.selection_ordering = selection_ordering;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            The title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Checks if this section contains any questions of type 'essay'
     * 
     * @return
     */
    public boolean containsEssayQuestions() {
        for (final Iterator i = this.items.iterator(); i.hasNext();) {
            final Item item = (Item) i.next();
            if (item != null && item.getQuestion().getType() == Question.TYPE_ESSAY) {
                return true;
            }
        }
        return false;
    }

}
