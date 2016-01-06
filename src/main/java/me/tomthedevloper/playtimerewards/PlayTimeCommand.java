package me.tomthedevloper.playtimerewards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Tom on 6/07/2015.
 */
public class PlayTimeCommand implements CommandExecutor {

    private PlayTimeRewards plugin;
    private HashMap<Integer,UUID> toplist = new LinkedHashMap<Integer, UUID>();


    public PlayTimeCommand(PlayTimeRewards plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if(!command.getLabel().equalsIgnoreCase("playtime"))
            return true;
        if(strings != null && strings.length == 1 && strings[0].equalsIgnoreCase("top")){
            FileConfiguration config = plugin.getData();
            for(int b=1;b<=10;b++){
                toplist.put(b,null);
            }
            for(String path:plugin.getData().getKeys(false)){
                if(!config.contains(path + ".time"))
                    continue;
                long i = config.getLong(path + ".time");
                Iterator it = toplist.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    Integer rang = (Integer) pair.getKey();
                    if((UUID) toplist.get(rang) == null) {
                        toplist.put(rang, UUID.fromString(path));
                        break;
                    }
                    if(i>config.getLong(toplist.get(rang) + ".time")){
                        insertScore(rang,UUID.fromString(path));
                        break;
                    }

                }

            }
            player.sendMessage(plugin.getMessage("Playtime-Top-Header-Line"));
            for(int rang:toplist.keySet()){
                if(toplist.get(rang) == null)
                    break;
                player.sendMessage(plugin.getMessage("Playtime-Top-Position", Bukkit.getOfflinePlayer(toplist.get(rang)), rang,config.getLong(toplist.get(rang).toString() + ".time")));
            }
            player.sendMessage(plugin.getMessage("Playtime-Top-Footer-Line"));
            return true;
        }
        player.sendMessage(plugin.getMessage("You-Have-Played") +plugin.getPlaytime(player));
        return true;
    }

    private void insertScore(int rang, UUID uuid){
        UUID after = toplist.get(rang);
        toplist.put(rang,uuid);
        if(!(rang>10)&& after != null)
            insertScore(rang + 1, after);
    }

   /* public HashMap<Integer, UUID> calculateResults(){

    } */




}
