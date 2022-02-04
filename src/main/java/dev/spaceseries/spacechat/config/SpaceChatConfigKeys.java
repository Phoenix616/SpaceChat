package dev.spaceseries.spacechat.config;

import com.google.common.collect.ImmutableMap;
import dev.spaceseries.spacechat.api.config.generic.KeyedConfiguration;
import dev.spaceseries.spacechat.api.config.generic.key.ConfigKey;
import dev.spaceseries.spacechat.api.config.generic.key.SimpleConfigKey;
import dev.spaceseries.spacechat.storage.impl.sql.mysql.StorageCredentials;

import java.util.List;
import java.util.Map;

import static dev.spaceseries.spacechat.api.config.generic.key.ConfigKeyFactory.key;
import static dev.spaceseries.spacechat.api.config.generic.key.ConfigKeyFactory.notReloadable;

public class SpaceChatConfigKeys {

    public static ConfigKey<String> STORAGE_USE = key(c -> c.getString("storage.use"));
    public static ConfigKey<String> STORAGE_MYSQL_TABLES_CHAT_LOGS = key(c -> c.getString("storage.mysql.tables.chat-logs"));
    public static ConfigKey<String> STORAGE_MYSQL_TABLES_PRIVATE_CHAT_LOGS = key(c -> c.getString("storage.mysql.tables.private-chat-logs"));
    public static ConfigKey<String> STORAGE_MYSQL_TABLES_USERS = key(c -> c.getString("storage.mysql.tables.users"));
    public static ConfigKey<String> STORAGE_MYSQL_TABLES_SUBSCRIBED_CHANNELS = key(c -> c.getString("storage.mysql.tables.subscribed-channels"));
    public static ConfigKey<String> STORAGE_MYSQL_TABLES_IGNORED_USERS = key(c -> c.getString("storage.mysql.tables.ignored-users"));

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<StorageCredentials> DATABASE_VALUES = notReloadable(key(c -> {
        int maxPoolSize = c.getInteger("storage.mysql.pool-settings.maximum-pool-size", c.getInteger("data.pool-size"));
        int minIdle = c.getInteger("storage.mysql.pool-settings.minimum-idle", maxPoolSize);
        int maxLifetime = c.getInteger("storage.mysql.pool-settings.maximum-lifetime");
        int keepAliveTime = c.getInteger("storage.mysql.pool-settings.keepalive-time");
        int connectionTimeout = c.getInteger("storage.mysql.pool-settings.connection-timeout");
        Map<String, String> props = ImmutableMap.copyOf(c.getStringMap("storage.mysql.pool-settings.properties", ImmutableMap.of()));

        return new StorageCredentials(
                c.getString("storage.mysql.address", null),
                c.getString("storage.mysql.database", null),
                c.getString("storage.mysql.username", null),
                c.getString("storage.mysql.password", null),
                maxPoolSize, minIdle, maxLifetime, keepAliveTime, connectionTimeout, props
        );
    }));

    public static ConfigKey<Boolean> REDIS_ENABLED = key(c -> c.getBoolean("redis.enabled"));
    public static ConfigKey<String> REDIS_URL = key(c -> c.getString("redis.url"));
    public static ConfigKey<String> REDIS_CHAT_CHANNEL = key(c -> c.getString("redis.chat-channel"));
    public static ConfigKey<String> REDIS_PRIVATE_CHAT_CHANNEL = key(c -> c.getString("redis.private-chat-channel"));
    public static ConfigKey<String> REDIS_BROADCAST_CHANNEL = key(c -> c.getString("redis.broadcast-channel"));
    public static ConfigKey<String> REDIS_SERVER_IDENTIFIER = key(c -> c.getString("redis.server.identifier"));
    public static ConfigKey<String> REDIS_SERVER_DISPLAYNAME = key(c -> c.getString("redis.server.displayName"));
    public static ConfigKey<String> REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY = key(c -> c.getString("redis.player-subscribed-channels-list-key"));
    public static ConfigKey<String> REDIS_PLAYER_CURRENT_CHANNEL_KEY = key(c -> c.getString("redis.player-current-channel-key"));
    public static ConfigKey<String> REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY = key(c -> c.getString("redis.channels-subscribed-uuids-list-key"));
    public static ConfigKey<String> REDIS_ONLINE_PLAYERS_SERVER_KEY = key(c -> c.getString("redis.online-players-server-key"));
    public static ConfigKey<String> REDIS_ONLINE_PLAYERS_KEY = key(c -> c.getString("redis.online-players-list-key"));

    public static ConfigKey<Boolean> LOGGING_CHAT_LOG_TO_STORAGE = key(c -> c.getBoolean("logging.chat.log-to-storage"));

    public static ConfigKey<String> PERMISSIONS_USE_CHAT_COLORS = key(c -> c.getString("permissions.use-chat-colors"));
    public static ConfigKey<String> PERMISSIONS_USE_CHAT_FORMATTING = key(c -> c.getString("permissions.use-chat-formatting"));
    public static ConfigKey<String> PERMISSIONS_USE_ITEM_CHAT = key(c -> c.getString("permissions.use-item-chat"));
    public static ConfigKey<String> PERMISSIONS_USE_CHAT_LINKS = key(c -> c.getString("permissions.use-chat-links"));
    public static ConfigKey<String> PERMISSIONS_BYPASS_IGNORE = key(c -> c.getString("permissions.bypass-ignore"));
    public static ConfigKey<String> PERMISSIONS_BYPASS_DISABLED_PUBLIC = key(c -> c.getString("permissions.bypass-disabled-public-chat"));
    public static ConfigKey<String> PERMISSIONS_BYPASS_DISABLED_PRIVATE = key(c -> c.getString("permissions.bypass-disabled-private-chat"));

    public static ConfigKey<Boolean> BROADCAST_USE_LANG_WRAPPER = key(c -> c.getBoolean("broadcast.use-lang-wrapper"));

    public static ConfigKey<Boolean> ITEM_CHAT_ENABLED = key(c -> c.getBoolean("item-chat.enabled"));
    public static ConfigKey<List<String>> ITEM_CHAT_REPLACE_ALIASES = key(c -> c.getStringList("item-chat.replace-aliases"));
    public static ConfigKey<String> ITEM_CHAT_WITH_CHAT = key(c -> c.getString("item-chat.with.chat"));
    public static ConfigKey<Boolean> ITEM_CHAT_WITH_LORE_USE_CUSTOM = key(c -> c.getBoolean("item-chat.with.lore.use-custom"));
    public static ConfigKey<List<String>> ITEM_CHAT_WITH_LORE_CUSTOM = key(c -> c.getStringList("item-chat.with.lore.custom"));
    public static ConfigKey<Integer> ITEM_CHAT_MAX_PER_MESSAGE = key(c -> c.getInteger("item-chat.max-per-message"));

    public static ConfigKey<Boolean> USE_RELATIONAL_PLACEHOLDERS = key(c -> c.getBoolean("use-relational-placeholders"));

    private static final List<SimpleConfigKey<?>> KEYS = KeyedConfiguration.initialise(SpaceChatConfigKeys.class);

    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }
}
