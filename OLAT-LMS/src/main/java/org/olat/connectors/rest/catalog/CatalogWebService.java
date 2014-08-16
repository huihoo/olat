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
package org.olat.connectors.rest.catalog;

import static org.olat.connectors.rest.catalog.CatalogVOFactory.get;
import static org.olat.connectors.rest.catalog.CatalogVOFactory.link;
import static org.olat.connectors.rest.security.RestSecurityHelper.getUserRequest;
import static org.olat.connectors.rest.security.RestSecurityHelper.isAdmin;
import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthor;
import static org.olat.presentation.catalog.CatalogController.LOCK_TOKEN;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.olat.connectors.rest.support.vo.ErrorVO;
import org.olat.connectors.rest.user.UserVO;
import org.olat.connectors.rest.user.UserVOFactory;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.catalog.CatalogController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.LocaleNegotiator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * A web service for the catalog
 * <P>
 * Initial Date: 5 may 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("catalog")
public class CatalogWebService {

    private static final String VERSION = "1.0";
    @Autowired
    private CatalogService catalogService;
    @Autowired
    private LockingService lockingService;
    @Autowired
    private BaseSecurity baseSecurity;
    private final OLATResourceable catalogRes;

    public CatalogWebService() {
        catalogRes = OresHelper.createOLATResourceableType(CatalogController.class);
    }

    /**
     * Retrieves the version of the Catalog Web Service.
     * 
     * @response.representation.200.mediaType text/plain
     * @response.representation.200.doc The version of this specific Web Service
     * @response.representation.200.example 1.0
     * @return
     */
    @GET
    @Path("version")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }

    /**
     * Returns the list of root catalog entries.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The list of roots catalog entries
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVOes}
     * @return The response
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getRoots() {
        final List<CatalogEntry> rootEntries = catalogService.getRootCatalogEntries();

        int count = 0;
        final CatalogEntryVO[] entryVOes = new CatalogEntryVO[rootEntries.size()];
        for (final CatalogEntry entry : rootEntries) {
            entryVOes[count++] = get(entry);
        }
        return Response.ok(entryVOes).build();
    }

    /**
     * Returns the metadata of the catalog entry.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param uriInfo
     *            The URI informations
     * @return The response
     */
    @GET
    @Path("{path:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCatalogEntry(@PathParam("path") final List<PathSegment> path, @Context final UriInfo uriInfo) {
        if (path.isEmpty()) {
            return getRoots();
        }

        final Long ceKey = getCatalogEntryKeyFromPath(path);
        if (ceKey == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(ceKey);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final CatalogEntryVO vo = link(get(ce), uriInfo);
        return Response.ok(vo).build();
    }

    /**
     * Returns a list of catalog entries.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The list of catalog entries
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVOes}
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @return The response
     */
    @GET
    @Path("{path:.*}/children")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getChildren(@PathParam("path") final List<PathSegment> path) {
        if (path.isEmpty()) {
            return getRoots();
        }

        final Long ceKey = getCatalogEntryKeyFromPath(path);
        if (ceKey == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(ceKey);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final List<CatalogEntry> entries = catalogService.getChildrenOf(ce);
        int count = 0;
        final CatalogEntryVO[] entryVOes = new CatalogEntryVO[entries.size()];
        for (final CatalogEntry entry : entries) {
            entryVOes[count++] = get(entry);
        }
        return Response.ok(entryVOes).build();
    }

    /**
     * Adds a catalog entry under the path specified in the URL.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param name
     *            The name
     * @param description
     *            The description
     * @param type
     *            The type (leaf or node)
     * @param repoEntryKey
     *            The id of the repository entry
     * @param httpRquest
     *            The HTTP request
     * @param uriInfo
     *            The URI informations
     * @return The response
     */
    @PUT
    @Path("{path:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addCatalogEntry(@PathParam("path") final List<PathSegment> path, @QueryParam("name") final String name,
            @QueryParam("description") final String description, @QueryParam("type") final Integer type, @QueryParam("repoEntryKey") final Long repoEntryKey,
            @Context final HttpServletRequest httpRequest, @Context final UriInfo uriInfo) {

        final CatalogEntryVO entryVo = new CatalogEntryVO();
        entryVo.setName(name);
        entryVo.setDescription(description);
        if (type != null) {
            entryVo.setType(type.intValue());
        }
        entryVo.setRepositoryEntryKey(repoEntryKey);
        return addCatalogEntry(path, entryVo, httpRequest, uriInfo);
    }

    /**
     * Adds a catalog entry under the path specified in the URL.
     * 
     * @response.representation.qname {http://www.example.com}catalogEntryVO
     * @response.representation.mediaType application/xml, application/json
     * @response.representation.doc The catalog entry
     * @response.representation.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The list of catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param entryVo
     *            The catalog entry
     * @param httpRquest
     *            The HTTP request
     * @param uriInfo
     *            The URI informations
     * @return The response
     */
    @PUT
    @Path("{path:.*}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addCatalogEntry(@PathParam("path") final List<PathSegment> path, final CatalogEntryVO entryVo, @Context final HttpServletRequest httpRequest,
            @Context final UriInfo uriInfo) {
        if (!isAuthor(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Long parentKey = getCatalogEntryKeyFromPath(path);
        if (parentKey == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry parent = catalogService.loadCatalogEntry(parentKey);
        if (parent == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final int type = guessType(entryVo);
        if (type == CatalogEntry.TYPE_NODE && !canAdminSubTree(parent, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        RepositoryEntry re = null;
        if (entryVo.getRepositoryEntryKey() != null) {
            re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(entryVo.getRepositoryEntryKey());
            if (re == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }
        }

        final Identity id = getUserRequest(httpRequest).getIdentity();
        final LockResult lock = lockingService.acquireLock(catalogRes, id, LOCK_TOKEN);
        if (!lock.isSuccess()) {
            return getLockedResponse(lock, httpRequest);
        }

        CatalogEntry ce = null;
        try {
            ce = catalogService.createCatalogEntry();
            ce.setType(guessType(entryVo));
            ce.setName(entryVo.getName());
            ce.setDescription(entryVo.getDescription());
            ce.setOwnerGroup(baseSecurity.createAndPersistSecurityGroup());
            if (re != null) {
                ce.setRepositoryEntry(re);
            }
            catalogService.addCatalogEntry(parent, ce);
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        } finally {
            lockingService.releaseLock(lock);
        }

        final CatalogEntryVO newEntryVo = link(get(ce), uriInfo);
        return Response.ok(newEntryVo).build();
    }

    /**
     * Updates the catalog entry under the path specified in the URL.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param name
     *            The name
     * @param description
     *            The description
     * @param httpRquest
     *            The HTTP request
     * @param uriInfo
     *            The URI informations
     * @return The response
     */
    @POST
    @Path("{path:.*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updatePostCatalogEntry(@PathParam("path") final List<PathSegment> path, @FormParam("name") final String name,
            @FormParam("description") final String description, @Context final HttpServletRequest httpRequest, @Context final UriInfo uriInfo) {

        final CatalogEntryVO entryVo = new CatalogEntryVO();
        entryVo.setName(name);
        entryVo.setDescription(description);
        return updateCatalogEntry(path, entryVo, httpRequest, uriInfo);
    }

    /**
     * Updates the catalog entry with the path specified in the URL.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param id
     *            The id of the catalog entry
     * @param name
     *            The name
     * @param description
     *            The description
     * @param httpRquest
     *            The HTTP request
     * @param uriInfo
     *            The URI informations
     * @return The response
     */
    @POST
    @Path("{path:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateCatalogEntry(@PathParam("path") final List<PathSegment> path, @QueryParam("name") final String name,
            @QueryParam("description") final String description, @Context final HttpServletRequest httpRequest, @Context final UriInfo uriInfo) {

        final CatalogEntryVO entryVo = new CatalogEntryVO();
        entryVo.setName(name);
        entryVo.setDescription(description);
        return updateCatalogEntry(path, entryVo, httpRequest, uriInfo);
    }

    /**
     * Updates the catalog entry with the path specified in the URL.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param entryVo
     *            The catalog entry
     * @param httpRquest
     *            The HTTP request
     * @param uriInfo
     *            The URI informations
     * @return The response
     */
    @POST
    @Path("{path:.*}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateCatalogEntry(@PathParam("path") final List<PathSegment> path, final CatalogEntryVO entryVo, @Context final HttpServletRequest httpRequest,
            @Context final UriInfo uriInfo) {

        if (!isAuthor(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Long key = getCatalogEntryKeyFromPath(path);
        if (key == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        CatalogEntry ce = catalogService.loadCatalogEntry(key);
        if (ce.getType() == CatalogEntry.TYPE_NODE) {
            // check if can admin category
            if (!canAdminSubTree(ce, httpRequest)) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
        }

        final Identity id = getUserRequest(httpRequest).getIdentity();
        final LockResult lock = lockingService.acquireLock(catalogRes, id, LOCK_TOKEN);
        if (!lock.isSuccess()) {
            return getLockedResponse(lock, httpRequest);
        }

        try {
            ce = catalogService.loadCatalogEntry(ce);
            if (ce == null) {
                return Response.serverError().status(Status.NOT_FOUND).build();
            }

            ce.setName(entryVo.getName());
            ce.setDescription(entryVo.getDescription());
            ce.setType(guessType(entryVo));
            catalogService.updateCatalogEntry(ce);
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        } finally {
            lockingService.releaseLock(lock);
        }

        final CatalogEntryVO newEntryVo = link(get(ce), uriInfo);
        return Response.ok(newEntryVo).build();
    }

    /**
     * Deletes the catalog entry with the path specified in the URL.
     * 
     * @response.representation.200.qname {http://www.example.com}catalogEntryVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.catalog.Examples#SAMPLE_CATALOGENTRYVO}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param httpRquest
     *            The HTTP request
     * @return The response
     */
    @DELETE
    @Path("{path:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deleteCatalogEntry(@PathParam("path") final List<PathSegment> path, @Context final HttpServletRequest httpRequest) {
        final Long key = getCatalogEntryKeyFromPath(path);
        if (key == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(key);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!canAdminSubTree(ce, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Identity id = getUserRequest(httpRequest).getIdentity();
        final LockResult lock = lockingService.acquireLock(catalogRes, id, LOCK_TOKEN);
        if (!lock.isSuccess()) {
            return getLockedResponse(lock, httpRequest);
        }

        try {
            catalogService.deleteCatalogEntry(ce);
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        } finally {
            lockingService.releaseLock(lock);
        }
        return Response.ok().build();
    }

    /**
     * Get the owners of the local sub tree
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param httpRquest
     *            The HTTP request
     * @return The response
     */
    @GET
    @Path("{path:.*}/owners")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getOwners(@PathParam("path") final List<PathSegment> path, @Context final HttpServletRequest httpRequest) {
        final Long key = getCatalogEntryKeyFromPath(path);
        if (key == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(key);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final SecurityGroup sg = ce.getOwnerGroup();
        if (sg == null) {
            return Response.ok(new UserVO[0]).build();
        }

        final List<Identity> ids = baseSecurity.getIdentitiesOfSecurityGroup(sg);
        int count = 0;
        final UserVO[] voes = new UserVO[ids.size()];
        for (final Identity id : ids) {
            voes[count++] = UserVOFactory.get(id);
        }
        return Response.ok(voes).build();
    }

    /**
     * Retrieves data of an owner of the local sub tree
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @Param identityKey The id of the user
     * @param httpRquest
     *            The HTTP request
     * @return The response
     */
    @GET
    @Path("{path:.*}/owners/{identityKey}")
    public Response getOwner(@PathParam("path") final List<PathSegment> path, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest httpRequest) {
        final Long key = getCatalogEntryKeyFromPath(path);
        if (key == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(key);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final SecurityGroup sg = ce.getOwnerGroup();
        if (sg == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final List<Identity> ids = baseSecurity.getIdentitiesOfSecurityGroup(sg);
        UserVO vo = null;
        for (final Identity id : ids) {
            if (id.getKey().equals(identityKey)) {
                vo = UserVOFactory.get(id);
                break;
            }
        }
        if (vo == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }
        return Response.ok(vo).build();
    }

    /**
     * Add an owner of the local sub tree
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param identityKey
     *            The id of the user
     * @param httpRquest
     *            The HTTP request
     * @return The response
     */
    @PUT
    @Path("{path:.*}/owners/{identityKey}")
    public Response addOwner(@PathParam("path") final List<PathSegment> path, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest httpRequest) {

        final Long key = getCatalogEntryKeyFromPath(path);
        if (key == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(key);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Identity identity = baseSecurity.loadIdentityByKey(identityKey, false);
        if (identity == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        final Identity id = getUserRequest(httpRequest).getIdentity();
        final LockResult lock = lockingService.acquireLock(catalogRes, id, LOCK_TOKEN);
        if (!lock.isSuccess()) {
            return getLockedResponse(lock, httpRequest);
        }

        try {
            final SecurityGroup sg = ce.getOwnerGroup();
            if (sg == null) {
                ce.setOwnerGroup(baseSecurity.createAndPersistSecurityGroup());
                DBFactory.getInstance().intermediateCommit();
            }
            baseSecurity.addIdentityToSecurityGroup(identity, ce.getOwnerGroup());
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        } finally {
            lockingService.releaseLock(lock);
        }
        return Response.ok().build();
    }

    /**
     * Remove an owner of the local sub tree
     * 
     * @response.representation.200.qname {http://www.example.com}userVO
     * @response.representation.200.mediaType application/xml, application/json
     * @response.representation.200.doc The catalog entry
     * @response.representation.200.example {@link org.olat.connectors.rest.user.Examples#SAMPLE_USERVOes}
     * @response.representation.401.doc Not authorized
     * @response.representation.404.doc The path could not be resolved to a valid catalog entry
     * @param path
     *            The path
     * @param identityKey
     *            The id of the user
     * @param httpRquest
     *            The HTTP request
     * @return The response
     */
    @DELETE
    @Path("{path:.*}/owners/{identityKey}")
    public Response removeOwner(@PathParam("path") final List<PathSegment> path, @PathParam("identityKey") final Long identityKey,
            @Context final HttpServletRequest httpRequest) {
        final Long key = getCatalogEntryKeyFromPath(path);
        if (key == null) {
            return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
        }

        final CatalogEntry ce = catalogService.loadCatalogEntry(key);
        if (ce == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        if (!isAuthor(httpRequest) && !canAdminSubTree(ce, httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final Identity identity = baseSecurity.loadIdentityByKey(identityKey, false);
        if (identity == null) {
            return Response.ok().build();
        }

        final SecurityGroup sg = ce.getOwnerGroup();
        if (sg == null) {
            return Response.ok().build();
        }

        final Identity id = getUserRequest(httpRequest).getIdentity();
        final LockResult lock = lockingService.acquireLock(catalogRes, id, LOCK_TOKEN);
        if (!lock.isSuccess()) {
            return getLockedResponse(lock, httpRequest);
        }

        try {
            baseSecurity.removeIdentityFromSecurityGroup(identity, ce.getOwnerGroup());
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        } finally {
            lockingService.releaseLock(lock);
        }
        return Response.ok().build();
    }

    private Response getLockedResponse(final LockResult lock, final HttpServletRequest request) {
        Locale locale = null;
        final UserRequest ureq = getUserRequest(request);
        if (ureq != null) {
            locale = LocaleNegotiator.getPreferedLocale(ureq);
        }
        if (locale == null) {
            locale = I18nModule.getDefaultLocale();
        }

        final Translator translator = PackageUtil.createPackageTranslator(CatalogController.class, locale);
        final String translation = translator.translate("catalog.locked.by", new String[] { lock.getOwner().getName() });
        final ErrorVO vo = new ErrorVO("org.olat.presentation.catalog", "catalog.locked.by", translation);
        final ErrorVO[] voes = new ErrorVO[] { vo };
        return Response.ok(voes).status(Status.UNAUTHORIZED).build();
    }

    private Long getCatalogEntryKeyFromPath(final List<PathSegment> path) {
        final PathSegment lastPath = path.get(path.size() - 1);
        Long key = null;
        try {
            key = new Long(lastPath.getPath());
        } catch (final NumberFormatException e) {
            key = null;
        }
        return key;
    }

    private boolean canAdminSubTree(final CatalogEntry ce, final HttpServletRequest httpRequest) {
        if (isAdmin(httpRequest)) {
            return true;
        }
        final Identity identity = getUserRequest(httpRequest).getIdentity();
        final SecurityGroup owners = ce.getOwnerGroup();
        if (owners != null && baseSecurity.isIdentityInSecurityGroup(identity, owners)) {
            return true;
        }
        return false;
    }

    private int guessType(final CatalogEntryVO vo) {
        final Integer type = vo.getType();
        if (type == null) {
            if (vo.getRepositoryEntryKey() == null) {
                return CatalogEntry.TYPE_NODE;
            }
            return CatalogEntry.TYPE_LEAF;
        }
        return type.intValue();

    }
}
