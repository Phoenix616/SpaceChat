package dev.spaceseries.spacechat.command;

import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.ChatType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandPermission("space.chat.command.enablechat")
@CommandAlias("enablechat")
public class EnableChatCommand extends SpaceChatCommand {

    public EnableChatCommand(SpaceChatPlugin plugin) {
        super(plugin);
    }

    @Default
    @CatchUnknown
    @HelpCommand
    public void onDefault(Player player, @Optional String chatType) {
        plugin.getUserManager().use(player.getUniqueId(), user -> {
            if (chatType != null) {
                ChatType type = ChatType.parse(chatType);
                if (type != null) {
                    user.getDisabledChats().remove(type);
                    Messages.getInstance(plugin).enabledSpecificChat.message(player, "%type%", type.name().toLowerCase().replace('_', ' '));
                } else {
                    Messages.getInstance(plugin).invalidChatType.message(player, "%type%", chatType, "%types%", Arrays.stream(ChatType.values())
                            .map(Enum::name)
                            .map(String::toLowerCase)
                            .collect(Collectors.joining(", ")));
                }
            } else {
                user.getDisabledChats().clear();
                Messages.getInstance(plugin).enabledAllChat.message(player);
            }
        });
    }
}
