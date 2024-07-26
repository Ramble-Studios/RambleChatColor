package me.mattyhd0.chatcolor.pattern;

import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.pattern.format.TextFormatOptions;
import net.md_5.bungee.api.ChatColor;

import java.util.Collections;
import java.util.List;

public class RandomGradientPattern extends GradientPattern {

    public RandomGradientPattern(String name, String permission, TextFormatOptions formatOptions, ChatColor... colors) {
        super(name, permission, formatOptions, colors);
    }

    @Override
    public String getText(CPlayer player, String text) {
        List<ChatColor> colors = getColors();
        Collections.shuffle(colors);
        return gradient(player, text, getColors(), getTextFormatOptions());
    }

}
