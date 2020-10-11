package fr.leconsulat.core;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.redis.RedisManager;
import fr.leconsulat.api.saver.Saver;
import fr.leconsulat.core.channel.SpyChannel;
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
import fr.leconsulat.core.players.PVPManager;
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
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Random;
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
    
    private boolean pvp = true;
    
    @Override
    public void onDisable(){
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "PlayerTestsurvie" : "PlayerSurvie").publishAsync(-1);
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
        new PVPManager();
        EnchantmentManager.getInstance();
        safari = new SafariServer();
        safari.setSlot(50);
        hub = new HubServer();
        hub.setSlot(Integer.MAX_VALUE);
        playerBaltop = new PlayerBaltop();
        cityBaltop = new CityBaltop();
        ConsulatAPI.getConsulatAPI().setSyncChat(true);
        new SpyChannel();
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
        registerCommands();
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "ConsulatCore loaded in " + (System.currentTimeMillis() - startLoading) + " ms.");
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "PlayerTestsurvie" : "PlayerSurvie").publishAsync(0);
    }
    
    public boolean isPvp(){
        return pvp;
    }
    
    public void setPvp(boolean pvp){
        this.pvp = pvp;
    }
    
    private void save(){
        ZoneManager.getInstance().saveZones();
        ChunkManager.getInstance().saveChunks();
        ShopManager.getInstance().saveAdminShops();
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
        new ModerateCommand().register();
        new MoneyCommand().register();
        new MpCommand().register();
        new PayCommand().register();
        new PVPCommand().register();
        new ReportCommand().register();
        new SafariCommand().register();
        new SetHomeCommand().register();
        new WebShopCommand().register();
        new ShopCommand().register();
        new SiteCommand().register();
        new SocialSpyCommand().register();
        new SpawnCommand().register();
        new StaffListCommand().register();
        new TopCommand().register();
        new TpaCommand().register();
        new TpmodCommand().register();
        new UnclaimCommand().register();
        new CityCommand().register();
        new TouristeCommand().register();
        if(ConsulatAPI.getConsulatAPI().isDevelopment()) {
            new TestCommand().register();
        }
    }
    
    private void registerEvents(){
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new InventoryListeners(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobListeners(), this);
        Bukkit.getPluginManager().registerEvents(new MovementChecker(), this);
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
