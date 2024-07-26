package me.mattyhd0.chatcolor.listener;

import me.mattyhd0.chatcolor.CPlayer;
import me.mattyhd0.chatcolor.ChatColorPlugin;
import me.mattyhd0.chatcolor.configuration.SimpleYMLConfiguration;
import me.mattyhd0.chatcolor.pattern.api.BasePattern;
import me.mattyhd0.chatcolor.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class ConnectionListener implements Listener {
    private ChatColorPlugin plugin;
    private HashMap<UUID, BukkitTask> playersBeingLoaded = new HashMap<>();

    public ConnectionListener(ChatColorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (playersBeingLoaded.containsKey(player.getUniqueId())) {
            playersBeingLoaded.remove(event.getPlayer().getUniqueId()).cancel();
        }
        int delay = Math.max(0, plugin.getConfigurationManager().getConfig().getInt("config.data-delay", 30));
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (ChatColorPlugin.getInstance().getMysqlConnection() == null) {
                    SimpleYMLConfiguration data = ChatColorPlugin.getInstance().getConfigurationManager().getData();
                    BasePattern basePattern = plugin.getPatternManager().getPatternByName(data.getString("data." + player.getUniqueId()));
                    plugin.getDataMap().put(player.getUniqueId(), new CPlayer(player, basePattern));
                } else {
                    try {
                        PreparedStatement statement = ChatColorPlugin.getInstance().getMysqlConnection().prepareStatement("SELECT * FROM playerdata WHERE uuid=?");
                        statement.setString(1, player.getUniqueId().toString());
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            BasePattern basePattern = plugin.getPatternManager().getPatternByName(resultSet.getString("pattern"));
                            plugin.getDataMap().put(player.getUniqueId(),
                                    new CPlayer(
                                            player, basePattern,
                                            resultSet.getBoolean("strikethrough"),
                                            resultSet.getBoolean("underline"),
                                            resultSet.getBoolean("obfuscated"),
                                            resultSet.getBoolean("italic"),
                                            resultSet.getBoolean("bold")
                                            ));
                        } else plugin.getDataMap().put(player.getUniqueId(), new CPlayer(player, null));
                    } catch (SQLException e) {
                        Bukkit.getServer().getConsoleSender().sendMessage(
                                Util.color(
                                        formatQuery(player, "&c[ChatColor] An error occurred while trying to get the pattern of {uuid} ({player}) via MySQL")
                                )
                        );
                        e.printStackTrace();
                    }
                }
                playersBeingLoaded.remove(player.getUniqueId());
            }
        }.runTaskLaterAsynchronously(plugin, delay);
        playersBeingLoaded.put(player.getUniqueId(), task);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (playersBeingLoaded.containsKey(event.getPlayer().getUniqueId())) {
            playersBeingLoaded.remove(event.getPlayer().getUniqueId()).cancel();
            return;
        }
        CPlayer cPlayer = plugin.getDataMap().get(event.getPlayer().getUniqueId());
        if (plugin.getDataMap().containsKey(event.getPlayer().getUniqueId())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    cPlayer.saveData();
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitMonitor(PlayerQuitEvent event) {
        plugin.getDataMap().remove(event.getPlayer().getUniqueId());
    }


    private String formatQuery(Player player, String string) {
        return formatQuery(player, string, null);
    }

    private String formatQuery(Player player, String string, BasePattern pattern) {

        String uuid = player.getUniqueId().toString();
        String name = player.getName();

        string = pattern == null ? string : string.replaceAll("\\{pattern}", pattern.getName(false));

        return string
                .replaceAll("\\{uuid}", uuid)
                .replaceAll("\\{player}", name);

    }
}
