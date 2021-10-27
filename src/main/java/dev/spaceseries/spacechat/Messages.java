package dev.spaceseries.spacechat;

import dev.spaceseries.spacechat.api.config.generic.adapter.ConfigurationAdapter;
import dev.spaceseries.spacechat.api.message.Message;

public class Messages {

    /**
     * Instance of this class
     */
    private static Messages instance;

    /**
     * Renews the messages
     */
    public static void renew() {
        instance = null;
    }

    /**
     * Gets instance of messages class
     * <p>(Singleton)</p>
     *
     * @return messages
     */
    public static Messages getInstance(SpaceChatPlugin context) {
        if (instance == null) {
            instance = new Messages(context);
        }
        return instance;
    }

    private final SpaceChatPlugin plugin;

    /* General */

    // help
    public Message generalHelp;

    /* Reload */

    // success
    public Message reloadSuccess;

    // failure
    public Message reloadFailure;
    /**
     * Broadcast
     */

    // args
    public Message broadcastArgs;

    // wrapper
    public Message broadcastWrapper;

    /**
     * Channel
     */

    // join
    public Message channelJoin;

    // leave
    public Message channelLeave;

    // listen
    public Message channelListen;

    // mute
    public Message channelMute;
    // invalid

    public Message channelInvalid;

    // access denied
    public Message channelAccessDenied;

    /**
     * Ignore
     */

    // player not found
    public Message playerNotFound;

    // ignored a player
    public Message ignoreAdded;

    // player is already ignored
    public Message playerAlreadyIgnored;

    // unignored a player
    public Message ignoreRemoved;

    // player is not ignored
    public Message playerNotIgnored;

    // the head of the ignored list
    public Message ignoreListHead;

    // the head of the ignored list
    public Message ignoreListEntry;

    // the head of the ignored list
    public Message ignoreListFooter;

    public Messages(SpaceChatPlugin plugin) {
        this.plugin = plugin;

        generalHelp = Message.fromConfigurationSection("general.help", this.getLangConfiguration());
        reloadSuccess = Message.fromConfigurationSection("reload.success", this.getLangConfiguration());
        reloadFailure = Message.fromConfigurationSection("reload.failure", this.getLangConfiguration());
        broadcastArgs = Message.fromConfigurationSection("broadcast.args", this.getLangConfiguration());
        broadcastWrapper = Message.fromConfigurationSection("broadcast.wrapper", this.getLangConfiguration());
        channelJoin = Message.fromConfigurationSection("channel.join", this.getLangConfiguration());
        channelLeave = Message.fromConfigurationSection("channel.leave", this.getLangConfiguration());
        channelListen = Message.fromConfigurationSection("channel.listen", this.getLangConfiguration());
        channelMute = Message.fromConfigurationSection("channel.mute", this.getLangConfiguration());
        channelAccessDenied = Message.fromConfigurationSection("channel.access-denied", this.getLangConfiguration());
        channelInvalid = Message.fromConfigurationSection("channel.invalid", this.getLangConfiguration());
        playerNotFound = Message.fromConfigurationSection("ignore.player-not-found", this.getLangConfiguration());
        ignoreAdded = Message.fromConfigurationSection("ignore.added", this.getLangConfiguration());
        playerAlreadyIgnored = Message.fromConfigurationSection("ignore.already-ignored", this.getLangConfiguration());
        ignoreRemoved = Message.fromConfigurationSection("ignore.removed", this.getLangConfiguration());
        playerNotIgnored = Message.fromConfigurationSection("ignore.not-ignored", this.getLangConfiguration());
        ignoreListHead = Message.fromConfigurationSection("ignore.list.head", this.getLangConfiguration());
        ignoreListEntry = Message.fromConfigurationSection("ignore.list.entry", this.getLangConfiguration());
        ignoreListFooter = Message.fromConfigurationSection("ignore.list.footer", this.getLangConfiguration());
    }

    /**
     * Gets the lang configuration from the main class
     *
     * @return The lang configuration
     */
    private ConfigurationAdapter getLangConfiguration() {
        return plugin.getLangConfig().getAdapter();
    }
}

