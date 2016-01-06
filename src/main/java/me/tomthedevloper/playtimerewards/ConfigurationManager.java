package me.tomthedevloper.playtimerewards;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author IvanTheBuilder
 */
public class ConfigurationManager {


    public static JavaPlugin plugin;
    public static File getFile(String filename){
        return new File(plugin.getDataFolder()+ File.separator + filename +".yml");
    }

    public static void Init(JavaPlugin pl){
        plugin = pl;
    }

    public static FileConfiguration getConfig(String filename){
        File ConfigFile = new File(plugin.getDataFolder()+ File.separator + filename +".yml");
        if(!ConfigFile.exists()){

            try {
                plugin.getLogger().info("Creating "+filename+".yml because it does not exist!");
                ConfigFile.createNewFile();
            } catch (IOException ex) {
                //	Logger.getLogger(PixelVaults.plugin.class.getName()).log(Level.SEVERE, null, ex);
            }


            ConfigFile = new File(plugin.getDataFolder(), filename+".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(ConfigFile);

            try {
                config.save(ConfigFile);

            } catch (IOException ex) {

            }


        }
        ConfigFile = new File(plugin.getDataFolder(), filename+".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(ConfigFile);
        return config;
    }



}
