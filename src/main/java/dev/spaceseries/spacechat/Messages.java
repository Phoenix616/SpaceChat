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
     * General chat
     */

    // chat disabled
    public Message chatDisabled;

    // enabled all chat via command
    public Message enabledAllChat;

    // enabled specific chat via command
    public Message enabledSpecificChat;

    // disable all chat via command
    public Message disabledAllChat;

    // disabled specific chat via command
    public Message disabledSpecificChat;

    // input invalid chat type
    public Message invalidChatType;

    // Placeholder if chat type is enabled
    public Message placeholderChatEnabled;

    // Placeholder if chat type is disabled
    public Message placeholderChatDisabled;

    /**
     * Channel
     */

    // channel help
    public Message channelHelp;

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

    // ignore help
    public Message ignoreHelp;

    // player not found
    public Message ignorePlayerNotFound;

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

    /**
     * Private message
     */

    // private message help
    public Message pmHelp;

    // player not found
    public Message pmPlayerNotFound;

    // sender has target ignored
    public Message pmTargetIgnored;

    // target has sender ignored
    public Message pmIgnoredByTarget;

    // sender has private chat disabled
    public Message pmChatDisabled;

    // target has private chat disabled
    public Message pmChatDisabledByTarget;

    // player not found
    public Message pmReceived;

    // sender has target ignored
    public Message pmSent;

    // no one messaged the user
    public Message replyNoOneMessaged;

    public Messages(SpaceChatPlugin plugin) {
        this.plugin = plugin;

        generalHelp = Message.fromConfigurationSection("general.help", this.getLangConfiguration());
        reloadSuccess = Message.fromConfigurationSection("reload.success", this.getLangConfiguration());
        reloadFailure = Message.fromConfigurationSection("reload.failure", this.getLangConfiguration());
        broadcastArgs = Message.fromConfigurationSection("broadcast.args", this.getLangConfiguration());
        broadcastWrapper = Message.fromConfigurationSection("broadcast.wrapper", this.getLangConfiguration());
        chatDisabled = Message.fromConfigurationSection("chat.disabled", this.getLangConfiguration());
        enabledAllChat = Message.fromConfigurationSection("chat.enabled-all", this.getLangConfiguration());
        enabledSpecificChat = Message.fromConfigurationSection("chat.enabled-specific", this.getLangConfiguration());
        disabledAllChat = Message.fromConfigurationSection("chat.disabled-all", this.getLangConfiguration());
        disabledSpecificChat = Message.fromConfigurationSection("chat.disabled-specific", this.getLangConfiguration());
        invalidChatType = Message.fromConfigurationSection("chat.invalid-type", this.getLangConfiguration());
        placeholderChatEnabled = Message.fromConfigurationSection("chat.placeholder-enabled", this.getLangConfiguration());
        placeholderChatDisabled = Message.fromConfigurationSection("chat.placeholder-disabled", this.getLangConfiguration());
        channelHelp = Message.fromConfigurationSection("channel.help", this.getLangConfiguration());
        channelJoin = Message.fromConfigurationSection("channel.join", this.getLangConfiguration());
        channelLeave = Message.fromConfigurationSection("channel.leave", this.getLangConfiguration());
        channelListen = Message.fromConfigurationSection("channel.listen", this.getLangConfiguration());
        channelMute = Message.fromConfigurationSection("channel.mute", this.getLangConfiguration());
        channelAccessDenied = Message.fromConfigurationSection("channel.access-denied", this.getLangConfiguration());
        channelInvalid = Message.fromConfigurationSection("channel.invalid", this.getLangConfiguration());
        ignorePlayerNotFound = Message.fromConfigurationSection("ignore.player-not-found", this.getLangConfiguration());
        ignoreAdded = Message.fromConfigurationSection("ignore.added", this.getLangConfiguration());
        playerAlreadyIgnored = Message.fromConfigurationSection("ignore.already-ignored", this.getLangConfiguration());
        ignoreRemoved = Message.fromConfigurationSection("ignore.removed", this.getLangConfiguration());
        playerNotIgnored = Message.fromConfigurationSection("ignore.not-ignored", this.getLangConfiguration());
        ignoreHelp = Message.fromConfigurationSection("ignore.help", this.getLangConfiguration());
        ignoreListHead = Message.fromConfigurationSection("ignore.list.head", this.getLangConfiguration());
        ignoreListEntry = Message.fromConfigurationSection("ignore.list.entry", this.getLangConfiguration());
        ignoreListFooter = Message.fromConfigurationSection("ignore.list.footer", this.getLangConfiguration());
        pmHelp = Message.fromConfigurationSection("privatemessage.help", this.getLangConfiguration());
        pmSent = Message.fromConfigurationSection("privatemessage.sent", this.getLangConfiguration());
        pmReceived = Message.fromConfigurationSection("privatemessage.received", this.getLangConfiguration());
        pmPlayerNotFound = Message.fromConfigurationSection("privatemessage.player-not-found", this.getLangConfiguration());
        pmTargetIgnored = Message.fromConfigurationSection("privatemessage.target-ignored", this.getLangConfiguration());
        pmIgnoredByTarget = Message.fromConfigurationSection("privatemessage.ignored-by-target", this.getLangConfiguration());
        pmChatDisabled = Message.fromConfigurationSection("privatemessage.disabled", this.getLangConfiguration());
        pmChatDisabledByTarget = Message.fromConfigurationSection("privatemessage.chat-disabled-by-target", this.getLangConfiguration());
        replyNoOneMessaged = Message.fromConfigurationSection("privatemessage.reply-no-one-messaged", this.getLangConfiguration());
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

