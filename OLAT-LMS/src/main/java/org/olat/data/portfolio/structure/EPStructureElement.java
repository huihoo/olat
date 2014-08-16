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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.data.portfolio.structure;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.database.PersistentObject;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.portfolio.restriction.CollectRestriction;
import org.olat.data.resource.OLATResource;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * EPStructureElement is the base element in portfolios, can have page or map as children
 * <P>
 * Initial Date: 08.06.2010 <br>
 * 
 * @author rhaag
 */
public class EPStructureElement extends PersistentObject implements PortfolioStructure, OLATResourceable {

    private static final String MORE_TEXT = "...";
    private static final int MAX_LENGTH_PORTFOLIO_DESCRIPTION = 2048 - MORE_TEXT.length();
    private OLATResource olatResource;
    private List<EPStructureToArtefactLink> artefacts;
    private List<EPStructureToStructureLink> children;

    /**
	 * 
	 */
    public EPStructureElement() {
        //
    }

    /**
     * Need for synching
     */
    private Long structureElSource;

    public Long getStructureElSource() {
        return structureElSource;
    }

    public void setStructureElSource(final Long structureElSource) {
        this.structureElSource = structureElSource;
    }

    /**
     * @uml.property name="order"
     */
    private String order;

    /**
     * Getter of the property <tt>order</tt>
     * 
     * @return Returns the order.
     * @uml.property name="order"
     */
    public String getOrder() {
        return order;
    }

    /**
     * Setter of the property <tt>order</tt>
     * 
     * @param order
     *            The order to set.
     * @uml.property name="order"
     */
    public void setOrder(final String order) {
        this.order = order;
    }

    /**
     * @uml.property name="title"
     */
    private String title;

    /**
     * Getter of the property <tt>title</tt>
     * 
     * @return Returns the title.
     * @uml.property name="title"
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Setter of the property <tt>title</tt>
     * 
     * @param title
     *            The title to set.
     * @uml.property name="title"
     */
    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @uml.property name="description"
     */
    private String description;

    /**
     * Getter of the property <tt>description</tt>
     * 
     * @return Returns the description.
     * @uml.property name="description"
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Setter of the property <tt>description</tt>
     * 
     * @param description
     *            The description to set.
     * @uml.property name="description"
     */
    @Override
    public void setDescription(final String description) {
        if (description != null && description.length() > MAX_LENGTH_PORTFOLIO_DESCRIPTION) {
            this.description = description.substring(0, MAX_LENGTH_PORTFOLIO_DESCRIPTION) + MORE_TEXT;
        } else {
            this.description = description;
        }
    }

    @Override
    public String getShortenedDescription() {
        String desc = getDescription();
        if (desc == null) {
            desc = "";
        } else if (desc.length() > 50) {
            // to remain valid html: remove html tags
            desc = FilterFactory.getHtmlTagAndDescapingFilter().filter(desc);
            if (desc.length() > 50) {
                desc = desc.substring(0, 50) + MORE_TEXT;
            }
        }
        return desc;
    }

    /**
     * [used by Hibernate]
     * 
     * @return
     */
    public List<EPStructureToStructureLink> getInternalChildren() {
        if (children == null) {
            children = new ArrayList<EPStructureToStructureLink>();
        }
        return children;
    }

    public void setInternalChildren(final List<EPStructureToStructureLink> children) {
        this.children = children;
    }

    /**
     * [used by Hibernate]
     * 
     * @return
     */
    public List<EPStructureToArtefactLink> getInternalArtefacts() {
        if (artefacts == null) {
            artefacts = new ArrayList<EPStructureToArtefactLink>();
        }
        return artefacts;
    }

    public void setInternalArtefacts(final List<EPStructureToArtefactLink> artefacts) {
        this.artefacts = artefacts;
    }

    /**
     * editable / non-editable
     * 
     * @uml.property name="status"
     */
    private String status;

    /**
     * Getter of the property <tt>status</tt>
     * 
     * @return Returns the status.
     * @uml.property name="status"
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter of the property <tt>status</tt>
     * 
     * @param status
     *            The status to set.
     * @uml.property name="status"
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * @uml.property name="collectRestriction"
     */
    private List<CollectRestriction> collectRestrictions;

    /**
     * Getter of the property <tt>collectRestriction</tt>
     * 
     * @return Returns the collectRestriction.
     * @uml.property name="collectRestriction"
     */
    @Override
    public List<CollectRestriction> getCollectRestrictions() {
        if (collectRestrictions == null) {
            collectRestrictions = new ArrayList<CollectRestriction>();
        }
        return collectRestrictions;
    }

    /**
     * Setter of the property <tt>collectRestriction</tt>
     * 
     * @param collectRestriction
     *            The collectRestriction to set.
     * @uml.property name="collectRestriction"
     */
    @Override
    public void setCollectRestrictions(final List<CollectRestriction> collectRestrictions) {
        this.collectRestrictions = collectRestrictions;
    }

    @Override
    public OLATResource getOlatResource() {
        return olatResource;
    }

    public void setOlatResource(final OLATResource olatResource) {
        this.olatResource = olatResource;
    }

    @Override
    public Long getResourceableId() {
        return olatResource == null ? null : olatResource.getResourceableId();
    }

    @Override
    public String getResourceableTypeName() {
        return olatResource == null ? null : olatResource.getResourceableTypeName();
    }

    @Override
    public String getIcon() {
        return "b_ep_struct_icon";
    }

    /**
     * @uml.property name="root"
     */
    private EPStructureElement root;

    /**
     * Getter of the property <tt>root</tt>
     * 
     * @return Returns the root.
     * @uml.property name="root"
     */
    @Override
    public EPStructureElement getRoot() {
        return root;
    }

    /**
     * Setter of the property <tt>root</tt>
     * 
     * @param root
     *            The root to set.
     * @uml.property name="root"
     */
    public void setRoot(final EPStructureElement root) {
        this.root = root;
    }

    /**
	 * 
	 */
    private PortfolioStructureMap rootMap;

    @Override
    public PortfolioStructureMap getRootMap() {
        return rootMap;
    }

    public void setRootMap(final PortfolioStructureMap rootMap) {
        this.rootMap = rootMap;
    }

    /**
     * @param style
     *            The class to use for css-styling infos for this element
     */
    public void setStyle(final String style) {
        this.style = style;
    }

    /**
     * @return Returns the style.
     */
    public String getStyle() {
        return style;
    }

    // The class to use for css-styling infos for this element
    private String style;

    /**
     * @param artefactRepresentationMode
     *            The artefactRepresentationMode (table, miniview) to set.
     */
    @Override
    public void setArtefactRepresentationMode(final String artefactRepresentationMode) {
        this.artefactRepresentationMode = artefactRepresentationMode;
    }

    /**
     * @return Returns the artefactRepresentationMode (table, miniview)
     */
    @Override
    public String getArtefactRepresentationMode() {
        return artefactRepresentationMode;
    }

    private String artefactRepresentationMode;

    @Override
    public void addStructureToStructure(PortfolioStructure parentStructure, PortfolioStructure childStructure, int destinationPos) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean moveStructureToNewParentStructure(PortfolioStructure structToBeMvd, PortfolioStructure oldParStruct, PortfolioStructure newParStruct,
            int destinationPos) {
        // TODO Auto-generated method stub
        return false;
    }

}
