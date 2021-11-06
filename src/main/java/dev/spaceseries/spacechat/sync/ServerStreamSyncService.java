package dev.spaceseries.spacechat.sync;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.sync.packet.ReceiveStreamDataPacket;
import dev.spaceseries.spacechat.sync.packet.SendStreamDataPacket;

public abstract class ServerStreamSyncService extends ServerSyncService {

    /**
     * Construct server sync service
     *
     * @param plugin         plugin
     * @param serviceManager service manager
     */
    public ServerStreamSyncService(SpaceChatPlugin plugin, ServerSyncServiceManager serviceManager) {
        super(plugin, serviceManager);
    }

    /**
     * Publishes a chat message across the server
     */
    public abstract void publishChat(SendStreamDataPacket<?> packet);

    /**
     * Publishes a private chat message across the server
     */
    public abstract void publishPrivateChat(SendStreamDataPacket<?> packet);

    /**
     * Publishes a broadcast message across the server
     */
    public abstract void publishBroadcast(SendStreamDataPacket<?> packet);

    /**
     * Receives an incoming chat message
     */
    public abstract void receiveChat(ReceiveStreamDataPacket<?> packet);

    /**
     * Receives an incoming private chat message
     */
    public abstract void receivePrivateChat(ReceiveStreamDataPacket<?> packet);

    /**
     * Receives an incoming chat message
     */
    public abstract void receiveBroadcast(ReceiveStreamDataPacket<?> packet);

    /**
     * Starts the service in question
     */
    public abstract void start();

    /**
     * Ends the service in question
     */
    public abstract void end();
}
