package dev.spaceseries.spacechat.chat;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.loader.ChatFormatLoader;
import dev.spaceseries.spacechat.loader.FormatLoader;
import dev.spaceseries.spacechat.loader.FormatManager;
import dev.spaceseries.spacechat.model.formatting.ChatFormat;
import dev.spaceseries.spacechat.model.formatting.FormatType;
import dev.spaceseries.spacechat.sync.ServerSyncServiceManager;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class PrivateFormatManager extends FormatManager<ChatFormat> {

    /**
     * The format loader
     */
    private final FormatLoader<ChatFormat> privateFormatFormatLoader;

    /**
     * Server sync service manager
     */
    private final ServerSyncServiceManager serverSyncServiceManager;

    /**
     * Initializes
     */
    public PrivateFormatManager(SpaceChatPlugin plugin) {
        super(plugin);
        this.serverSyncServiceManager = plugin.getServerSyncServiceManager();

        // create format manager
        this.privateFormatFormatLoader = new ChatFormatLoader(plugin, FormatType.PRIVATE.getSectionKey().toLowerCase(Locale.ROOT));

        // load
        this.loadFormats();
    }

    /**
     * Loads formats
     */
    public void loadFormats() {
        privateFormatFormatLoader.load(this);

        // fall back to chat format manager
        if (getAll().isEmpty()) {
            for (Map.Entry<String, ChatFormat> formatEntry : plugin.getChatFormatManager().getAll().entrySet()) {
                add(formatEntry.getKey(), formatEntry.getValue());
            }
        }
    }

    /**
     * Sends a private message using the applicable format
     *
     * @param player        The player
     * @param targetName    The target player
     * @param message       The message
     */
    public void send(Player player, String targetName, String message) {
        ChatManager chatManager = plugin.getChatManager();

        // get applicable format
        ChatFormat applicableFormat = getAll()
                .values()
                .stream()
                .filter(format -> player.hasPermission(format.getPermission()) || format.getHandle().equals("default")) // player has permission OR the format is default
                .max(Comparator.comparing(ChatFormat::getPriority))
                .orElse(getAll().values().stream()
                        .findFirst()
                        .orElse(null));

        chatManager.sendPrivateMessage(player, targetName, message, applicableFormat == null ? null : applicableFormat.getFormat(), null);
    }
}
