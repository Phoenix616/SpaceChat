package dev.spaceseries.spacechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.command.SpaceChatCommand;
import org.bukkit.entity.Player;

@CommandPermission("space.chat.command.ignore")
@CommandAlias("ignore")
public class IgnoreCommand extends SpaceChatCommand {

    public IgnoreCommand(SpaceChatPlugin plugin) {
        super(plugin);
    }

    @Subcommand("add")
    @CommandAlias("ignore")
    @CommandPermission("space.chat.command.ignore.add")
    public class AddCommand extends BaseCommand {

        @Default
        @CommandCompletion("@globalplayers")
        public void onAdd(Player player, @Single String targetName) {
            // get user
            plugin.getUserManager().getByName(targetName, (targetUser) -> {
                if (targetUser == null) {
                    Messages.getInstance(plugin).ignorePlayerNotFound.message(player);
                    return;
                }

                // get sender user
                plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                    // add ignored
                    if (user.ignorePlayer(targetUser)) {
                        // send message
                        Messages.getInstance(plugin).ignoreAdded.message(player, "%user%", targetUser.getUsername());
                    } else {
                        // already ignored, send error
                        Messages.getInstance(plugin).playerAlreadyIgnored.message(player, "%user%", targetUser.getUsername());
                    }
                });
            });
        }

        @Default
        @CatchUnknown
        @HelpCommand
        public void onDefault(Player player) {
            // send help message
            Messages.getInstance(plugin).ignoreHelp.message(player);
        }
    }

    @Subcommand("list")
    @CommandAlias("ignore")
    @CommandPermission("space.chat.command.ignore.list")
    public class ListCommand extends BaseCommand {

        @Default
        public void onList(Player player) {
            // get sender user
            plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                Messages.getInstance(plugin).ignoreListHead.message(player, "%amount%", String.valueOf(user.getIgnored().size()));

                user.getIgnored().entrySet().stream()
                        .sorted((e1, e2) -> e1.getValue().compareToIgnoreCase(e2.getValue()))
                        .forEachOrdered(entry -> Messages.getInstance(plugin).ignoreListEntry.message(player,
                                "%name%", entry.getValue(),
                                "%uuid%", entry.getKey().toString()
                        ));

                Messages.getInstance(plugin).ignoreListFooter.message(player, "%amount%", String.valueOf(user.getIgnored().size()));
            });
        }
    }

    @Subcommand("remove")
    @CommandAlias("ignore")
    @CommandPermission("space.chat.command.ignore.remove")
    public class RemoveCommand extends BaseCommand {

        @Default
        @CommandCompletion("@ignored")
        public void onRemove(Player player, @Single String targetName) {
            // get user
            plugin.getUserManager().getByName(targetName, (targetUser) -> {
                if (targetUser == null) {
                    Messages.getInstance(plugin).ignorePlayerNotFound.message(player);
                    return;
                }

                // get sender user
                plugin.getUserManager().use(player.getUniqueId(), (user) -> {
                    // remove ignored
                    if (user.unignorePlayer(targetUser)) {
                        // send message
                        Messages.getInstance(plugin).ignoreRemoved.message(player, "%user%", targetUser.getUsername());
                    } else {
                        // not ignored, send error
                        Messages.getInstance(plugin).playerNotIgnored.message(player, "%user%", targetUser.getUsername());
                    }
                });

            });
        }

        @Default
        @CatchUnknown
        @HelpCommand
        public void onDefault(Player player) {
            // send help message
            Messages.getInstance(plugin).ignoreHelp.message(player);
        }
    }

    @Default
    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player) {
        // send help message
        Messages.getInstance(plugin).ignoreHelp.message(player);
    }
}
