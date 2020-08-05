package fr.amisoz.consulatcore.guis.shop.admin;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.gui.template.DataGui;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public abstract class AdminShopGui extends DataGui<AdminShop> {
    
    protected static final byte INFO_SLOT = 13;
    protected static final byte ITEM_1_SLOT = 37;
    protected static final byte ITEM_16_SLOT = 39;
    protected static final byte ITEM_64_SLOT = 41;
    protected static final byte ITEM_ALL_SLOT = 43;
    
    public AdminShopGui(AdminShop data, String title){
        super(data, title, 6);
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onCreate(){
        GuiItem item = new GuiItem(getData().getItem(), INFO_SLOT);
        item.setDescription(GuiItem.getDescription(getData().getItem(),"", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice())));
        item.removeItemFlags(ItemFlag.values());
        setItem(item);
    }
}
