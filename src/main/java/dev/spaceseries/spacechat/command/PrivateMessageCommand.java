package dev.spaceseries.spacechat.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Single;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.command.SpaceChatCommand;
import org.bukkit.entity.Player;

@CommandPermission("space.chat.command.privatemessage")
@CommandAlias("msg|message|pm|privatemessage|tell")
public class PrivateMessageCommand extends SpaceChatCommand {

    public PrivateMessageCommand(SpaceChatPlugin plugin) {
        super(plugin);
    }

    @Default
    public void onCommand(Player player, @Single String targetName, String message) {
        String targetServer = plugin.getServerSyncServiceManager().getDataService().getPlayerServer(targetName);
        if (targetServer != null) {
            plugin.getUserManager().use(player.getUniqueId(), user -> {
                if (!user.isIgnored(targetName)) {
                    plugin.getPrivateFormatManager().send(player, targetName, message);
                } else {
                    Messages.getInstance(plugin).pmTargetIgnored.message(player, "%user%", targetName);
                }
            });
        } else {
            Messages.getInstance(plugin).pmPlayerNotFound.message(player, "%user%", targetName);
        }
    }
}
