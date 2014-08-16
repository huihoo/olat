package org.olat.lms.instantmessaging.task;

import java.util.Date;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.olat.lms.instantmessaging.InstantMessagingEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;

public class GroupChatJoinTask implements Runnable {

    MultiUserChat muc;
    XMPPConnection connection;
    String roomJID;
    String nickname;
    GenericEventListener listeningController;
    private PacketListener messageListener;
    private PacketListener participationsListener;
    String roomName;
    private static final Logger log = LoggerHelper.getLogger();

    private final OLATResourceable ores;

    public GroupChatJoinTask(final OLATResourceable ores, final MultiUserChat muc, final XMPPConnection connection, final String roomJID, final String nickname,
            final String roomName, final GenericEventListener listeningController) {
        this.ores = ores;
        this.muc = muc;
        this.connection = connection;
        this.roomJID = roomJID;
        this.roomName = roomName;
        this.nickname = nickname;
        this.listeningController = listeningController;
    }

    @Override
    public void run() {
        // o_clusterOK by guido
        final OLATResourceable syncOres = OresHelper.createOLATResourceableInstance("GroupChatJoinTask", ores.getResourceableId());
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(syncOres, new SyncerExecutor() {
            @Override
            public void execute() {
                if (connection != null && connection.isAuthenticated()) {
                    try {
                        final RoomInfo info = MultiUserChat.getRoomInfo(connection, roomJID);
                        // throws an exception if the room does not
                        // exists
                        muc.join(nickname); // log in anonymous
                        addMessageListener();
                        addParticipationsListener();
                    } catch (final XMPPException e) {
                        try {
                            if (e.getXMPPError().getCode() == 404) {
                                muc.create(nickname);
                                final Form form = muc.getConfigurationForm();
                                final Form answer = form.createAnswerForm();

                                final FormField fieldRoomName = new FormField("muc#roomconfig_roomname");
                                fieldRoomName.addValue(roomName);
                                answer.addField(fieldRoomName);

                                final FormField fieldMaxUsers = new FormField("muc#roomconfig_maxusers");
                                fieldMaxUsers.addValue("0");// 0 means
                                                            // unlimited
                                answer.addField(fieldMaxUsers);

                                muc.sendConfigurationForm(answer);
                                addMessageListener();
                                addParticipationsListener();

                                listeningController.event(new Event("ready"));

                            } else {
                                log.warn(
                                        "Could not join group chatroom (case 1): "
                                                + roomName
                                                + " for user: "
                                                + nickname
                                                + ". "
                                                + "Check your configuration if you get service unavailable error (503), the dns name for the property has to be exaclty the same as in the server config and localhost does normally not work",
                                        e);
                                log.warn("Beware of openfire 3.7.0 which is buggy and groupchat does not work properly. Use a newer version!");
                                removeListeners();
                            }
                        } catch (final XMPPException e1) {
                            log.warn("Could not create group chatroom(case 2): " + roomName + " for user: " + nickname, e1);
                        } catch (final Exception e1) {
                            /**
                             * this case happens when the servername is set to localhost or the servername is not configured the same as in OLAT config file and Openfire
                             * (need restart when chanched)
                             */
                            log.warn("Could not create group chatroom(case 3): " + roomName + " for user: " + nickname, e1);
                        }
                    } catch (final Exception e) {// we catch also
                                                 // nullpointers that
                                                 // may occure in
                                                 // smack lib.
                        log.warn("Could not create group chatroom(case 4): " + roomName + " for user: " + nickname, e);
                    }
                }
            }
        });
    }

    void removeListeners() {
        if (messageListener != null) {
            muc.removeMessageListener(messageListener);
        }
        if (participationsListener != null) {
            muc.removeParticipantListener(participationsListener);
        }
    }

    /**
     * listens to new messages for this chatroom
     */
    void addMessageListener() {
        messageListener = new PacketListener() {

            @Override
            public void processPacket(final Packet packet) {
                final Message jabbmessage = (Message) packet;
                if (log.isDebugEnabled()) {
                    log.debug("processPacket Msg: to=" + jabbmessage.getTo());
                }
                jabbmessage.setProperty("receiveTime", new Long(new Date().getTime()));
                if ((jabbmessage.getType() == Message.Type.groupchat) && jabbmessage.getBody() != null) {
                    listeningController.event(new InstantMessagingEvent(jabbmessage, "groupchat"));
                }
            }
        };
        muc.addMessageListener(messageListener);
    }

    /**
     * listen to new people joining the room in realtime and and set the new content which sets the component to dirty which forces it to redraw
     */
    void addParticipationsListener() {
        participationsListener = new PacketListener() {

            @Override
            public void processPacket(final Packet packet) {
                final Presence presence = (Presence) packet;
                if (log.isDebugEnabled()) {
                    log.debug("processPacket Presence: to=" + presence.getTo() + " , ");
                }
                if (presence.getFrom() != null) {
                    listeningController.event(new InstantMessagingEvent(presence, "participant"));
                }
            }
        };
        muc.addParticipantListener(participationsListener);

    }

    public PacketListener getMessageListener() {
        return messageListener;
    }

    public PacketListener getParticipationsListener() {
        return participationsListener;
    }

}
