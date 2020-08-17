package fr.amisoz.consulatcore.shop.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.enchantments.CEnchantedItem;
import fr.amisoz.consulatcore.enchantments.CEnchantment;
import fr.amisoz.consulatcore.guis.shop.ShopGui;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerShop implements Shop {
    
    public static final String TYPE = "PLAYER";
    private static BlockFace[] chestFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    
    private long coords;
    private UUID owner;
    private String ownerName;
    private ItemStack forSale;
    private ShopItemType[] types;
    private double price;
    private int amount;
    private boolean open = false;
    
    public PlayerShop(UUID owner, String ownerName, ItemStack forSale, double price, Location location, boolean legacy){
        this.owner = owner;
        this.ownerName = ownerName;
        this.forSale = new ItemStack(forSale);
        this.forSale.setAmount(1);
        this.price = price;
        this.coords = CoordinatesUtils.convertCoordinates(location);
        this.amount = legacy ? setAmountLegacy() : setAmount();
        buy(0);
        List<ShopItemType> types = new ArrayList<>();
        Material material = forSale.getType();
        if(forSale.getEnchantments().size() != 0){
            for(Map.Entry<Enchantment, Integer> enchantment : forSale.getEnchantments().entrySet()){
                if(enchantment.getValue() != 0){
                    types.add(new ShopItemType.EnchantmentItem(enchantment.getKey()));
                }
            }
        }
        if(forSale.getItemMeta() instanceof EnchantmentStorageMeta){
            for(Map.Entry<Enchantment, Integer> enchantment : ((EnchantmentStorageMeta)forSale.getItemMeta()).getStoredEnchants().entrySet()){
                if(enchantment.getValue() != 0){
                    types.add(new ShopItemType.EnchantmentItem(enchantment.getKey()));
                }
            }
        } else if(forSale.getItemMeta() instanceof PotionMeta){
            PotionEffectType effectType = ((PotionMeta)forSale.getItemMeta()).getBasePotionData().getType().getEffectType();
            if(effectType != null){
                types.add(new ShopItemType.PotionItem(effectType));
            }
        }
        if(CEnchantedItem.isEnchanted(forSale)){
            CEnchantedItem item = new CEnchantedItem(forSale);
            for(CEnchantment enchantment : item.getEnchants()){
                types.add(new ShopItemType.CEnchantmentItem(enchantment.getEnchantment()));
            }
        }
        types.add(new ShopItemType.MaterialItem(material));
        this.types = types.toArray(new ShopItemType[0]);
    }
    
    public long getCoords(){
        return coords;
    }
    
    public int getX(){
        return CoordinatesUtils.getX(coords);
    }
    
    public int getY(){
        return CoordinatesUtils.getY(coords);
    }
    
    public int getZ(){
        return CoordinatesUtils.getZ(coords);
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
    
    public void addInGui(){
        GuiContainer<ShopItemType> container = GuiManager.getInstance().getContainer("shop");
        for(ShopItemType type : types){
            ((ShopGui)container.getGui(type)).addShop(this);
        }
        ((ShopGui)container.getGui(ShopItemType.ALL)).addShop(this);
        ShopManager.getInstance().addType(this);
    }
    
    public void removeInGui(){
        GuiContainer<ShopItemType> container = GuiManager.getInstance().getContainer("shop");
        for(ShopItemType type : types){
            ((ShopGui)container.getGui(type)).removeShop(this);
        }
        ((ShopGui)container.getGui(ShopItemType.ALL)).removeShop(this);
        ShopManager.getInstance().removeType(this);
    }
    
    public boolean isItemAccepted(ItemStack item){
        if(item == null){
            return false;
        }
        return item.getType() == forSale.getType() && item.getItemMeta().equals(forSale.getItemMeta());
    }
    
    public int getAmount(int amount){
        return Integer.min(getAmount(), amount);
    }
    
    public boolean placeItemFrame(){
        Location location = getLocation();
        ItemFrame frame;
        try {
            frame = location.getWorld().spawn(location.clone().add(0, 1, 0), ItemFrame.class);
        } catch(IllegalArgumentException e){
            return false;
        }
        frame.setFacingDirection(BlockFace.UP);
        frame.setItem(forSale);
        frame.setInvulnerable(true);
        return true;
    }
    
    public void destroyFrame(){
        ItemFrame frame = getItemFrame();
        if(frame != null){
            frame.remove();
        }
    }
    
    public void buy(int amount){
        if(isEmpty()){
            return;
        }
        this.amount -= amount;
        Inventory inventory = getInventory();
        inventory.clear();
        int count = 0, index = 0, stack = forSale.getMaxStackSize();
        while(count < this.amount && index < 27){
            ItemStack newItem = new ItemStack(forSale);
            if(count + stack > this.amount){
                newItem.setAmount(this.amount - count);
            } else {
                newItem.setAmount(stack);
            }
            inventory.setItem(index, newItem);
            count += newItem.getAmount();
            ++index;
        }
        if(isEmpty()){
            removeInGui();
        }
    }
    
    public boolean isOwner(UUID uuid){
        return owner.equals(uuid);
    }
    
    public boolean isOpen(){
        return open;
    }
    
    public void setOpen(boolean open){
        this.open = open;
        if(!open){
            boolean wasEmpty = isEmpty();
            amount = setAmount();
            buy(0);
            if(wasEmpty && !isEmpty()){
                addInGui();
            } else if(!wasEmpty && isEmpty()){
                removeInGui();
            }
        }
    }
    
    public int getAmount(){
        return amount;
    }
    
    public Sign getSign(){
        Block chest = getLocation().getBlock();
        for(BlockFace face : chestFaces){
            Block sign = chest.getRelative(face);
            if(sign.getType() == Material.OAK_WALL_SIGN){
                Sign state = (Sign)sign.getState();
                Chest c = ShopManager.getInstance().getChestFromSign(sign);
                if(c == null){
                    continue;
                }
                if(state.getLine(0).equals("§8[§aConsulShop§8]") && c.equals(chest.getState())){
                    return state;
                }
            }
        }
        return null;
    }
    
    public Location getLocation(){
        return new Location(ConsulatCore.getInstance().getOverworld(), getX(), getY(), getZ());
    }
    
    public Material getItemType(){
        return forSale.getType();
    }
    
    public double getPrice(){
        return price;
    }
    
    public UUID getOwner(){
        return owner;
    }
    
    public Inventory getInventory(){
        Location location = getLocation();
        return ((Chest)location.getWorld().getBlockAt(location).getState()).getBlockInventory();
    }
    
    public ItemFrame getItemFrame(){
        return getItemFrame(getLocation());
    }
    
    public boolean isEmpty(){
        return amount == 0;
    }
    
    public List<ShopItemType> getTypes(){
        return Collections.unmodifiableList(Arrays.asList(types));
    }
    
    public String getOwnerName(){
        return ownerName;
    }
    
    public ItemStack getItem(){
        return forSale;
    }
    
    private int setAmountLegacy(){
        int result = 0;
        for(ItemStack item : getInventory()){
            if(item != null){
                result += item.getAmount();
            }
        }
        return result;
    }
    
    private int setAmount(){
        int result = 0;
        for(ItemStack item : getInventory()){
            if(item != null && isItemAccepted(item)){
                result += item.getAmount();
            }
        }
        return result;
    }
    
    public static ItemFrame getItemFrame(Location location){
        Collection<Entity> entities = location.clone().add(0.5, 1.5, 0.5).getNearbyEntities(0.5, 0.5, 0.5);
        for(Entity entity : entities){
            if(entity.getType() == EntityType.ITEM_FRAME){
                ItemFrame itemFrame = (ItemFrame)entity;
                if(itemFrame.isInvulnerable()){
                    return itemFrame;
                }
            }
        }
        return null;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        PlayerShop shop = (PlayerShop)o;
        return coords == shop.coords;
    }
    
    @Override
    public String toString(){
        return "Shop{" +
                "coords=" + getLocation() +
                ", owner=" + owner +
                ", ownerName='" + ownerName + '\'' +
                ", forSale=" + forSale +
                ", price=" + price +
                ", amount=" + amount +
                ", open=" + open +
                '}';
    }
}
