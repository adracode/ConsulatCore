package fr.amisoz.consulatcore.guis.city.claimlist;

import fr.amisoz.consulatcore.guis.city.claimlist.claims.ManageClaimGui;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiRemoveEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Material;

import java.util.Iterator;

//TODO: enlever les flèches lors de la suppression
public class ListCityClaimsGui extends GuiListener<City> {
    
    private static final byte PREVIOUS = 47;
    private static final byte NEXT = 51;
    
    private ManageClaimGui listAccessGui;
    
    public ListCityClaimsGui(){
        super(6);
        setTemplate("Claims",
                getItem("§eClaims", 4, Material.FILLED_MAP,
                        "§7Vous pouvez gérer les claims", "§7de votre ville.")
        ).setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
        ).setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8);
        setMoveableItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        listAccessGui = ZoneManager.getInstance().getManageClaimGui();
    }
    
    @Override
    public void onCreate(GuiCreateEvent<City> event){
        City city = event.getData();
        for(Claim claim : city.getZoneClaims()){
            addItemClaim(event.getGui(), claim);
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        int page = event.getPage();
        if(page != 0){
            event.getPagedGui().setItem(getItem("§7Précédent", PREVIOUS, Material.ARROW));
            event.getGui().getPage(page - 1).setItem(getItem("§7Suivant", NEXT, Material.ARROW));
        }
    }
    
    @Override
    public void onRemove(PagedGuiRemoveEvent<City> event){
        System.out.println(event.getPage());
        int page = event.getPage();
        if(page != 0){
            event.getGui().getPage(page - 1).setDeco(Material.BLACK_STAINED_GLASS_PANE, NEXT);
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        ConsulatPlayer player = event.getPlayer();
        switch(event.getSlot()){
            case PREVIOUS:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().open(player, event.getPage() - 1);
                }
                break;
            case NEXT:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().open(player, event.getPage() + 1);
                }
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && event.getClickedItem().getType() == Material.GRASS_BLOCK){
            ((Claim)event.getClickedItem().getAttachedObject()).openManageClaim(player);
        }
    }
    
    public void addItemClaim(Gui<City> gui, Claim claim){
        //Si null, alors il n'a été crée et le claim sera ajouté à la création
        if(gui == null){
            return;
        }
        //Création de l'item
        GuiItem item = getItem("§e" + (claim.getX() << 4) + " " + (claim.getZ() << 4), -1, Material.GRASS_BLOCK);
        //Le claim est placé sur l'item
        item.setAttachedObject(claim);
        //L'item est ajouté au PagedGui répertoriant les claims
        gui.addItem(item);
        gui.prepareChild(claim, () -> listAccessGui.createGui(claim, gui));
    }
    
    public void removeItemClaim(Gui<City> gui, Claim claim){
        //PagedGui pour gérer les accès
        if(gui == null){
            return;
        }
        gui.removeChild(claim);
        for(Iterator<GuiItem> iterator = gui.iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(claim)){
                iterator.remove();
                return;
            }
        }
    }
    
}
