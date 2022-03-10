package dev.spaceseries.spacechat.command;

import co.aikar.commands.annotation.*;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.ChatType;
import dev.spaceseries.spacechat.api.command.SpaceChatCommand;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandPermission("space.chat.command.disablechat")
@CommandAlias("disablechat")
public class DisableChatCommand extends SpaceChatCommand {

    public DisableChatCommand(SpaceChatPlugin plugin) {
        super(plugin);
    }

    @Default
    @CommandCompletion("@chattypes")
    public void onDefault(Player player, @Optional String chatType) {
        plugin.getUserManager().use(player.getUniqueId(), user -> {
            if (chatType != null) {
                ChatType type = ChatType.parse(chatType);
                if (type != null) {
                    user.disableChats(type);
                    Messages.getInstance(plugin).disabledSpecificChat.message(player, "%type%", type.name().toLowerCase().replace('_', ' '));
                } else {
                    Messages.getInstance(plugin).invalidChatType.message(player, "%type%", chatType, "%types%", Arrays.stream(ChatType.values())
                            .map(Enum::name)
                            .map(String::toLowerCase)
                            .collect(Collectors.joining(", ")));
                }
            } else {
                user.disableChats(ChatType.values());
                Messages.getInstance(plugin).disabledAllChat.message(player);
            }
        });
    }
}
