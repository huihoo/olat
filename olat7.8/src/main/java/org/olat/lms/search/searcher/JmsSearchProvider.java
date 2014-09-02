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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.taskexecutor.TaskExecutorService;
import org.olat.lms.search.SearchResults;
import org.olat.lms.search.SearchService;
import org.olat.lms.search.ServiceNotAvailableException;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This is a server side search proxy - delegates the search to the searchWorker.
 * <P>
 * Initial Date: 02.06.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class JmsSearchProvider implements MessageListener {

    private static final Logger log = LoggerHelper.getLogger();

    private SearchService searchService;
    private ConnectionFactory connectionFactory_;
    private Connection connection_;
    private Queue searchQueue_;
    private Session session_;
    private MessageConsumer consumer_;
    private final LinkedList<Session> sessions_ = new LinkedList<Session>();
    private long receiveTimeout = 60000;
    @Autowired
    TaskExecutorService taskExecutorService;
    @Autowired
    BaseSecurity baseSecurity;

    /**
     * [used by spring]
     */
    protected JmsSearchProvider() {
        // default constructor
    }

    public void setConnectionFactory(final ConnectionFactory conFac) {
        connectionFactory_ = conFac;
    }

    public void setSearchQueue(final Queue searchQueue) {
        this.searchQueue_ = searchQueue;
    }

    public void setReceiveTimeout(final long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Delegates execution to the searchService.
     * 
     */
    public SearchResults doSearch(final String queryString, final List<String> condQueries, final Identity identity, final Roles roles, final int firstResult,
            final int maxResults, final boolean doHighlighting) throws ServiceNotAvailableException, ParseException, QueryException {
        if (searchService == null) {
            throw new AssertException("searchService in ClusteredSearchProvider is null, please check the search configuration!");
        }
        return searchService.doSearch(queryString, condQueries, identity, roles, firstResult, maxResults, doHighlighting);
    }

    public Set<String> spellCheck(final String query) throws ServiceNotAvailableException {
        if (searchService == null) {
            throw new AssertException("searchService in ClusteredSearchProvider is null, please check the search configuration!");
        }
        return searchService.spellCheck(query);
    }

    /**
	 */
    public long getQueryCount() {
        if (searchService == null) {
            throw new AssertException("searchService in ClusteredSearchProvider is null, please check the search configuration!");
        }
        return searchService.getQueryCount();
    }

    /**
	 */
    public void stop() {
        if (searchService == null) {
            throw new AssertException("searchService in ClusteredSearchProvider is null, please check the search configuration!");
        }
        searchService.stop();
        try {
            session_.close();
            connection_.close();
            log.info("ClusteredSearchProvider stopped");
        } catch (final JMSException e) {
            log.warn("Exception in stop ClusteredSearchProvider, ", e);
        }
    }

    /**
     * [used by spring]
     * 
     */
    public void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    public void springInit() throws JMSException {
        connection_ = connectionFactory_.createConnection();
        session_ = connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer_ = session_.createConsumer(searchQueue_);
        consumer_.setMessageListener(this);
        connection_.start();
        log.info("ClusteredSearchProvider JMS started");
    }

    @Override
    public void onMessage(final Message message) {
        if (log.isDebugEnabled()) {
            log.debug("onMessage, message=" + message);
        }
        try {
            final long sentTimestamp = message.getJMSTimestamp();
            final long currentTimestamp = System.currentTimeMillis();
            // check if received message is not too old because in case of overload we could have old search-messages
            if ((currentTimestamp - sentTimestamp) < receiveTimeout) {
                final String correlationID = message.getJMSCorrelationID();
                final Destination replyTo = message.getJMSReplyTo();
                if (message instanceof ObjectMessage) {
                    final ObjectMessage objectMessage = (ObjectMessage) message;
                    final SearchRequest searchRequest = (SearchRequest) objectMessage.getObject();
                    taskExecutorService.runTask(new Runnable() {

                        @Override
                        public void run() {
                            onSearchMessage(searchRequest, correlationID, replyTo);
                        }

                    });
                } else if (message instanceof TextMessage) {
                    final TextMessage testMessage = (TextMessage) message;
                    final String spellText = testMessage.getText();
                    taskExecutorService.runTask(new Runnable() {

                        @Override
                        public void run() {
                            onSpellMessage(spellText, correlationID, replyTo);
                        }

                    });
                }
            } else {
                // JMS message is too old, discard it (do nothing)
                log.warn("JMS message was too old, discard message,  timeout=" + receiveTimeout + "ms , received time=" + (currentTimestamp - sentTimestamp) + "ms");
            }
        } catch (final JMSException e) {
            log.error("error when receiving jms messages", e);
            return; // signal search not available
        } catch (final Error err) {
            log.warn("Error in onMessage, ", err);
            // OLAT-3973: don't throw exceptions here
        } catch (final RuntimeException runEx) {
            log.warn("RuntimeException in onMessage, ", runEx);
            // OLAT-3973: don't throw exceptions here
        }
    }

    private synchronized Session acquireSession() throws JMSException {
        if (sessions_.size() == 0) {
            return connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } else {
            return sessions_.getFirst();
        }
    }

    private synchronized void releaseSession(final Session session) {
        if (session == null) {
            return;
        }
        sessions_.addLast(session);
    }

    void onSearchMessage(final SearchRequest searchRequest, final String correlationID, final Destination replyTo) {
        if (log.isDebugEnabled()) {
            log.debug("onSearchMessage, correlationID=" + correlationID + " , replyTo=" + replyTo + " , searchRequest=" + searchRequest);
        }
        Session session = null;
        try {
            final Identity identity = baseSecurity.loadIdentityByKey(searchRequest.getIdentityId());

            final SearchResults searchResults = this.doSearch(searchRequest.getQueryString(), searchRequest.getCondQueries(), identity, searchRequest.getRoles(),
                    searchRequest.getFirstResult(), searchRequest.getMaxResults(), searchRequest.isDoHighlighting());
            if (log.isDebugEnabled()) {
                log.debug("searchResults: " + searchResults.getLength());
            }
            if (searchResults != null) {
                session = acquireSession();
                final Message responseMessage = session.createObjectMessage(searchResults);
                responseMessage.setJMSCorrelationID(correlationID);
                responseMessage.setStringProperty(SearchClientJMSProxy.JMS_RESPONSE_STATUS_PROPERTY_NAME, SearchClientJMSProxy.JMS_RESPONSE_STATUS_OK);
                final MessageProducer producer = session.createProducer(replyTo);
                if (log.isDebugEnabled()) {
                    log.debug("onSearchMessage, send ResponseMessage=" + responseMessage + " to replyTo=" + replyTo);
                }
                producer.send(responseMessage);
                producer.close();
                return;
            } else {
                log.info("onSearchMessage, no searchResults (searchResults=null)");
            }
        } catch (final JMSException e) {
            log.error("error when receiving jms messages", e);
            return; // signal search not available
            // do not throw exceptions here throw new OLATRuntimeException();
        } catch (final ServiceNotAvailableException sex) {
            sendErrorResponse(SearchClientJMSProxy.JMS_RESPONSE_STATUS_SERVICE_NOT_AVAILABLE_EXCEPTION, correlationID, replyTo);
        } catch (final QueryException qex) {
            sendErrorResponse(SearchClientJMSProxy.JMS_RESPONSE_STATUS_QUERY_EXCEPTION, correlationID, replyTo);
        } catch (final Throwable th) {
            log.error("error at ClusteredSearchProvider.receive()", th);
            return;// signal search not available
            // do not throw exceptions throw new OLATRuntimeException();
        } finally {
            releaseSession(session);
            DBFactory.getInstance().commitAndCloseSession();
        }
    }

    private void sendErrorResponse(final String jmsResponseStatus, final String correlationID, final Destination replyTo) {
        Session session = null;
        try {
            session = acquireSession();
            final Message responseMessage = session.createObjectMessage();
            responseMessage.setJMSCorrelationID(correlationID);
            responseMessage.setStringProperty(SearchClientJMSProxy.JMS_RESPONSE_STATUS_PROPERTY_NAME, jmsResponseStatus);
            final MessageProducer producer = session.createProducer(replyTo);
            if (log.isDebugEnabled()) {
                log.debug("onSearchMessage, send ResponseMessage=" + responseMessage + " to replyTo=" + replyTo);
            }
            producer.send(responseMessage);
            producer.close();
            return;

        } catch (final JMSException e) {
            log.error("error when receiving jms messages", e);
            return; // signal search not available
        } finally {
            releaseSession(session);
        }
    }

    void onSpellMessage(final String spellText, final String correlationID, final Destination replyTo) {
        Session session = null;
        try {
            final Set<String> spellStrings = this.spellCheck(spellText);
            if (spellStrings != null) {
                final ArrayList<String> spellStringList = new ArrayList<String>(spellStrings);
                session = acquireSession();
                final Message responseMessage = session.createObjectMessage(spellStringList);
                responseMessage.setJMSCorrelationID(correlationID);
                final MessageProducer producer = session.createProducer(replyTo);
                producer.send(responseMessage);
                producer.close();
                return;
            }
            return; // signal search not available
        } catch (final JMSException e) {
            log.error("error when receiving jms messages", e);
            return; // signal search not available
            // do not throw exceptions here throw new OLATRuntimeException();
        } catch (final Throwable th) {
            log.error("error at ClusteredSearchProvider.receive()", th);
            return;// signal search not available
            // do not throw exceptions throw new OLATRuntimeException();
        } finally {
            releaseSession(session);
            DBFactory.getInstance().commitAndCloseSession();
        }
    }

}
