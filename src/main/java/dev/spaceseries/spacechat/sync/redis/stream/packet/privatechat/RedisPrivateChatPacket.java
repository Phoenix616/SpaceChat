package dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat;

import dev.spaceseries.spacechat.sync.packet.SendStreamDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.PacketType;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisPacket;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class RedisPrivateChatPacket extends RedisPacket implements SendStreamDataPacket<Void> {

    /**
     * Who the message was sent by
     */
    private UUID sender;

    /**
     * Sender name
     */
    private String senderName;

    /**
     * The target of this private message
     */
    private String targetName;

    /**
     * The message
     */
    private String message;

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
    public RedisPrivateChatPacket(UUID sender, String senderName, String targetName, String message, String serverIdentifier, String serverDisplayName, Component component, boolean canBypassIgnore, boolean canBypassDisabled) {
        this();
        this.sender = sender;
        this.senderName = senderName;
        this.targetName = targetName;
        this.message = message;
        this.serverIdentifier = serverIdentifier;
        this.serverDisplayName = serverDisplayName;
        this.component = component;
        this.canBypassIgnore = canBypassIgnore;
        this.canBypassDisabled = canBypassDisabled;
    }

    /**
     * Construct redis chat message
     */
    public RedisPrivateChatPacket() {
        super(PacketType.PRIVATE_CHAT);
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
     * Returns the target's name
     *
     * @return the target's name
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Sets the target's name
     *
     * @param targetName the target's name
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * Get the message
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
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
