package dev.spaceseries.spacechat.command;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.ChatType;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CommandManager extends BukkitCommandManager {

    public CommandManager(SpaceChatPlugin plugin) {
        super(plugin);

        enableUnstableAPI("help");
        enableUnstableAPI("brigadier");

        setFormat(MessageType.INFO, ChatColor.WHITE);
        setFormat(MessageType.HELP, ChatColor.GRAY);
        setFormat(MessageType.ERROR, ChatColor.RED);
        setFormat(MessageType.SYNTAX, ChatColor.GRAY);

        PrivateMessageCommand pmCommand = new PrivateMessageCommand(plugin);
        Arrays.asList(
                new SpaceChatCommand(plugin),
                new ChannelCommand(plugin),
                pmCommand,
                new ReplyCommand(plugin, pmCommand),
                new IgnoreCommand(plugin),
                new EnableChatCommand(plugin),
                new DisableChatCommand(plugin),
                new BroadcastCommand(plugin),
                new BroadcastMinimessageCommand(plugin)
        ).forEach(this::registerCommand);

        getCommandCompletions().registerAsyncCompletion("chattypes",
                c -> Arrays.stream(ChatType.values()).map(ChatType::name).map(String::toLowerCase).collect(Collectors.toList()));
        getCommandCompletions().registerAsyncCompletion("ignored",
                c -> plugin.getUserManager().get(c.getPlayer().getUniqueId()).getIgnored().values());
        getCommandCompletions().registerAsyncCompletion("globalplayers",
                c -> plugin.getServerSyncServiceManager().getDataService().getPlayers());
    }
}
