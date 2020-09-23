package fr.leconsulat.core.guis.moderation;

import fr.leconsulat.api.gui.GuiHeadItem;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.template.PagedGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.Text;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Iterator;

public class XRayHelperGui extends PagedGui {
    
    public XRayHelperGui(){
        super("Joueurs < 16", 6,
                IGui.getItem("§eJoueurs < 16", 4, Material.PAPER, "§7Les joueur en dessous", "§7de la couche 16", "§7apparaissent ici"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setDynamicItems(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    public void addPlayer(ConsulatPlayer player){
        GuiItem item = new GuiHeadItem(player.getUUID(), this).
                setDisplayName("§e" + player.getName()).
                setDescription(getDescription(player));
        item.setAttachedObject(player);
        addItem(item);
    }
    
    public void removePlayer(ConsulatPlayer player){
        for(Iterator<GuiItem> iterator = this.iterator(); iterator.hasNext(); ){
            if(player.equals(iterator.next().getAttachedObject())){
                iterator.remove();
                break;
            }
        }
    }
    
    @Override
    public void onPageOpened(GuiOpenEvent event, Pageable pageGui){
        if(pageGui.getPage() == 0){
            update();
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
        if(page.getPage() != 0){
            IGui gui = page.getGui();
            gui.setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page.getPage() - 1).getGui().setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
            gui.setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        GuiItem clickedItem = page.getGui().getItem(event.getSlot());
        switch(event.getSlot()){
            case 47:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(event.getPlayer());
                }
                break;
            case 51:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(event.getPlayer());
                }
                break;
        }
        if(event.getSlot() >= 10 && event.getSlot() <= 43 && clickedItem.getType() == Material.PLAYER_HEAD){
            ConsulatPlayer player = event.getPlayer();
            ConsulatPlayer target = (ConsulatPlayer)clickedItem.getAttachedObject();
            player.getPlayer().teleportAsync(target.getPlayer().getLocation());
            player.sendMessage(Text.YOU_TELEPORTED_TO(target.getName()));
        }
    }
    
    private void update(){
        for(GuiItem item : this){
            setDescription(item.getSlot(), getDescription((ConsulatPlayer)item.getAttachedObject()));
        }
    }
    
    private String[] getDescription(ConsulatPlayer player){
        return new String[]{"", "§7Se téléporter", "", "§bDiamants: §c" + getDiamonds(player.getPlayer())};
    }
    
    private int getDiamonds(Player player){
        int diamonds = 0;
        for(ItemStack item : player.getInventory()){
            if(item == null){
                continue;
            }
            if(item.getType() == Material.SHULKER_BOX){
                for(ItemStack shulkerItem : ((ShulkerBox)((BlockStateMeta)item.getItemMeta()).getBlockState()).getSnapshotInventory()){
                    if(shulkerItem != null){
                        diamonds += getDiamondValue(shulkerItem.getType()) * shulkerItem.getAmount();
                    }
                }
            }
            diamonds += getDiamondValue(item.getType()) * item.getAmount();
        }
        return diamonds;
    }
    
    private int getDiamondValue(Material material){
        switch(material){
            case DIAMOND_ORE:
            case DIAMOND:
                return 1;
            case DIAMOND_BLOCK:
                return 9;
        }
        return 0;
    }
}
