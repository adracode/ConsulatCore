package fr.amisoz.consulatcore.shop.admin;

import fr.amisoz.consulatcore.guis.shop.admin.AdminShopGui;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.leconsulat.api.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AdminShop implements Shop, Comparable<Shop> {
    
    private final long coords;
    private double price;
    private ItemStack item;
    
    public AdminShop(long coords){
        this(coords, 0);
    }
    
    public AdminShop(long coords, double price){
        this.coords = coords;
        this.price = price;
        this.item = ((Chest)Bukkit.getWorlds().get(0).getBlockAt(getX(), getY(), getZ()).getState()).getBlockInventory().getItem(0);
    }
    
    @Override
    public long getCoords(){
        return coords;
    }
    
    @Override
    public int getX(){
        return CoordinatesUtils.getX(coords);
    }
    
    @Override
    public int getY(){
        return CoordinatesUtils.getY(coords);
    }
    
    @Override
    public int getZ(){
        return CoordinatesUtils.getZ(coords);
    }
    
    public abstract void createGui();
    
    public abstract @NotNull AdminShopGui getGui();
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof AdminShop)){
            return false;
        }
        AdminShop adminShop = (AdminShop)o;
        return coords == adminShop.coords;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    @Override
    public int compareTo(@NotNull Shop o){
        return Long.compare(coords, o.getCoords());
    }
    
    @Override
    public String toString(){
        return "AdminShop{" +
                "coords=" + coords +
                ", price=" + price +
                ", item=" + item +
                '}';
    }
    
    public CompoundTag saveNBT(){
        CompoundTag shopTag = new CompoundTag();
        shopTag.putLong("Coords", coords);
        shopTag.putDouble("Price", price);
        shopTag.putString("Type", getType());
        return shopTag;
    }
    
    public void loadNBT(CompoundTag tag){
        this.price = tag.getDouble("Price");
    }
    
    public ItemStack getItem(){
        return item;
    }
    
    public double getPrice(){
        return price;
    }
}
