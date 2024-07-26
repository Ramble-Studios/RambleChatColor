package me.mattyhd0.chatcolor.gui;

import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.ChatColorPlugin;
import me.mattyhd0.chatcolor.configuration.ConfigurationManager;
import me.mattyhd0.chatcolor.gui.clickaction.api.GuiClickAction;
import me.mattyhd0.chatcolor.gui.clickaction.util.GuiClickActionManager;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import me.mattyhd0.chatcolor.util.Placeholders;
import me.mattyhd0.chatcolor.util.Util;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class ChatColorGUI {

    public static void openGui(Player player){

        ConfigurationManager configurationManager = ChatColorPlugin.getInstance().getConfigurationManager();

        FileConfiguration file = configurationManager.getGui();
        FileConfiguration patterns = configurationManager.getPatterns();

        String openSoundStr = file.getString("gui.open-sound");

        Sound sound = null;
        try {
            sound = Sound.valueOf(openSoundStr);
        } catch (IllegalArgumentException | NullPointerException ignored){
        }

        GuiBuilder builder = new GuiBuilder()
                .setRows(file.getInt("gui.gui.rows"))
                .setTitle(file.getString("gui.gui.title"));

        for(String key : file.getConfigurationSection("gui.decorations").getKeys(false)){
            String orgKey = key;
            key = "gui.decorations."+key;
            String itemSection = "has-not-permission";
            if(player.hasPermission(Objects.requireNonNull(file.getString(key + ".permission")))){
                CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
                switch (orgKey){
                    case "bold":
                        itemSection = cPlayer.isBold() ? "enabled" : "disabled";
                        break;
                    case "underline":
                        itemSection = cPlayer.isUnderline() ? "enabled" : "disabled";
                        break;
                    case "italic":
                        itemSection = cPlayer.isItalic() ? "enabled" : "disabled";
                        break;
                    case "strikethrough":
                        itemSection = cPlayer.isStrikethrough() ? "enabled" : "disabled";
                        break;
                    case "obfuscated":
                        itemSection = cPlayer.isObfuscated() ? "enabled" : "disabled";
                        break;
                    default: itemSection = "has-not-permission";
                }
            }

            int slot = file.getInt(key+"."+itemSection+".slot");
            List<String> actionsStr = file.getStringList(key+"."+itemSection+".actions");

            List<GuiClickAction> actions = GuiClickActionManager.getClickActionsFromList(
                    actionsStr
            );

            ItemStack itemStack = Util.getItemFromConfig(file, key+"."+itemSection);

            builder = builder.setGuiItem(slot, itemStack, actions);
        }


        for(String key : file.getConfigurationSection("gui.items").getKeys(false)){

            key = "gui.items."+key;

            int slot = file.getInt(key+".slot");
            List<Integer> slots = file.getIntegerList(key+".slots");
            List<String> actionsStr = file.getStringList(key+".actions");

            List<GuiClickAction> actions = GuiClickActionManager.getClickActionsFromList(
                    Placeholders.setPlaceholders(actionsStr, null, player)
            );

            if(!slots.isEmpty()){
                for(int s: slots){
                    builder = builder.setGuiItem(s, Util.getItemFromConfig(file, key), actions);
                }
            } else {
                builder = builder.setGuiItem(slot, Util.getItemFromConfig(file, key), actions);
            }

        }

        for(BasePattern pattern : ChatColorPlugin.getInstance().getPatternManager().getAllPatterns()){

            String key = pattern.getName(false)+".gui-item";

            if(!patterns.contains(key)) continue;

            String hasPermission;

            if(pattern.getPermission() == null || player.hasPermission(pattern.getPermission())){
                hasPermission = "has-permission";
            } else {
                hasPermission = "has-not-permission";
            }

            List<String> actionsStr = patterns.getStringList(key+"."+hasPermission+".actions");

            ItemStack itemStack = Util.getItemFromConfig(patterns, key+"."+hasPermission);
            ItemMeta meta = itemStack.getItemMeta();

            if(meta != null) {

                meta.setDisplayName(Placeholders.setPlaceholders(meta.getDisplayName(), pattern, player));
                meta.setLore(Placeholders.setPlaceholders(meta.getLore(), pattern, player));
                itemStack.setItemMeta(meta);

            }


            int slot = patterns.getInt(key+".slot");
            List<GuiClickAction> actions = GuiClickActionManager.getClickActionsFromList(
                    Placeholders.setPlaceholders(actionsStr, pattern, player)
            );


            builder = builder.setGuiItem(slot, itemStack, actions);

        }

        builder.open(player);
        if(sound != null) player.playSound(player.getLocation(), sound, 1, 1);

    }

}
