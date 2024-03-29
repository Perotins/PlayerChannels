package me.perotin.playerchannels.storage.files;

import com.google.common.base.Charsets;
import me.perotin.playerchannels.PlayerChannels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.List;

/* Created by Perotin on 8/14/19 */
public class ChannelFile {

    private final FileType type;
    private File file;
    private FileConfiguration configuration;

    public ChannelFile(FileType type){
        this.type = type;
        switch(type){
            case PLAYERS:
                file = new File(PlayerChannels.getInstance().getDataFolder(), "players.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;
            case CHATROOM:
                file = new File(PlayerChannels.getInstance().getDataFolder(), "chatrooms.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;

            case MESSAGES:
                file = new File(PlayerChannels.getInstance().getDataFolder(), "messages.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;
            case MENUS:
                file = new File(PlayerChannels.getInstance().getDataFolder(), "menus.yml");
                configuration = YamlConfiguration.loadConfiguration(file);
                break;

        }
    }
    public void save() {
        try {
            configuration.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    public ConfigurationSection getConfigSection(String path){
        return configuration.getConfigurationSection(path);
    }
    // some generic methods to speed up the process
    public boolean getBool(String path){
        return getConfiguration().getBoolean(path);
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public Object get(String path) {
        return configuration.get(path);
    }

    public List<String> getStringList(String path) {
        return configuration.getStringList(path);
    }
    public void set(String path, Object value) {
        configuration.set(path, value);
    }

    public String getString(String path) {
        if(configuration.getString(path) == null){
            Bukkit.getLogger().severe("Path " + path + " is null!");
            Bukkit.getLogger().severe(file.getAbsolutePath() + " is the file that this occurred!");
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', configuration.getString(path));
    }

    public void sendConfigMsg(Player player, String message) {
        if (player != null) {
            player.sendMessage(getString(message));
        }
    }

    /**
     * loads all files with defaults
     */
    public void load() {

        File lang = null;
        InputStream defLangStream = null;

        switch (type) {
            case PLAYERS:
                lang = new File(PlayerChannels.getInstance().getDataFolder(), "players.yml");
                defLangStream = PlayerChannels.getInstance().getResource("players.yml");
                break;
            case CHATROOM:
                lang = new File(PlayerChannels.getInstance().getDataFolder(), "chatrooms.yml");
                defLangStream = PlayerChannels.getInstance().getResource("chatrooms.yml");
                break;
            case MESSAGES:
                lang = new File(PlayerChannels.getInstance().getDataFolder(), "messages.yml");
                defLangStream = PlayerChannels.getInstance().getResource("messages.yml");
                break;
            case MENUS:
                lang = new File(PlayerChannels.getInstance().getDataFolder(), "menus.yml");
                defLangStream = PlayerChannels.getInstance().getResource("menus.yml");
                break;

        }
        OutputStream out = null;
        if (!lang.exists()) {
            try {
                PlayerChannels.getInstance().getDataFolder().mkdir();
                lang.createNewFile();
                if (defLangStream != null) {
                    out = new FileOutputStream(lang);
                    int read;
                    byte[] bytes = new byte[1024];

                    while ((read = defLangStream.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace(); // So they notice
                Bukkit.getLogger().severe("[PlayerChannels] Couldn't create " + type.toString().toLowerCase() + " file.");
                Bukkit.getLogger().severe("[PlayerChannels] This is a fatal error. Now disabling");
                PlayerChannels.getInstance().getPluginLoader().disablePlugin(PlayerChannels.getInstance()); // Without
                // it
                // loaded,
                // we
                // can't
                // send
                // them
                // messages
            } finally {
                if (defLangStream != null) {
                    try {
                        defLangStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public void reloadFile(){

        configuration = YamlConfiguration.loadConfiguration(file);

        String name = type.name().toLowerCase();
        final InputStream defConfigStream = PlayerChannels.getInstance().getResource(".yml");
        if (defConfigStream == null) {
            return;
        }

        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    public File getFile(){
        return this.file;
    }
    public static void loadFiles(){
        if (!new File( PlayerChannels.getInstance().getDataFolder(), "chatrooms.yml").exists()) {
            PlayerChannels.getInstance().saveResource("chatrooms.yml", false);
        }
        if (!new File( PlayerChannels.getInstance().getDataFolder(), "messages.yml").exists()) {
            PlayerChannels.getInstance().saveResource("messages.yml", false);
        }  if (!new File( PlayerChannels.getInstance().getDataFolder(), "players.yml").exists()) {
            PlayerChannels.getInstance().saveResource("players.yml", false);
        } if (!new File( PlayerChannels.getInstance().getDataFolder(), "menus.yml").exists()) {
            PlayerChannels.getInstance().saveResource("menus.yml", false);
        }
    }






}
