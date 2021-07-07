package dev.spaceseries.spacechat.command.channel;

import dev.spaceseries.spaceapi.command.Command;
import dev.spaceseries.spaceapi.command.Permissible;
import dev.spaceseries.spaceapi.command.PlayersOnly;
import dev.spaceseries.spaceapi.command.SpaceCommandSender;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChat;
import dev.spaceseries.spacechat.api.message.Message;
import dev.spaceseries.spacechat.model.Channel;

@Permissible("space.chat.command.channel.join")
@PlayersOnly
public class JoinCommand extends Command {

    private final SpaceChat plugin;

    public JoinCommand(SpaceChat plugin) {
        super(plugin.getPlugin(), "join");
        this.plugin = plugin;
    }

    @Override
    public void onCommand(SpaceCommandSender sender, String label, String... args) {
        // args
        if (args.length != 1) {
            Messages.getInstance(plugin).generalHelp.msg(sender);
            return;
        }

        String channel = args[0];

        // get channel
        Channel applicable = plugin.getChannelManager().get(channel, null);
        if (applicable == null) {
            // send message
            Messages.getInstance(plugin).channelInvalid.msg(sender, "%channel%", channel);
            return;
        }

        // do they have permission?
        if (!sender.hasPermission(applicable.getPermission())) {
            Message.Global.ACCESS_DENIED.msg(sender);
            return;
        }

        // set current channel
        plugin.getUserManager().use(sender.getUuid(), (user) -> {
            user.joinChannel(applicable);

            // send message
            Messages.getInstance(plugin).channelJoin.msg(sender, "%channel%", channel);
        });
    }
}