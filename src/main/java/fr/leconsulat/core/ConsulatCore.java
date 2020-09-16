package fr.leconsulat.core;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.redis.RedisManager;
import fr.leconsulat.api.saver.Saver;
import fr.leconsulat.core.channel.StaffChannel;
import fr.leconsulat.core.chunks.ChunkManager;
import fr.leconsulat.core.commands.cities.CityCommand;
import fr.leconsulat.core.commands.claims.AccessCommand;
import fr.leconsulat.core.commands.claims.ClaimCommand;
import fr.leconsulat.core.commands.claims.GetkeyCommand;
import fr.leconsulat.core.commands.claims.UnclaimCommand;
import fr.leconsulat.core.commands.economy.*;
import fr.leconsulat.core.commands.enchantments.CEnchantCommand;
import fr.leconsulat.core.commands.moderation.*;
import fr.leconsulat.core.commands.players.*;
import fr.leconsulat.core.commands.safari.SafariCommand;
import fr.leconsulat.core.duel.DuelManager;
import fr.leconsulat.core.economy.CityBaltop;
import fr.leconsulat.core.economy.PlayerBaltop;
import fr.leconsulat.core.enchantments.EnchantmentManager;
import fr.leconsulat.core.fly.FlyManager;
import fr.leconsulat.core.listeners.entity.MobListeners;
import fr.leconsulat.core.listeners.entity.player.*;
import fr.leconsulat.core.listeners.world.ClaimCancelListener;
import fr.leconsulat.core.listeners.world.SignListener;
import fr.leconsulat.core.moderation.ModerationDatabase;
import fr.leconsulat.core.moderation.channels.SpyChannel;
import fr.leconsulat.core.players.SPlayerManager;
import fr.leconsulat.core.runnable.AFKRunnable;
import fr.leconsulat.core.runnable.MeceneRunnable;
import fr.leconsulat.core.runnable.MessageRunnable;
import fr.leconsulat.core.runnable.MonitoringRunnable;
import fr.leconsulat.core.server.HubServer;
import fr.leconsulat.core.server.SafariServer;
import fr.leconsulat.core.shop.ShopManager;
import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.claims.ClaimManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
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
    
    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
    
    private HubServer hub;
    private SafariServer safari;
    
    private PlayerBaltop playerBaltop;
    private CityBaltop cityBaltop;
    
    private DecimalFormat moneyFormat;
    
    private World overworld;
    private Location spawn;
    
    private ModerationDatabase moderationDatabase;
    
    private Channel spy;
    private StaffChannel staffChannel;
    private boolean chat = true;
    
    private TextComponent[] textPerso;
    
    private Set<String> forbiddenPerso = new HashSet<>(Arrays.asList(
            "modo", "moderateur", "modérateur", "admin", "animateur", "partenaire", "youtubeur", "streamer", "ami",
            "fonda", "dev", "builder", "fondateur"));
    
    @Override
    public void onDisable(){
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "PlayerTestsurvie" : "PlayerSurvie").publish(-1);
        save();
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
        FileConfiguration config = getConfig();
        overworld = Bukkit.getWorlds().get(0);
        ConfigurationSection spawnSection = config.getConfigurationSection("spawn");
        if(spawnSection == null){
            spawn = new Location(overworld, 330, 65, -438, -145, 0);
            config.set("spawn.x", 330.0);
            config.set("spawn.y", 65.0);
            config.set("spawn.z", -438.0);
            config.set("spawn.yaw", -145.0);
            config.set("spawn.pitch", 0.0);
            saveConfig();
        } else {
            spawn = new Location(overworld,
                    spawnSection.getDouble("x", 330.0),
                    spawnSection.getDouble("y", 65.0),
                    spawnSection.getDouble("z", -438.0),
                    (float)spawnSection.getDouble("yaw", -145.0),
                    (float)spawnSection.getDouble("pitch", 0.0)
            );
        }
        moneyFormat = new DecimalFormat("###,###,###,###.## ¢", custom);
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
        new PlayerBaltop();
        new FlyManager();
        moderationDatabase = new ModerationDatabase(this);
        EnchantmentManager.getInstance();
        safari = new SafariServer();
        safari.setSlot(50);
        hub = new HubServer();
        hub.setSlot(Integer.MAX_VALUE);
        playerBaltop = new PlayerBaltop();
        cityBaltop = new CityBaltop();
        Bukkit.getScheduler().runTaskTimer(this, new AFKRunnable(), 0L, 5 * 60 * 20);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new MonitoringRunnable(this), 0L, 10 * 60 * 20);
        Bukkit.getScheduler().runTaskTimer(this, new MessageRunnable(), 0L, 15 * 60 * 20);
        Bukkit.getScheduler().runTaskTimer(this, new MeceneRunnable(), 0L, 20 * 60 * 60);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::save, 15 * 60 * 20, 15 * 60 * 20);
        Saver.getInstance().addSave(this::save);
        registerEvents();
        for(World world : Bukkit.getWorlds()){
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
        List<TextComponent> textPerso = new ArrayList<>();
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
        this.textPerso = textPerso.toArray(new TextComponent[0]);
        registerCommands();
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "ConsulatCore loaded in " + (System.currentTimeMillis() - startLoading) + " ms.");
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "PlayerTestsurvie" : "PlayerSurvie").publish(0);
    }
    
    private void save(){
        ZoneManager.getInstance().saveZones();
        ChunkManager.getInstance().saveChunks();
        ShopManager.getInstance().saveAdminShops();
    }
    
    public boolean isCustomRankForbidden(String rank){
        return forbiddenPerso.contains(rank.toLowerCase());
    }
    
    public String getPermission(String permission){
        return getName().toLowerCase() + "." + permission;
    }
    
    public HubServer getHub(){
        return hub;
    }
    
    public SafariServer getSafari(){
        return safari;
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
    
    public TextComponent[] getTextPerso(){
        return textPerso;
    }
    
    public Location getSpawn(){
        return spawn;
    }
    
    public PlayerBaltop getPlayerBaltop(){
        return playerBaltop;
    }
    
    public CityBaltop getCityBaltop(){
        return cityBaltop;
    }
    
    public void setSpawn(Location spawn){
        this.spawn = spawn;
        FileConfiguration config = getConfig();
        config.set("spawn.x", spawn.getX());
        config.set("spawn.y", spawn.getY());
        config.set("spawn.z", spawn.getZ());
        config.set("spawn.yaw", spawn.getYaw());
        config.set("spawn.pitch", spawn.getPitch());
        saveConfig();
    }
    
    public World getOverworld(){
        return overworld;
    }
    
    public Channel getSpy(){
        return spy;
    }
    
    public StaffChannel getStaffChannel(){
        return staffChannel;
    }
    
    
    public void setChat(boolean chat){
        this.chat = chat;
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
        new WebShopCommand().register();
        new ShopCommand().register();
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
        new TouristeCommand().register();
        if(ConsulatAPI.getConsulatAPI().isDevelopment()) {
            new TestCommand().register();
        }
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
    
    public synchronized static String formatMoney(double money){
        return getInstance().moneyFormat.format(money);
    }
    
    public static ConsulatCore getInstance(){
        return instance;
    }
    
    public static Random getRandom(){
        return random;
    }
}
