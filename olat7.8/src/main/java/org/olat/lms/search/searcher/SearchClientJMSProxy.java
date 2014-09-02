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

package org.olat.lms.search.searcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.ServiceNotAvailableException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This is a client side search proxy - delegates the search to the remote searcher.
 * <P>
 * Initial Date: 03.06.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class SearchClientJMSProxy implements SearchClient {

    private static final Logger log = LoggerHelper.getLogger();

    protected static final String JMS_RESPONSE_STATUS_PROPERTY_NAME = "response_status";
    protected static final String JMS_RESPONSE_STATUS_OK = "ok";
    protected static final String JMS_RESPONSE_STATUS_QUERY_EXCEPTION = "query_exception";
    protected static final String JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION = "service_not_available";

    private long queryCount_ = 0; // counter for this cluster node
    private ConnectionFactory connectionFactory_;
    private Queue searchQueue_;
    private long receiveTimeout_ = 45000;
    private long timeToLive_ = 45000;
    private Connection connection_;
    private final LinkedList<Destination> tempQueues_ = new LinkedList<Destination>();
    private final LinkedList<Session> sessions_ = new LinkedList<Session>();

    /**
     * [used by spring]
     */
    private SearchClientJMSProxy() {
        super();
    }

    public void setConnectionFactory(final ConnectionFactory conFac) {
        connectionFactory_ = conFac;
    }

    public void setSearchQueue(final Queue searchQueue) {
        this.searchQueue_ = searchQueue;
    }

    public void setReceiveTimeout(final long receiveTimeout) {
        this.receiveTimeout_ = receiveTimeout;
    }

    public void setTimeToLive(final long timeToLive) {
        this.timeToLive_ = timeToLive;
    }

    public void springInit() throws JMSException {
        connection_ = connectionFactory_.createConnection();
        connection_.start();
        log.info("springInit: JMS connection started with connectionFactory=" + connectionFactory_);
    }

    private synchronized Destination acquireTempQueue(final Session session) throws JMSException {
        if (tempQueues_.size() == 0) {
            if (session == null) {
                final Session s = connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
                final Destination tempQ = s.createTemporaryQueue();
                s.close();
                return tempQ;
            }
            return session.createTemporaryQueue();
        } else {
            return tempQueues_.removeFirst();
        }
    }

    private synchronized Session acquireSession() throws JMSException {
        if (sessions_.size() == 0) {
            return connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } else {
            return sessions_.removeFirst();
        }
    }

    private synchronized void releaseTempQueue(final Destination tempQueue) {
        if (tempQueue == null) {
            return;
        }
        tempQueues_.addLast(tempQueue);
    }

    private synchronized void releaseSession(final Session session) {
        if (session == null) {
            return;
        }
        sessions_.addLast(session);
    }

    /**
     * Uses Request/reply mechanism for synchronous operation.
     */
    @Override
    public SearchResults doSearch(final String queryString, final List<String> condQueries, final Identity identity, final Roles roles, final int firstResult,
            final int maxResults, final boolean doHighlighting) throws ServiceNotAvailableException, QueryException {
        if (log.isDebugEnabled()) {
            log.debug("STARTqueryString=" + queryString);
        }
        final SearchRequest searchRequest = new SearchRequest(queryString, condQueries, identity.getKey(), roles, firstResult, maxResults, doHighlighting);
        Session session = null;
        try {
            session = acquireSession();
            if (log.isDebugEnabled()) {
                log.debug("doSearch session=" + session);
            }
            final Message requestMessage = session.createObjectMessage(searchRequest);
            final Message returnedMessage = doSearchRequest(session, requestMessage);
            queryCount_++;
            if (returnedMessage != null) {
                final String responseStatus = returnedMessage.getStringProperty(JMS_RESPONSE_STATUS_PROPERTY_NAME);
                if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_OK)) {
                    final SearchResults searchResult = (SearchResults) ((ObjectMessage) returnedMessage).getObject();
                    if (log.isDebugEnabled()) {
                        log.debug("ENDqueryString=" + queryString);
                    }
                    return searchResult;
                } else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_QUERY_EXCEPTION)) {
                    throw new QueryException("invalid query=" + queryString);
                } else if (responseStatus.equalsIgnoreCase(JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION)) {
                    throw new ServiceNotAvailableException("Remote search service not available" + queryString);
                } else {
                    log.warn("doSearch: receive unkown responseStatus=" + responseStatus);
                    return null;
                }
            } else {
                // null returnedMessage
                throw new ServiceNotAvailableException("communication error with JMS - cannot receive messages!!!");
            }
        } catch (final JMSException e) {
            log.error("Search failure I", e);
            throw new ServiceNotAvailableException("communication error with JMS - cannot send messages!!!");
        } finally {
            releaseSession(session);
        }
    }

    /**
     * Uses Request/reply mechanism for synchronous operation.
     * 
     */
    @Override
    public Set<String> spellCheck(final String query) throws ServiceNotAvailableException {
        Session session = null;
        try {
            session = acquireSession();
            final TextMessage requestMessage = session.createTextMessage(query);
            final Message returnedMessage = doSearchRequest(session, requestMessage);
            if (returnedMessage != null) {
                final List<String> spellStringList = (List<String>) ((ObjectMessage) returnedMessage).getObject();
                return new HashSet<String>(spellStringList);
            } else {
                // null returnedMessage
                throw new ServiceNotAvailableException("spellCheck, communication error with JMS - cannot receive messages!!!");
            }
        } catch (final JMSException e) {
            throw new ServiceNotAvailableException("spellCheck, communication error with JMS - cannot send messages!!!");
        } catch (final ServiceNotAvailableException e) {
            throw e;
        } catch (final Throwable th) {
            throw new OLATRuntimeException("ClusteredSearchRequester.spellCheck() error!!!", th);
        } finally {
            releaseSession(session);
        }
    }

    private String createRandomString() {
        final Random random = new Random(System.currentTimeMillis());
        final long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    /**
     * Returns the queryCount number for this cluster node.
     * 
     */
    public long getQueryCount() {
        return queryCount_;
    }

    public void stop() {
        try {
            for (final Iterator iterator = sessions_.iterator(); iterator.hasNext();) {
                final Session session = (Session) iterator.next();
                session.close();
            }
            connection_.close();
            log.info("ClusteredSearchRequester stopped");
        } catch (final JMSException e) {
            log.error("Exception in stop ClusteredSearchRequester , ");
        }
    }

    private Message doSearchRequest(final Session session, final Message message) throws JMSException {
        final Destination replyQueue = acquireTempQueue(session);
        if (log.isDebugEnabled()) {
            log.debug("doSearchRequest replyQueue=" + replyQueue);
        }
        try {
            final MessageConsumer responseConsumer = session.createConsumer(replyQueue);

            message.setJMSReplyTo(replyQueue);
            final String correlationId = createRandomString();
            message.setJMSCorrelationID(correlationId);

            final MessageProducer producer = session.createProducer(searchQueue_);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.setTimeToLive(timeToLive_);
            if (log.isDebugEnabled()) {
                log.debug("Sending search request message with correlationId=" + correlationId);
            }
            producer.send(message);
            producer.close();

            Message returnedMessage = null;
            final long start = System.currentTimeMillis();
            while (true) {
                final long diff = (start + receiveTimeout_) - System.currentTimeMillis();
                if (diff <= 0) {
                    // timeout
                    log.info("Timeout in search. Remaining time zero or negative.");
                    break;
                }
                if (log.isDebugEnabled()) {
                    log.debug("doSearchRequest: call receive with timeout=" + diff);
                }
                returnedMessage = responseConsumer.receive(diff);
                if (returnedMessage == null) {
                    // timeout case, we're stopping now with a reply...
                    log.info("Timeout in search. Reply was null.");
                    break;
                } else if (!correlationId.equals(returnedMessage.getJMSCorrelationID())) {
                    // we got an old reply from a previous search request
                    log.info("Got a response with a wrong correlationId. Ignoring and waiting for the next");
                    continue;
                } else {
                    // we got a valid reply
                    break;
                }
            }
            responseConsumer.close();
            if (log.isDebugEnabled()) {
                log.debug("doSearchRequest: returnedMessage=" + returnedMessage);
            }
            return returnedMessage;
        } finally {
            releaseTempQueue(replyQueue);
        }
    }

}
