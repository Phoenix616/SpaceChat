package dev.spaceseries.spacechat.chat;

import com.google.common.collect.ImmutableMap;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.config.generic.adapter.ConfigurationAdapter;
import dev.spaceseries.spacechat.api.message.Message;
import dev.spaceseries.spacechat.api.wrapper.Quad;
import dev.spaceseries.spacechat.api.wrapper.Trio;
import dev.spaceseries.spacechat.builder.live.NormalLiveChatFormatBuilder;
import dev.spaceseries.spacechat.builder.live.RelationalLiveChatFormatBuilder;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.logging.wrap.LogChatWrapper;
import dev.spaceseries.spacechat.logging.wrap.LogPrivateChatWrapper;
import dev.spaceseries.spacechat.logging.wrap.LogToType;
import dev.spaceseries.spacechat.logging.wrap.LogType;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.model.ChatType;
import dev.spaceseries.spacechat.model.User;
import dev.spaceseries.spacechat.model.formatting.Format;
import dev.spaceseries.spacechat.model.manager.Manager;
import dev.spaceseries.spacechat.sync.ServerDataSyncService;
import dev.spaceseries.spacechat.sync.ServerStreamSyncService;
import dev.spaceseries.spacechat.sync.redis.stream.packet.chat.RedisChatPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat.RedisPrivateChatPacket;
import dev.spaceseries.spacechat.util.color.ColorUtil;
import me.mattstudios.msg.adventure.AdventureMessage;
import me.mattstudios.msg.base.MessageOptions;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ChatManager implements Manager {

    private final SpaceChatPlugin plugin;
    private ServerStreamSyncService serverStreamSyncService;
    private ServerDataSyncService serverDataSyncService;
    private final ConfigurationAdapter config;

    /**
     * Construct chat event manager
     *
     * @param plugin plugin
     */
    public ChatManager(SpaceChatPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getSpaceChatConfig().getAdapter();
    }

    /**
     * Initializes server sync services
     */
    public void initSyncServices() {
        this.serverStreamSyncService = plugin.getServerSyncServiceManager().getStreamService();
        this.serverDataSyncService = plugin.getServerSyncServiceManager().getDataService();
    }

    /**
     * Send a chat message
     * <p>
     * This does the same thing as {@link ChatManager#sendComponentMessage(Component)} but I just made it different for the sake
     * of understanding
     *
     * @param component component
     * @deprecated Use {@link #sendComponentChatMessage(UUID, Component, boolean, boolean)}
     */
    @Deprecated
    public void sendComponentChatMessage(Component component) {
        sendComponentMessage(component);
    }

    /**
     * Send a chat message to all online players checking whether or not they ignore the sender
     *
     * @param from      sender UUID
     * @param component component
     */
    public void sendComponentChatMessage(UUID from, Component component, boolean canBypassIgnore, boolean canBypassDisabled) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendComponentChatMessage(from, component, player, canBypassIgnore, canBypassDisabled);
        }
    }

    /**
     * Send a chat message to a specific player
     * <p>
     * This does the same thing as {@link ChatManager#sendComponentMessage(Component, Player)} but I just made it different for the sake
     * of understanding
     *
     * @param component component
     * @param to        to
     * @deprecated Use {@link #sendComponentChatMessage(UUID, Component, Player, boolean, boolean)}
     */
    @Deprecated
    public void sendComponentChatMessage(Component component, Player to) {
        sendComponentMessage(Identity.nil(), component, to);
    }

    /**
     * Send a chat message to a specific player if they don't ignore the sender
     *
     * @param from      sender UUID
     * @param component component
     * @param to        to
     * @param canBypassIgnore   sender bypasses ignores
     * @param canBypassDisabled sender bypasses disabled chat
     */
    public void sendComponentChatMessage(UUID from, Component component, Player to, boolean canBypassIgnore, boolean canBypassDisabled) {
        User user = plugin.getUserManager().get(to.getUniqueId());
        if (user == null || ((user.hasChatEnabled(ChatType.PUBLIC) || canBypassDisabled) && (!user.isIgnored(from) || canBypassIgnore))) {
            sendComponentMessage(canBypassIgnore ? Identity.nil() : Identity.identity(from), component, to);
        }
    }

    /**
     * Send a raw component to all players with a nil Identity as the source
     *
     * @param component component
     */
    public void sendComponentMessage(Component component) {
        // send chat message to all online players
        Message.getAudienceProvider().players().sendMessage(component);
    }

    /**
     * Send a raw component to a player with a nil Identity as the source
     *
     * @param component component
     * @param to        to
     * @deprecated Use {@link #sendComponentMessage(Identity, Component, Player)}
     */
    @Deprecated
    public void sendComponentMessage(Component component, Player to) {
        sendComponentMessage(Identity.nil(), component, to);
    }

    /**
     * Send a raw component to a player
     *
     * @param from      the sender identity
     * @param component component
     * @param to        to
     */
    public void sendComponentMessage(Identity from, Component component, Player to) {
        // send chat message to a specific player
        Message.getAudienceProvider().player(to.getUniqueId()).sendMessage(from, component);
    }

    /**
     * Send a raw component to a channel
     *  @param component component
     * @param channel   channel
     * @param canBypassIgnore sender bypasses ignores
     * @param canBypassDisabled sender bypasses disabled public chats
     */
    public void sendComponentChannelMessage(UUID from, Component component, Channel channel, boolean canBypassIgnore, boolean canBypassDisabled) {
        // get all subscribed players to that channel
        serverDataSyncService.getSubscribedUUIDs(channel).thenAccept(subscribedUUIDs -> {
            List<Player> subscribedPlayers = subscribedUUIDs.stream()
                    .filter(uuid -> {
                        User user = plugin.getUserManager().get(uuid);
                        return user == null || ((user.hasChatEnabled(ChatType.PUBLIC) || canBypassDisabled) && (!user.isIgnored(from) || canBypassIgnore));
                    })
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Player fromPlayer = plugin.getServer().getPlayer(from);

            // even if not listening, add the sender to the list of players listening so that they can view the message
            // that they sent themselves
            if (fromPlayer != null && !subscribedPlayers.contains(fromPlayer)) {
                subscribedPlayers.add(fromPlayer);
            }

            List<Player> subscribedPlayersWithPermission = subscribedPlayers.stream()
                    .filter(p -> p.hasPermission(channel.getPermission()))
                    .collect(Collectors.toList());

            // if a player in the list doesn't have permission to view it, then unsubscribe them
            subscribedPlayers.forEach(p -> {
                if (!p.hasPermission(channel.getPermission())) {
                    serverDataSyncService.unsubscribeFromChannel(p.getUniqueId(), channel);
                }
            });

            subscribedPlayersWithPermission.forEach(p -> sendComponentMessage(Identity.identity(from), component, p));
        });
    }

    /**
     * Send a chat message
     *
     * @param from    player that the message is from
     * @param message message
     * @param format  format
     * @param event   event
     */
    public void sendChatMessage(Player from, String message, Format format, @Nullable AsyncPlayerChatEvent event) {
        // get player's current channel, and send through that (if null, that means 'global')
        serverDataSyncService.getCurrentChannel(from.getUniqueId()).thenAccept(applicableChannel ->
                sendChatMessage(from, message, applicableChannel, format, event));
    }

    /**
     * Send a chat message
     *
     * @param from    player that the message is from
     * @param message message the message to send
     * @param channel the channel to send to, if null then it's global
     * @param format  format the format to use, can be null
     * @param event   optionally the chat event
     */
    public void sendChatMessage(Player from, String message, @Nullable Channel channel, @Nullable Format format, @Nullable AsyncPlayerChatEvent event) {
        boolean canBypassIgnore = from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_BYPASS_IGNORE.get(config));
        boolean canBypassDisabled = from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_BYPASS_DISABLED_PUBLIC.get(config));

        Component components;

        // if null, return
        if (format == null) {
            // build components default message
            // this only happens if it's not possible to find a chat format
            components = Component.text()
                    .append(Component.text(from.getDisplayName(), NamedTextColor.AQUA))
                    .append(Component.text("> ", NamedTextColor.GRAY))
                    .append(Component.text(message))
                    .build();
        } else { // if not null
            // get baseComponents from live builder
            components = new NormalLiveChatFormatBuilder(plugin).build(new Trio<>(from, message, format));
        }

        // if channel exists, then send through it
        if (channel != null) {
            sendComponentChannelMessage(from.getUniqueId(), components, channel, canBypassIgnore, canBypassDisabled);
        } else {
            // send component message to entire server
            sendComponentChatMessage(from.getUniqueId(), components, canBypassIgnore, canBypassDisabled);
        }

        // log to storage
        plugin.getLogManagerImpl()
                .log(new LogChatWrapper(LogType.CHAT, from.getName(), from.getUniqueId(), message, new Date()),
                        LogType.CHAT,
                        LogToType.STORAGE
                );

        // send via redis (it won't do anything if redis isn't enabled, so we can be sure that we aren't using dead methods that will throw an exception)
        serverStreamSyncService.publishChat(new RedisChatPacket(from.getUniqueId(), from.getName(), channel, SpaceChatConfigKeys.REDIS_SERVER_IDENTIFIER.get(config), SpaceChatConfigKeys.REDIS_SERVER_DISPLAYNAME.get(config), components, canBypassIgnore, canBypassDisabled));

        // log to console
        if (event != null) { // if there's an event, log w/ the event
            plugin.getLogManagerImpl()
                    .log(components.children()
                            .stream()
                            .map(c -> LegacyComponentSerializer.legacySection().serialize(c))
                            .map(ColorUtil::translateFromAmpersand)
                            .map(ColorUtil::stripColor)
                            .collect(Collectors.joining()), LogType.CHAT, LogToType.CONSOLE, event);
        } else {
            plugin.getLogManagerImpl() // if there's no event, just log to console without using the event
                    .log(components.children()
                            .stream()
                            .map(c -> LegacyComponentSerializer.legacySection().serialize(c))
                            .map(ColorUtil::translateFromAmpersand)
                            .map(ColorUtil::stripColor)
                            .collect(Collectors.joining()), LogType.CHAT, LogToType.CONSOLE);
        }

        // note: storage logging is handled in the actual chat format manager because there's no need to log
        // if a message come from redis. This is really a generified version of my initial idea
        // but it's pretty good and it works
    }


    /**
     * Send a chat message with relational placeholders
     *
     * @param from    player that the message is from
     * @param message message
     * @param format  format format
     * @param event   event
     */
    public void sendRelationalChatMessage(Player from, String message, Format format, AsyncPlayerChatEvent event) {
        boolean canBypassIgnore = from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_BYPASS_IGNORE.get(config));
        boolean canBypassDisabled = from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_BYPASS_DISABLED_PUBLIC.get(config));

        // component to use with storage and logging
        Component sampledComponent;

        if (format == null) {
            // build components default message
            // this only happens if it's not possible to find a chat format
            sampledComponent = Component.text()
                    .append(Component.text(from.getDisplayName(), NamedTextColor.AQUA))
                    .append(Component.text("> ", NamedTextColor.GRAY))
                    .append(Component.text(message))
                    .build();
        } else { // if not null
            // get baseComponents from live builder
            sampledComponent = new NormalLiveChatFormatBuilder(plugin).build(new Trio<>(from, message, format));
        }

        // do relational parsing
        Bukkit.getOnlinePlayers().forEach(to -> {
            Component component;

            if (format == null) {
                // build components default message
                // this only happens if it's not possible to find a chat format
                component = Component.text()
                        .append(Component.text(from.getDisplayName(), NamedTextColor.AQUA))
                        .append(Component.text("> ", NamedTextColor.GRAY))
                        .append(Component.text(message))
                        .build();
            } else { // if not null
                // get baseComponents from live builder
                component = new RelationalLiveChatFormatBuilder(plugin).build(new Quad<>(from, to, message, format));
            }

            // send to 'to-player'
            sendComponentChatMessage(from.getUniqueId(), component, to, canBypassIgnore, canBypassDisabled);
        });

        // log to storage
        plugin.getLogManagerImpl()
                .log(new LogChatWrapper(LogType.CHAT, from.getName(), from.getUniqueId(), message, new Date()),
                        LogType.CHAT,
                        LogToType.STORAGE
                );

        // log to console
        if (event != null) {// if there's an event, log w/ the event
            plugin.getLogManagerImpl()
                    .log(sampledComponent.children()
                            .stream()
                            .map(c -> LegacyComponentSerializer.legacySection().serialize(c))
                            .map(ColorUtil::translateFromAmpersand)
                            .map(ColorUtil::stripColor)
                            .collect(Collectors.joining()), LogType.CHAT, LogToType.CONSOLE, event);

        } else {
            plugin.getLogManagerImpl() // if there's no event, just log to console without using the event
                    .log(sampledComponent.children()
                            .stream()
                            .map(c -> LegacyComponentSerializer.legacySection().serialize(c))
                            .map(ColorUtil::translateFromAmpersand)
                            .map(ColorUtil::stripColor)
                            .collect(Collectors.joining()), LogType.CHAT, LogToType.CONSOLE);
        }

        // note: storage logging is handled in the actual chat format manager because there's no need to log
        // if a message come from redis. This is really a generified version of my initial idea
        // but it's pretty good and it works
    }

    /**
     * Send a chat message
     *
     * @param from          player that the message is from
     * @param targetName    the name of the target player
     * @param message       message
     * @param format        format
     */
    public void sendPrivateMessage(Player from, String targetName, String message, Format format, AsyncPlayerChatEvent event) {
        boolean canBypassIgnore = from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_BYPASS_IGNORE.get(config));
        boolean canBypassDisabled = from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_BYPASS_DISABLED_PRIVATE.get(config));

        Component sentComponents = null;
        Component receivedComponents;

        Player to = plugin.getServer().getPlayerExact(targetName);


        // get chat message (formatted)
        MessageOptions.Builder messageOptionsBuilder = MessageOptions.builder(me.mattstudios.msg.base.internal.Format.NONE);
        if (from.hasPermission(SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_COLORS.get(plugin.getSpaceChatConfig().getAdapter()))) {
            messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.COLOR);
            messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.HEX);
            messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.GRADIENT);
            messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.RAINBOW);
        }
        String formattingPermission = SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_FORMATTING.get(plugin.getSpaceChatConfig().getAdapter());
        for (me.mattstudios.msg.base.internal.Format f : me.mattstudios.msg.base.internal.Format.ALL) {
            if (from.hasPermission(formattingPermission + f.name().toLowerCase(Locale.ROOT))) {
                messageOptionsBuilder.addFormat(f);
            }
        }

        Component messageComponents = AdventureMessage.create(messageOptionsBuilder.build()).parse(message);
        String formattedMessage = LegacyComponentSerializer.legacySection().serialize(messageComponents);
        if (formattedMessage.startsWith(ChatColor.WHITE.toString()) && !message.startsWith("&f")) {
            formattedMessage = formattedMessage.substring(2);
        }

        // if null, return
        if (format == null) {
            // build components default message
            // this only happens if it's not possible to find a chat format
            sentComponents = Messages.getInstance(plugin).pmSent
                    .compile(
                            "%format%", (to != null ? to.getDisplayName() : targetName) + ChatColor.GRAY + "> " + message,
                            "%receivername%", to != null ? to.getName() : targetName,
                            "%receiverdisplayname%",  to != null ? to.getDisplayName() : targetName,
                            "%sendername%", from.getName(),
                            "%senderdisplayname%", from.getDisplayName(),
                            "%message%", formattedMessage
                    );
            receivedComponents = Messages.getInstance(plugin).pmReceived
                    .compile(
                            "%format%", from.getDisplayName() + ChatColor.GRAY + "> " + message,
                            "%receivername%", to != null ? to.getName() : targetName,
                            "%receiverdisplayname%",  to != null ? to.getDisplayName() : targetName,
                            "%sendername%", from.getName(),
                            "%senderdisplayname%", from.getDisplayName(),
                            "%message%", formattedMessage
                    );
        } else { // if not null
            Map<String, Component> generalReplacements = ImmutableMap.of(
                    "%receivername%", Component.text(to != null ? to.getName() : targetName),
                    "%receiverdisplayname%", LegacyComponentSerializer.legacySection().deserialize(to != null ? to.getDisplayName() : targetName),
                    "%sendername%", Component.text(from.getName()),
                    "%senderdisplayname%", LegacyComponentSerializer.legacySection().deserialize(from.getDisplayName()),
                    "%message%", messageComponents
            );
            // get baseComponents from live builder
            if (SpaceChatConfigKeys.USE_RELATIONAL_PLACEHOLDERS.get(plugin.getSpaceChatConfig().getAdapter()) && !plugin.getServerSyncServiceManager().isUsingNetwork()) {
                if (to != null) {
                    Map<String, Component> replacements = new LinkedHashMap<>();
                    replacements.put("%format%", new RelationalLiveChatFormatBuilder(plugin).build(new Quad<>(to, from, message, format)));
                    replacements.putAll(generalReplacements);
                    sentComponents = Messages.getInstance(plugin).pmSent.compile(replacements);
                }
                Map<String, Component> replacements = new LinkedHashMap<>();
                replacements.put("%format%", new RelationalLiveChatFormatBuilder(plugin).build(new Quad<>(from, to, message, format)));
                replacements.putAll(generalReplacements);
                receivedComponents = Messages.getInstance(plugin).pmReceived.compile(replacements);
            } else {
                if (to != null) {
                    Map<String, Component> replacements = new LinkedHashMap<>();
                    replacements.put("%format%", new NormalLiveChatFormatBuilder(plugin).build(new Trio<>(to, message, format)));
                    replacements.putAll(generalReplacements);
                    sentComponents = Messages.getInstance(plugin).pmSent.compile(replacements);
                }
                Map<String, Component> replacements = new LinkedHashMap<>();
                replacements.put("%format%",new NormalLiveChatFormatBuilder(plugin).build(new Trio<>(from, message, format)));
                replacements.putAll(generalReplacements);
                receivedComponents = Messages.getInstance(plugin).pmReceived.compile(replacements);
            }

        }

        // log to storage
        plugin.getLogManagerImpl()
                .log(new LogPrivateChatWrapper(LogType.PRIVATE_CHAT, from.getName(), from.getUniqueId(), targetName, message, new Date()),
                        LogType.PRIVATE_CHAT,
                        LogToType.STORAGE
                );


        if (to != null) {
            sendComponentMessage(Identity.identity(from.getUniqueId()), sentComponents, from);
            plugin.getUserManager().use(to.getUniqueId(), user -> {
                if (!user.hasChatEnabled(ChatType.PRIVATE) && !canBypassDisabled) {
                    Messages.getInstance(plugin).pmChatDisabledByTarget.message(from, "%user%", to.getName());
                } else if (user.isIgnored(from.getUniqueId()) && !canBypassIgnore) {
                    Messages.getInstance(plugin).pmIgnoredByTarget.message(from, "%user%", to.getName());
                } else {
                    sendComponentMessage(canBypassIgnore ? Identity.nil() : Identity.identity(from.getUniqueId()), receivedComponents, to);
                    user.setLastMessaged(from.getName());
                }
            });
        } else {
            // send via redis (it won't do anything if redis isn't enabled, so we can be sure that we aren't using dead methods that will throw an exception)
            serverStreamSyncService.publishPrivateChat(new RedisPrivateChatPacket(from.getUniqueId(), from.getName(), from.getDisplayName(), targetName, formattedMessage, SpaceChatConfigKeys.REDIS_SERVER_IDENTIFIER.get(config), SpaceChatConfigKeys.REDIS_SERVER_DISPLAYNAME.get(config), receivedComponents, canBypassIgnore, canBypassDisabled));
        }

        // log to console
        plugin.getLogManagerImpl()
                .log(from.getName() + " -> " + targetName + ": " + message, LogType.CHAT, LogToType.CONSOLE, event);
    }
}
