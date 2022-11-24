package io.papermc.collabcraft;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.boss.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.lang.*;

public class collabcraft extends JavaPlugin implements Listener,CommandExecutor {
    private Player controller;
    private ArrayList<Player> waiting = new ArrayList<Player>();
    private long time = 300L; //ms
    private BossBar turnTime = Bukkit.createBossBar("No one is controlling",BarColor.BLUE,BarStyle.SOLID);
    private PlayerInventory inventory = null;
    private Location location = null;
    private Location spawn = null;
    private float exp = 0;
    private boolean joined = false;
    private double progress = 0.0;
    private double health = 0.0;
    private TimerTask createRunnable(Player p){
        turnTime.setTitle(p.getName() + "'s turn");
        turnTime.setProgress(0.0);
        TimerTask r = new TimerTask(){
            public void run(){
                if (waiting.get(0) == p){
                    progress = progress + 0.01;
                    turnTime.setProgress(progress);
                    if (progress >= 1.0){
                        p.setGameMode(GameMode.SPECTATOR);
                        waiting.remove(p);
                        inventory = p.getInventory();
                        location = p.getLocation();
                        spawn = p.getBedSpawnLocation();
                        exp = p.getExp();
                        health = p.getHealth();
                        if (!(waiting.size() == 0)){
                            Player author = waiting.get(0);
                            author.getInventory().setArmorContents(p.getInventory().getArmorContents());
                            author.getInventory().setExtraContents(p.getInventory().getExtraContents());
                            author.teleport(p.getLocation());
                            author.setExp(exp);
                            author.setBedSpawnLocation(p.getBedSpawnLocation());
                            author.setGameMode(GameMode.SURVIVAL);
                            progress = 0.0;
                            turnThread = new Timer();
                            turnThread.scheduleAtFixedRate(createRunnable(author),time,time);
                        }
                    }
                }
            }
        };
        return r;
    }
    private Timer turnThread;
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        turnTime.setVisible(true);
        turnTime.setProgress(0.0);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent p){
        if (joined == false){
            inventory = p.getPlayer().getInventory();
            location = p.getPlayer().getLocation();
            spawn = p.getPlayer().getBedSpawnLocation();
            health = p.getPlayer().getHealth();
            joined = true;
        }
        p.getPlayer().setGameMode(GameMode.SPECTATOR);
        turnTime.addPlayer(p.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent p){
        if (waiting.get(0) == p.getPlayer()){
            turnTime.setTitle("No one is controlling");
            turnTime.setProgress(0.0);
        }
        if (waiting.contains(p.getPlayer())){
            waiting.remove(p.getPlayer());
        }
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player) {
            Player author = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("bypassturn")){
                waiting.add(0,author);
                inventory = controller.getInventory();
                location = controller.getLocation();
                spawn = controller.getBedSpawnLocation();
                health = controller.getHealth();
                exp = controller.getExp();
                controller.setGameMode(GameMode.SPECTATOR);
                author.getInventory().setArmorContents(controller.getInventory().getArmorContents());
                author.getInventory().setExtraContents(controller.getInventory().getExtraContents());
                author.teleport(controller.getLocation());
                author.setExp(exp);
                author.setHealth(health);
                author.setBedSpawnLocation(spawn);
                controller = author;
                controller.setGameMode(GameMode.SURVIVAL);
                progress = 0.0;
                turnThread = new Timer();
                turnThread.scheduleAtFixedRate(createRunnable(author),time,time);
                return true;
            }else if (cmd.getName().equalsIgnoreCase("endturn")){
                if (waiting.contains(author)){
                    if (waiting.size() == 1){
                        inventory = author.getInventory();
                        location = author.getLocation();
                        spawn = author.getBedSpawnLocation();
                        health = author.getHealth();
                        exp = author.getExp();
                        turnTime.setTitle("No one is controlling");
                        progress = 0.0;
                        turnTime.setProgress(0.0);
                    }
                    waiting.remove(author);
                    author.setGameMode(GameMode.SPECTATOR);
                    return true;
                }else{
                    return false;
                }
            }else if (cmd.getName().equalsIgnoreCase("taketurn")){
                if (!(waiting.contains(author))){
                    waiting.add(author);
                    if (waiting.get(0) == author){
                        controller = author;
                        author.setGameMode(GameMode.SURVIVAL);
                        author.getInventory().setArmorContents(inventory.getArmorContents());
                        author.getInventory().setExtraContents(inventory.getExtraContents());
                        author.teleport(location);
                        author.setExp(exp);
                        author.setHealth(health);
                        author.setBedSpawnLocation(spawn);
                        progress = 0.0;
                        turnThread = new Timer();
                        turnThread.scheduleAtFixedRate(createRunnable(author),time,time);
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
