package me.mattyhd0.chatcolor.pattern;

import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import me.mattyhd0.chatcolor.pattern.format.TextFormatOptions;
import me.mattyhd0.chatcolor.util.Util;
import net.md_5.bungee.api.ChatColor;

public class LinearPattern extends BasePattern {

    private boolean ignoreSpaces;

    public LinearPattern(String name, String permission, TextFormatOptions formatOptions, ChatColor... colors){
        super(name, permission, formatOptions, colors);
        ignoreSpaces = false;
    }

    @Override
    public String getText(CPlayer player, String text) {

        String[] characters = text.split("");
        StringBuilder finalString = new StringBuilder();
        int index = 0;

        for (String character : characters) {

            finalString.append(getColors().get(index)).append(character);
            if (index < getColors().size() - 1) {
                if(!ignoreSpaces){
                    index++;
                } else {
                    if (!character.equals(" ")) {
                        ++index;
                    }
                }
            } else {
                index = 0;
            }
        }
        String fs = finalString.toString();
        if (player != null) {
            if (player.isBold()) {
                fs = ChatColor.BOLD + fs;
            }
            if (player.isStrikethrough()) {
                fs = ChatColor.STRIKETHROUGH + fs;
            }
            if (player.isUnderline()) {
                fs = ChatColor.UNDERLINE + fs;
            }
            if (player.isItalic()) {
                fs = ChatColor.ITALIC + fs;
            }
            if (player.isObfuscated()) {
                fs = ChatColor.MAGIC + fs;
            }
        }
        return fs;
    }

    public void setIgnoreSpaces(boolean ignoreSpaces) {
        this.ignoreSpaces = ignoreSpaces;
    }

    public boolean isIgnoreSpaces() {
        return ignoreSpaces;
    }
}
