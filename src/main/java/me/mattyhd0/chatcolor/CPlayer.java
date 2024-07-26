package me.mattyhd0.chatcolor;

import lombok.Getter;
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

    @Getter
    private BasePattern pattern;
    @Getter
    private boolean bold; // l
    @Getter
    private boolean italic; // o
    @Getter
    private boolean obfuscated; // k
    @Getter
    private boolean underline; // n
    @Getter
    private boolean strikethrough ; // m

    public CPlayer(Player player, BasePattern basePattern){
        this.player = player;
        this.pattern = basePattern;
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
                    statement = ChatColorPlugin.getInstance().getMysqlConnection().prepareStatement(
                            "INSERT INTO playerdata(uuid, pattern) VALUES(?,?) ON DUPLICATE KEY UPDATE pattern= VALUES(pattern)");
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setString(2, getPattern().getName(false));
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
