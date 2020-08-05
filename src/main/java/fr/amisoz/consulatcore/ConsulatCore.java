package fr.amisoz.consulatcore;


import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.amisoz.consulatcore.commands.cities.CityCommand;
import fr.amisoz.consulatcore.commands.claims.AccessCommand;
import fr.amisoz.consulatcore.commands.claims.ClaimCommand;
import fr.amisoz.consulatcore.commands.claims.UnclaimCommand;
import fr.amisoz.consulatcore.commands.economy.AdminShopCommand;
import fr.amisoz.consulatcore.commands.economy.BaltopCommand;
import fr.amisoz.consulatcore.commands.economy.MoneyCommand;
import fr.amisoz.consulatcore.commands.economy.PayCommand;
import fr.amisoz.consulatcore.commands.enchantments.CEnchantCommand;
import fr.amisoz.consulatcore.commands.moderation.*;
import fr.amisoz.consulatcore.commands.players.*;
import fr.amisoz.consulatcore.commands.safari.SafariCommand;
import fr.amisoz.consulatcore.duel.DuelManager;
import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.amisoz.consulatcore.enchantments.EnchantmentManager;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.listeners.entity.MobListeners;
import fr.amisoz.consulatcore.listeners.entity.player.*;
import fr.amisoz.consulatcore.listeners.world.ClaimCancelListener;
import fr.amisoz.consulatcore.listeners.world.SignListener;
import fr.amisoz.consulatcore.moderation.ModerationDatabase;
import fr.amisoz.consulatcore.moderation.channels.SpyChannel;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.runnable.AFKRunnable;
import fr.amisoz.consulatcore.runnable.MeceneRunnable;
import fr.amisoz.consulatcore.runnable.MessageRunnable;
import fr.amisoz.consulatcore.runnable.MonitoringRunnable;
import fr.amisoz.consulatcore.server.SafariServer;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.events.PostInitEvent;
import fr.leconsulat.api.redis.RedisManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * CREATE TABLE cities (
 * id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
 * uuid CHAR(36) NOT NULL,
 * name VARCHAR(255) NOT NULL,
 * money DOUBLE NOT NULL DEFAULT 0,
 * owner CHAR(36) NOT NULL
 * );
 * CREATE TABLE zones (
 * id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
 * uuid CHAR(36) NOT NULL,
 * name VARCHAR(255) NOT NULL,
 * owner CHAR(36) NOT NULL
 * );
 * ALTER TABLE players ADD city CHAR(36);
 */
public class ConsulatCore extends JavaPlugin implements Listener {
    
    private static ConsulatCore instance;
    private static Random random;
    
    private Location spawn;
    public SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
    private DecimalFormat moneyFormat;
    private ModerationDatabase moderationDatabase;
    private boolean chat = true;
    private Channel spy;
    private SafariServer safari;
    
    private List<TextComponent> textPerso = new ArrayList<>();
    
    private Set<String> forbiddenPerso = new HashSet<>(Arrays.asList(
            "modo", "moderateur", "modérateur", "admin", "animateur", "partenaire", "youtubeur", "streamer", "ami",
            "fonda", "dev", "builder", "fondateur"));
    
    @Override
    public void onEnable(){
        if(instance != null){
            return;
        }
        instance = this;
        random = new Random();
        saveDefaultConfig();
        long startLoading = System.currentTimeMillis();
        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setGroupingSeparator(' ');
        moneyFormat = new DecimalFormat("###,###,###,###.## ¢", custom);
        spawn = new Location(Bukkit.getWorlds().get(0), 330, 65, -438, -145, 0);
        new DuelManager();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        spy = new SpyChannel();
        new ZoneManager();
        try {
            ChunkManager chunkManager = ChunkManager.getInstance();
            chunkManager.loadChunks();
            ClaimManager.getInstance();
        } catch(UnsupportedOperationException e){
            e.printStackTrace();
            Bukkit.shutdown();
        }
        ShopManager shopManager = ShopManager.getInstance();
        shopManager.loadAdminShops();
        new SPlayerManager();
        new BaltopManager();
        new FlyManager();
        moderationDatabase = new ModerationDatabase(this);
        EnchantmentManager.getInstance();
        safari = new SafariServer();
        safari.setSlot(50);
        Bukkit.getScheduler().runTaskTimer(this, new AFKRunnable(), 0L, 5 * 60 * 20);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new MonitoringRunnable(this), 0L, 10 * 60 * 20);
        Bukkit.getScheduler().runTaskTimer(this, new MessageRunnable(), 0L, 15 * 60 * 20);
        Bukkit.getScheduler().runTaskTimer(this, new MeceneRunnable(), 0L, 20 * 60 * 60);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            ZoneManager.getInstance().saveZones();
            ChunkManager.getInstance().saveChunks();
            ShopManager.getInstance().saveAdminShops();
        }, 60 * 60 * 20, 60 * 60 * 20);
        registerEvents();
        for(World world : Bukkit.getWorlds()){
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
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
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "ConsulatCore loaded in " + (System.currentTimeMillis() - startLoading) + " ms.");
    }
    
    @Override
    public void onDisable(){
        RedisManager.getInstance().getRedis().getTopic("PlayerSurvie").publish(0);
        ZoneManager.getInstance().saveZones();
        ChunkManager.getInstance().saveChunks();
        ShopManager.getInstance().saveAdminShops();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPostInit(PostInitEvent event){
        registerCommands();
    }
    
    @SuppressWarnings("ConstantConditions")
    private void registerCommands(){
        new AccessCommand();
        new AdminShopCommand();
        new AdvertCommand();
        new AnswerCommand();
        new BackCommand();
        new BaltopCommand();
        new BroadcastCommand();
        new CDebugCommand();
        new CEnchantCommand();
        new ClaimCommand();
        new DelHomeCommand();
        new DiscordCommand();
        new DuelCommand();
        new EnderchestCommand();
        new FlyCommand();
        new GamemodeCommand();
        new HelpCommand();
        new HomeCommand();
        new HubCommand();
        new IgnoreCommand();
        new InfosCommand();
        new InvseeCommand();
        new KickCommand();
        new ModerateCommand();
        new MoneyCommand();
        new MpCommand();
        new PayCommand();
        new PersoCommand();
        new ReportCommand();
        new SafariCommand();
        new SanctionCommand();
        new SeenCommand();
        new SetHomeCommand();
        getCommand("boutique").setExecutor(new ShopCommand());
        new fr.amisoz.consulatcore.commands.economy.ShopCommand();
        new SiteCommand();
        new SocialSpyCommand();
        new SpawnCommand();
        new StaffChatCommand();
        new StaffListCommand();
        new ToggleChatCommand();
        new TopCommand();
        new TpaCommand();
        new TpmodCommand();
        new UnbanCommand();
        new UnmuteCommand();
        new UnclaimCommand();
        new AntecedentsComand();
        new CityCommand();
    }
    
    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(this, this);
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
        Bukkit.getPluginManager().registerEvents(ClaimManager.getInstance(), this);
        Bukkit.getPluginManager().registerEvents(SPlayerManager.getInstance(), this);
        Bukkit.getPluginManager().registerEvents(ShopManager.getInstance(), this);
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
    
    public boolean isCustomRankForbidden(String rank){
        return forbiddenPerso.contains(rank.toLowerCase());
    }
    
    public synchronized static String formatMoney(double money){
        return getInstance().moneyFormat.format(money);
    }
    
    public static Random getRandom(){
        return random;
    }
    
    public Channel getSpy(){
        return spy;
    }
    
    public SafariServer getSafari(){
        return safari;
    }
}
