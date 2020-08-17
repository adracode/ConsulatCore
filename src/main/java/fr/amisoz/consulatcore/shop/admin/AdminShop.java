package fr.amisoz.consulatcore.shop.admin;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.guis.shop.admin.AdminShopGui;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.leconsulat.api.nbt.CompoundTag;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
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
        try {
            this.item = ((Chest)ConsulatCore.getInstance().getOverworld().getBlockAt(getX(), getY(), getZ()).getState()).getBlockInventory().getItem(0);
        } catch(ClassCastException e){
            ShopManager.getInstance().removeAdminShop(this);
        }
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
    
    public abstract void createGui();
    
    public abstract @NotNull AdminShopGui getGui();
    
    public ItemStack getItem(){
        return item;
    }
    
    protected void setItem(ItemStack item){
        Inventory chest = ((Chest)ConsulatCore.getInstance().getOverworld().getBlockAt(getX(), getY(), getZ()).getState()).getBlockInventory();
        chest.clear();
        chest.addItem(item);
        this.item = item;
    }
    
    public double getPrice(){
        return price;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
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
    public String toString(){
        return "AdminShop{" +
                "coords=" + coords +
                ", price=" + price +
                ", item=" + item +
                '}';
    }
    
    @Override
    public int compareTo(@NotNull Shop o){
        return Long.compare(coords, o.getCoords());
    }
}
