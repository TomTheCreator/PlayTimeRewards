package me.tomthedevloper.playtimerewards;

import org.apache.logging.log4j.core.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Tom on 6/07/2015.
 */
public class PlayTimeRewards extends JavaPlugin implements Listener {

    private ConfigurationManager configurationManager;
    private FileConfiguration data;
    private FileConfiguration config;
    private HashMap<Long, String> objectives = new HashMap<Long, String>();

    private HashMap<String, String> messages = new HashMap<String, String>();

    private String prefix = ChatColor.DARK_BLUE + "[PlayTimeRewards] " + ChatColor.BLUE;
    @Override
    public void onEnable(){
        configurationManager = new ConfigurationManager();
        configurationManager.plugin = this;
        this.getServer().getPluginManager().registerEvents(this, this);
        data =ConfigurationManager.getConfig("data");
        loadObjectives();
        this.getCommand("playtime").setExecutor(new PlayTimeCommand(this));
        loadMessages();
        this.getServer().getScheduler().runTaskTimer(this, new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player: Bukkit.getServer().getOnlinePlayers()){
                    for (Map.Entry<Long, String> entry : objectives.entrySet()) {
                        long key = entry.getKey();
                        String value = entry.getValue();
                        long time = getPlaytimeInSeconds(player);
                        if(time<key || data.contains(player.getUniqueId() + value.replaceAll("\\s","")) || player.hasPermission("playtimerewards.override." + key))
                            continue;
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),value.replaceAll("%PLAYER%", player.getName()));
                        data.set(player.getUniqueId() + value.replaceAll("\\s",""),true);
                        try {
                            data.save(ConfigurationManager.getFile("data"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }, 600L,600L);
        for(Player player:getServer().getOnlinePlayers()){
            jointime.put(player.getUniqueId(), System.currentTimeMillis());
            long time = this.getData().getLong(player.getUniqueId().toString() +".time");
            timeplayed.put(player.getUniqueId(), time);
        }
    }

    private void loadMessages(){
        FileConfiguration messageconfig = ConfigurationManager.getConfig("messages");
        messages.put("Playtime-Top-Header-Line", ChatColor.DARK_GREEN + "----------{" + ChatColor.GREEN + " TOP PLAYERS" + ChatColor.DARK_GREEN +" }----------");
        messages.put("Playtime-Top-Footer-Line", ChatColor.DARK_GREEN +  "------------------------------------");
        messages.put("Playtime-Top-Position", ChatColor.DARK_GREEN + "%NUMBER%. " + ChatColor.GREEN + "%PLAYER% - %PLAYTIME%");
        messages.put("Playtime-Help-Header-Line",  ChatColor.DARK_GREEN + "----------{" + ChatColor.GREEN + " PLAYTIME HELP" + ChatColor.DARK_GREEN +" }----------");
        messages.put("Playtime-Help-Playtime-Command", ChatColor.DARK_GREEN + "/playtime :" + ChatColor.GREEN + " Checks your playtime");
        messages.put("Playtime-Help-Top-Command", ChatColor.DARK_GREEN + "/Playtime top :" + ChatColor.GREEN + " Check out the top players!");
        messages.put("Playtime-Help-Footer-Line", ChatColor.DARK_GREEN +  "-----------------------------------");
        messages.put("You-Have-Played", ChatColor.GREEN + "You have played ");
        messages.put("Seconds", "seconds");
        messages.put("Minutes", "minutes");
        messages.put("Hours", "hours");

        for(String messagePath:messages.keySet()){
            if(!messageconfig.contains(messagePath)){
                messageconfig.set(messagePath, messages.get(messagePath));
            }else{
                messages.put(messagePath,messageconfig.getString(messagePath));
            }
        }
        try {
            messageconfig.save(ConfigurationManager.getFile("messages"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String string, OfflinePlayer player, int i, long playtime){
        String returnstring = messages.get(string);
        returnstring = returnstring.replaceAll("%PLAYER%",player.getName());
        returnstring = returnstring.replaceAll("%NUMBER%",Integer.toString(i));
        returnstring = returnstring.replaceAll("%PLAYTIME%",calculateTime(playtime));

        returnstring = returnstring.replaceAll("(&([a-f0-9]))", "\u00A7$2");
        return returnstring;
    }

    public String getMessage(String string){
        return messages.get(string);
    }

    @Override
    public void onDisable(){
        for(Player player:getServer().getOnlinePlayers()){
            timeplayed.put(player.getUniqueId(), timeplayed.get(player.getUniqueId()) +
                    (System.currentTimeMillis() -jointime.get(player.getUniqueId())));
            this.getData().set(player.getUniqueId().toString() + ".time", timeplayed.get(player.getUniqueId()));
            try {
                this.getData().save(ConfigurationManager.getFile("data"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadObjectives(){
        FileConfiguration objconfig = ConfigurationManager.getConfig("Goals");
        if(!objconfig.contains("TIME_HERE_IN_SECONDS"))
            objconfig.set("TIME_HERE_IN_SECONDS", "PUT_HERE_A_COMMAND_WITHOUT_THE_SLASH");
        try {
            objconfig.save(ConfigurationManager.getFile("Goals"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String path:objconfig.getKeys(false)){
            if(!path.contains("TIME_HERE_IN_SECONDS"))
            objectives.put(Long.parseLong(path),objconfig.getString(path));
        }
    }


    public ConfigurationManager getConfigurationManager(){
        return configurationManager;
    }

    public FileConfiguration getData(){
        return data;
    }

    @Override
    public FileConfiguration getConfig(){
        return config;
    }


    private HashMap<UUID,Long> jointime = new HashMap<UUID, Long>();
    private HashMap<UUID, Long> timeplayed = new HashMap<UUID, Long>();



    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        jointime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        long time = this.getData().getLong(event.getPlayer().getUniqueId().toString() +".time");
        timeplayed.put(event.getPlayer().getUniqueId(), time);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        timeplayed.put(event.getPlayer().getUniqueId(), timeplayed.get(event.getPlayer().getUniqueId()) +
                (System.currentTimeMillis() -jointime.get(event.getPlayer().getUniqueId())));
        this.getData().set(event.getPlayer().getUniqueId().toString() + ".time", timeplayed.get(event.getPlayer().getUniqueId()));
        try {
            this.getData().save(ConfigurationManager.getFile("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlaytime(Player player){
        if(!timeplayed.containsKey(player.getUniqueId()))
            timeplayed.put(player.getUniqueId(),0L);
        long time = timeplayed.get(player.getUniqueId()) +
                (System.currentTimeMillis() -jointime.get(player.getUniqueId()));
        return calculateTime(time);
    }

    public long getPlaytimeInSeconds(Player player){
        long time = timeplayed.get(player.getUniqueId()) +
                (System.currentTimeMillis() -jointime.get(player.getUniqueId()));
        return (time/1000);
    }


    public static String formatIntoHHMMSS(long secsIn) {

        long hours = secsIn / 3600,
                remainder = secsIn % 3600,
                minutes = remainder / 60,
                seconds = remainder % 60;

        return ((hours < 10 ? "0" : "") + hours
                + ":" + (minutes < 10 ? "0" : "") + minutes
                + ":" + (seconds < 10 ? "0" : "") + seconds);

    }

    public static String formatIntoMMSS(int secsIn) {

        int minutes = secsIn / 60,
                seconds = secsIn % 60;

        return ((minutes < 10 ? "0" : "") + minutes
                + ":" + (seconds < 10 ? "0" : "") + seconds);

    }

    private String calculateTime(long tijd) {
        final long second = 1000;
        final long minute = 60 * second;
        final long hour = 60 * minute;

        long timeLeft =tijd;

        int hours = (int) Math.floor(timeLeft / hour);
        timeLeft -= hour * hours;
        int minutes = (int) Math.floor(timeLeft / minute);
        timeLeft -= minute * minutes;
        int seconds = (int) Math.floor(timeLeft / second);
        int milliseconds = (int) (timeLeft - (second * seconds));
        String time = MessageFormat.format("{0} "+messages.get("Hours")+", {1} "+messages.get("Minutes")+", {2} "+messages.get("Seconds")+"", hours, minutes,
                seconds);
        return time;
    }



}
