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

package org.olat.connectors.rest.repository.course;

import static org.olat.connectors.rest.security.RestSecurityHelper.isAuthor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.olat.connectors.rest.security.RestSecurityHelper;
import org.olat.connectors.rest.support.vo.LinkVO;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.lms.commons.filemetadata.FileMetadataInfoHelper;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This will handle the resources folders in the course: the course storage folder and the shared folder. The course folder has a read-write access but the shared folder
 * can only be read.
 * <P>
 * Initial Date: 26 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses/{courseId}/resourcefolders")
public class CourseResourceFolderWebService {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "1.0";

    public static CacheControl cc = new CacheControl();

    static {
        cc.setMaxAge(-1);
    }

    /**
     * The version of the resources folders Web Service
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
     * This retrieves the files in the shared folder
     * 
     * @response.representation.200.doc The list of files
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or the shared folder not found
     * @param courseId
     *            The course resourceable's id
     * @param uri
     *            The uri infos
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return
     */
    @GET
    @Path("sharedfolder")
    public Response getSharedFiles(@PathParam("courseId") final Long courseId, @Context final UriInfo uriInfo, @Context final HttpServletRequest httpRequest,
            @Context final Request request) {
        return getFiles(courseId, Collections.<PathSegment> emptyList(), FolderType.SHARED_FOLDER, uriInfo, httpRequest, request);
    }

    /**
     * This retrieves the files in the shared folder
     * 
     * @response.representation.200.doc The list of files
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or the file not found
     * @response.representation.406.doc The course node is not acceptable to copy a file
     * @param courseId
     *            The course resourceable's id
     * @param path
     *            The path of the file or directory
     * @param uri
     *            The uri infos
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return
     */
    @GET
    @Path("sharedfolder/{path:.*}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM })
    public Response getSharedFiles(@PathParam("courseId") final Long courseId, @PathParam("path") final List<PathSegment> path, @Context final UriInfo uriInfo,
            @Context final HttpServletRequest httpRequest, @Context final Request request) {
        return getFiles(courseId, path, FolderType.COURSE_FOLDER, uriInfo, httpRequest, request);
    }

    /**
     * This retrieves the files in the course folder
     * 
     * @response.representation.200.doc The list of files
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course not found
     * @param courseId
     *            The course resourceable's id
     * @param uri
     *            The uri infos
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return
     */
    @GET
    @Path("coursefolder")
    public Response getCourseFiles(@PathParam("courseId") final Long courseId, @Context final UriInfo uriInfo, @Context final HttpServletRequest httpRequest,
            @Context final Request request) {
        return getFiles(courseId, Collections.<PathSegment> emptyList(), FolderType.COURSE_FOLDER, uriInfo, httpRequest, request);
    }

    /**
     * This retrieves the files in the course folder
     * 
     * @response.representation.200.doc The list of files
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or the file not found
     * @response.representation.406.doc The course node is not acceptable to copy a file
     * @param courseId
     *            The course resourceable's id
     * @param path
     *            The path of the file or directory
     * @param uri
     *            The uri infos
     * @param httpRequest
     *            The HTTP request
     * @param request
     *            The REST request
     * @return
     */
    @GET
    @Path("coursefolder/{path:.*}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaType.APPLICATION_OCTET_STREAM })
    public Response getCourseFiles(@PathParam("courseId") final Long courseId, @PathParam("path") final List<PathSegment> path, @Context final UriInfo uriInfo,
            @Context final HttpServletRequest httpRequest, @Context final Request request) {
        return getFiles(courseId, path, FolderType.COURSE_FOLDER, uriInfo, httpRequest, request);
    }

    /**
     * This attaches the uploaded file(s) to the supplied folder id.
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The file
     * @response.representation.200.doc The file is correctly saved
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or course node not found
     * @response.representation.406.doc The course node is not acceptable to copy a file
     * @param courseId
     *            The course resourceable's id
     * @param filename
     *            The filename
     * @param file
     *            The file resource to upload
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("coursefolder")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response attachFileToFolderPost(@PathParam("courseId") final Long courseId, @FormParam("filename") final String filename,
            @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        return attachFileToCourseFolder(courseId, Collections.<PathSegment> emptyList(), filename, file, request);
    }

    /**
     * This attaches the uploaded file(s) to the supplied folder id at the specified path.
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The file
     * @response.representation.200.doc The file is correctly saved
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or course node not found
     * @response.representation.406.doc The course node is not acceptable to copy a file
     * @param courseId
     *            The course resourceable's id
     * @param path
     *            The path of the file
     * @param filename
     *            The filename
     * @param file
     *            The file resource to upload
     * @param request
     *            The HTTP request
     * @return
     */
    @POST
    @Path("coursefolder/{path:.*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response attachFileToFolderPost(@PathParam("courseId") final Long courseId, @PathParam("path") final List<PathSegment> path,
            @FormParam("filename") final String filename, @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        return attachFileToCourseFolder(courseId, path, filename, file, request);
    }

    /**
     * This attaches the uploaded file(s) to the supplied folder id at the root level
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The file
     * @response.representation.200.doc The file is correctly saved
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or course node not found
     * @response.representation.406.doc The course node is not acceptable to copy a file
     * @param courseId
     *            The course resourceable's id
     * @param nodeId
     *            The id for the folder that will contain the file(s)
     * @param filename
     *            The filename
     * @param file
     *            The file resource to upload
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("coursefolder")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response attachFileToFolder(@PathParam("courseId") final Long courseId, @FormParam("filename") final String filename,
            @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        return attachFileToCourseFolder(courseId, Collections.<PathSegment> emptyList(), filename, file, request);
    }

    /**
     * This attaches the uploaded file(s) to the supplied folder id at the specified path
     * 
     * @response.representation.mediaType multipart/form-data
     * @response.representation.doc The file
     * @response.representation.200.doc The file is correctly saved
     * @response.representation.401.doc The roles of the authenticated user are not sufficient
     * @response.representation.404.doc The course or course node not found
     * @response.representation.406.doc The course node is not acceptable to copy a file
     * @param courseId
     *            The course resourceable's id
     * @param nodeId
     *            The id for the folder that will contain the file(s)
     * @param filename
     *            The filename
     * @param file
     *            The file resource to upload
     * @param request
     *            The HTTP request
     * @return
     */
    @PUT
    @Path("coursefolder/{path:.*}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response attachFileToFolder(@PathParam("courseId") final Long courseId, @PathParam("path") final List<PathSegment> path,
            @FormParam("filename") final String filename, @FormParam("file") final InputStream file, @Context final HttpServletRequest request) {
        return attachFileToCourseFolder(courseId, path, filename, file, request);
    }

    private Response attachFileToCourseFolder(final Long courseId, final List<PathSegment> path, final String filename, final InputStream file,
            final HttpServletRequest request) {
        if (!isAuthor(request)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final ICourse course = loadCourse(courseId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        VFSContainer container = course.getCourseFolderContainer();
        for (final PathSegment segment : path) {
            final VFSItem item = container.resolve(segment.getPath());
            if (item instanceof VFSContainer) {
                container = (VFSContainer) item;
            } else if (item == null) {
                // create the folder
                container = container.createChildContainer(segment.getPath());
            }
        }

        VFSItem newFile;
        final UserRequest ureq = RestSecurityHelper.getUserRequest(request);
        if (container.resolve(filename) != null) {
            final VFSItem existingVFSItem = container.resolve(filename);
            if (existingVFSItem instanceof VFSContainer) {
                // already exists
                return Response.ok().build();
            }

            // check if it's locked
            if (existingVFSItem instanceof MetaTagged
                    && FileMetadataInfoHelper.isLocked(existingVFSItem, ureq.getIdentity(), ureq.getUserSession().getRoles().isOLATAdmin())) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }

            if (existingVFSItem instanceof Versionable && ((Versionable) existingVFSItem).getVersions().isVersioned()) {
                final Versionable existingVersionableItem = (Versionable) existingVFSItem;
                final boolean ok = existingVersionableItem.getVersions().addVersion(ureq.getIdentity(), "REST upload", file);
                if (ok) {
                    log.info("Audit:");
                }
                newFile = (VFSLeaf) existingVersionableItem;
            } else {
                existingVFSItem.delete();
                newFile = container.createChildLeaf(filename);
                final OutputStream out = ((VFSLeaf) newFile).getOutputStream(false);
                FileUtils.copy(file, out);
                FileUtils.closeSafely(out);
                FileUtils.closeSafely(file);
            }
        } else if (file != null) {
            newFile = container.createChildLeaf(filename);
            final OutputStream out = ((VFSLeaf) newFile).getOutputStream(false);
            FileUtils.copy(file, out);
            FileUtils.closeSafely(out);
            FileUtils.closeSafely(file);
        } else {
            newFile = container.createChildContainer(filename);
        }

        if (newFile instanceof MetaTagged && ((MetaTagged) newFile).getMetaInfo() != null) {
            final MetaInfo infos = ((MetaTagged) newFile).getMetaInfo();
            infos.setAuthor(ureq.getIdentity().getName());
            infos.write();
        }

        return Response.ok().build();
    }

    public Response getFiles(final Long courseId, final List<PathSegment> path, final FolderType type, final UriInfo uriInfo, final HttpServletRequest httpRequest,
            final Request request) {
        if (!isAuthor(httpRequest)) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }

        final ICourse course = loadCourse(courseId);
        if (course == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        VFSContainer container = null;
        switch (type) {
        case COURSE_FOLDER:
            container = course.getCourseFolderContainer();
            break;
        case SHARED_FOLDER:
            container = null;
            break;
        }

        if (container == null) {
            return Response.serverError().status(Status.NOT_FOUND).build();
        }

        VFSLeaf leaf = null;
        for (final PathSegment seg : path) {
            final VFSItem item = container.resolve(seg.getPath());
            if (item instanceof VFSLeaf) {
                leaf = (VFSLeaf) item;
                break;
            } else if (item instanceof VFSContainer) {
                container = (VFSContainer) item;
            }
        }

        if (leaf != null) {
            final Date lastModified = new Date(leaf.getLastModified());
            Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
            if (response == null) {
                String mimeType = WebappHelper.getMimeType(leaf.getName());
                if (mimeType == null) {
                    mimeType = MediaType.APPLICATION_OCTET_STREAM;
                }
                response = Response.ok(leaf.getInputStream(), mimeType).lastModified(lastModified).cacheControl(cc);
            }
            return response.build();
        }

        final List<VFSItem> items = container.getItems(new SystemItemFilter());
        int count = 0;
        final LinkVO[] links = new LinkVO[items.size()];
        for (final VFSItem item : items) {
            final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
            final UriBuilder repoUri = baseUriBuilder.path(CourseResourceFolderWebService.class).path("files");
            for (final PathSegment pathSegment : path) {
                repoUri.path(pathSegment.getPath());
            }
            final String uri = repoUri.path(item.getName()).build(courseId).toString();
            links[count++] = new LinkVO("self", uri, item.getName());
        }

        return Response.ok(links).build();
    }

    private ICourse loadCourse(final Long courseId) {
        try {
            final ICourse course = CourseFactory.loadCourse(courseId);
            return course;
        } catch (final Exception ex) {
            log.error("cannot load course with id: " + courseId, ex);
            return null;
        }
    }

    public enum FolderType {
        COURSE_FOLDER, SHARED_FOLDER
    }

    public static class SystemItemFilter implements VFSItemFilter {
        @Override
        public boolean accept(final VFSItem vfsItem) {
            final String name = vfsItem.getName();
            return !name.startsWith(".");
        }
    }
}
