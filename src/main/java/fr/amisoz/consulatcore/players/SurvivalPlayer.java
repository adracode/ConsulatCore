package fr.amisoz.consulatcore.players;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.duel.Arena;
import fr.amisoz.consulatcore.enchantments.CEnchantedItem;
import fr.amisoz.consulatcore.enchantments.EnchantmentManager;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.moderation.BanEnum;
import fr.amisoz.consulatcore.moderation.MuteEnum;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.utils.CustomEnum;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

public class SurvivalPlayer extends ConsulatPlayer {
    
    private boolean initialized;
    
    private long lastTeleport;
    private Arena arena;
    private boolean isFighting;
    private Map<String, Location> homes = new HashMap<>();
    private Location oldLocation;
    private int limitHomes;
    private double money;
    private long lastMove = System.currentTimeMillis();
    private boolean perkTop;
    private boolean isFrozen = false;
    private boolean inModeration = false;
    private ItemStack[] stockedInventory = null;
    private boolean lookingInventory;
    private String sanctionTarget;
    private boolean spying;
    private boolean isMuted;
    private Long muteExpireMillis;
    private String muteReason;
    private UUID lastPrivate;
    private CustomEnum persoState = CustomEnum.START;
    private int limitShop;
    private Fly fly = null;
    private Set<Shop> shops = new HashSet<>();
    private Zone zone;
    private City city;
    private CEnchantedItem[] enchantedArmor;
    private HashMap<BanEnum, Integer> banHistory = new HashMap<>();
    private HashMap<MuteEnum, Integer> muteHistory = new HashMap<>();
    
    public SurvivalPlayer(UUID uuid, String name){
        super(uuid, name);
        ItemStack[] currentArmor = getPlayer().getInventory().getArmorContents();
        this.enchantedArmor = new CEnchantedItem[4];
        for(int i = 0; i < currentArmor.length; i++){
            ItemStack armor = currentArmor[i];
            if(CEnchantedItem.isEnchanted(armor)){
                CEnchantedItem enchantedItem = new CEnchantedItem(armor);
                this.enchantedArmor[3 - i] = enchantedItem;
                EnchantmentManager.getInstance().applyCEnchantment(this, enchantedItem.getEnchants());
            }
        }
    }
    
    private int setExtraHomes(){
        switch(getRank()){
            case JOUEUR:
                return 1;
            case TOURISTE:
                return 2;
            case FINANCEUR:
                return 3;
            default:
                return 4;
        }
    }
    
    private int setExtraShops(){
        switch(getRank()){
            case JOUEUR:
            case TOURISTE:
                return 5;
            case FINANCEUR:
                return 10;
            default:
                return 15;
        }
    }
    
    public void initialize(double money, int extraHomes, int limitShop,
                           Map<String, Location> homes, boolean perkTop, Fly fly, Collection<Shop> shops, Zone zone, City city){
        this.money = money;
        this.limitHomes = setExtraHomes() + extraHomes;
        this.limitShop = setExtraShops() + limitShop;
        setHomes(homes);
        this.perkTop = perkTop;
        this.fly = fly;
        if(shops != null){
            this.shops.addAll(shops);
        }
        this.zone = zone;
        this.city = city;
    }
    
    @Override
    public boolean isInitialized(){
        return initialized;
    }
    
    public void setInitialized(boolean initialized){
        this.initialized = initialized;
    }
    
    public boolean canAddNewShop(){
        return shops.size() < limitShop || ConsulatAPI.getConsulatAPI().isDebug();
    }
    
    public @Nullable Claim getClaim(){
        return ClaimManager.getInstance().getClaim(this.getPlayer().getChunk());
    }
    
    public Long getMuteExpireMillis(){
        return muteExpireMillis;
    }
    
    public String getMuteReason(){
        return muteReason;
    }
    
    public boolean canAddNewHome(String home){
        return homes.size() < limitHomes || homes.containsKey(home);
    }
    
    public void addNewHome(String name, Location location) throws SQLException{
        name = name.toLowerCase();
        if(homes.size() >= limitHomes && !homes.containsKey(name)){
            return;
        }
        SPlayerManager.getInstance().addHome(this, name, location);
        addHome(name, location);
    }
    
    public Location getHome(String name){
        return homes.get(name);
    }
    
    public Set<String> getNameHomes(){
        return Collections.unmodifiableSet(homes.keySet());
    }
    
    public Location getOldLocation(){
        return oldLocation;
    }
    
    public void setOldLocation(Location oldLocation){
        this.oldLocation = oldLocation;
    }
    
    public int numberOfHomes(){
        return homes.size();
    }
    
    public boolean hasMoney(double amount){
        return money - amount >= 0D;
    }
    
    public void addMoney(double amount){
        money += amount;
    }
    
    public double getMoney(){
        return money;
    }
    
    public long getLastMove(){
        return lastMove;
    }
    
    public void setLastMove(long lastMove){
        this.lastMove = lastMove;
    }
    
    public void setHomes(Map<String, Location> homes){
        if(homes == null){
            return;
        }
        for(Map.Entry<String, Location> home : homes.entrySet()){
            addHome(home.getKey(), home.getValue());
        }
    }
    
    private void addHome(String name, Location location){
        homes.put(name, location);
    }
    
    public boolean hasPerkTop(){
        return perkTop;
    }
    
    public void setPerkTop(boolean perkTop) throws SQLException{
        SPlayerManager.getInstance().setPerkUp(getUUID(), true);
        this.perkTop = perkTop;
    }
    
    public boolean isFrozen(){
        return isFrozen;
    }
    
    public void setFrozen(boolean frozen){
        isFrozen = frozen;
    }
    
    public boolean isInModeration(){
        return inModeration;
    }
    
    public void setInModeration(boolean inModeration){
        this.inModeration = inModeration;
    }
    
    public ItemStack[] getStockedInventory(){
        return stockedInventory;
    }
    
    public void setStockedInventory(ItemStack[] stockedInventory){
        this.stockedInventory = stockedInventory;
    }
    
    public boolean isLookingInventory(){
        return lookingInventory;
    }
    
    public void setLookingInventory(boolean lookingInventory){
        this.lookingInventory = lookingInventory;
    }
    
    public void setSanctionTarget(String sanctionTarget){
        this.sanctionTarget = sanctionTarget;
    }
    
    public String getSanctionTarget(){
        return sanctionTarget;
    }
    
    public boolean isSpying(){
        return spying;
    }
    
    public void setSpying(boolean spying){
        this.spying = spying;
    }
    
    public boolean isMuted(){
        return isMuted;
    }
    
    public void setMuted(boolean muted){
        isMuted = muted;
    }
    
    public MuteObject getMute(){
        if(System.currentTimeMillis() < muteExpireMillis){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(muteExpireMillis);
            String resultDate = ConsulatCore.getInstance().DATE_FORMAT.format(calendar.getTime());
            String reason = muteReason;
            return new MuteObject(reason, resultDate);
        }
        return null;
    }
    
    public UUID getLastPrivate(){
        return lastPrivate;
    }
    
    public void setLastPrivate(UUID lastPrivate){
        this.lastPrivate = lastPrivate;
    }
    
    public boolean hasFly(){
        return fly != null;
    }
    
    public boolean canFlyHere(){
        return canFlyHere(getClaim());
    }
    
    public boolean canFlyHere(Chunk chunk){
        return canFlyHere(ClaimManager.getInstance().getClaim(chunk));
    }
    
    public boolean canFlyHere(Claim claim){
        if(claim == null){
            return false;
        }
        return claim.canInteract(this, ClaimPermission.FLY);
    }
    
    public boolean hasInfiniteFly(){
        return hasFly() && fly.hasInfiniteFly();
    }
    
    public void setFly(Fly fly) throws SQLException{
        Fly newFly = new Fly(fly);
        SPlayerManager.getInstance().setFly(getUUID(), newFly);
        this.fly = newFly;
    }
    
    public CustomEnum getPersoState(){
        return persoState;
    }
    
    public void setPersoState(CustomEnum persoState){
        this.persoState = persoState;
    }
    
    public void removeHome(String name) throws SQLException{
        SPlayerManager.getInstance().removeHome(getUUID(), name);
        this.homes.remove(name);
    }
    
    public boolean hasHome(String name){
        return homes.containsKey(name.toLowerCase());
    }
    
    public boolean isFighting(){
        return isFighting;
    }
    
    public void setFighting(boolean fighting){
        isFighting = fighting;
    }
    
    public Arena getArena(){
        return arena;
    }
    
    public void setArena(Arena arena){
        this.arena = arena;
    }
    
    public long getLastTeleport(){
        return lastTeleport;
    }
    
    public void setLastTeleport(long lastTeleport){
        this.lastTeleport = lastTeleport;
    }
    
    public void setMuteReason(String reason){
        this.muteReason = reason;
    }
    
    public void setMuteExpireMillis(Long muteExpireMillis){
        this.muteExpireMillis = muteExpireMillis;
    }
    
    public void removeMoney(double amount){
        if(!hasMoney(amount)){
            return;
        }
        addMoney(-amount);
    }
    
    public void incrementLimitHome() throws SQLException{
        SPlayerManager.getInstance().incrementLimitHome(getUUID());
        ++limitHomes;
    }
    
    public boolean isFlyAvailable(){
        return hasFly() && fly.canFly();
    }
    
    public boolean isFlying(){
        return hasFly() && fly.isFlying();
    }
    
    public void enableFly(){
        if(!hasFly()){
            return;
        }
        this.fly.setFlying(true);
        FlyManager.getInstance().addFlyingPlayer(this);
        Player player = getPlayer();
        player.setAllowFlight(true);
        player.setFlying(true);
    }
    
    public void disableFly() throws SQLException{
        if(!hasFly()){
            return;
        }
        this.fly.setFlying(false);
        SPlayerManager.getInstance().setFly(getUUID(), this.fly);
        FlyManager.getInstance().removeFlyingPlayer(this);
        if(!isInModeration()){
            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                Player player = getPlayer();
                player.setAllowFlight(false);
                player.setFlying(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
            });
        }
    }
    
    public int getFlyTimeLeft(){
        return hasFly() ? fly.getTimeLeft() : 0;
    }
    
    public void addShop(Shop shop){
        this.shops.add(shop);
    }
    
    public void removeShop(Shop shop){
        this.shops.remove(shop);
    }
    
    public Set<Shop> getShops(){
        return shops;
    }
    
    public int spaceAvailable(ItemStack item){
        int available = 0;
        for(ItemStack itemInv : getPlayer().getInventory().getStorageContents()){
            if(itemInv == null){
                available += item.getType().getMaxStackSize();
            } else if(itemInv.getType() == item.getType() && itemInv.getItemMeta().equals(item.getItemMeta())){
                available += item.getType().getMaxStackSize() - itemInv.getAmount();
            }
        }
        return available;
    }
    
    public void addItemInInventory(int amount, ItemStack item){
        Inventory inventory = getPlayer().getInventory();
        while(amount > 0){
            ItemStack newItem = new ItemStack(item);
            int min = Integer.min(item.getMaxStackSize(), amount);
            newItem.setAmount(min);
            amount -= min;
            inventory.addItem(newItem);
        }
        
    }
    
    public int getLimitHome(){
        return limitHomes - setExtraHomes();
    }
    
    public long getFlyReset(){
        return fly.getReset();
    }
    
    public void decrementTimeLeft(){
        fly.decrementTimeLeft();
    }
    
    public int getFlyTime(){
        return hasFly() ? fly.getFlyTime() : 0;
    }
    
    public boolean belongsToCity(){
        return city != null;
    }
    
    public HashMap<BanEnum, Integer> getBanHistory(){
        return banHistory;
    }
    
    public HashMap<MuteEnum, Integer> getMuteHistory(){
        return muteHistory;
    }
    
    public Zone getZone(){
        return zone;
    }
    
    public void setZone(Zone zone){
        this.zone = zone;
    }
    
    public void setCity(City city){
        if(city == null && this.city != null){
            this.city.getChannel().removePlayer(this);
        } else if(city != null && this.city == null){
            city.getChannel().addPlayer(this);
        }
        this.city = city;
    }
    
    public City getCity(){
        return city;
    }
    
    public void setArmor(PlayerArmorChangeEvent.SlotType slotType, @Nullable ItemStack item){
        setArmor(slotType.ordinal(), item);
    }
    
    public void setArmor(int i, @Nullable ItemStack item){
        this.enchantedArmor[i] = CEnchantedItem.isEnchanted(item) ? new CEnchantedItem(item) : null;
    }
    
    public @Nullable CEnchantedItem getArmor(PlayerArmorChangeEvent.SlotType slotType){
        return getArmor(slotType.ordinal());
    }
    
    public CEnchantedItem getArmor(int i){
        return enchantedArmor[i];
    }
    
    public void initChannels(){
        if(belongsToCity()){
            city.getChannel().addPlayer(this);
        }
    }
    
    public void removeFromChannels(){
        if(belongsToCity()){
            city.getChannel().removePlayer(this);
        }
    }
    
    public String chat(String message){
        boolean cancel = false;
        if(getCurrentChannel() == null){
            if(!ConsulatCore.getInstance().isChatActivated() && !hasPower(Rank.RESPONSABLE)){
                sendMessage("§cChat coupé.");
                cancel = true;
            }
        }
        if(getPersoState() == CustomEnum.PREFIX){
            if(message.equalsIgnoreCase("cancel")){
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        resetCustomRank();
                    } catch(SQLException e){
                        e.printStackTrace();
                    }
                });
                setPersoState(CustomEnum.START);
                sendMessage("§aChangement de grade annulé.");
                return null;
            }
            if(message.length() > 10){
                sendMessage("§cTon grade doit faire 10 caractères maximum ! Tape §ocancel §r§csi tu veux annuler.");
                return null;
            }
            if(ConsulatCore.getInstance().isCustomRankForbidden(message)){
                sendMessage("§cTu ne peux pas appeler ton grade comme cela ! Tape §ocancel §r§csi tu veux annuler.");
                return null;
            }
            if(!message.matches("^[a-zA-Z]+$")){
                sendMessage("§cTu dois utiliser uniquement des lettres dans ton grade.");
                return null;
            }
            setPrefix(message);
            setPersoState(CustomEnum.NAME_COLOR);
            sendMessage("§6Voici ton grade : " + ChatColor.translateAlternateColorCodes('&', getCustomPrefix()));
            sendMessage("§7Maintenant, choisis la couleur de ton pseudo :");
            TextComponent[] textComponents = ConsulatCore.getInstance().getTextPerso().toArray(new TextComponent[0]);
            sendMessage(textComponents);
            return null;
        }
        if(isMuted() && System.currentTimeMillis() < getMuteExpireMillis()){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getMuteExpireMillis());
            String resultDate = ConsulatCore.getInstance().DATE_FORMAT.format(calendar.getTime());
            String reason = getMuteReason();
            sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + reason + "\n§4Jusqu'au : §c" + resultDate);
            return null;
        }
        if(cancel){
            return null;
        }
        if(getCurrentChannel() == null){
            if(hasPower(Rank.MODO)){
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        } else {
            getCurrentChannel().sendMessage(this, message);
            return null;
        }
        return message;
    }
    
    @Override
    public String toString(){
        return super.toString() +
                " SurvivalPlayer{" +
                "initialized=" + initialized +
                ", lastTeleport=" + lastTeleport +
                ", arena=" + arena +
                ", isFighting=" + isFighting +
                ", homes=" + homes +
                ", oldLocation=" + oldLocation +
                ", limitHomes=" + limitHomes +
                ", money=" + money +
                ", lastMove=" + lastMove +
                ", perkTop=" + perkTop +
                ", isFrozen=" + isFrozen +
                ", inModeration=" + inModeration +
                ", stockedInventory=" + Arrays.toString(stockedInventory) +
                ", lookingInventory=" + lookingInventory +
                ", sanctionTarget='" + sanctionTarget + '\'' +
                ", spying=" + spying +
                ", isMuted=" + isMuted +
                ", muteExpireMillis=" + muteExpireMillis +
                ", muteReason='" + muteReason + '\'' +
                ", lastPrivate=" + lastPrivate +
                ", persoState=" + persoState +
                ", limitShop=" + limitShop +
                ", fly=" + fly +
                ", shops=" + shops +
                ", zone=" + zone +
                ", city=" + city +
                ", banHistory=" + banHistory +
                ", muteHistory=" + muteHistory +
                '}';
    }
}
