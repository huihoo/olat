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
package org.olat.lms.commons.mediaresource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * The media resource for synd feeds. Used for dispatching.
 * <P>
 * Initial Date: Mar 12, 2009 <br>
 * 
 * @author gwassmann
 */
public class SyndFeedMediaResource implements MediaResource {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final Logger log = LoggerHelper.getLogger();

    private final SyndFeed feed;
    private String feedString;
    private static final String CONTENT_TYPE = "application/rss+xml";

    public SyndFeedMediaResource(final SyndFeed feed) {
        this.feed = feed;

        feedString = null;
        try {
            final SyndFeedOutput output = new SyndFeedOutput();
            feedString = output.outputString(feed);
        } catch (final FeedException e) {
            /* TODO: ORID-1007 ExceptionHandling */
            log.error(e.getMessage());
        }
    }

    /**
	 */
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
	 */
    @Override
    public InputStream getInputStream() {
        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(feedString.getBytes(DEFAULT_ENCODING));
        } catch (final UnsupportedEncodingException e) {
            /* TODO: ORID-1007 ExceptionHandling */
            log.error(e.getMessage());
        }
        return inputStream;
    }

    /**
	 */
    @Override
    public Long getLastModified() {
        Long lastModified = null;
        final Date date = feed.getPublishedDate();
        if (date != null) {
            lastModified = Long.valueOf(date.getTime());
        }
        return lastModified;
    }

    /**
	 */
    @Override
    public Long getSize() {
        return new Long(feedString.getBytes().length);
    }

    /**
	 */
    @Override
    public void prepare(@SuppressWarnings("unused") final HttpServletResponse hres) {
        // nothing to prepare
    }

    /**
	 */
    @Override
    public void release() {
        // nothing to release
    }

}
