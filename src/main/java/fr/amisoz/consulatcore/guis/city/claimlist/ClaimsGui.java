package fr.amisoz.consulatcore.guis.city.claimlist;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiRemoveEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatPagedGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

public class ClaimsGui extends DataRelatPagedGui<City> {
    
    private static final byte PREVIOUS = 47;
    private static final byte NEXT = 51;
    
    public ClaimsGui(City city){
        super(city, "Claims", 6,
                IGui.getItem("§eClaims", 4, Material.FILLED_MAP,
                        "", "§7Gérer les claims", "§7de la ville."));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onCreate(){
        for(Claim claim : getData().getZoneClaims()){
            addItemClaim(claim);
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
        if(page.getPage() != 0){
            IGui gui = page.getGui();
            gui.setItem(IGui.getItem("§7Précédent", PREVIOUS, Material.ARROW));
            getPage(page.getPage() - 1).getGui().setItem(IGui.getItem("§7Suivant", NEXT, Material.ARROW));
            gui.setDeco(Material.BLACK_STAINED_GLASS_PANE, NEXT);
        }
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable page){
        if(page.getPage() != 0){
            getPage(page.getPage() - 1).getGui().setDeco(Material.BLACK_STAINED_GLASS_PANE, NEXT);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        ConsulatPlayer player = event.getPlayer();
        GuiItem clickedItem = Objects.requireNonNull(page.getGui().getItem(event.getSlot()));
        switch(event.getSlot()){
            case PREVIOUS:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(player);
                }
                break;
            case NEXT:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(player);
                }
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && clickedItem.getType() == Material.GRASS_BLOCK){
            GuiManager.getInstance().getContainer("claim").getGui(clickedItem.getAttachedObject()).open(player);
        }
    }
    
    public void addItemClaim(Claim claim){
        //Création de l'item
        GuiItem item = IGui.getItem("§e" + (claim.getX() << 4) + " " + (claim.getZ() << 4), -1, Material.GRASS_BLOCK);
        //Le claim est placé sur l'item
        item.setAttachedObject(claim);
        //L'item est ajouté au PagedGui répertoriant les claims
        System.out.println("additem");
        addItem(item);
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof Claim){
            return (Relationnable)GuiManager.getInstance().getContainer("claim").getGui(key);
        }
        return super.createChild(key);
    }
    
    public void  removeItemClaim(Claim claim){
        removeChild(claim);
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(claim)){
                iterator.remove();
                return;
            }
        }
    }
    
}
