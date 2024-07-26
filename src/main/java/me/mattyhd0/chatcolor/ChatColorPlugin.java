package me.mattyhd0.chatcolor;

import me.mattyhd0.chatcolor.command.ChatColorAdminCommand;
import me.mattyhd0.chatcolor.configuration.ConfigurationManager;
import me.mattyhd0.chatcolor.configuration.SimpleYMLConfiguration;
import me.mattyhd0.chatcolor.gui.GuiListener;
import me.mattyhd0.chatcolor.pattern.manager.PatternManager;
import me.mattyhd0.chatcolor.util.Util;
import org.bstats.bukkit.Metrics;
import me.mattyhd0.chatcolor.placeholderapi.ChatColorPlaceholders;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import me.mattyhd0.chatcolor.listener.ConnectionListener;
import me.mattyhd0.chatcolor.listener.ChatListener;
import me.mattyhd0.chatcolor.command.ChatColorCommand;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatColorPlugin extends JavaPlugin {

    private static ChatColorPlugin INSTANCE;
    private PatternManager patternManager;
    private ConfigurationManager configurationManager;
    private final List<String> supportedPlugins = new ArrayList<>();

    private String prefix;
    private Connection mysqlConnection;
    private final HashMap<UUID, CPlayer> dataMap = new HashMap<>();

    public void onEnable() {
        ChatColorPlugin.INSTANCE = this;
        prefix = Util.color("&8[&4&lC&c&lh&6&la&e&lt&2&lC&a&lo&b&ll&3&lo&1&lr&8]");
        Bukkit.getConsoleSender().sendMessage(Util.color(prefix+" &7Enabling ChatColor v" + this.getDescription().getVersion()));
        Metrics metrics = new Metrics(this, 11648);
        saySupport("PlaceholderAPI");
        reload();
        setupListeners();
        setupCommands();
        setupPlaceholderAPI();
    }

    public void reload(){
        configurationManager = new ConfigurationManager();
        patternManager = new PatternManager();
        if(mysqlConnection != null){
            try {
                mysqlConnection.close();
                mysqlConnection = null;
            } catch (SQLException ignored){}
        }
        if(configurationManager.getConfig().getBoolean("config.mysql.enable")) openMysqlConnection();
    }
    
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(Util.color(prefix+" &7Disabling ChatColor v" + this.getDescription().getVersion()));
        for (CPlayer cPlayer: dataMap.values()){
            cPlayer.saveData();
        }
        if(mysqlConnection != null) {
            try {
                mysqlConnection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public void setupListeners(){

        EventPriority priority = configurationManager.getConfig().contains("config.listener-priority") ?
                EventPriority.valueOf(configurationManager.getConfig().getString("config.listener-priority")) :
                EventPriority.LOW;



        getServer().getPluginManager().registerEvent(
                AsyncPlayerChatEvent.class,
                new Listener() {},
                priority,
                new ChatListener(this),
                this

        );
       //getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    public void setupCommands(){
        ChatColorCommand chatColorCommand = new ChatColorCommand(this);
        ChatColorAdminCommand chatColorAdminCommand = new ChatColorAdminCommand(this);
        getCommand("chatcolor").setExecutor(chatColorCommand);
        getCommand("chatcolor").setTabCompleter(chatColorCommand);
        getCommand("chatcoloradmin").setExecutor(chatColorAdminCommand);
        getCommand("chatcoloradmin").setTabCompleter(chatColorAdminCommand);
    }
    
    public void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ChatColorPlaceholders().register();
        }
    }

    public void saySupport(String plugin){

        boolean support = Bukkit.getPluginManager().getPlugin(plugin) != null;
        String supportStr = "&cNo";

        if(support) {
            supportStr = "&aYes";
            supportedPlugins.add(plugin);
        }

        Bukkit.getConsoleSender().sendMessage(Util.color( prefix+"&7 "+plugin+" support: "+supportStr));

    }

    public void openMysqlConnection(){

        SimpleYMLConfiguration config = configurationManager.getConfig();

        String host     = config.getString("config.mysql.host");
        String port     = config.getString("config.mysql.port");
        String username = config.getString("config.mysql.username");
        String password = config.getString("config.mysql.password");
        String database = config.getString("config.mysql.database");
        String additionalUrl = config.getString("config.mysql.additional-url","&useSSL=false&&autoReconnect=true");

        try{

            String urlConnection = ("jdbc:mysql://{host}:{port}/{database}?user={username}&password={password}"+additionalUrl)
                    .replaceAll("\\{host}", host)
                    .replaceAll("\\{port}", port)
                    .replaceAll("\\{username}", username)
                    .replaceAll("\\{password}", password)
                    .replaceAll("\\{database}", database);

            mysqlConnection = DriverManager.getConnection(urlConnection);

            if(mysqlConnection == null) Bukkit.getServer().getConsoleSender().sendMessage(
                    Util.color("&c[ChatColor] There was an error connecting to the MySQL Database")
            );

            Statement statement = mysqlConnection.createStatement();
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS playerdata (" +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "pattern VARCHAR(45) NOT NULL, " +
                            "bold BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "strikethrough BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "underline BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "italic BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "obfuscated BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "PRIMARY KEY (uuid)" +
                            ");"
            );

        } catch (SQLException e){
            Bukkit.getServer().getConsoleSender().sendMessage(
                    Util.color("&c[ChatColor] There was an error connecting to the MySQL Database")
            );
            e.printStackTrace();
        }


    }

    public boolean supportPlugin(String plugin){
        return supportedPlugins.contains(plugin);
    }

    public static ChatColorPlugin getInstance() {
        return INSTANCE;
    }

    public Connection getMysqlConnection() {
        return mysqlConnection;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public PatternManager getPatternManager() {
        return patternManager;
    }

    public void sendConsoleMessage(String message){
        getServer().getConsoleSender().sendMessage(prefix+" "+Util.color(message));
    }

    public String getPrefix() {
        return prefix;
    }

    public HashMap<UUID, CPlayer> getDataMap() {
        return dataMap;
    }
}
