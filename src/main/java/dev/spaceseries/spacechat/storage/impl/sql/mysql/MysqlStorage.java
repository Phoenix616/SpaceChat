package dev.spaceseries.spacechat.storage.impl.sql.mysql;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.logging.wrap.LogChatWrapper;
import dev.spaceseries.spacechat.logging.wrap.LogPrivateChatWrapper;
import dev.spaceseries.spacechat.logging.wrap.LogWrapper;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.model.ChatType;
import dev.spaceseries.spacechat.model.User;
import dev.spaceseries.spacechat.storage.Storage;
import dev.spaceseries.spacechat.storage.StorageInitializationException;
import dev.spaceseries.spacechat.storage.impl.sql.mysql.factory.MySqlConnectionFactory;
import dev.spaceseries.spacechat.util.date.DateUtil;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static dev.spaceseries.spacechat.config.SpaceChatConfigKeys.*;

public class MysqlStorage extends Storage {

    public static final String LOG_CHAT_CREATION_STATEMENT = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "`uuid` TEXT NOT NULL,\n" +
            "`name` TEXT,\n" +
            "`message` TEXT,\n" +
            "`date` TEXT NOT NULL,\n" +
            "`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "PRIMARY KEY (`id`)\n" +
            ");";
    private final String LOG_CHAT = "INSERT INTO " + STORAGE_MYSQL_TABLES_CHAT_LOGS.get(plugin.getSpaceChatConfig().getAdapter()) + " (uuid, name, message, date) VALUES(?, ?, ?, ?);";
    public static final String LOG_PRIVATE_CHAT_CREATION_STATEMENT = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "`uuid` TEXT NOT NULL,\n" +
            "`name` TEXT,\n" +
            "`target` TEXT,\n" +
            "`message` TEXT,\n" +
            "`date` TEXT NOT NULL,\n" +
            "`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "PRIMARY KEY (`id`)\n" +
            ");";
    private final String LOG_PRIVATE_CHAT = "INSERT INTO " + STORAGE_MYSQL_TABLES_CHAT_LOGS.get(plugin.getSpaceChatConfig().getAdapter()) + " (uuid, name, target, message, date) VALUES(?, ?, ?, ?, ?);";
    public static final String USERS_CREATION_STATEMENT = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "`uuid` TEXT NOT NULL,\n" +
            "`username` TEXT NOT NULL,\n" +
            "`date` TEXT NOT NULL,\n" +
            "`lastMessaged` TEXT,\n" +
            "`disabledChats` TEXT,\n" +
            "`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "PRIMARY KEY (`id`)\n" +
            ");";
    private final String CREATE_USER = "INSERT INTO " + STORAGE_MYSQL_TABLES_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " (uuid, username, date) VALUES(?, ?, ?);";
    private final String SELECT_USER = "SELECT * FROM " + STORAGE_MYSQL_TABLES_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " WHERE uuid=?;";
    private final String SELECT_USER_USERNAME = "SELECT * FROM " + STORAGE_MYSQL_TABLES_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " WHERE username=?;";
    private final String UPDATE_USER = "UPDATE " + STORAGE_MYSQL_TABLES_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " SET username=?,lastMessaged=?,disabledChats=? WHERE uuid=?;";
    public static final String USERS_SUBSCRIBED_CHANNELS_CREATION_STATEMENT = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "`uuid` TEXT NOT NULL,\n" +
            "`channel` TEXT NOT NULL,\n" +
            "`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "PRIMARY KEY (`id`)\n" +
            ");";
    private final String SELECT_SUBSCRIBED_CHANNELS = "SELECT channel FROM " + STORAGE_MYSQL_TABLES_SUBSCRIBED_CHANNELS.get(plugin.getSpaceChatConfig().getAdapter()) + " WHERE uuid=?;";
    private final String DELETE_SUBSCRIBED_CHANNEL = "DELETE FROM " + STORAGE_MYSQL_TABLES_SUBSCRIBED_CHANNELS.get(plugin.getSpaceChatConfig().getAdapter()) + " WHERE uuid=? AND channel=?;";
    private final String INSERT_SUBSCRIBED_CHANNEL = "INSERT INTO " + STORAGE_MYSQL_TABLES_SUBSCRIBED_CHANNELS.get(plugin.getSpaceChatConfig().getAdapter()) + " (uuid, channel) VALUES(?, ?);";
    public static final String USERS_IGNORED_USERS_CREATION_STATEMENT = "CREATE TABLE IF NOT EXISTS `%s` (\n" +
            "`uuid` TEXT NOT NULL,\n" +
            "`ignoredid` TEXT NOT NULL,\n" +
            "`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "PRIMARY KEY (`id`)," +
            "UNIQUE (`uuid`, `ignoredid`)\n" +
            ");";
    private final String SELECT_IGNORED_USERS = "SELECT i.ignoredid as ignoredid, u.username as ignoredname FROM " + STORAGE_MYSQL_TABLES_IGNORED_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " AS i INNER JOIN " + STORAGE_MYSQL_TABLES_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " AS u ON i.ignoredid = u.uuid WHERE i.uuid=?;";
    private final String DELETE_UNIGNORED_USER = "DELETE FROM " + STORAGE_MYSQL_TABLES_IGNORED_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " WHERE uuid=? AND ignoredid NOT IN (%ignoredids%);";
    private final String INSERT_IGNORED_USER = "INSERT INTO " + STORAGE_MYSQL_TABLES_IGNORED_USERS.get(plugin.getSpaceChatConfig().getAdapter()) + " (uuid, ignoredid) VALUES(?, ?) ON DUPLICATE KEY IGNORE;";

    /**
     * The connection manager
     */
    private final MySqlConnectionFactory mysqlConnectionFactory;

    /**
     * Initializes new mysql storage
     */
    public MysqlStorage(SpaceChatPlugin plugin) throws StorageInitializationException {
        super(plugin);
        // initialize new connection manager
        mysqlConnectionFactory = new MySqlConnectionFactory(plugin.getSpaceChatConfig().get(SpaceChatConfigKeys.DATABASE_VALUES));
        this.mysqlConnectionFactory.init();

        this.init();
    }

    /**
     * Initializes the storage medium
     */
    @Override
    public void init() throws StorageInitializationException {
        try {
            SqlHelper.execute(mysqlConnectionFactory.getConnection(), String.format(MysqlStorage.LOG_CHAT_CREATION_STATEMENT, STORAGE_MYSQL_TABLES_CHAT_LOGS.get(plugin.getSpaceChatConfig().getAdapter())));
            SqlHelper.execute(mysqlConnectionFactory.getConnection(), String.format(MysqlStorage.LOG_PRIVATE_CHAT_CREATION_STATEMENT, STORAGE_MYSQL_TABLES_PRIVATE_CHAT_LOGS.get(plugin.getSpaceChatConfig().getAdapter())));
            SqlHelper.execute(mysqlConnectionFactory.getConnection(), String.format(MysqlStorage.USERS_CREATION_STATEMENT, STORAGE_MYSQL_TABLES_USERS.get(plugin.getSpaceChatConfig().getAdapter())));
            SqlHelper.execute(mysqlConnectionFactory.getConnection(), String.format(MysqlStorage.USERS_SUBSCRIBED_CHANNELS_CREATION_STATEMENT, STORAGE_MYSQL_TABLES_SUBSCRIBED_CHANNELS.get(plugin.getSpaceChatConfig().getAdapter())));
            SqlHelper.execute(mysqlConnectionFactory.getConnection(), String.format(MysqlStorage.USERS_IGNORED_USERS_CREATION_STATEMENT, STORAGE_MYSQL_TABLES_SUBSCRIBED_CHANNELS.get(plugin.getSpaceChatConfig().getAdapter())));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StorageInitializationException();
        }
    }

    @Override
    public void log(LogWrapper data, boolean async) {
        // if chat
        switch (data.getLogType()) {
            case CHAT:
                logChat((LogChatWrapper) data, async);
                break;
            case PRIVATE_CHAT:
                logChat((LogPrivateChatWrapper) data, async);
                break;
        }
    }

    /**
     * Gets a user
     *
     * @param uuid uuid
     * @return user
     */
    @Override
    public User getUser(UUID uuid) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER)) {
            // replace
            preparedStatement.setString(1, uuid.toString());

            // execute
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                String username = Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).getName())
                        .orElse("");

                // create new user
                User user = new User(plugin, uuid, username, new Date(), new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
                createUser(user);
                return user;
            }

            // build user and return
            String username = resultSet.getString("username");
            Date date = DateUtil.fromString(resultSet.getString("date"));

            // get channels that are subscribed
            List<Channel> subscribedChannels = getSubscribedChannels(uuid);

            String lastMessaged = resultSet.getString("lastMessaged");

            // get ignored users
            Map<UUID, String> ignoredUsers = getIgnoredUsers(uuid);

            // disabled chats
            List<ChatType> disabledChats = Arrays.stream(resultSet.getString("disabledChats").split(","))
                    .map(name -> {
                        try {
                            return ChatType.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().log(Level.WARNING, "Invalid value in user data of " + username + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new User(plugin, uuid, username, date, subscribedChannels, lastMessaged, ignoredUsers, disabledChats);
        } catch (SQLException throwables) {
            throwables.printStackTrace();

            return null;
        }
    }

    /**
     * Returns subscribed channels
     *
     * @return channels
     */
    private List<Channel> getSubscribedChannels(UUID uuid) {
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SUBSCRIBED_CHANNELS)) {
            // replace
            preparedStatement.setString(1, uuid.toString());

            // execute
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Channel> channels = new ArrayList<>();

            while (resultSet.next()) {
                String channelHandle = resultSet.getString("channel");

                Channel channel = plugin.getChannelManager().get(channelHandle, null);
                if (channel != null) {
                    channels.add(channel);
                }
            }

            return channels;
        } catch (SQLException throwables) {
            throwables.printStackTrace();

            return new ArrayList<>();
        }
    }

    /**
     * Returns ignored users
     *
     * @return ignored users
     */
    private Map<UUID, String> getIgnoredUsers(UUID uuid) {
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_IGNORED_USERS)) {
            // replace
            preparedStatement.setString(1, uuid.toString());

            // execute
            ResultSet resultSet = preparedStatement.executeQuery();

            Map<UUID, String> ignored = new HashMap<>();

            while (resultSet.next()) {
                UUID ignoredId = UUID.fromString(resultSet.getString("ignoredid"));
                String ignoredName = resultSet.getString("ignoredname");

                ignored.put(ignoredId, ignoredName);
            }

            return ignored;
        } catch (SQLException throwables) {
            throwables.printStackTrace();

            return new HashMap<>();
        }
    }

    /**
     * Gets a user by their username
     *
     * @param username username
     * @return user
     */
    @Override
    public User getUser(String username) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_USERNAME)) {
            // replace
            preparedStatement.setString(1, username);

            // execute
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            // build user and return
            UUID uuid = UUID.fromString(resultSet.getString("uuid"));
            Date date = DateUtil.fromString(resultSet.getString("date"));

            // get channels that are subscribed
            List<Channel> subscribedChannels = getSubscribedChannels(uuid);

            String lastMessaged = resultSet.getString("lastMessaged");

            // get ignored
            Map<UUID, String> ignored = getIgnoredUsers(uuid);

            // disabled chats
            List<ChatType> disabledChats = Arrays.stream(resultSet.getString("disabledChats").split(","))
                    .map(name -> {
                        try {
                            return ChatType.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().log(Level.WARNING, "Invalid value in user data of " + username + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new User(plugin, uuid, username, date, subscribedChannels, lastMessaged, ignored, disabledChats);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new user in the database
     *
     * @param user user
     */
    private void createUser(User user) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(CREATE_USER)) {
            // replace
            preparedStatement.setString(1, user.getUuid().toString());
            preparedStatement.setString(2, user.getUsername());
            preparedStatement.setString(3, DateUtil.toString(user.getDate()));

            // execute
            preparedStatement.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Updates a user
     *
     * @param user user
     */
    @Override
    public void updateUser(User user) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER)) {
            // replace
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getLastMessaged());
            preparedStatement.setString(3, user.getDisabledChats().stream().map(ChatType::name).collect(Collectors.joining(",")));
            preparedStatement.setString(4, user.getUuid().toString());

            // execute
            preparedStatement.execute();

            // delete remaining channels that shouldn't be there
            List<Channel> serverSideSubscribedList = getSubscribedChannels(user.getUuid());

            serverSideSubscribedList.forEach(serverSideSubscribedChannel -> {
                if (user.getSubscribedChannels().stream()
                        .noneMatch(c -> c.getHandle().equals(serverSideSubscribedChannel.getHandle()))) {
                    deleteChannelRow(user.getUuid(), serverSideSubscribedChannel);
                }
            });

            user.getSubscribedChannels().forEach(channel -> {
                if (serverSideSubscribedList.stream()
                        .anyMatch(c -> c.getHandle().equals(channel.getHandle()))) {
                    return;
                }
                insertChannelRow(user.getUuid(), channel);
            });


            deleteUnignoredRows(user.getUuid(), user.getIgnored().keySet());
            insertIgnoredRows(user.getUuid(), user.getIgnored().keySet());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Inserts a channel row
     *
     * @param uuid    uuid
     * @param channel channel
     */
    private void insertChannelRow(UUID uuid, Channel channel) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SUBSCRIBED_CHANNEL)) {
            // replace
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, channel.getHandle());

            // execute
            preparedStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Deletes a channel row
     *
     * @param uuid    uuid
     * @param channel channel
     */
    private void deleteChannelRow(UUID uuid, Channel channel) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SUBSCRIBED_CHANNEL)) {
            // replace
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, channel.getHandle());

            // execute
            preparedStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Inserts ignored users
     *
     * @param uuid    uuid
     * @param ignored ignored UUIDs
     */
    private void insertIgnoredRows(UUID uuid, Collection<UUID> ignored) {
        // create prepared statement
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_IGNORED_USER)) {
            // replace
            for (UUID ignoredId : ignored) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, ignoredId.toString());
                preparedStatement.addBatch();
            }

            // execute
            preparedStatement.executeBatch();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Deletes unignored users
     *
     * @param uuid    uuid
     * @param ignored ignored UUIDs
     */
    private void deleteUnignoredRows(UUID uuid, Collection<UUID> ignored) {
        // create prepared statement
        String query = DELETE_UNIGNORED_USER.replace("%ignoredids%", String.join(",", Collections.nCopies(ignored.size(), "?")));
        try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // replace
            preparedStatement.setString(1, uuid.toString());
            int i = 2;
            for (UUID ignoredId : ignored) {
                preparedStatement.setString(i, ignoredId.toString());
                i++;
            }

            // execute
            preparedStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void close() {
        this.mysqlConnectionFactory.shutdown();
    }

    /**
     * Logs chat to the MySQL database
     *
     * @param data  The data
     * @param async async
     */
    private void logChat(LogChatWrapper data, boolean async) {
        Runnable task = () -> {
            // create prepared statement
            try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(LOG_CHAT)) {
                // replace
                preparedStatement.setString(1, data.getSenderUUID().toString());
                preparedStatement.setString(2, data.getSenderName());
                preparedStatement.setString(3, data.getMessage());
                preparedStatement.setString(4, DateUtil.toString(data.getAt()));

                // execute
                preparedStatement.execute();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        };

        if (async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        else
            Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Logs private chat to the MySQL database
     *
     * @param data  The data
     * @param async async
     */
    private void logChat(LogPrivateChatWrapper data, boolean async) {
        Runnable task = () -> {
            // create prepared statement
            try (Connection connection = mysqlConnectionFactory.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(LOG_PRIVATE_CHAT)) {
                // replace
                preparedStatement.setString(1, data.getSenderUUID().toString());
                preparedStatement.setString(2, data.getSenderName());
                preparedStatement.setString(3, data.getTargetName());
                preparedStatement.setString(3, data.getMessage());
                preparedStatement.setString(4, DateUtil.toString(data.getAt()));

                // execute
                preparedStatement.execute();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        };

        if (async)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        else
            Bukkit.getScheduler().runTask(plugin, task);
    }
}
