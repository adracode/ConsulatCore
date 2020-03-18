package fr.amisoz.consulatcore;


import fr.amisoz.consulatcore.commands.manager.CommandManager;
import fr.amisoz.consulatcore.duel.DuelManager;
import fr.amisoz.consulatcore.listeners.manager.ListenersManager;
import fr.amisoz.consulatcore.moderation.ModerationDatabase;
import fr.amisoz.consulatcore.runnable.*;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.ranks.RankDatabase;
import fr.leconsulat.api.ranks.RankManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsulatCore extends JavaPlugin {

    public static Location spawnLocation;

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");

    private RankDatabase rankDatabase;
    private RankManager rankManager;
    private ModerationDatabase moderationDatabase;

    public static boolean chat_activated = true;

    public static ConsulatCore INSTANCE;

    public static List<TextComponent> textPerso = new ArrayList<>();

    public static List<String> forbiddenPerso = Arrays.asList("Modo", "Moderateur", "Modérateur", "Admin", "Animateur", "Partenaire", "Youtubeur", "Streamer", "Ami");
    @Override
    public void onEnable() {
        INSTANCE = this;
        long startLoading = System.currentTimeMillis();
        spawnLocation = new Location(Bukkit.getWorlds().get(0), 330, 65, -438, -145, 0);

        new DuelManager();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        rankDatabase = new RankDatabase();
        rankManager = new RankManager(rankDatabase);
        moderationDatabase = new ModerationDatabase(this);

        Bukkit.getScheduler().runTaskTimer(this, new AFKRunnable(), 0L, 20*60*5);
        Bukkit.getScheduler().runTaskTimer(this, new MonitoringRunnable(this), 0L, 20*60*10);
        Bukkit.getScheduler().runTaskTimer(this, new MessageRunnable(), 0L, 20*60*15);
        Bukkit.getScheduler().runTaskTimer(this, new MeceneRunnable(), 0L, 20*60*60);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ServerTPS(), 100L, 1L);

        new ListenersManager(this);
        new CommandManager(this);

        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        });

        for(ChatColor color : ChatColor.values()){
            if(color == ChatColor.RED) continue;
            if(color == ChatColor.MAGIC) break;

            TextComponent textComponent;
            if(color != ChatColor.WHITE) {
                textComponent = new TextComponent(color + color.name() + "§r§7 - ");
            }else{
                textComponent = new TextComponent(color + color.name());
            }

            textComponent.setHoverEvent(new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§oChoisir cette couleur").create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/perso " + color.getChar()));

            ConsulatCore.textPerso.add(textComponent);
        }
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
