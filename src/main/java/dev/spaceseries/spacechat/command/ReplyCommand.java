package dev.spaceseries.spacechat.command;

import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.command.SpaceChatCommand;
import org.bukkit.entity.Player;

@CommandPermission("space.chat.command.reply")
@CommandAlias("reply|r")
public class ReplyCommand extends SpaceChatCommand {

    private final PrivateMessageCommand pmCommand;

    public ReplyCommand(SpaceChatPlugin plugin, PrivateMessageCommand pmCommand) {
        super(plugin);
        this.pmCommand = pmCommand;
    }

    @Default
    public void onCommand(Player player, String message) {
        plugin.getUserManager().use(player.getUniqueId(), user -> {
            if (user.getLastMessaged() != null) {
                pmCommand.onCommand(player, user.getLastMessaged(), message);
            } else {
                Messages.getInstance(plugin).replyNoOneMessaged.message(player);
            }
        });
    }

    @Default
    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        // send help message
        Messages.getInstance(plugin).pmHelp.message(player);
    }
}
