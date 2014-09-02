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
package org.olat.connectors.rest.repository;

import static org.olat.connectors.rest.security.RestSecurityHelper.getIdentity;
import static org.olat.connectors.rest.security.RestSecurityHelper.getRoles;
import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthor;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.connectors.rest.support.ObjectFactory;
import org.olat.connectors.rest.support.vo.RepositoryEntryVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * This handles the repository entries
 * <P>
 * Initial Date: 19.05.2009 <br>
 * 
 * @author patrickb, srosse, stephane.rosse@frentix.com
 */
@Path("repo/entries")
public class RepositoryEntriesResource {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "1.0";

    /**
     * The version number of this web service
     * 
     * @return
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }

    /**
     * List all entries in the OLAT repository
     * 
     * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
     * @response.representation.200.mediaType text/plain, text/html, application/xml, application/json
     * @response.representation.200.doc List all entries in the repository
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_REPOENTRYVOes}
     * @param uriInfo
     *            The URI information
     * @param httpRequest
     *            The HTTP request
     * @return
     */
    @GET
    @Produces({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    public Response getEntriesText(@Context final UriInfo uriInfo, @Context final HttpServletRequest httpRequest) {
        try {
            // list of courses open for everybody
            final Roles roles = getRoles(httpRequest);
            final List<String> types = new ArrayList<String>();
            final List<RepositoryEntry> coursRepos = RepositoryServiceImpl.getInstance().genericANDQueryWithRolesRestriction("*", "*", "*", types, roles, null);

            final StringBuilder sb = new StringBuilder();
            sb.append("Course List\n");
            for (final RepositoryEntry repoE : coursRepos) {
                final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
                final URI repoUri = baseUriBuilder.path(RepositoryEntriesResource.class).path(repoE.getKey().toString()).build();

                sb.append("<a href=\"").append(repoUri).append(">").append(repoE.getDisplayname()).append("(").append(repoE.getKey()).append(")").append("</a>")
                        .append("\n");
            }

            return Response.ok(sb.toString()).build();
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * List all entries in the OLAT repository
     * 
     * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
     * @response.representation.200.mediaType text/plain, text/html, application/xml, application/json
     * @response.representation.200.doc List all entries in the repository
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_REPOENTRYVOes}
     * @param httpRequest
     *            The HTTP request
     * @return
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getEntries(@Context final HttpServletRequest httpRequest) {
        try {
            // list of courses open for everybody
            final Roles roles = getRoles(httpRequest);
            final List<String> types = new ArrayList<String>();
            final List<RepositoryEntry> coursRepos = RepositoryServiceImpl.getInstance().genericANDQueryWithRolesRestriction("*", "*", "*", types, roles, null);

            int i = 0;
            final RepositoryEntryVO[] entryVOs = new RepositoryEntryVO[coursRepos.size()];
            for (final RepositoryEntry repoE : coursRepos) {
                entryVOs[i++] = ObjectFactory.get(repoE);
            }
            return Response.ok(entryVOs).build();
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Search for repository entries, possible search attributes are name, author and type
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc Search for repository entries
     * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Search for repository entries
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_REPOENTRYVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @param type
     *            Filter by the file resource type of the repository entry
     * @param author
     *            Filter by the author's username
     * @param name
     *            Filter by name of repository entry
     * @param myEntries
     *            Only search entries the requester owns
     * @param httpRequest
     *            The HTTP request
     * @return
     */
    @GET
    @Path("search")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response searchEntries(@QueryParam("type") final String type, @QueryParam("author") @DefaultValue("*") final String author,
            @QueryParam("name") @DefaultValue("*") final String name, @QueryParam("myentries") @DefaultValue("false") final boolean myEntries,
            @Context final HttpServletRequest httpRequest) {
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        try {
            final List<RepositoryEntry> reposFound = new ArrayList<RepositoryEntry>();
            final Identity identity = getIdentity(httpRequest);
            final boolean restrictedType = type != null && !type.isEmpty();

            // list of courses open for everybody
            final Roles roles = getRoles(httpRequest);

            if (myEntries) {
                final List<RepositoryEntry> lstRepos = rm.queryByOwner(identity, restrictedType ? new String[] { type } : null);
                final boolean restrictedName = !name.equals("*");
                final boolean restrictedAuthor = !author.equals("*");
                if (restrictedName | restrictedAuthor) {
                    // filter by search conditions
                    for (final RepositoryEntry re : lstRepos) {
                        final boolean nameOk = restrictedName ? re.getDisplayname().toLowerCase().contains(name.toLowerCase()) : true;
                        final boolean authorOk = restrictedAuthor ? re.getInitialAuthor().toLowerCase().equals(author.toLowerCase()) : true;
                        if (nameOk & authorOk) {
                            reposFound.add(re);
                        }
                    }
                } else {
                    if (!lstRepos.isEmpty()) {
                        reposFound.addAll(lstRepos);
                    }
                }
            } else {
                final List<String> types = new ArrayList<String>(1);
                if (restrictedType) {
                    types.add(type);
                }
                final List<RepositoryEntry> lstRepos = rm.genericANDQueryWithRolesRestriction(name, author, "*", restrictedType ? types : null, roles, null);
                if (!lstRepos.isEmpty()) {
                    reposFound.addAll(lstRepos);
                }
            }

            int i = 0;
            final RepositoryEntryVO[] reVOs = new RepositoryEntryVO[reposFound.size()];
            for (final RepositoryEntry re : reposFound) {
                reVOs[i++] = ObjectFactory.get(re);
            }
            return Response.ok(reVOs).build();
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Import a resource in the repository
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The file, its name and the resourcename
     * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc Import the resource and return the repository entry
     * @response.representation.200.example {@link org.olat.connectors.rest.support.vo.Examples#SAMPLE_REPOENTRYVO}
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @param filename
     *            The name of the imported file
     * @param file
     *            The file input stream
     * @param resourcename
     *            The name of the resource
     * @param displayname
     *            The display name
     * @param softkey
     *            The soft key (can be null)
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    public Response putResource(@FormParam("filename") final String filename, @FormParam("file") final InputStream file,
            @FormParam("resourcename") final String resourcename, @FormParam("displayname") final String displayname, @FormParam("softkey") final String softkey,
            @Context final HttpServletRequest request) {
        if (!isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        File tmpFile = null;
        long length = 0;
        try {
            final Identity identity = RestSecurityHelper.getUserRequest(request).getIdentity();
            tmpFile = getTmpFile(filename);
            FileUtils.save(file, tmpFile);
            FileUtils.closeSafely(file);
            length = tmpFile.length();

            if (length > 0) {
                final RepositoryEntry re = importFileResource(identity, tmpFile, resourcename, displayname, softkey);
                final RepositoryEntryVO vo = ObjectFactory.get(re);
                return Response.ok(vo).build();
            }
            return Response.serverError().status(Status.NO_CONTENT).build();
        } catch (final Exception e) {
            log.error("Error while importing a file", e);
        } finally {
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        }
        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
    }

    private RepositoryEntry importFileResource(final Identity identity, final File fResource, final String resourcename, final String displayname, final String softkey) {
        try {
            final FileResourceManager frm = FileResourceManager.getInstance();
            final FileResource newResource = frm.addFileResource(fResource, fResource.getName());

            final RepositoryEntry addedEntry = RepositoryServiceImpl.getInstance().createRepositoryEntryInstance(identity.getName());
            addedEntry.setCanDownload(false);
            addedEntry.setCanLaunch(true);
            if (StringHelper.containsNonWhitespace(resourcename)) {
                addedEntry.setResourcename(resourcename);
            }
            if (StringHelper.containsNonWhitespace(displayname)) {
                addedEntry.setDisplayname(displayname);
            }
            if (StringHelper.containsNonWhitespace(softkey)) {
                addedEntry.setSoftkey(softkey);
            }
            // Do set access for owner at the end, because unfinished course should be
            // invisible
            // addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
            addedEntry.setAccess(0);// Access for nobody

            // Set the resource on the repository entry and save the entry.
            final RepositoryService rm = RepositoryServiceImpl.getInstance();
            final OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(newResource);
            addedEntry.setOlatResource(ores);

            final BaseSecurity securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            // create security group
            final SecurityGroup newGroup = securityManager.createAndPersistSecurityGroup();
            // member of this group may modify member's membership
            securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
            // members of this group are always authors also
            securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);

            securityManager.addIdentityToSecurityGroup(identity, newGroup);
            addedEntry.setOwnerGroup(newGroup);
            // Do set access for owner at the end, because unfinished course should be
            // invisible
            addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
            rm.saveRepositoryEntry(addedEntry);
            return addedEntry;
        } catch (final Exception e) {
            log.error("Fail to import a resource", e);
            throw new WebApplicationException(e);
        }
    }

    private File getTmpFile(String suffix) {
        suffix = (suffix == null ? "" : suffix);
        final File tmpFile = new File(WebappHelper.getUserDataRoot() + "/tmp/", CodeHelper.getGlobalForeverUniqueID() + "_" + suffix);
        FileUtils.createEmptyFile(tmpFile);
        return tmpFile;
    }

    @Path("{repoEntryKey}")
    public RepositoryEntryResource getRepositoryEntryResource() {
        return new RepositoryEntryResource();
    }
}
