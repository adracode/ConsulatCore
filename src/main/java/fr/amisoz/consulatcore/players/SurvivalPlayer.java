package fr.amisoz.consulatcore.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.duel.Arena;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.utils.CustomEnum;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.*;

public class SurvivalPlayer extends ConsulatPlayer {
    
    private boolean initialized;
    
    private long lastTeleport;
    private Arena arena;
    private boolean isFighting;
    private Set<Claim> ownedClaims = new HashSet<>();
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
    
    public SurvivalPlayer(UUID uuid, String name){
        super(uuid, name);
    }
    
    public boolean isSurvivalInitialized(){
        return initialized;
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
                           Map<String, Location> homes, boolean perkTop, Fly fly, Collection<Shop> shops){
        this.money = money;
        this.limitHomes = setExtraHomes() + extraHomes;
        this.limitShop = setExtraShops() + limitShop;
        setHomes(homes);
        this.perkTop = perkTop;
        this.fly = fly;
        if(shops != null){
            this.shops.addAll(shops);
        }
        this.initialized = true;
    }
    
    public boolean canAddNewShop(){
        return shops.size() < limitShop;
    }
    
    public Claim getClaimLocation(){
        return ClaimManager.getInstance().getClaim(this.getPlayer().getChunk());
    }
    
    public Long getMuteExpireMillis(){
        return muteExpireMillis;
    }
    
    public String getMuteReason(){
        return muteReason;
    }
    
    public void addClaim(Claim claim){
        this.ownedClaims.add(claim);
    }
    
    public Set<Claim> getOwnedChunks(){
        return Collections.unmodifiableSet(ownedClaims);
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
    
    public void addMoneyNoBDD(double amount){
        money += amount;
    }
    
    public void addMoney(double amount) throws SQLException{
        addMoneyNoBDD(amount);
        SPlayerManager.getInstance().addMoney(getUUID(), amount);
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
    
    public boolean canFly(){
        return canFly(getClaimLocation());
    }
    
    public boolean canFly(Chunk chunk){
        return canFly(ClaimManager.getInstance().getClaim(chunk));
    }
    
    public boolean canFly(Claim claim){
        if(claim == null){
            return false;
        }
        return claim.isAllowed(getUUID());
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
    
    public void removeMoney(double amount) throws SQLException{
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
        if(!hasFly()){
            return false;
        }
        return fly.canFly();
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
        Player player = getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), ()->{
            player.setAllowFlight(false);
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
        });
    }
    
    public int getFlyTimeLeft(){
        return hasFly() ? fly.getTimeLeft() : 0;
    }
    
    public void removeClaim(Claim claim){
        this.ownedClaims.remove(claim);
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
        return  limitHomes - setExtraHomes();
    }
    
    public long getFlyReset(){
        return fly.getReset();
    }
    
    public void decrementTimeLeft(){
        fly.decrementTimeLeft();
    }
}
