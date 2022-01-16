package dev.spaceseries.spacechat.command;

import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Single;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.command.SpaceChatCommand;
import dev.spaceseries.spacechat.model.ChatType;
import org.bukkit.entity.Player;

@CommandPermission("space.chat.command.privatemessage")
@CommandAlias("msg|message|pm|privatemessage|tell")
public class PrivateMessageCommand extends SpaceChatCommand {

    public PrivateMessageCommand(SpaceChatPlugin plugin) {
        super(plugin);
    }

    @Default
    @CommandCompletion("@globalplayers")
    public void onCommand(Player player, @Single String targetName, String message) {
        String targetServer = plugin.getServerSyncServiceManager().getDataService().getPlayerServer(targetName);
        if (targetServer != null) {
            plugin.getUserManager().use(player.getUniqueId(), user -> {
                if (!user.hasChatEnabled(ChatType.PRIVATE)) {
                    Messages.getInstance(plugin).pmChatDisabled.message(player);
                } else if (!user.isIgnored(targetName)) {
                    plugin.getPrivateFormatManager().send(player, targetName, message);
                } else {
                    Messages.getInstance(plugin).pmTargetIgnored.message(player, "%user%", targetName);
                }
            });
        } else {
            Messages.getInstance(plugin).pmPlayerNotFound.message(player, "%user%", targetName);
        }
    }

    @Default
    public void onCommand(Player player, @Single String targetName) {
        // send help message
        Messages.getInstance(plugin).pmHelp.message(player);
    }

    @Default
    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        // send help message
        Messages.getInstance(plugin).pmHelp.message(player);
    }
}
