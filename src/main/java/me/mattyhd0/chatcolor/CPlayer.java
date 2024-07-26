package me.mattyhd0.chatcolor;

import lombok.Getter;
import lombok.Setter;
import me.mattyhd0.chatcolor.configuration.SimpleYMLConfiguration;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import me.mattyhd0.chatcolor.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CPlayer {

    public Player player;
    private String lastMessage = null;

    @Getter @Setter
    private BasePattern pattern;
    @Getter @Setter
    private boolean bold; // l
    @Getter @Setter
    private boolean italic; // o
    @Getter @Setter
    private boolean obfuscated; // k
    @Getter @Setter
    private boolean underline; // n
    @Getter @Setter
    private boolean strikethrough ; // m

    public CPlayer(Player player, BasePattern pattern) {
        this.pattern = pattern;
        this.player = player;
    }

    public CPlayer(Player player, BasePattern pattern, boolean strikethrough, boolean underline, boolean obfuscated, boolean italic, boolean bold) {
        this.strikethrough = strikethrough;
        this.pattern = pattern;
        this.underline = underline;
        this.obfuscated = obfuscated;
        this.italic = italic;
        this.bold = bold;
        this.player = player;
    }

    public void disablePattern(){
        this.pattern = null;
    }

    public boolean canUsePattern(BasePattern pattern){
        return (pattern.getPermission() == null || player.hasPermission(pattern.getPermission()));
    }

    public void setLastMessages(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessages() {
        return lastMessage == null ? "" : lastMessage;
    }

    public void saveData(){
        if (ChatColorPlugin.getInstance().getMysqlConnection() == null) {
            SimpleYMLConfiguration data = ChatColorPlugin.getInstance().getConfigurationManager().getData();
            data.set("data." + player.getUniqueId(), getPattern() == null ? null : getPattern().getName(false));
            data.save();
        } else {
            try {
                PreparedStatement statement;
                if (getPattern() == null) {
                    statement = ChatColorPlugin.getInstance().getMysqlConnection().prepareStatement(
                            "DELETE FROM playerdata WHERE uuid=?");
                    statement.setString(1, player.getUniqueId().toString());
                } else {
                    // Preparamos el SQL para insertar o actualizar con los nuevos campos
                    statement = ChatColorPlugin.getInstance().getMysqlConnection().prepareStatement(
                            "INSERT INTO playerdata(uuid, pattern, bold, strikethrough, underline, italic, obfuscated) " +
                                    "VALUES(?, ?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE pattern=VALUES(pattern), bold=VALUES(bold), strikethrough=VALUES(strikethrough), " +
                                    "underline=VALUES(underline), italic=VALUES(italic), obfuscated=VALUES(obfuscated)");

                    // Establecemos los valores de los parámetros
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setString(2, getPattern().getName(false));
                    statement.setBoolean(3, isBold());
                    statement.setBoolean(4, isStrikethrough());
                    statement.setBoolean(5, isUnderline());
                    statement.setBoolean(6, isItalic());
                    statement.setBoolean(7, isObfuscated());
                }

                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(
                        Util.color("&c[ChatColor] An error occurred while trying to set the pattern of "+player.getUniqueId()+" ("+player.getName()+") via MySQL")
                );
                e.printStackTrace();
            }

        }
    }

}
