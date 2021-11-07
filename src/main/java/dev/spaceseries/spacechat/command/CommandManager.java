package dev.spaceseries.spacechat.command;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class CommandManager extends BukkitCommandManager {

    public CommandManager(SpaceChatPlugin plugin) {
        super(plugin);

        enableUnstableAPI("help");
        enableUnstableAPI("brigadier");

        setFormat(MessageType.INFO, ChatColor.WHITE);
        setFormat(MessageType.HELP, ChatColor.GRAY);
        setFormat(MessageType.ERROR, ChatColor.RED);
        setFormat(MessageType.SYNTAX, ChatColor.GRAY);

        Arrays.asList(
                new SpaceChatCommand(plugin),
                new ChannelCommand(plugin),
                new PrivateMessageCommand(plugin),
                new ReplyCommand(plugin),
                new IgnoreCommand(plugin),
                new EnableChatCommand(plugin),
                new DisableChatCommand(plugin),
                new BroadcastCommand(plugin),
                new BroadcastMinimessageCommand(plugin)
        ).forEach(this::registerCommand);
    }
}
