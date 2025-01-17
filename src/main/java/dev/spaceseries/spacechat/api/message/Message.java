package dev.spaceseries.spacechat.api.message;

import com.google.common.base.Joiner;
import de.themoep.minedown.adventure.Replacer;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.config.generic.adapter.ConfigurationAdapter;
import me.mattstudios.msg.adventure.AdventureMessage;
import me.mattstudios.msg.base.MessageOptions;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Message {

    private static AudienceProvider audienceProvider;

    public static void initAudience(SpaceChatPlugin plugin) {
        audienceProvider = BukkitAudiences.create(plugin);
    }

    public static AudienceProvider getAudienceProvider() {
        return audienceProvider;
    }

    /**
     * Gets a message from a configuration section
     *
     * @param identifier identifier, also the path in the configuration at which the message is
     * @param adapter    adapter
     * @return message
     */
    public static Message fromConfigurationSection(String identifier, ConfigurationAdapter adapter) {
        // get lines list from adapter
        List<String> lines = adapter.getStringList(identifier, Collections.emptyList());
        return new Message(identifier, lines, null);
    }

    /**
     * Returns a builder using the identifier provided
     *
     * @param identifier identifier
     * @return builder
     */
    public static Builder builder(String identifier) {
        return new Builder(identifier);
    }

    /**
     * Returns a builder with no identifier
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The message identifier
     */
    private final String identifier;

    /**
     * The lines of the message
     * Will be combined into one component, split by the newline separator
     */
    private final List<String> lines;

    /**
     * Message options
     */
    private final MessageOptions messageOptions;

    /**
     * Message
     *
     * @param identifier identifier
     * @param lines      lines
     */
    public Message(String identifier, List<String> lines, MessageOptions messageOptions) {
        this.identifier = identifier;
        this.lines = lines;
        this.messageOptions = messageOptions;
    }

    /**
     * Returns the message identifier
     *
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the lines
     *
     * @return lines
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Returns the message options
     *
     * @return message options
     */
    public MessageOptions getMessageOptions() {
        return messageOptions;
    }

    /**
     * Parses the message into a component
     *
     * @return component
     */
    private Component parse() {
        // adventure message
        AdventureMessage adventureMessage = messageOptions != null ? AdventureMessage.create(messageOptions) : AdventureMessage.create();

        // convert lines into one line
        String line = Joiner.on("\\n").join(lines);

        // parse into component
        return adventureMessage.parse(line);
    }

    /**
     * Replaces replacers inside the component
     *
     * @param component component
     * @param replacers replacers
     * @return component
     */
    private Component replace(Component component, String... replacers) {
        return new Replacer().placeholderPrefix("").placeholderSuffix("")
                .replace(replacers).replaceIn(component);
    }

    /**
     * Replaces replacers inside the component
     *
     * @param component component
     * @param replacers replacers
     * @return component
     */
    private Component replace(Component component, Map<String, Component> replacers) {
        return new Replacer().placeholderPrefix("").placeholderSuffix("")
                .replace(replacers).replaceIn(component);
    }

    /**
     * Used as a proxy method to compile the entire message (parsing, replacing, etc) into a component
     *
     * @param replacers replacers
     * @return component
     */
    public Component compile(String... replacers) {
        return replace(parse(), replacers);
    }

    /**
     * Used as a proxy method to compile the entire message (parsing, replacing, etc) into a component
     *
     * @param replacers replacers
     * @return component
     */
    public Component compile(Map<String, Component> replacers) {
        return replace(parse(), replacers);
    }

    /**
     * Sends a message to a command sender
     *
     * @param commandSender command sender
     * @param replacers     replacers
     */
    public void message(CommandSender commandSender, String... replacers) {
        message(Collections.singletonList(commandSender), replacers);
    }

    /**
     * Sends a message to a 'list' of senders
     *
     * @param commandSenders command senders 'list'
     * @param replacers      replacers
     */
    public void message(Iterable<CommandSender> commandSenders, String... replacers) {
        Component output = compile(replacers);

        commandSenders.forEach(sender -> {
            if (sender instanceof Player) {
                audienceProvider.player(((Player) sender).getUniqueId()).sendMessage(output);
            } else {
                audienceProvider.console().sendMessage(output);
            }
        });
    }

    /**
     * Broadcasts a message to the entire server
     *
     * @param replacers replacers
     */
    public void broadcast(String... replacers) {
        Component output = compile(replacers);
        audienceProvider.all().sendMessage(output);
    }

    public static class Builder {

        /**
         * The message identifier
         */
        private final String identifier;

        /**
         * The lines of the message
         * Will be combined into one component, split by the newline separator
         */
        private final List<String> lines;

        /**
         * Message options
         */
        private MessageOptions messageOptions;

        /**
         * Builder
         *
         * @param identifier identifier
         */
        public Builder(String identifier) {
            this.identifier = identifier;
            this.lines = new ArrayList<>();
        }

        /**
         * Builder
         **/
        public Builder() {
            this(null);
        }

        /**
         * Returns the identifier
         *
         * @return identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Adds a line
         *
         * @param line line
         * @return this
         */
        public Builder addLine(String line) {
            lines.add(line);
            return this;
        }

        /**
         * Returns the lines
         *
         * @return lines
         */
        public List<String> getLines() {
            return lines;
        }

        /**
         * Sets message options
         *
         * @param messageOptions message options
         * @return this
         */
        public Builder setMessageOptions(MessageOptions messageOptions) {
            this.messageOptions = messageOptions;
            return this;
        }

        /**
         * Returns the message options
         *
         * @return message options
         */
        public MessageOptions getMessageOptions() {
            return messageOptions;
        }

        /**
         * Builds a builder into a message
         *
         * @return message
         */
        public Message build() {
            return new Message(this.identifier, this.lines, this.messageOptions);
        }
    }
}
