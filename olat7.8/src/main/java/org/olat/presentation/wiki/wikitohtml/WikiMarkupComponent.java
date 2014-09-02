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

package org.olat.presentation.wiki.wikitohtml;

import javax.servlet.http.HttpServletRequest;

import org.jamwiki.DataHandler;
import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.wiki.WikiContainer;
import org.olat.lms.wiki.WikiPage;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;
import org.olat.presentation.framework.core.control.JSAndCSSAdder;
import org.olat.presentation.framework.core.render.ValidationResult;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.presentation.framework.dispatcher.mapper.MapperRegistry;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * This component renders a string containing media wiki syntax to html for a reference of the media wiki syntax see: http://meta.wikimedia.org/wiki/Help:Editing
 * <P>
 * Initial Date: May 17, 2006 <br>
 * 
 * @author guido
 */
public class WikiMarkupComponent extends Component {
    // single renderer for all users, lazy creation upon first object cration of
    // this class.
    private static final ComponentRenderer RENDERER = new WikiMarkupRenderer();
    private String wikiContent;
    private final int minHeight;
    private ParserInput parserInput;
    private JFlexParser parser;
    private final OLATResourceable ores;
    private OlatWikiDataHandler datahandler;
    private String imageBaseUri;

    public WikiMarkupComponent(final String name, final OLATResourceable ores, final int minHeight) {
        super(name);
        this.ores = ores;
        this.minHeight = Math.max(minHeight, 15);

        // configure wiki parser
        final OlatRootFolderImpl tempFolder = new OlatRootFolderImpl("/tmp", null);
        Environment.setValue(Environment.PROP_BASE_FILE_DIR, tempFolder.getBasefile().getAbsolutePath());
        Environment.setValue(Environment.PROP_DB_TYPE, "org.olat.presentation.framework.components.wikiToHtml.OlatWikiDataHandler");
    }

    /**
     */
    @Override
    protected void doDispatchRequest(final UserRequest ureq) {
        final String moduleUri = ureq.getModuleURI();

        // FIXME:gs:a access string constants by NameSpaceHandler
        if (moduleUri.startsWith("Special:Edit")) {
            final String topic = moduleUri.substring(moduleUri.indexOf("topic=") + 6, moduleUri.length());

            if (topic.length() > 175) {
                fireEvent(ureq, new ErrorEvent("wiki.error.too.long"));
            } else if (topic.length() == 0) {
                fireEvent(ureq, new ErrorEvent("wiki.error.contains.bad.chars"));
            } else {
                fireEvent(ureq, new RequestNewPageEvent(topic));
            }

        } else if (moduleUri.startsWith("Media:")) { // these are media links like pdf or audio files
            fireEvent(ureq, new RequestMediaEvent(moduleUri.substring(6, moduleUri.length())));

        } else if (moduleUri.startsWith("Image:")) {
            fireEvent(ureq, new RequestImageEvent(moduleUri.substring(6, moduleUri.length())));

            // trap special pages (like: Special:Upload) which are not yet implemented in OLAT
        } else if (moduleUri.startsWith("Special:Upload")) {
            fireEvent(ureq, new ErrorEvent("wiki.error.file.not.found"));

        } else if (moduleUri.equals("")) {
            fireEvent(ureq, new RequestPageEvent(WikiPage.WIKI_INDEX_PAGE));
        } else {
            fireEvent(ureq, new RequestPageEvent(moduleUri));
        }
        setDirty(true);
    }

    @Deprecated
    // This is used by WikiMainControlle to patch button clicks as clicks on tabs. Do not use it!
    public void dispatchRequestForNewTopic(final UserRequest ureq, final String topic) {
        if (topic.length() > 175) {
            fireEvent(ureq, new ErrorEvent("wiki.error.too.long"));
        } else if (topic.length() == 0) {
            fireEvent(ureq, new ErrorEvent("wiki.error.contains.bad.chars"));
        } else {
            fireEvent(ureq, new RequestNewPageEvent(topic));
        }
        setDirty(true);
    }

    /**
     */
    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }

    public String getWikiContent() {
        return wikiContent;
    }

    public void setWikiContent(final String wikiContent) {
        this.wikiContent = wikiContent;
        setDirty(true);
    }

    /**
     * if the wiki need to serve images you have to set the image mapper uri first! The mapper creates an user session based mapper for the media files which can be
     * 
     * @param ureq
     * @param wikiContainer
     */
    public void setImageMapperUri(final UserRequest ureq, final VFSContainer wikiContainer) {
        // get a usersession-local mapper for images in this wiki
        final Mapper contentMapper = new Mapper() {

            @Override
            public MediaResource handle(final String relPath, final HttpServletRequest request) {
                final VFSItem vfsItem = wikiContainer.resolve(relPath);
                MediaResource mr;
                if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) {
                    mr = new NotFoundMediaResource(relPath);
                } else {
                    mr = new VFSMediaResource((VFSLeaf) vfsItem);
                }
                return mr;
            }
        };
        // datahandler.setImageURI(MapperRegistry.getInstanceFor(ureq.getUserSession()).register(contentMapper)+"/"+WikiContainer.MEDIA_FOLDER_NAME+"/");
        final MapperRegistry mr = MapperRegistry.getInstanceFor(ureq.getUserSession());
        String mapperPath;
        // Register mapper as cacheable
        String mapperID = VFSManager.getRealPath(wikiContainer);
        if (mapperID == null) {
            // Can't cache mapper, no cacheable context available
            mapperPath = mr.register(contentMapper);
        } else {
            // Add classname to the file path to remove conflicts with other
            // usages of the same file path
            mapperID = this.getClass().getSimpleName() + ":" + mapperID;
            mapperPath = mr.registerCacheable(mapperID, contentMapper);
        }
        imageBaseUri = mapperPath + "/" + WikiContainer.MEDIA_FOLDER_NAME + "/";
    }

    /**
     * @return
     */
    public String getImageBaseUri() {
        if (this.imageBaseUri == null) {
            throw new AssertException("the uri ist null, you must call setImageMapperUri first!");
        }
        return this.imageBaseUri;
    }

    @Override
    public String getExtendedDebugInfo() {

        // see velocitycontainer on how to implement
        return null;
    }

    protected ParserInput getParserInput() {
        return parserInput;
    }

    protected JFlexParser getParser() {
        return parser;
    }

    /**
     */
    @Override
    public void validate(final UserRequest ureq, final ValidationResult vr) {
        super.validate(ureq, vr);
        final JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
        jsa.addRequiredJsFile(WikiMarkupComponent.class, "js/wiki.js");
        jsa.addRequiredCSSFile(WikiMarkupComponent.class, "css/wiki.css", true);
    }

    /**
     * @return the min height the wiki content display div should have
     */
    protected int getMinHeight() {
        return minHeight;
    }

    protected OLATResourceable getOres() {
        return ores;
    }

    /**
     * returns the datahandler for the jamwiki parser
     * 
     */
    public DataHandler lookupDataHandler() {
        return datahandler;
    }
}
