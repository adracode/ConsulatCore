package fr.leconsulat.core.players;

import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.channel.Speakable;
import fr.leconsulat.api.commands.CommandManager;
import fr.leconsulat.api.database.SaveManager;
import fr.leconsulat.api.nbt.CompoundTag;
import fr.leconsulat.api.nbt.ListTag;
import fr.leconsulat.api.nbt.NBTType;
import fr.leconsulat.api.nbt.StringTag;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.duel.Arena;
import fr.leconsulat.core.enchantments.CEnchantedItem;
import fr.leconsulat.core.enchantments.EnchantmentManager;
import fr.leconsulat.core.fly.FlyManager;
import fr.leconsulat.core.moderation.BanReason;
import fr.leconsulat.core.moderation.MuteReason;
import fr.leconsulat.core.moderation.MutedPlayer;
import fr.leconsulat.core.shop.player.PlayerShop;
import fr.leconsulat.core.utils.CustomEnum;
import fr.leconsulat.core.utils.ItemUtils;
import fr.leconsulat.core.zones.Zone;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimManager;
import fr.leconsulat.core.zones.claims.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
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
    private int limitHomes = -1;
    private double money;
    private long lastMove = System.currentTimeMillis();
    private boolean perkTop;
    private boolean isFrozen = false;
    private boolean inModeration = false;
    private ItemStack[] stockedInventory = null;
    private boolean spying;
    private boolean isMuted;
    private long muteExpireMillis;
    private String muteReason;
    private UUID lastPrivate;
    private CustomEnum persoState = CustomEnum.START;
    private int limitShop;
    private Fly fly = null;
    private Set<PlayerShop> shops = new HashSet<>();
    private Zone zone;
    private City city;
    private CEnchantedItem[] enchantedArmor;
    private Set<UUID> ignoredPlayers = new HashSet<>(1);
    private HashMap<BanReason, Integer> banHistory = new HashMap<>();
    private HashMap<MuteReason, Integer> muteHistory = new HashMap<>();
    
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
    
    @Override
    public boolean isInitialized(){
        return initialized;
    }
    
    public void setInitialized(boolean initialized){
        this.initialized = initialized;
    }
    
    @Override
    public void onQuit(){
        super.onQuit();
        if(isInModeration()){
            Player bukkitPlayer = getPlayer();
            for(PotionEffect effect : bukkitPlayer.getActivePotionEffects()){
                if(effect.getType().equals(PotionEffectType.NIGHT_VISION) || effect.getType().equals(PotionEffectType.INVISIBILITY)){
                    bukkitPlayer.removePotionEffect(effect.getType());
                }
            }
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(ConsulatCore.getInstance(), getPlayer()));
            bukkitPlayer.getInventory().setContents(getStockedInventory());
        }
        if(isFlying()){
            disableFly();
        }
        removeFromChannels();
        saveOnLeave();
    }
    
    @Override
    public void loadNBT(@NotNull CompoundTag playerTag){
        super.loadNBT(playerTag);
        if(playerTag.has("Ignored")){
            List<StringTag> ignored = playerTag.getList("Ignored", NBTType.STRING);
            for(StringTag uuid : ignored){
                ignoredPlayers.add(UUID.fromString(uuid.getValue()));
            }
        }
    }
    
    @Override
    public CompoundTag saveNBT(){
        CompoundTag playerTag = super.saveNBT();
        if(!ignoredPlayers.isEmpty()){
            ListTag<StringTag> ignored = new ListTag<>(NBTType.STRING);
            for(UUID uuid : ignoredPlayers){
                ignored.addTag(new StringTag(uuid.toString()));
            }
            playerTag.put("Ignored", ignored);
        }
        return playerTag;
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
    
    public int canBuyHome(){
        return limitHomes - setExtraHomes(getRank());
    }
    
    public int canBuySlotShop(){
        return limitShop - setExtraShops(getRank());
    }
    
    public void initialize(double money, int extraHomes, int limitShop,
                           Map<String, Location> homes, boolean perkTop, Fly fly, Collection<PlayerShop> shops, Zone zone, City city){
        this.money = money;
        this.limitHomes = setExtraHomes(getRank()) + extraHomes;
        this.limitShop = setExtraShops(getRank()) + limitShop;
        setHomes(homes);
        this.perkTop = perkTop;
        this.fly = fly;
        if(shops != null){
            this.shops.addAll(shops);
        }
        this.zone = zone;
        this.city = city;
    }
    
    public void initializeHomes(Rank newRank){
        this.limitHomes = limitHomes - setExtraHomes(getRank()) + setExtraHomes(newRank);
    }
    
    public void initializeShops(Rank newRank){
        this.limitShop = Math.max(limitShop - setExtraShops(getRank()) + setExtraShops(newRank), 0);
    }
    
    public boolean canAddNewShop(){
        return shops.size() < limitShop || hasPermission(ConsulatCore.getInstance().getPermission("bypass-shop-limit"));
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
    
    public int numberOfHomes(){
        return homes.size();
    }
    
    public boolean hasMoney(double amount){
        return money - amount >= 0D;
    }
    
    public void addMoney(double amount){
        money += amount;
    }
    
    public boolean hasPerkTop(){
        return perkTop;
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
    
    public void removeHome(String name) throws SQLException{
        SPlayerManager.getInstance().removeHome(getUUID(), name);
        this.homes.remove(name);
    }
    
    public boolean hasHome(String name){
        return homes.containsKey(name.toLowerCase());
    }
    
    public void removeMoney(double amount){
        if(!hasMoney(amount)){
            return;
        }
        addMoney(-amount);
    }
    
    public void incrementLimitHome(){
        SPlayerManager.getInstance().incrementLimitHome(getUUID());
        ++limitHomes;
    }
    
    public void incrementSlotShopHome(){
        SPlayerManager.getInstance().incrementSlotShopHome(getUUID());
        ++limitShop;
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
    
    public void disableFly(){
        if(!hasFly()){
            return;
        }
        this.fly.setFlying(false);
        FlyManager.getInstance().removeFlyingPlayer(this);
        if(!isInModeration()){
            Player player = getPlayer();
            player.setAllowFlight(false);
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
        }
    }
    
    public void addShop(PlayerShop shop){
        this.shops.add(shop);
    }
    
    public void removeShop(PlayerShop shop){
        this.shops.remove(shop);
    }
    
    @SuppressWarnings("ConstantConditions")
    public int spaceAvailable(ItemStack item){
        int available = 0;
        for(ItemStack itemInv : getPlayer().getInventory().getStorageContents()){
            if(itemInv == null){
                available += item.getType().getMaxStackSize();
            } else if(ItemUtils.areItemEquals(itemInv, item)){
                available += item.getType().getMaxStackSize() - itemInv.getAmount();
            }
        }
        return available;
    }
    
    public int getSimilarItems(ItemStack item){
        int size = 0;
        ItemStack[] storageContents = getPlayer().getInventory().getStorageContents();
        for(ItemStack itemInv : storageContents){
            if(itemInv != null && itemInv.isSimilar(item)){
                size += itemInv.getAmount();
            }
        }
        return size;
    }
    
    public void removeSimilarItems(ItemStack item, int amountToRemove){
        int deleted = 0;
        PlayerInventory inventory = getPlayer().getInventory();
        ItemStack[] storageContents = inventory.getStorageContents();
        for(int i = 0; i < storageContents.length; i++){
            ItemStack itemInv = storageContents[i];
            if(itemInv != null && itemInv.isSimilar(item)){
                if(deleted + itemInv.getAmount() > amountToRemove){
                    itemInv.setAmount(itemInv.getAmount() - (amountToRemove - deleted));
                    inventory.setItem(i, itemInv);
                    return;
                }
                deleted += itemInv.getAmount();
                inventory.clear(i);
            }
        }
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
    
    public void decrementTimeLeft(){
        fly.decrementTimeLeft();
    }
    
    public boolean belongsToCity(){
        return city != null;
    }
    
    public void initChannels(){
        if(belongsToCity()){
            city.getChannel().addPlayer(this);
        }
        if(hasPermission(CommandManager.getInstance().getCommand("staffchat").getPermission())){
            ConsulatCore.getInstance().getStaffChannel().addPlayer(this);
        }
    }
    
    public void removeFromChannels(){
        if(belongsToCity()){
            city.getChannel().removePlayer(this);
        }
        if(isSpying()){
            ConsulatCore.getInstance().getSpy().removePlayer(this);
        }
        Channel staffChannel = ConsulatCore.getInstance().getStaffChannel();
        if(staffChannel.isMember(this)){
            staffChannel.removePlayer(this);
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
            sendMessage("§6Voici ton grade: " + getCustomPrefix());
            sendMessage("§7Maintenant, choisis la couleur de ton pseudo:");
            sendMessage(ConsulatCore.getInstance().getTextPerso());
            return null;
        }
        if(isMuted() && System.currentTimeMillis() < getMuteExpireMillis()){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getMuteExpireMillis());
            String resultDate = ConsulatCore.getInstance().DATE_FORMAT.format(calendar.getTime());
            String reason = getMuteReason();
            sendMessage("§cTu es actuellement mute.\n§4Raison: §c" + reason + "\n§4Jusqu'au: §c" + resultDate);
            return null;
        }
        if(cancel){
            return null;
        }
        Channel channel = getCurrentChannel();
        if(channel == null){
            if(hasPower(Rank.MODO)){
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        } else {
            if(channel instanceof Speakable){
                channel.sendMessage(((Speakable)channel).speak(this, message));
            }
            return null;
        }
        return message;
    }
    
    public boolean ignorePlayer(UUID uuid){
        return ignoredPlayers.add(uuid);
    }
    
    public boolean removeIgnoredPlayer(UUID uuid){
        return ignoredPlayers.remove(uuid);
    }
    
    public boolean isIgnored(UUID uuid){
        return ignoredPlayers.contains(uuid);
    }
    
    public @Nullable Claim getClaim(){
        return ClaimManager.getInstance().getClaim(this.getPlayer().getChunk());
    }
    
    public long getMuteExpireMillis(){
        return muteExpireMillis;
    }
    
    public void setMuteExpireMillis(long muteExpireMillis){
        this.muteExpireMillis = muteExpireMillis;
    }
    
    public String getMuteReason(){
        return muteReason;
    }
    
    public void setMuteReason(String reason){
        this.muteReason = reason;
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
    
    public double getMoney(){
        return money;
    }
    
    public long getLastMove(){
        return lastMove;
    }
    
    public void setLastMove(long lastMove){
        this.lastMove = lastMove;
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
        setInventoryBlocked(inModeration);
    }
    
    public ItemStack[] getStockedInventory(){
        return stockedInventory;
    }
    
    public void setStockedInventory(ItemStack[] stockedInventory){
        this.stockedInventory = stockedInventory;
    }
    
    public boolean isSpying(){
        return spying;
    }
    
    public void setSpying(boolean spying){
        this.spying = spying;
        if(spying){
            ConsulatCore.getInstance().getSpy().addPlayer(this);
        } else {
            ConsulatCore.getInstance().getSpy().removePlayer(this);
        }
    }
    
    public boolean isMuted(){
        return isMuted;
    }
    
    public void setMuted(boolean muted){
        isMuted = muted;
    }
    
    public MutedPlayer getMute(){
        if(System.currentTimeMillis() < muteExpireMillis){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(muteExpireMillis);
            String resultDate = ConsulatCore.getInstance().DATE_FORMAT.format(calendar.getTime());
            String reason = muteReason;
            return new MutedPlayer(reason, resultDate);
        }
        return null;
    }
    
    public UUID getLastPrivate(){
        return lastPrivate;
    }
    
    public void setLastPrivate(UUID lastPrivate){
        this.lastPrivate = lastPrivate;
    }
    
    public CustomEnum getPersoState(){
        return persoState;
    }
    
    public void setPersoState(CustomEnum persoState){
        this.persoState = persoState;
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
    
    public boolean isFlyAvailable(){
        return hasFly() && fly.canFly();
    }
    
    public boolean isFlying(){
        return hasFly() && fly.isFlying();
    }
    
    public int getFlyTimeLeft(){
        return hasFly() ? fly.getTimeLeft() : 0;
    }
    
    public Set<PlayerShop> getShops(){
        return shops;
    }
    
    public long getFlyReset(){
        return fly.getReset();
    }
    
    public int getFlyTime(){
        return hasFly() ? fly.getFlyTime() : 0;
    }
    
    public Fly getFly(){
        return hasFly() ? fly : null;
    }
    
    public boolean setFly(Fly fly){
        if(this.fly.compareTo(fly) >= 0){
            return false;
        }
        this.fly = new Fly(fly);
        addCommandPermission(CommandManager.getInstance().getCommand("fly").getPermission());
        return true;
    }
    
    public HashMap<BanReason, Integer> getBanHistory(){
        return banHistory;
    }
    
    public HashMap<MuteReason, Integer> getMuteHistory(){
        return muteHistory;
    }
    
    public Zone getZone(){
        return zone;
    }
    
    public void setZone(Zone zone){
        this.zone = zone;
    }
    
    public City getCity(){
        return city;
    }
    
    public void setCity(City city){
        if(city == null && this.city != null){
            this.city.getChannel().removePlayer(this);
        } else if(city != null && this.city == null){
            city.getChannel().addPlayer(this);
        }
        this.city = city;
    }
    
    public Set<UUID> getIgnoredPlayers(){
        return Collections.unmodifiableSet(ignoredPlayers);
    }
    
    public void setHomes(Map<String, Location> homes){
        if(homes == null){
            return;
        }
        for(Map.Entry<String, Location> home : homes.entrySet()){
            addHome(home.getKey(), home.getValue());
        }
    }
    
    public void setPerkTop(boolean perkTop){
        SPlayerManager.getInstance().setPerkUp(getUUID(), true);
        this.perkTop = perkTop;
        if(perkTop){
            addCommandPermission(CommandManager.getInstance().getCommand("top").getPermission());
        } else {
            removeCommandPermission(CommandManager.getInstance().getCommand("top").getPermission());
        }
    }
    
    private int setExtraHomes(Rank rank){
        switch(rank){
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
    
    private int setExtraShops(Rank rank){
        switch(rank){
            case JOUEUR:
            case TOURISTE:
                return 2;
            case FINANCEUR:
                return 3;
            default:
                return 4;
        }
    }
    
    private void addHome(String name, Location location){
        homes.put(name, location);
    }
    
    private void saveOnLeave(){
        if(!isInitialized()){
            return;
        }
        SaveManager saveManager = SaveManager.getInstance();
        saveManager.removeData("player-money", this, true);
        saveManager.removeData("player-fly", this, true);
        saveManager.removeData("player-city", this, true);
    }
    
    
}
