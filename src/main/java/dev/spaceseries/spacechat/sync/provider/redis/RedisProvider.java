package dev.spaceseries.spacechat.sync.provider.redis;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.sync.provider.Provider;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.net.URISyntaxException;

public class RedisProvider implements Provider<RedisClient> {

    /**
     * Pool
     */
    private RedisClient client;

    /**
     * Construct redis provider
     */
    public RedisProvider(SpaceChatPlugin plugin) {
        try {
            // initialize pool
            client = RedisClient.create(SpaceChatConfigKeys.REDIS_URL.get(plugin.getSpaceChatConfig().getAdapter()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            client = null;
        }
    }

    @Override
    public RedisClient provide() {
        return client;
    }

    /**
     * Ends the provided pool
     */
    public void end() {
        this.client.shutdown();
    }
}
