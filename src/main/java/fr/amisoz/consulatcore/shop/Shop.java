package fr.amisoz.consulatcore.shop;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class Shop {
    
    private static final int SHIFT = 25; //Max coordonnées MC
    private static final int SHIFT_Y = 8; //Max y MC
    private static final int LIMIT_X = 1 << SHIFT; //33 554 432 > 30 000 000
    private static final int LIMIT_Y = 1 << SHIFT_Y; //256 > 255
    private static final int LIMIT_Z = 1 << SHIFT; //33 554 432 > 30 000 000
    private static final long CONVERT_Y = ((long)1 << SHIFT + SHIFT_Y + 1) - 1;
    private static final int CONVERT_X = (1 << SHIFT + 1) - 1;
    
    private static BlockFace[] chestFaces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    
    private long coords;
    private UUID owner;
    private String ownerName;
    private ItemStack forSale;
    private double price;
    private int amount;
    private boolean open = false;
    
    
    public Shop(UUID owner, String ownerName, ItemStack forSale, double price, Location location, boolean legacy){
        this.owner = owner;
        this.ownerName = ownerName;
        this.forSale = new ItemStack(forSale);
        this.forSale.setAmount(1);
        this.price = price;
        setCoords(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.amount = legacy ? setAmountLegacy() : setAmount();
        buy(0);
    }
    
    public boolean isOpen(){
        return open;
    }
    
    public void setOpen(boolean open){
        this.open = open;
        if(!open){
            amount = setAmount();
            buy(0);
        }
    }
    
    public boolean isItemAccepted(ItemStack item){
        if(item == null){
            return false;
        }
        return item.getType() == forSale.getType() && item.getItemMeta().equals(forSale.getItemMeta());
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
    
    public int getAmount(){
        return amount;
    }
    
    public Sign getSign(){
        Block chest = getLocation().getBlock();
        for(BlockFace face : chestFaces){
            Block sign = chest.getRelative(face);
            if(sign.getType() == Material.OAK_WALL_SIGN){
                Sign state = (Sign)sign.getState();
                if(state.getLine(0).equals("§8[§aConsulShop§8]") && ShopManager.getInstance().getChestFromSign(sign).equals(chest.getState())){
                    return state;
                }
            }
        }
        return null;
    }
    
    private void setCoords(int x, int y, int z){
        if(x < -LIMIT_X || x > LIMIT_X || y < 0 || y > LIMIT_Y || z < -LIMIT_Z || z > LIMIT_Z){
            throw new IllegalArgumentException("Les coordonnées d'un shop ne peuvent dépasse les limites");
        }
        this.coords = convertCoordinates(x, y, z);
    }
    
    public long getCoords(){
        return coords;
    }
    
    public int getX(){
        return (int)((coords & CONVERT_X) - LIMIT_X);
    }
    
    public int getY(){
        return (int)((coords & CONVERT_Y) >> SHIFT + 1);
    }
    
    public int getZ(){
        return (int)((coords >> SHIFT + 1 + SHIFT_Y + 1) - LIMIT_Z);
    }
    
    public Location getLocation(){
        return new Location(Bukkit.getWorlds().get(0), getX(), getY(), getZ());
    }
    
    public static long convertCoordinates(Location location){
        return convertCoordinates(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    public static long convertCoordinates(int x, int y, int z){
        return (((long)z + LIMIT_Z) << SHIFT + 1 + SHIFT_Y + 1) | ((long)y << SHIFT + 1) | (x + LIMIT_X);
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
    
    public ItemFrame getItemFrame(){
        return getItemFrame(getLocation());
    }
    
    public void destroyFrame(){
        getItemFrame().remove();
    }
    
    public void buy(int amount){
        if(isEmpty()){
            return;
        }
        this.amount -= amount;
        Inventory inventory = getInventory();
        inventory.clear();
        int count = 0, index = 0, stack = forSale.getMaxStackSize();
        while(count <= this.amount && index < 27){
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
    }
    
    public boolean isEmpty(){
        return amount == 0;
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Shop shop = (Shop)o;
        return coords == shop.coords;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    public String getOwnerName(){
        return ownerName;
    }
    
    @Override
    public String toString(){
        return "Shop{" +
                "coords=" + getLocation() +
                ", owner=" + owner +
                ", ownerName='" + ownerName + '\'' +
                ", forSale=" + forSale +
                ", price=" + price +
                '}';
    }
    
    public ItemStack getItem(){
        return forSale;
    }
    
    
    public boolean isOwner(UUID uuid){
        return owner.equals(uuid);
    }
}