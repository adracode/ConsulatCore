package fr.amisoz.consulatcore.guis.city.claimlist.claims;

import fr.amisoz.consulatcore.guis.city.claimlist.claims.permissions.AccessPermissionsGui;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Iterator;
import java.util.UUID;

public class ManageClaimGui extends GuiListener<Claim> {
    
    private static final byte UNCLAIM_SLOT = 7;
    
    private AccessPermissionsGui accessPermissionsGui = new AccessPermissionsGui();
    
    public ManageClaimGui(){
        super(6);
        setTemplate("<position>",
                getItem("§cUnclaim", UNCLAIM_SLOT, Material.DIAMOND_SHOVEL)
        ).setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setMoveableItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<Claim> event){
        Claim claim = event.getData();
        for(UUID uuid : claim.getPlayers()){
            addPlayerToClaim(event.getGui(), uuid, Bukkit.getOfflinePlayer(uuid).getName());
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<Claim> event){
        int page = event.getPage();
        Claim claim = event.getData();
        event.getPagedGui().setName("§2" + (claim.getX() << 4) + " " + (claim.getZ() << 4));
        if(page != 0){
            event.getPagedGui().setItem(getItem("§7Précédent", 45, Material.ARROW));
            event.getGui().getPage(page - 1).setItem(getItem("§7Suivant", 53, Material.ARROW));
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<Claim> event){
        ConsulatPlayer player = event.getPlayer();
        switch(event.getSlot()){
            case UNCLAIM_SLOT:
                Claim claim = event.getData();
                ClaimManager.getInstance().unClaim(claim);
                break;
            case 45:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().open(player, event.getPage() - 1);
                }
                break;
            case 53:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().open(player, event.getPage() + 1);
                }
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && event.getClickedItem().getType() == Material.PLAYER_HEAD){
            event.getGui().getChild(event.getClickedItem().getAttachedObject()).open(player);
        }
    }
    
    public void addPlayerToClaim(Gui<Claim> gui, UUID uuid, String name){
        if(gui == null){
            return;
        }
        GuiItem item = getItem("§e" + name, -1, uuid);
        gui.addItem(item);
        item.setAttachedObject(uuid);
        gui.prepareChild(uuid, () -> accessPermissionsGui.createGui(uuid, gui));
    }
    
    public void removePlayerFromClaim(Gui<Claim> gui, UUID uuid){
        if(gui == null){
            return;
        }
        gui.removeChild(uuid);
        for(Iterator<GuiItem> iterator = gui.iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
}
