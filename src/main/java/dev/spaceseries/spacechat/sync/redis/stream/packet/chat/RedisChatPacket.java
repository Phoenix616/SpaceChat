package dev.spaceseries.spacechat.sync.redis.stream.packet.chat;

import net.kyori.adventure.text.Component;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.sync.packet.SendStreamDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.PacketType;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisPacket;

import java.util.UUID;

public class RedisChatPacket extends RedisPacket implements SendStreamDataPacket<Void> {

    /**
     * Who the message was sent by
     */
    private UUID sender;

    /**
     * Sender name
     */
    private String senderName;

    /**
     * The chat channel that the user is currently in
     */
    private Channel channel;

    /**
     * The identifier of the server that the chat message is from
     */
    private String serverIdentifier;

    /**
     * The display name  of the server that the chat message is from
     */
    private String serverDisplayName;

    /**
     * The actual chat message as a component
     */
    private Component component;

    /**
     * Whether the sender can bypass ignores
     */
    private boolean canBypassIgnore;

    /**
     * Whether the sender can bypass disabled private chat
     */
    private boolean canBypassDisabled;

    /**
     * Construct redis chat message
     */
    public RedisChatPacket(UUID sender, String senderName, Channel channel, String serverIdentifier, String serverDisplayName, Component component, boolean canBypassIgnore, boolean canBypassDisabled) {
        this();
        this.sender = sender;
        this.senderName = senderName;
        this.channel = channel;
        this.serverIdentifier = serverIdentifier;
        this.serverDisplayName = serverDisplayName;
        this.component = component;
        this.canBypassIgnore = canBypassIgnore;
        this.canBypassDisabled = canBypassDisabled;
    }

    /**
     * Construct redis chat message
     */
    public RedisChatPacket() {
        super(PacketType.CHAT);
    }

    /**
     * Gets sender
     *
     * @return sender
     */
    public UUID getSender() {
        return sender;
    }

    /**
     * Sets sender
     *
     * @param sender sender
     */
    public void setSender(UUID sender) {
        this.sender = sender;
    }

    /**
     * Gets component
     *
     * @return component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Sets component
     *
     * @param component component
     */
    public void setComponent(Component component) {
        this.component = component;
    }

    /**
     * Get sender name
     *
     * @return sender name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Set sender name
     *
     * @param senderName sender name
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Returns channel
     *
     * @return channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets channel
     *
     * @param channel channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Get the name of the server that the packet is from
     *
     * @return server name
     */
    public String getServerIdentifier() {
        return serverIdentifier;
    }

    /**
     * Set the name of the server that the packet is from
     *
     * @param serverIdentifier server name
     */
    public void setServerIdentifier(String serverIdentifier) {
        this.serverIdentifier = serverIdentifier;
    }

    /**
     * Get the server display name
     *
     * @return display name
     */
    public String getServerDisplayName() {
        return serverDisplayName;
    }

    /**
     * Set the server display name
     *
     * @param serverDisplayName display name
     */
    public void setServerDisplayName(String serverDisplayName) {
        this.serverDisplayName = serverDisplayName;
    }

    /**
     * Get whether the sender can bypass ignores
     *
     * @return Whether the sender can bypass ignores
     */
    public boolean canBypassIgnore() {
        return canBypassIgnore;
    }

    /**
     * Set whether the sender can bypass ignores
     * @param canBypassIgnore Whether the sender can bypass ignores
     */
    public void setCanBypassIgnore(boolean canBypassIgnore) {
        this.canBypassIgnore = canBypassIgnore;
    }

    /**
     * Get whether the sender can bypass disabled private chat
     *
     * @return Whether the sender can bypass disabled private chat
     */
    public boolean canBypassDisabled() {
        return canBypassDisabled;
    }

    /**
     * Set whether the sender can bypass disabled private chat
     * @param canBypassDisabled Whether the sender can bypass disabled private chat
     */
    public void setCanBypassDisabled(boolean canBypassDisabled) {
        this.canBypassDisabled = canBypassDisabled;
    }
}
