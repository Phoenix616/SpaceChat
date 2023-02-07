package dev.spaceseries.spacechat.builder.live;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.api.wrapper.Trio;
import dev.spaceseries.spacechat.builder.Builder;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.model.formatting.Extra;
import dev.spaceseries.spacechat.model.formatting.Format;
import dev.spaceseries.spacechat.parser.MessageParser;
import dev.spaceseries.spacechat.replacer.AmpersandReplacer;
import dev.spaceseries.spacechat.replacer.SectionReplacer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.mattstudios.msg.adventure.AdventureMessage;
import me.mattstudios.msg.base.MessageOptions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Locale;

public class NormalLiveChatFormatBuilder extends LiveChatFormatBuilder implements Builder<Trio<Player, String, Format>, TextComponent> {

    /**
     * Ampersand replacer
     */
    private static final AmpersandReplacer AMPERSAND_REPLACER = new AmpersandReplacer();

    /**
     * Section replacer
     */
    private static final SectionReplacer SECTION_REPLACER = new SectionReplacer();

    public NormalLiveChatFormatBuilder(SpaceChatPlugin plugin) {
        super(plugin);
    }

    /**
     * Builds an array of baseComponents from a message, player, and format
     *
     * @param input The trio of inputs
     * @return The array of baseComponents
     */
    @Override
    public TextComponent build(Trio<Player, String, Format> input) {
        // get input parameters
        Player player = input.getLeft();
        String messageString = input.getMid();
        Format format = input.getRight();

        // create component builder for message
        ComponentBuilder<TextComponent, TextComponent.Builder> componentBuilder = Component.text();

        // loop through format parts
        format.getFormatParts().forEach(formatPart -> {
            // create component builder
            ComponentBuilder<TextComponent, TextComponent.Builder> partComponentBuilder = Component.text();
            // if the part has "line", it is a SINGLE MiniMessage...in that case, just parse & return (continues to next part if exists, which it shouldn't)
            if (formatPart.getLine() != null) {
                // replace placeholders
                String mmWithPlaceholdersReplaced = SECTION_REPLACER.apply(PlaceholderAPI.setPlaceholders(player, AMPERSAND_REPLACER.apply(formatPart.getLine(), player)), player);

                // get chat message (formatted)
                MessageOptions.Builder messageOptionsBuilder = MessageOptions.builder(me.mattstudios.msg.base.internal.Format.NONE);
                if (player != null && player.hasPermission(SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_COLORS.get(plugin.getSpaceChatConfig().getAdapter()))) {
                    messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.COLOR);
                    messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.HEX);
                    messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.GRADIENT);
                    messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.RAINBOW);
                }
                String formattingPermission = SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_FORMATTING.get(plugin.getSpaceChatConfig().getAdapter());
                for (me.mattstudios.msg.base.internal.Format f : me.mattstudios.msg.base.internal.Format.ALL) {
                    if (player != null && player.hasPermission(formattingPermission + f.name().toLowerCase(Locale.ROOT))) {
                        messageOptionsBuilder.addFormat(f);
                    }
                }

                String chatMessage = LegacyComponentSerializer.legacySection().serialize(AdventureMessage.create(messageOptionsBuilder.build()).parse(messageString));
                if (chatMessage.startsWith(ChatColor.WHITE.toString()) && !messageString.startsWith("&f")) {
                    chatMessage = chatMessage.substring(2);
                }

                // parse message
                Component message = new MessageParser(plugin).parse(player, Component.text(chatMessage));

                // parse miniMessage
                Component parsedMiniMessage = MiniMessage.miniMessage().deserialize(mmWithPlaceholdersReplaced);

                // replace chat message
                parsedMiniMessage = parsedMiniMessage.replaceText((text) -> text.match("<chat_message>").replacement(message));

                // parse MiniMessage into builder
                partComponentBuilder.append(parsedMiniMessage);
                // append partComponentBuilder to main builder
                componentBuilder.append(partComponentBuilder.build());
                return;
            }

            String text = formatPart.getText();

            // basically what I am doing here is converting & -> section, then replacing placeholders, then section -> &
            // this just bypasses PAPI's hacky way of coloring text which shouldn't even be implemented...
            text = SECTION_REPLACER.apply(PlaceholderAPI.setPlaceholders(player, AMPERSAND_REPLACER.apply(text, player)), player);

            // build text from legacy (and replace <chat_message> with the actual message)
            // and check permissions for chat colors
            Component parsedText;

            // get chat message (formatted)
            MessageOptions.Builder messageOptionsBuilder = MessageOptions.builder(me.mattstudios.msg.base.internal.Format.NONE);
            if (player != null && player.hasPermission(SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_COLORS.get(plugin.getSpaceChatConfig().getAdapter()))) {
                messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.COLOR);
                messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.HEX);
                messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.GRADIENT);
                messageOptionsBuilder.addFormat(me.mattstudios.msg.base.internal.Format.RAINBOW);
            }
            String formattingPermission = SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_FORMATTING.get(plugin.getSpaceChatConfig().getAdapter());
            for (me.mattstudios.msg.base.internal.Format f : me.mattstudios.msg.base.internal.Format.ALL) {
                if (player != null && player.hasPermission(formattingPermission + f.name().toLowerCase(Locale.ROOT))) {
                    messageOptionsBuilder.addFormat(f);
                }
            }

            // need to convert back to legacy in order to handle links als mf-msg can't do that
            String chatMessage = LegacyComponentSerializer.legacySection().serialize(AdventureMessage.create(messageOptionsBuilder.build()).parse(messageString));
            if (chatMessage.startsWith(ChatColor.WHITE.toString()) && !messageString.startsWith("&f")) {
                chatMessage = chatMessage.substring(2);
            }

            Component messageComponent = computeChatMessageComponentSerializer(player)
                    .deserialize(chatMessage);

            parsedText = LegacyComponentSerializer.legacyAmpersand().deserialize(text)
                    .replaceText((b) -> b.matchLiteral("<chat_message>").replacement(messageComponent));

            // parse message
            parsedText = new MessageParser(plugin).parse(player, parsedText);

            /* Retaining events for MULTIPLE components */

            // parse extra (if applicable)
            if (formatPart.getExtra() != null) {
                Extra extra = formatPart.getExtra();

                // if contains click action
                if (extra.getClickAction() != null) {
                    // apply
                    parsedText = parsedText.clickEvent(extra.getClickAction().toClickEvent(player));
                }

                // if contains hover action
                if (extra.getHoverAction() != null) {
                    // apply
                    parsedText = parsedText.hoverEvent(extra.getHoverAction().toHoverEvent(player));
                }
            }

            partComponentBuilder.append(parsedText);

            // append build partComponentBuilder to main componentBuilder
            componentBuilder.append(partComponentBuilder.build());
        });

        // return built component builder
        return componentBuilder.build();
    }

    /**
     * Computes the legacy component serializer based on player context for chat messages
     *
     * @param player player
     * @return legacy component serializer
     */
    private LegacyComponentSerializer computeChatMessageComponentSerializer(Player player) {
        LegacyComponentSerializer.Builder legacyComponentSerializerBuilder = LegacyComponentSerializer.builder();

        if (player != null && player.hasPermission(SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_COLORS.get(plugin.getSpaceChatConfig().getAdapter()))) {
            legacyComponentSerializerBuilder = legacyComponentSerializerBuilder.hexColors();
            legacyComponentSerializerBuilder = legacyComponentSerializerBuilder.character('&');
        }

        if (player != null && player.hasPermission(SpaceChatConfigKeys.PERMISSIONS_USE_CHAT_LINKS.get(plugin.getSpaceChatConfig().getAdapter()))) {
            legacyComponentSerializerBuilder = legacyComponentSerializerBuilder.extractUrls();
        }

        return legacyComponentSerializerBuilder.build();
    }
}
