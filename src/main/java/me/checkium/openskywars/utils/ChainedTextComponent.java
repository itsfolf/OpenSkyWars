package me.checkium.openskywars.utils;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Class to easily create text components
 * using chained methods.
 *
 * @author Checkium
 */
public class ChainedTextComponent {

    private TextComponent comp;

    public ChainedTextComponent(String text) {
         comp = new TextComponent(text);
    }

    public ChainedTextComponent color(ChatColor color) {
        comp.setColor(color);
        return this;
    }

    public ChainedTextComponent bold() {
        comp.setBold(true);
        return this;
    }

    public ChainedTextComponent underlined() {
        comp.setUnderlined(true);
        return this;
    }

    public ChainedTextComponent clickEvent(ClickEvent e) {
        comp.setClickEvent(e);
        return this;
    }

    public ChainedTextComponent commandOnCLick(String cmd) {
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
        return this;
    }

    public ChainedTextComponent suggestOnClick(String cmd) {
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd));
        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new ChainedTextComponent("Click to edit").color(ChatColor.GREEN).get()}));
        return this;
    }

    public ChainedTextComponent clickEvent(ClickEvent.Action act, String cmd) {
        comp.setClickEvent(new ClickEvent(act, cmd));
        return this;
    }

    public ChainedTextComponent hover(HoverEvent e) {
        comp.setHoverEvent(e);
        return this;
    }

    public ChainedTextComponent strikethrough() {
        comp.setStrikethrough(true);
        return this;
    }

    public ChainedTextComponent obfuscated() {
        comp.setObfuscated(true);
        return this;
    }

    public ChainedTextComponent add(TextComponent e) {
        comp.addExtra(e);
        return this;
    }

    public ChainedTextComponent add(ChainedTextComponent e) {
        comp.addExtra(e.get());
        return this;
    }

    public TextComponent get() {
        return comp;
    }
}
