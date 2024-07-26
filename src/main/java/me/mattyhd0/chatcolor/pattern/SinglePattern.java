package me.mattyhd0.chatcolor.pattern;

import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import me.mattyhd0.chatcolor.pattern.format.TextFormatOptions;
import me.mattyhd0.chatcolor.util.Util;
import net.md_5.bungee.api.ChatColor;

public class SinglePattern extends BasePattern {

    public SinglePattern(String name, String permission, TextFormatOptions formatOptions, ChatColor... colors) {
        super(name, permission, formatOptions, colors);
    }

    @Override
    public String getText(CPlayer player, String text) {
        text = getTextFormatOptions().setFormat(text);
        if (player != null) {
            if (player.isBold()) {
                text = ChatColor.BOLD + text;
            }
            if (player.isStrikethrough()) {
                text = ChatColor.STRIKETHROUGH + text;
            }
            if (player.isUnderline()) {
                text = ChatColor.UNDERLINE + text;
            }
            if (player.isItalic()) {
                text = ChatColor.ITALIC + text;
            }
            if (player.isObfuscated()) {
                text = ChatColor.MAGIC + text;
            }
        }
        return getColors().get(0)+text;
    }

}
