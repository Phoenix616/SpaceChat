package dev.spaceseries.spacechat.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.model.ChatType;
import dev.spaceseries.spacechat.util.async.AsyncFetcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
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

        AsyncFetcher<BukkitCommandCompletionContext, Collection<String>> ignoredFetcher = new AsyncFetcher<>(
                c -> plugin.getUserManager().get(c.getPlayer().getUniqueId()).getIgnored().values(),
                c -> plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> c.getPlayer().canSee(p))
                        .map(Player::getName).collect(Collectors.toList()));
        AsyncFetcher<BukkitCommandCompletionContext, Collection<String>> globalPlayerFetcher = new AsyncFetcher<>(
                c -> plugin.getServerSyncServiceManager().getDataService().getPlayers(),
                c -> plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> c.getPlayer().canSee(p))
                        .map(Player::getName).collect(Collectors.toList()));

        getCommandCompletions().registerAsyncCompletion("chattypes",
                c -> Arrays.stream(ChatType.values()).map(ChatType::name).map(String::toLowerCase).collect(Collectors.toList()));
        getCommandCompletions().registerAsyncCompletion("ignored", ignoredFetcher::fetch);
        getCommandCompletions().registerAsyncCompletion("globalplayers", globalPlayerFetcher::fetch);
        getCommandCompletions().registerAsyncCompletion("channels",
                c -> plugin.getChannelManager().getAll().values().stream()
                        .filter(channel -> c.getPlayer().hasPermission(channel.getPermission()))
                        .map(Channel::getHandle)
                        .collect(Collectors.toList()));
    }
}
