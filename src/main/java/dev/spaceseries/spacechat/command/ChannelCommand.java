package dev.spaceseries.spacechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.command.SpaceChatCommand;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.model.ChatType;
import org.bukkit.entity.Player;

import java.util.Collection;

@CommandPermission("space.chat.command.channel")
@CommandAlias("channel")
public class ChannelCommand extends SpaceChatCommand {

    public ChannelCommand(SpaceChatPlugin plugin) {
        super(plugin);
    }

    @CommandPermission("space.chat.command.channel.list")
    @CommandAlias("channel")
    @Subcommand("list")
    public class ListCommand extends BaseCommand {
        @Default
        @CatchUnknown
        @HelpCommand
        public void onDefault(Player player) {
            // send list message
            Collection<Channel> channels = plugin.getChannelManager().getAll().values();
            Messages.getInstance(plugin).channelList.message(player, "%channelsamount%", String.valueOf(channels.size()));
            for (Channel channel : channels) {
                if (player.hasPermission(channel.getPermission())) {
                    Messages.getInstance(plugin).channelListEntry.message(player, "%channel%", channel.getHandle());
                }
            }
        }
    }

    @CommandPermission("space.chat.command.channel.mute")
    @CommandAlias("channel")
    @Subcommand("mute")
    public class MuteCommand extends BaseCommand {

        @Default
        @CommandCompletion("@channels")
        public void onMute(Player player, @Single String channel) {
            // get channel
            Channel applicable = plugin.getChannelManager().get(channel, null);
            if (applicable == null) {
                // send message
                Messages.getInstance(plugin).channelInvalid.message(player, "%channel%", channel);
                return;
            }

            // do they have permission?
            if (!player.hasPermission(applicable.getPermission())) {
                Messages.getInstance(plugin).channelAccessDenied.message(player);
                return;
            }

            // set current channel
            plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                user.unsubscribeFromChannel(applicable);

                // send message
                Messages.getInstance(plugin).channelMute.message(player, "%channel%", channel);
            });
        }
    }

    @CommandPermission("space.chat.command.channel.listen")
    @Subcommand("listen")
    @CommandAlias("channel")
    public class ListenCommand extends BaseCommand {

        @Default
        @CommandCompletion("@channels")
        public void onListen(Player player, @Single String channel) {
            // get channel
            Channel applicable = plugin.getChannelManager().get(channel, null);
            if (applicable == null) {
                // send message
                Messages.getInstance(plugin).channelInvalid.message(player, "%channel%", channel);
                return;
            }

            // do they have permission?
            if (!player.hasPermission(applicable.getPermission())) {
                Messages.getInstance(plugin).channelAccessDenied.message(player);
                return;
            }

            // set current channel
            plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                user.subscribeToChannel(applicable);

                // send message
                Messages.getInstance(plugin).channelListen.message(player, "%channel%", channel);
            });
        }
    }

    @CommandPermission("space.chat.command.channel.leave")
    @Subcommand("leave")
    @CommandAlias("channel")
    public class LeaveCommand extends BaseCommand {

        @Default
        public void onLeave(Player player) {
            // get current
            plugin.getServerSyncServiceManager().getDataService().getCurrentChannel(player.getUniqueId()).thenAccept(current -> {
                // if current null
                if (current == null) {
                    Messages.getInstance(plugin).generalHelp.message(player);
                    return;
                }

                // update current channel (aka remove)
                plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                    user.leaveChannel(current);

                    // send message
                    Messages.getInstance(plugin).channelLeave.message(player, "%channel%", current.getHandle());
                });
            });
        }
    }

    @Subcommand("join")
    @CommandAlias("channel")
    @CommandPermission("space.chat.command.channel.join")
    public class JoinCommand extends BaseCommand {

        @Default
        @CommandCompletion("@channels")
        public void onJoin(Player player, @Single String channel) {
            // get channel
            Channel applicable = plugin.getChannelManager().get(channel, null);
            if (applicable == null) {
                // send message
                Messages.getInstance(plugin).channelInvalid.message(player, "%channel%", channel);
                return;
            }

            // do they have permission?
            if (!player.hasPermission(applicable.getPermission())) {
                Messages.getInstance(plugin).channelAccessDenied.message(player);
                return;
            }

            // set current channel
            plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                user.joinChannel(applicable);

                // send message
                Messages.getInstance(plugin).channelJoin.message(player, "%channel%", channel);
            });
        }
    }

    @Subcommand("message|msg|talk")
    @CommandAlias("channel")
    @CommandPermission("space.chat.command.channel.talk")
    public class MessageCommand extends BaseCommand {

        @Default
        @CommandCompletion("@channels")
        public void onMessage(Player player, @Single String channel, String message) {
            // get channel
            Channel applicable = plugin.getChannelManager().get(channel, null);
            if (applicable == null) {
                // send message
                Messages.getInstance(plugin).channelInvalid.message(player, "%channel%", channel);
                return;
            }

            // do they have permission?
            if (!player.hasPermission(applicable.getPermission())) {
                Messages.getInstance(plugin).channelAccessDenied.message(player);
                return;
            }

            // set current channel
            plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                if (!user.hasChatEnabled(ChatType.PUBLIC)) {
                    Messages.getInstance(plugin).chatDisabled.message(player);
                    return;
                }

                if (!user.getSubscribedChannels().contains(applicable)) {
                    user.subscribeToChannel(applicable);

                    // send message
                    Messages.getInstance(plugin).channelListen.message(player, "%channel%", channel);
                }

                // send message to channel
                plugin.getChannelManager().send(player, null, String.join(" ", message), applicable);
            });
        }
    }

    @Default
    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        // send help message
        Messages.getInstance(plugin).channelHelp.message(player);
    }
}
