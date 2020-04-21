package fr.amisoz.consulatcore;


import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.commands.claims.AccessCommand;
import fr.amisoz.consulatcore.commands.claims.ClaimCommand;
import fr.amisoz.consulatcore.commands.claims.UnclaimCommand;
import fr.amisoz.consulatcore.commands.economy.BaltopCommand;
import fr.amisoz.consulatcore.commands.economy.MoneyCommand;
import fr.amisoz.consulatcore.commands.economy.PayCommand;
import fr.amisoz.consulatcore.commands.moderation.*;
import fr.amisoz.consulatcore.commands.players.*;
import fr.amisoz.consulatcore.duel.DuelManager;
import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.listeners.entity.MobListeners;
import fr.amisoz.consulatcore.listeners.entity.player.*;
import fr.amisoz.consulatcore.listeners.world.ClaimCancelListener;
import fr.amisoz.consulatcore.listeners.world.SignListener;
import fr.amisoz.consulatcore.moderation.ModerationDatabase;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.runnable.AFKRunnable;
import fr.amisoz.consulatcore.runnable.MessageRunnable;
import fr.amisoz.consulatcore.runnable.MonitoringRunnable;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.leconsulat.api.ConsulatAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class ConsulatCore extends JavaPlugin {
    
    private static ConsulatCore instance;
    private static Random random;
    
    private ClaimManager claimManager;
    private SPlayerManager playerManager;
    private BaltopManager baltopManager;
    private FlyManager flyManager;
    
    private Location spawn;
    
    public SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
    
    private ModerationDatabase moderationDatabase;
    
    private boolean chat = true;
    
    private List<TextComponent> textPerso = new ArrayList<>();
    
    private Set<String> forbiddenPerso = new HashSet<>(Arrays.asList("Modo", "Moderateur", "Modérateur", "Admin", "Animateur", "Partenaire", "Youtubeur", "Streamer", "Ami"));
    
    @Override
    public void onEnable(){
        if(instance != null){
            return;
        }
        instance = this;
        random = new Random();
        long startLoading = System.currentTimeMillis();
        spawn = new Location(Bukkit.getWorlds().get(0), 330, 65, -438, -145, 0);
        new DuelManager();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        claimManager = new ClaimManager();
        playerManager = new SPlayerManager();
        baltopManager = new BaltopManager();
        flyManager = new FlyManager();
        moderationDatabase = new ModerationDatabase(this);
        Bukkit.getScheduler().runTaskTimer(this, new AFKRunnable(), 0L, 5 * 60 * 20);
        Bukkit.getScheduler().runTaskTimer(this, new MonitoringRunnable(this), 0L, 10 * 60 * 20);
        Bukkit.getScheduler().runTaskTimer(this, new MessageRunnable(), 0L, 15 * 60 * 20);
        registerEvents();
        registerCommands();
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        });
        for(ChatColor color : ChatColor.values()){
            if(color == ChatColor.RED) continue;
            if(color == ChatColor.MAGIC) break;
            TextComponent textComponent;
            if(color != ChatColor.WHITE){
                textComponent = new TextComponent(color + color.name() + "§r§7 - ");
            } else {
                textComponent = new TextComponent(color + color.name());
            }
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§oChoisir cette couleur").create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/perso " + color.getChar()));
            textPerso.add(textComponent);
        }
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            for(Player p : Bukkit.getOnlinePlayers()){
                getServer().getPluginManager().callEvent(new PlayerJoinEvent(p, ""));
            }
        }
        ConsulatAPI.getConsulatAPI().log(Level.FINE, "ConsulatCore loaded in " + (System.currentTimeMillis() - startLoading) + " ms.");
    }
    
    @SuppressWarnings("ConstantConditions")
    private void registerCommands(){
        this.getCommand("report").setExecutor(new ReportCommand());
        this.getCommand("rank").setExecutor(new RankCommand());
        this.getCommand("sanction").setExecutor(new SanctionCommand());
        this.getCommand("annonce").setExecutor(new BroadcastCommand());
        this.getCommand("unban").setExecutor(new UnbanCommand());
        this.getCommand("unmute").setExecutor(new UnmuteCommand());
        this.getCommand("staff").setExecutor(new ModerateCommand());
        this.getCommand("tpmod").setExecutor(new TpmodCommand());
        this.getCommand("spawn").setExecutor(new SpawnCommand());
        this.getCommand("help").setExecutor(new HelpCommand());
        this.getCommand("msg").setExecutor(new MpCommand());
        this.getCommand("advert").setExecutor(new AdvertCommand());
        this.getCommand("kick").setExecutor(new KickCommand());
        this.getCommand("sc").setExecutor(new StaffChatCommand());
        this.getCommand("chat").setExecutor(new ToggleChatCommand());
        this.getCommand("gm").setExecutor(new GamemodeCommand());
        this.getCommand("back").setExecutor(new BackCommand());
        this.getCommand("stafflist").setExecutor(new StaffListCommand());
        this.getCommand("hub").setExecutor(new HubCommand());
        this.getCommand("r").setExecutor(new AnswerCommand());
        this.getCommand("news").setExecutor(new NewsCommand());
        this.getCommand("seen").setExecutor(new SeenCommand());
        this.getCommand("invsee").setExecutor(new InvseeCommand());
        this.getCommand("boutique").setExecutor(new ShopCommand());
        this.getCommand("perso").setExecutor(new PersoCommand());
        this.getCommand("site").setExecutor(new SiteCommand());
        this.getCommand("fly").setExecutor(new FlyCommand());
        this.getCommand("discord").setExecutor(new DiscordCommand());
        this.getCommand("duel").setExecutor(new DuelCommand());
        this.getCommand("ec").setExecutor(new EnderchestCommand());
        this.getCommand("top").setExecutor(new TopCommand());
        this.getCommand("sp").setExecutor(new SocialSpyCommand());
        this.getCommand("home").setExecutor(new HomeCommand());
        this.getCommand("sethome").setExecutor(new SetHomeCommand());
        this.getCommand("delhome").setExecutor(new DelHomeCommand());
        this.getCommand("access").setExecutor(new AccessCommand());
        this.getCommand("claim").setExecutor(new ClaimCommand());
        this.getCommand("unclaim").setExecutor(new UnclaimCommand());
        this.getCommand("baltop").setExecutor(new BaltopCommand());
        this.getCommand("money").setExecutor(new MoneyCommand());
        this.getCommand("pay").setExecutor(new PayCommand());
        this.getCommand("tpa").setExecutor(new TPaCommand());
        this.getCommand("shop").setExecutor(new fr.amisoz.consulatcore.commands.economy.ShopCommand());
    }
    
    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(new ChatListeners(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListeners(), this);
        Bukkit.getPluginManager().registerEvents(new MoveListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ExperienceListener(), this);
        Bukkit.getPluginManager().registerEvents(new FoodListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);
        Bukkit.getPluginManager().registerEvents(new ModeratorInteraction(), this);
        //Bukkit.getPluginManager().registerEvents(new DuelListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkChangeListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClaimCancelListener(), this);
        Bukkit.getPluginManager().registerEvents(claimManager, this);
        Bukkit.getPluginManager().registerEvents(playerManager, this);
        Bukkit.getPluginManager().registerEvents(new ShopManager(), this);
    }
    
    public Connection getDatabaseConnection(){
        return ConsulatAPI.getDatabase();
    }
    
    public ModerationDatabase getModerationDatabase(){
        return moderationDatabase;
    }
    
    public boolean isChatActivated(){
        return chat;
    }
    
    public void setChat(boolean chat){
        this.chat = chat;
    }
    
    public List<TextComponent> getTextPerso(){
        return textPerso;
    }
    
    public static ConsulatCore getInstance(){
        return instance;
    }
    
    public Location getSpawn(){
        return spawn;
    }
    
    public void setSpawn(Location spawn){
        this.spawn = spawn;
    }
    
    public Set<String> getForbiddenCustomRank(){
        return forbiddenPerso;
    }
    
    public static Random getRandom(){
        return random;
    }
}
