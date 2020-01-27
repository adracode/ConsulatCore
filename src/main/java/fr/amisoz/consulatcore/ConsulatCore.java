package fr.amisoz.consulatcore;


import fr.amisoz.consulatcore.commands.manager.CommandManager;
import fr.amisoz.consulatcore.listeners.manager.ListenersManager;
import fr.amisoz.consulatcore.moderation.ModerationDatabase;
import fr.amisoz.consulatcore.runnable.AFKRunnable;
import fr.amisoz.consulatcore.runnable.MonitoringRunnable;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.ranks.RankDatabase;
import fr.leconsulat.api.ranks.RankManager;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.text.SimpleDateFormat;

public class ConsulatCore extends JavaPlugin {

    public static Location spawnLocation;

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");

    private RankDatabase rankDatabase;
    private RankManager rankManager;
    private ModerationDatabase moderationDatabase;

    public static boolean chat_activated = true;

    public static ConsulatCore INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        long startLoading = System.currentTimeMillis();
        spawnLocation = new Location(Bukkit.getWorlds().get(0), 144, 65, -361, -114, 0);


        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        rankDatabase = new RankDatabase();
        rankManager = new RankManager(rankDatabase);
        moderationDatabase = new ModerationDatabase(this);

        Bukkit.getScheduler().runTaskTimer(this, new AFKRunnable(), 0L, 20*60*5);
        Bukkit.getScheduler().runTaskTimer(this, new MonitoringRunnable(this), 0L, 20*60*10);

        new ListenersManager(this);
        new CommandManager(this);

        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        });

        sendConsole("ShazenCore loaded in " + (System.currentTimeMillis() - startLoading) + " ms.");
    }

    // Logger
    private void sendConsole(String log){
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ShazenCore] >> " + log);
    }

    public Connection getDatabaseConnection(){
        return ConsulatAPI.getDatabase();
    }

    public RankDatabase getRankDatabase() {
        return rankDatabase;
    }

    public RankManager getRankManager() { return rankManager; }

    public ModerationDatabase getModerationDatabase() { return moderationDatabase;  }
}
