package org.olat.lms.instantmessaging;

import org.jivesoftware.smack.packet.Packet;
import org.olat.system.event.Event;

public class InstantMessagingEvent extends Event {

    private final Packet packet;

    public InstantMessagingEvent(final Packet packet, final String command) {
        super(command);
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

}
