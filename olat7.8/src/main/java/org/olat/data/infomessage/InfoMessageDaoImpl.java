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

package org.olat.data.infomessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * The manager for info messages
 * <P>
 * Initial Date: 26 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class InfoMessageDaoImpl extends InfoMessageDao {

    @Autowired
    private DB dbInstance;

    /**
     * [used by Spring]
     */
    private InfoMessageDaoImpl() {
        //
    }

    @Override
    public InfoMessage createInfoMessage(final OLATResourceable ores, final String subPath, final String businessPath, final Identity author) {
        if (ores == null) {
            throw new NullPointerException("OLAT Resourceable cannot be null");
        }

        final InfoMessageImpl info = new InfoMessageImpl();
        info.setResId(ores.getResourceableId());
        info.setResName(ores.getResourceableTypeName());
        info.setResSubPath(subPath);
        info.setBusinessPath(normalizeBusinessPath(businessPath));
        info.setAuthor(author);
        return info;
    }

    @Override
    public void saveInfoMessage(final InfoMessage infoMessage) {
        if (infoMessage instanceof InfoMessageImpl) {
            final InfoMessageImpl impl = (InfoMessageImpl) infoMessage;
            if (impl.getKey() == null) {
                dbInstance.saveObject(impl);
            } else {
                dbInstance.updateObject(impl);
            }
        }
    }

    @Override
    public void deleteInfoMessage(final InfoMessage infoMessage) {
        if (infoMessage instanceof InfoMessageImpl) {
            final InfoMessageImpl impl = (InfoMessageImpl) infoMessage;
            if (impl.getKey() != null) {
                dbInstance.deleteObject(impl);
            }
        }
    }

    @Override
    public InfoMessage loadInfoMessageByKey(final Long key) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select msg from ").append(InfoMessageImpl.class.getName()).append(" msg where msg.key=:key");

        final DBQuery query = dbInstance.createQuery(sb.toString());
        query.setLong("key", key);
        @SuppressWarnings("unchecked")
        final List<InfoMessage> msgs = query.list();
        if (msgs.isEmpty()) {
            return null;
        }
        return msgs.get(0);
    }

    @Override
    public List<InfoMessage> loadInfoMessageByResource(final OLATResourceable ores, final String subPath, final String businessPath, final Date after, final Date before,
            final int firstResult, final int maxResults) {

        final DBQuery query = queryInfoMessageByResource(ores, subPath, businessPath, after, before, false);
        if (firstResult >= 0) {
            query.setFirstResult(firstResult);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }

        @SuppressWarnings("unchecked")
        final List<InfoMessage> msgs = query.list();
        return msgs;
    }

    @Override
    public int countInfoMessageByResource(final OLATResourceable ores, final String subPath, final String businessPath, final Date after, final Date before) {

        final DBQuery query = queryInfoMessageByResource(ores, subPath, businessPath, after, before, true);
        final Number count = (Number) query.uniqueResult();
        return count.intValue();
    }

    private DBQuery queryInfoMessageByResource(final OLATResourceable ores, final String subPath, final String businessPath, final Date after, final Date before,
            final boolean count) {

        final StringBuilder sb = new StringBuilder();
        sb.append("select ");
        if (count) {
            sb.append("count(msg.key)");
        } else {
            sb.append("msg");
        }

        sb.append(" from ").append(InfoMessageImpl.class.getName()).append(" msg");

        if (ores != null) {
            appendAnd(sb, "msg.resId=:resId and msg.resName=:resName ");
        }
        if (StringHelper.containsNonWhitespace(subPath)) {
            appendAnd(sb, "msg.resSubPath=:subPath");
        }
        if (StringHelper.containsNonWhitespace(businessPath)) {
            appendAnd(sb, "msg.businessPath=:businessPath");
        }
        if (after != null) {
            appendAnd(sb, "msg.creationDate>=:after");
        }
        if (before != null) {
            appendAnd(sb, "msg.creationDate<=:before");
        }
        if (!count) {
            sb.append(" order by msg.creationDate desc");
        }

        final DBQuery query = dbInstance.createQuery(sb.toString());
        if (ores != null) {
            query.setLong("resId", ores.getResourceableId());
            query.setString("resName", ores.getResourceableTypeName());
        }
        if (StringHelper.containsNonWhitespace(subPath)) {
            query.setString("subPath", subPath);
        }
        if (StringHelper.containsNonWhitespace(businessPath)) {
            query.setString("businessPath", normalizeBusinessPath(businessPath));
        }
        if (after != null) {
            query.setTimestamp("after", after);
        }
        if (before != null) {
            query.setTimestamp("before", before);
        }

        return query;
    }

    private StringBuilder appendAnd(final StringBuilder sb, final String query) {
        if (sb.indexOf("where") > 0) {
            sb.append(" and ");
        } else {
            sb.append(" where ");
        }
        sb.append(query);
        return sb;
    }

    private String normalizeBusinessPath(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("ROOT")) {
            url = url.substring(4, url.length());
        }
        final List<String> tokens = new ArrayList<String>();
        for (final StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens();) {
            final String token = tokenizer.nextToken();
            if (!tokens.contains(token)) {
                tokens.add(token);
            }
        }

        final StringBuilder sb = new StringBuilder();
        for (final String token : tokens) {
            sb.append('[').append(token).append(']');
        }
        return sb.toString();
    }
}
