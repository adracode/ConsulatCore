package fr.amisoz.consulatcore.guis.city.members.member.claims;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.AliasPagedGui;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiRemoveEvent;
import org.bukkit.Material;

import java.util.Iterator;
import java.util.UUID;

public class AccessibleClaimGui extends GuiListener<UUID> {
    
    private static final byte PREVIOUS = 47;
    private static final byte NEXT = 51;
    private static final byte GIVE_ALL_SLOT = 3;
    private static final byte REMOVE_ALL_SLOT = 5;
    
    public AccessibleClaimGui(){
        super(6);
        setTemplate("Claims accessible",
                getItem("§eAccès", GIVE_ALL_SLOT, Material.END_CRYSTAL, "§7Donner l'accès à tous les claims"),
                getItem("§eAccès", REMOVE_ALL_SLOT, Material.BARRIER, "§7Retirer l'accès de tous les claims"))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setMoveableItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<UUID> event){
        City city = getPlayerCity(event.getGui());
        for(Claim claim : city.getZoneClaims()){
            if(claim.hasAccess(event.getData())){
                addItemClaim(event.getGui(), claim);
            }
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<UUID> event){
        int page = event.getPage();
        if(page != 0){
            event.getPagedGui().setItem(getItem("§7Précédent", PREVIOUS, Material.ARROW));
            event.getGui().getPage(page - 1).setItem(getItem("§7Suivant", NEXT, Material.ARROW));
        }
    }
    
    @Override
    public void onRemove(PagedGuiRemoveEvent<UUID> event){
        int page = event.getPage();
        if(page != 0){
            event.getGui().getPage(page - 1).setDeco(Material.BLACK_STAINED_GLASS_PANE, NEXT);
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<UUID> event){
        City city = getPlayerCity(event.getGui());
        switch(event.getSlot()){
            case PREVIOUS:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().getPage(event.getPage() - 1).open(event.getPlayer());
                }
                break;
            case NEXT:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().getPage(event.getPage() + 1).open(event.getPlayer());
                }
                break;
            case GIVE_ALL_SLOT:
                city.addAccess(event.getData());
                return;
            case REMOVE_ALL_SLOT:
                city.removeAccess(event.getData());
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 43 && event.getClickedItem().getType() == Material.GRASS_BLOCK){
            Claim claim = (Claim)event.getClickedItem().getAttachedObject();
            Gui<Claim> manageClaim = claim.getGui(true);
            //Je n'ai pas trouvé de meilleure solution pour l'instant (que de faire un new)
            //et je préfère d'abord faire le reste au lieu de m'y attarder
            new AliasPagedGui<>(manageClaim.getChild(event.getData()).getPage(), event.getGui(), (claim.getX() << 4) + " " + (claim.getZ() << 4)).open(event.getPlayer());
        }
    }
    
    public void addItemClaim(Gui<UUID> gui, Claim claim){
        if(gui == null){
            return;
        }
        GuiItem item = getItem("§e" + (claim.getX() << 4) + " " + (claim.getZ() << 4), -1, Material.GRASS_BLOCK);
        item.setAttachedObject(claim);
        gui.addItem(item);
    }
    
    public void removeItemClaim(Gui<UUID> gui, Claim claim){
        if(gui == null){
            return;
        }
        for(Iterator<GuiItem> iterator = gui.iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(claim)){
                iterator.remove();
                return;
            }
        }
    }
    
    private City getPlayerCity(Gui<UUID> current){
        return (City)current.getFather().getFather().getData();
    }
    
}
