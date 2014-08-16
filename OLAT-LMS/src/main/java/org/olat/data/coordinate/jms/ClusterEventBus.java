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
package org.olat.data.coordinate.jms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.cache.cluster.ClusterConfig;
import org.olat.system.coordinate.jms.JMSWrapper;
import org.olat.system.coordinate.jms.PerfItem;
import org.olat.system.coordinate.jms.SimpleProbe;
import org.olat.system.event.AbstractEventBus;
import org.olat.system.event.Event;
import org.olat.system.event.EventLogger;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.event.businfo.BusListenerInfo;
import org.olat.system.event.businfo.BusListenerInfos;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class realizes a clustered (multiple java vm) system event bus. it uses JMS (per default, apache activeMQ 4.1.4 is configured using spring) as an implementation.
 * 
 * @author Felix Jost
 */
public class ClusterEventBus extends AbstractEventBus implements MessageListener, GenericEventListener {
    private static final Logger log = LoggerHelper.getLogger();

    // ores helper is limited to 50 character, so truncate it
    static OLATResourceable CLUSTER_CHANNEL = OresHelper.createOLATResourceableType(ClusterEventBus.class.getName());

    public ClusterConfig clusterConfig;

    // settings
    long sendInterval = 1000; // 1000 miliseconds between each "ping/alive/info" message, can be set using spring
    long jmsMsgDelayLimit = 5000; // max duration of ClusterInfoEvent send-receive time in ms

    // counters
    private long latestSentMsgId = -1;
    private long numOfSentMessages = 0;

    // stats
    private final List<String> msgsSent = new ArrayList<String>();
    private final List<String> msgsReceived = new ArrayList<String>();
    private int msgsSentCount = 0;
    private int msgsReceivedCount = 0;

    // latest incoming info from other Nodes
    private final Map<Integer, NodeInfo> nodeInfos = new HashMap<Integer, NodeInfo>();

    private final int maxListSize = 10; // how many entries are kept in the outbound/inbound history. Just for administrative purposes

    // for bookkeeping how many resources have how many listeners
    public BusListenerInfos busInfos = new BusListenerInfos();
    protected boolean isClusterInfoEventThreadRunning = true;
    private ConnectionFactory connectionFactory;
    private Topic destination;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private MessageProducer producer;

    private long lastOnMessageFinishTime_ = -1;
    private final SimpleProbe mrtgProbeJMSLoad_ = new SimpleProbe();
    private final SimpleProbe mrtgProbeJMSDeliveryTime_ = new SimpleProbe();
    private final SimpleProbe mrtgProbeJMSProcessingTime_ = new SimpleProbe();

    private final SimpleProbe mrtgProbeJMSEnqueueTime_ = new SimpleProbe();
    final LinkedList<Object> incomingMessagesQueue_ = new LinkedList<Object>();

    private final static int LIMIT_ON_INCOMING_MESSAGE_QUEUE = 200;

    @Autowired
    private EventLogger eventLogger;

    /**
     * [used by spring]
     * 
     * @param jmsTemplate
     */
    ClusterEventBus() {
        super();
    }

    public void springInit() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);
        producer = session.createProducer(destination);

        connection.start();
        log.info("ClusterEventBus JMS started");

        final Integer nodeId = clusterConfig.getNodeId();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // send an infopacket to all olat nodes at regular intervals.
                while (isClusterInfoEventThreadRunning) {
                    try {
                        final ClusterInfoEvent cie = new ClusterInfoEvent(clusterConfig, createBusListenerInfo());
                        fireEventToListenersOf(cie, CLUSTER_CHANNEL);
                        if (log.isDebugEnabled()) {
                            log.debug("sent via jms clusterInfoEvent with timestamp:" + cie.getCreated() + " from node:" + nodeId);
                        }
                    } catch (final Exception e) {
                        // log error, but do not throw exception, but retry.
                        try {
                            log.error("error while sending ClusterInfoEvent", e);
                        } catch (final NullPointerException nex) {
                            // ignore, could happen when shutting down
                            System.err.println("ClusterEventBus : error while sending ClusterInfoEvent, could happen in shutting down, Ex=" + e);
                        }
                    }
                    try {
                        Thread.sleep(sendInterval);
                    } catch (final InterruptedException e) {
                        // ignore
                    }
                }
                try {
                    log.info("ClusterEventBus stopped, do no longer send ClusterInfoEvents");
                } catch (final NullPointerException nex) {
                    System.err.println("ClusterEventBus stopped, do no longer send ClusterInfoEvents");
                }
            }
        });
        t.setDaemon(true); // VM can shutdown even when this thread is still running
        t.start();
        // register to listen for other nodes' clusterinfoevents
        this.registerFor(this, null, CLUSTER_CHANNEL);

        final Thread serveThread = new Thread(new Runnable() {

            @Override
            public void run() {
                eventLogger.initEmptyLogger();
                while (true) {
                    try {
                        Message m = null;
                        long time = -1;
                        synchronized (incomingMessagesQueue_) {
                            while (incomingMessagesQueue_.size() < 2) {
                                try {
                                    incomingMessagesQueue_.wait();
                                } catch (final InterruptedException e) {
                                    // ignore
                                }
                            }
                            m = (Message) incomingMessagesQueue_.removeLast();
                            time = (Long) incomingMessagesQueue_.removeLast();
                            incomingMessagesQueue_.notifyAll();
                        }
                        serveMessage(m, time);
                    } catch (final RuntimeException re) {
                        log.error("RuntimeException enountered by serve-thread:", re);
                        // continue
                    } catch (final Error er) {
                        log.error("Error enountered by serve-thread:", er);
                        // continue
                    }
                }

            }

        });
        serveThread.setDaemon(true);
        serveThread.start();
    }

    public SimpleProbe getMrtgProbeJMSDeliveryTime() {
        return mrtgProbeJMSDeliveryTime_;
    }

    public SimpleProbe getMrtgProbeJMSProcessingTime() {
        return mrtgProbeJMSProcessingTime_;
    }

    public SimpleProbe getMrtgProbeJMSLoad() {
        return mrtgProbeJMSLoad_;
    }

    public SimpleProbe getMrtgProbeJMSEnqueueTime() {
        return mrtgProbeJMSEnqueueTime_;
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void event(final Event event) {
        // we listen only on our own channel, the event must be a clusterInfoEvent.
        final ClusterInfoEvent cie = (ClusterInfoEvent) event;
        final Integer nodeId = cie.getConfig().getNodeId();
        final NodeInfo ni = getNodeInfoFor(nodeId);
        ni.update(cie);
        // check duration of send-receive ClusterInfoEvent
        final long now = System.currentTimeMillis();
        if ((now - cie.getCreated()) > jmsMsgDelayLimit) {
            log.warn("JMS-Performance problem: JMS-Message delay is too big, send-receive took:" + (now - cie.getCreated()) + "ms. event=" + event);
        }

        // update the eventBusInfo from the node
        final BusListenerInfo busInfo = cie.getBusListenerInfo();
        busInfos.updateInfoFor(nodeId, busInfo);
    }

    /**
     * this implementation must sum up all counts from all cluster nodes to return the correct number.
     */
    @Override
    public int getListeningIdentityCntFor(final OLATResourceable ores) {
        return busInfos.getListenerCountFor(ores);
    }

    /**
	 */
    @Override
    public void fireEventToListenersOf(final MultiUserEvent event, final OLATResourceable ores) {

        // 1. fire directly within vm, because it used to be so before, and in this way this olat node can run even if jms is down
        doFire(event, ores, eventLogger);

        // 2. send the event wrapped over jms to all nodes
        // (the receiver will detect whether messages are from itself and thus can be ignored, since they were already sent directly.
        long msgId;
        Integer nodeId;

        nodeId = clusterConfig.getNodeId();
        try {
            // <XXX> TODO: cg/18.11.2008 ev JMS performance bottleneck; Do not check message-sequence => remove sync-block
            synchronized (this) { // cluster_ok needed, not atomar read in one vm
                msgId = ++latestSentMsgId;
                final ObjectMessage message = session.createObjectMessage();
                message.setObject(new JMSWrapper(nodeId, msgId, ores, event));
                producer.send(message);
            }
        } catch (final Exception e) {
            // cluster:::: what shall we do here: the JMS bus is broken! and we thus cannot know if other nodes are alive.
            // if we are the only node running, then we could continue.
            // a) either throw an exception - meaning olat doesn't really run at all and produces redscreens all the time and logging in is not possible.
            // b) or warn in the log/jmx - but surveillance is critical here!!
            // -> do the more fail-fast option a) at the moment for correctness reasons.
            System.err.println("###############################################################################################");
            System.err.println("### ClusterEventBus: communication error with JMS - cannot send messages!!!" + e);
            System.err.println("###############################################################################################");

            throw new OLATRuntimeException("communication error with JMS - cannot send messages!!!", e);
        }
        numOfSentMessages++;

        // store it for later access by the admin controller
        final String sentMsg = "sent msg: from node:" + nodeId + ", olat-id:" + msgId + ", ores:" + ores.getResourceableTypeName() + ":" + ores.getResourceableId()
                + ", event:" + event;
        addToSentScreen(sentMsg);
        if (log.isDebugEnabled()) {
            log.debug(sentMsg);
        }
    }

    /**
     * called by springs org.springframework.jms.listener.DefaultMessageListenerContainer, see coredefaultconfig.xml we receive a message here on the topic reserved for
     * olat system bus messages.
     */
    @Override
    public void onMessage(final Message message) {
        synchronized (incomingMessagesQueue_) {
            while (incomingMessagesQueue_.size() > LIMIT_ON_INCOMING_MESSAGE_QUEUE) {
                try {
                    incomingMessagesQueue_.wait();
                } catch (final InterruptedException e) {
                    // this empty catch is okay
                }
            }
            incomingMessagesQueue_.addFirst(message);
            incomingMessagesQueue_.addFirst(System.currentTimeMillis());
            incomingMessagesQueue_.notifyAll();
        }
    }

    void serveMessage(final Message message, final long receiveEnqueueTime) {
        // stats
        final long receiveTime = System.currentTimeMillis();
        if (receiveEnqueueTime > 0) {
            final long diff = receiveTime - receiveEnqueueTime;
            mrtgProbeJMSEnqueueTime_.addMeasurement(diff);
        }
        if (lastOnMessageFinishTime_ != -1) {
            final long waitingTime = receiveTime - lastOnMessageFinishTime_;
            // the waiting time is inverted to represent more like a frequency
            // the values it translates to are the following:
            // 0ms -> 100
            // 1ms -> 66
            // 2ms -> 50
            // 4ms -> 33
            // 6ms -> 25
            // 8ms -> 20
            // 18ms -> 10
            // 20ms -> 9
            // 23ms -> 8
            // 26.5ms -> 7
            // 31ms -> 6
            // 38ms -> 5
            mrtgProbeJMSLoad_.addMeasurement((long) (100.0 / ((waitingTime / 2.0) + 1.0)));
            lastOnMessageFinishTime_ = -1;
        }

        final ObjectMessage om = (ObjectMessage) message;
        try {
            // unpack
            final JMSWrapper jmsWrapper = (JMSWrapper) om.getObject();
            final Integer nodeId = jmsWrapper.getNodeId();
            final MultiUserEvent event = jmsWrapper.getMultiUserEvent();
            final OLATResourceable ores = jmsWrapper.getOres();
            final boolean fromSameNode = clusterConfig.getNodeId().equals(nodeId);

            // update nodeinfo statistics
            final NodeInfo nodeInfo = getNodeInfoFor(nodeId);
            if (!nodeInfo.update(jmsWrapper)) {
                log.warn("onMessage: update failed. clustereventbus: " + this);
            }

            final String recMsg = "received msg: " + (fromSameNode ? "[same node]" : "") + " from node:" + nodeId + ", olat-id:" + jmsWrapper.getMsgId() + ", ores:"
                    + ores.getResourceableTypeName() + ":" + ores.getResourceableId() + ", event:" + event + "}";

            // stats
            final long jmsTimestamp = om.getJMSTimestamp();
            if (jmsTimestamp != 0) {
                final long deliveryTime = receiveTime - jmsTimestamp;
                if (deliveryTime > 1500) {
                    // then issue a log statement
                    log.warn("message received with long delivery time (longer than 1500ms: " + deliveryTime + "): " + recMsg);
                }
                mrtgProbeJMSDeliveryTime_.addMeasurement(deliveryTime);
            }

            addToReceivedScreen(recMsg);
            if (log.isDebugEnabled()) {
                log.debug(recMsg);
            }

            // message with destination and source both having this vm are ignored here, since they were already
            // "inline routed" when having been sent (direct call within the vm).
            if (!fromSameNode) {
                // distribute the unmarshalled event to all JVM wide listeners for this channel.
                doFire(event, ores, eventLogger);
                DBFactory.getInstance(false).commitAndCloseSession();
            } // else message already sent "in-vm"

            // stats
            final long doneTime = System.currentTimeMillis();
            final long processingTime = doneTime - receiveTime;
            if (processingTime > 500) {
                // then issue a log statement
                log.warn("message received with long processing time (longer than 500ms: " + processingTime + "): " + recMsg);
            }
            mrtgProbeJMSProcessingTime_.addMeasurement(processingTime);
        } catch (final Error er) {
            log.error("Uncaught Error in ClusterEventBus.onMessage!", er);
            throw er;
        } catch (final RuntimeException re) {
            log.error("Uncaught RuntimeException in ClusterEventBus.onMessage!", re);
            throw re;
        } catch (final JMSException e) {
            log.warn("JMSException in ClusterEventBus.onMessage", e);
            throw new OLATRuntimeException("error when receiving jms messages", e);
        } catch (final Throwable th) {
            log.error("Uncaught Throwable in ClusterEventBus.onMessage!", th);
        } finally {
            lastOnMessageFinishTime_ = System.currentTimeMillis();
        }
    }

    private NodeInfo getNodeInfoFor(final Integer nodeId) {
        synchronized (nodeInfos) {// cluster_ok node info is per vm only
            NodeInfo f = nodeInfos.get(nodeId);
            if (f == null) {
                f = new NodeInfo(nodeId);
                nodeInfos.put(nodeId, f);
            }
            return f;
        }
    }

    /**
     * [used by spring]
     */
    public void setClusterConfig(final ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    /**
     * [used by spring to auto export mbean data]
     * 
     * @return the number of sent cluster event bus message since startup of this java vm
     */
    public long getNumOfSentMessages() {
        return numOfSentMessages;
    }

    /**
     * [used by spring to auto export mbean data]
     * 
     * @return the id of the latest msg sent from this cluster
     */
    public long getLatestSentMsgId() {
        return latestSentMsgId;
    }

    /**
     * [used by jmx] cluster:::: to be improved: this is just a quick solution to output all data from all nodes
     * 
     * @return jmx-readable data of all statistics of all foreign cluster nodes
     */
    public CompositeDataSupport getForeignClusterNodeStatistics() {
        final Map<String, String> p = new HashMap<String, String>();
        for (final Integer key : nodeInfos.keySet()) {
            final NodeInfo fns = nodeInfos.get(key);
            final Integer nodeId = fns.getNodeId();
            p.put(nodeId + ".getLatestReceivedMsgId", "" + fns.getLatestReceivedMsgId());
            p.put(nodeId + ".getNumOfReceivedMessages", "" + fns.getNumOfReceivedMessages());
            p.put(nodeId + ".getNumOfMissedMsgs", "" + fns.getNumOfMissedMsgs());
        }
        return propertiesToCompositeData(p);
    }

    private CompositeDataSupport propertiesToCompositeData(final Map<String, ?> properties) {
        // try {
        try {
            final String[] keys = properties.keySet().toArray(new String[0]);
            final OpenType[] itemTypes = new OpenType[keys.length];
            for (int i = 0; i < itemTypes.length; i++) {
                itemTypes[i] = SimpleType.STRING;
            }
            CompositeType propsType;
            propsType = new CompositeType("Properties type", "properties", keys, keys, itemTypes);
            final CompositeDataSupport propsData = new CompositeDataSupport(propsType, properties);
            return propsData;
        } catch (final OpenDataException e) {
            throw new AssertException("problem with jmx data generation", e);
        }
    }

    public Map<Integer, NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public List<PerfItem> getPerfItems() {
        final List<PerfItem> l = new ArrayList<PerfItem>(2);
        l.add(new PerfItem("Cluster Events Sent", -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, msgsSentCount));
        l.add(new PerfItem("Cluster Events Received", -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, msgsReceivedCount));
        return l;
    }

    public void resetStats() {
        msgsSentCount = 0;
        msgsReceivedCount = 0;
    }

    private void addToSentScreen(final String msg) {
        synchronized (msgsSent) {// cluster_ok is per vm only
            msgsSentCount++;
            msgsSent.add(msg);
            if (msgsSent.size() > maxListSize) {
                msgsSent.remove(0);
            }
        }
    }

    private void addToReceivedScreen(final String msg) {
        synchronized (msgsReceived) {// cluster_ok is per vm only
            msgsReceivedCount++;
            msgsReceived.add(msg);
            if (msgsReceived.size() > maxListSize) {
                msgsReceived.remove(0);
            }
        }
    }

    /**
     * @return the copied list of the latest "maxListSize" received messages (copied so that iterating is failsafe)
     */
    public List<String> getListOfReceivedMsgs() {
        synchronized (msgsReceived) {// cluster_ok is per vm only
            return new ArrayList<String>(msgsReceived);
        }
    }

    /**
     * @return the copied list of the latest "maxListSize" sent messages (copied so that iterating is failsafe)
     */
    public List<String> getListOfSentMsgs() {
        synchronized (msgsSent) {// cluster_ok is per vm only
            return new ArrayList<String>(msgsSent);
        }
    }

    /**
     * [used by spring]
     */
    public void stop() {
        log.info("ClusterEventBus: Set stop flag for ClusterInfoEvent-Thread.");
        isClusterInfoEventThreadRunning = false;
        try {
            session.close();
            connection.close();
            log.info("ClusterEventBus stopped");
        } catch (final JMSException e) {
            log.warn("Exception in stop ClusteredSearchProvider, ", e);
        }
    }

    /**
     * [used by spring]
     */
    public void setSendInterval(final long sendInterval) {
        this.sendInterval = sendInterval;
    }

    /**
     * [used by spring]
     */
    public void setJmsMsgDelayLimit(final long jmsMsgDelayLimit) {
        this.jmsMsgDelayLimit = jmsMsgDelayLimit;
    }

    /**
     * [used by spring]
     */
    public void setConnectionFactory(final ConnectionFactory conFac) {
        this.connectionFactory = conFac;
    }

    public void setDestination(final Topic destination) {
        this.destination = destination;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
