package dev.spaceseries.spacechat.external.papi;

import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.model.ChatType;
import dev.spaceseries.spacechat.model.User;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * This class will automatically register as a placeholder expansion
 * when a jar including this class is added to the directory
 * {@code /plugins/PlaceholderAPI/expansions} on your server.
 * <br>
 * <br>If you create such a class inside your own plugin, you have to
 * register it manually in your plugins {@code onEnable()} by using
 * {@code new SpaceChatExpansion().register();}
 */
public class SpaceChatExpansion extends PlaceholderExpansion {

    private final SpaceChatPlugin plugin;

    public SpaceChatExpansion(SpaceChatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * This method should always return true unless we
     * have a dependency we need to make sure is on the server
     * for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        return "yakovliam";
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "spacechat";
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
     * @param identifier A String containing the identifier/value.
     * @return Possibly-null String of the requested identifier.
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        // server-identifier
        if (identifier.equalsIgnoreCase("server-identifier")) {
            return SpaceChatConfigKeys.REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter());
        }

        // server-displayname
        if (identifier.equalsIgnoreCase("server-displayname")) {
            return SpaceChatConfigKeys.REDIS_SERVER_DISPLAYNAME.get(plugin.getSpaceChatConfig().getAdapter());
        }

        if (player.isOnline() && identifier.equalsIgnoreCase("public-chat-status")) {
            User user = plugin.getUserManager().get(player.getUniqueId());
            return LegacyComponentSerializer.legacySection().serialize(
                    (user.hasChatEnabled(ChatType.PUBLIC)
                            ? Messages.getInstance(plugin).placeholderChatEnabled
                            : Messages.getInstance(plugin).placeholderChatDisabled
            ).compile());
        }

        if (player.isOnline() && identifier.equalsIgnoreCase("private-chat-status")) {
            User user = plugin.getUserManager().get(player.getUniqueId());
            return LegacyComponentSerializer.legacySection().serialize(
                    (user.hasChatEnabled(ChatType.PRIVATE)
                            ? Messages.getInstance(plugin).placeholderChatEnabled
                            : Messages.getInstance(plugin).placeholderChatDisabled
            ).compile());
        }

        // We return null if an invalid placeholder
        // was provided
        return null;
    }
}
