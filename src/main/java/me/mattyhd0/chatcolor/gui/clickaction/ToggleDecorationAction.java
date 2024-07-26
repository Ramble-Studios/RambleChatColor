package me.mattyhd0.chatcolor.gui.clickaction;

import lombok.AllArgsConstructor;
import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.ChatColorPlugin;
import me.mattyhd0.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ToggleDecorationAction implements GuiClickAction {

    private final @NotNull String decoration;

    @Override
    public void execute(Player player) {
        CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
        if(cPlayer != null) {
            switch (decoration){
                case "bold": {
                    cPlayer.setBold(!cPlayer.isBold());
                    break;
                }
                case "italic": {
                    cPlayer.setItalic(!cPlayer.isItalic());
                    break;
                }
                case "strikethrough": {
                    cPlayer.setStrikethrough(!cPlayer.isStrikethrough());
                    break;
                }
                case "underline": {
                    cPlayer.setUnderline(!cPlayer.isUnderline());
                    break;
                }
                case "obfuscated": {
                    cPlayer.setObfuscated(!cPlayer.isObfuscated());
                    break;
                }
                default: break;
            };
        }
    }
}

