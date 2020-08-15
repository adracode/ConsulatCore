package fr.amisoz.consulatcore;

import fr.amisoz.consulatcore.channel.StaffChannel;
import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.amisoz.consulatcore.commands.cities.CityCommand;
import fr.amisoz.consulatcore.commands.claims.AccessCommand;
import fr.amisoz.consulatcore.commands.claims.ClaimCommand;
import fr.amisoz.consulatcore.commands.claims.GetkeyCommand;
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
import fr.amisoz.consulatcore.server.HubServer;
import fr.amisoz.consulatcore.server.SafariServer;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.redis.RedisManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class ConsulatCore extends JavaPlugin implements Listener {
    
    private static ConsulatCore instance;
    private static Random random;
    public SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
    private Location spawn;
    private World overworld;
    private DecimalFormat moneyFormat;
    private ModerationDatabase moderationDatabase;
    private boolean chat = true;
    private Channel spy;
    private StaffChannel staffChannel;
    private SafariServer safari;
    private HubServer hub;
    
    private List<TextComponent> textPerso = new ArrayList<>();
    
    private Set<String> forbiddenPerso = new HashSet<>(Arrays.asList(
            "modo", "moderateur", "modérateur", "admin", "animateur", "partenaire", "youtubeur", "streamer", "ami",
            "fonda", "dev", "builder", "fondateur"));
    
    public Connection getDatabaseConnection(){
        return ConsulatAPI.getDatabase();
    }
    
    public ModerationDatabase getModerationDatabase(){
        return moderationDatabase;
    }
    
    public boolean isChatActivated(){
        return chat;
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
    
    public World getOverworld(){
        return overworld;
    }
    
    public static Random getRandom(){
        return random;
    }
    
    public Channel getSpy(){
        return spy;
    }
    
    public StaffChannel getStaffChannel(){
        return staffChannel;
    }
    
    public SafariServer getSafari(){
        return safari;
    }
    
    public HubServer getHub(){
        return hub;
    }
    
    public void setChat(boolean chat){
        this.chat = chat;
    }
    
    public boolean isCustomRankForbidden(String rank){
        return forbiddenPerso.contains(rank.toLowerCase());
    }
    
    public String getPermission(String permission){
        return getName().toLowerCase() + "." + permission;
    }
    
    @SuppressWarnings("ConstantConditions")
    private void registerCommands(){
        new AccessCommand().register();
        new AdminShopCommand().register();
        new AdvertCommand().register();
        new AnswerCommand().register();
        new BackCommand().register();
        new BaltopCommand().register();
        new BroadcastCommand().register();
        new CDebugCommand().register();
        new CEnchantCommand().register();
        new ClaimCommand().register();
        new DelHomeCommand().register();
        new DiscordCommand().register();
        new DuelCommand().register();
        new EnderchestCommand().register();
        new FlyCommand().register();
        new GamemodeCommand().register();
        new GetkeyCommand().register();
        new ConsulatHelpCommand().register();
        new HomeCommand().register();
        new HubCommand().register();
        new IgnoreCommand().register();
        new InfosCommand().register();
        new InvseeCommand().register();
        new KickCommand().register();
        new ModerateCommand().register();
        new MoneyCommand().register();
        new MpCommand().register();
        new PayCommand().register();
        new PersoCommand().register();
        new ReportCommand().register();
        new SafariCommand().register();
        new SanctionCommand().register();
        new SeenCommand().register();
        new SetHomeCommand().register();
        getCommand("boutique").setExecutor(new ShopCommand());
        new fr.amisoz.consulatcore.commands.economy.ShopCommand().register();
        new SiteCommand().register();
        new SocialSpyCommand().register();
        new SpawnCommand().register();
        new StaffChatCommand().register();
        new StaffListCommand().register();
        new ToggleChatCommand().register();
        new TopCommand().register();
        new TpaCommand().register();
        new TpmodCommand().register();
        new UnbanCommand().register();
        new UnmuteCommand().register();
        new UnclaimCommand().register();
        new AntecedentsCommand().register();
        new CityCommand().register();
    }
    
    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new ChatListeners(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListeners(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListeners(), this);
        Bukkit.getPluginManager().registerEvents(new MoveListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ExperienceListener(), this);
        Bukkit.getPluginManager().registerEvents(new FoodListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);
        //Bukkit.getPluginManager().registerEvents(new DuelListeners(), this);
        Bukkit.getPluginManager().registerEvents(new ClaimCancelListener(), this);
        Bukkit.getPluginManager().registerEvents(ClaimManager.getInstance(), this);
        Bukkit.getPluginManager().registerEvents(SPlayerManager.getInstance(), this);
        Bukkit.getPluginManager().registerEvents(ShopManager.getInstance(), this);
    }
    
    @Override
    public void onDisable(){
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "PlayerTestsurvie" : "PlayerSurvie").publish(-1);
        ZoneManager.getInstance().saveZones();
        ChunkManager.getInstance().saveChunks();
        ShopManager.getInstance().saveAdminShops();
    }
    
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
        overworld = Bukkit.getWorlds().get(0);
        spawn = new Location(overworld, 330, 65, -438, -145, 0);
        new DuelManager();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        spy = new SpyChannel();
        staffChannel = new StaffChannel();
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
        hub = new HubServer();
        hub.setSlot(Integer.MAX_VALUE);
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
        registerCommands();
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "ConsulatCore loaded in " + (System.currentTimeMillis() - startLoading) + " ms.");
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "PlayerTestsurvie" : "PlayerSurvie").publish(0);
    }
    
    public synchronized static String formatMoney(double money){
        return getInstance().moneyFormat.format(money);
    }
}
