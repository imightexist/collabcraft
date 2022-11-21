package io.papermc.collabcraft;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.boss.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.lang.*;

public class collabcraft extends JavaPlugin implements Listener,CommandExecutor {
    private Player controller;
    private ArrayList<Player> waiting = new ArrayList<Player>();
    private int time = 30; //in seconds
    private BossBar turnTime = Bukkit.createBossBar("PlayerNameHere's turn",BarColor.BLUE,BarStyle.SOLID);
    private Runnable createRunnable(Player p){
        Runnable r = new Runnable(){
            public void run(){
                String name = p.getName();
                double progress = 0.0;
                turnTime.setTitle(name + "'s turn");
                while (waiting.get(0) == p){
                    if (progress >= 100.0){
                        waiting.remove(0);
                        if (!(waiting.size() == 0)){
                            Player author = waiting.get(0);
                            p.setGameMode(GameMode.SPECTATOR);
                            author.getInventory().setArmorContents(p.getInventory().getArmorContents());
                            author.getInventory().setExtraContents(p.getInventory().getExtraContents());
                            author.teleport(p.getLocation());
                            author.setGameMode(GameMode.SURVIVAL);
                            turnThread = new Thread(createRunnable(author));
                            turnThread.start();
                        }
                    }
                    progress = progress + 1.0;
                    turnTime.setProgress(progress);
                    try{
                        Thread.sleep(time*10);
                    }catch(Exception e){
                        
                    }
                }
            }
        };
        return r;
    }
    private Thread turnThread;
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        turnTime.show();
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player) {
            Player author = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("bypassturn")){
                waiting.add(0,author);
                controller.setGameMode(GameMode.SPECTATOR);
                author.getInventory().setArmorContents(controller.getInventory().getArmorContents());
                author.getInventory().setExtraContents(controller.getInventory().getExtraContents());
                author.teleport(controller.getLocation());
                controller = author;
                controller.setGameMode(GameMode.SURVIVAL);
                turnThread = new Thread(createRunnable(controller));
                turnThread.start();
                return true;
            }else if (cmd.getName().equalsIgnoreCase("endturn")){
                if (waiting.contains(author)){
                    waiting.remove(author);
                    return true;
                }else{
                    return false;
                }
            }else if (cmd.getName().equalsIgnoreCase("taketurn")){
                if (!(waiting.contains(author))){
                    waiting.add(author);
                    if (waiting.get(0) == author){
                        turnThread = new Thread(createRunnable(author));
                        turnThread.start();
                    }
                    return true;
                }else{
                    return false;
                }
            }else if (cmd.getName().equalsIgnoreCase("controllertp")) {
                author.teleport(controller.getLocation());
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
