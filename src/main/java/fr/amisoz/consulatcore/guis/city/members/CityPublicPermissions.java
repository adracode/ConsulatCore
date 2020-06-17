package fr.amisoz.consulatcore.guis.city.members;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Material;

public class CityPublicPermissions extends GuiListener<City> {
    
    private static final byte DOOR_SLOT = 22;
    
    public CityPublicPermissions(){
        super(6);
        GuiItem deactivate = getItem("§cDésactivé", -1, Material.RED_CONCRETE);
        setTemplate("Publiques",
                getItem("§7Interaction", DOOR_SLOT, Material.OAK_DOOR, "§7Interagir avec", "§7- Portes", "§7- Trappes", "§7- Portillon"),
                getItem(deactivate, DOOR_SLOT + 9))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setCreateOnOpen(true);
    }
    
    private byte getSlotPermission(ClaimPermission permission){
        switch(permission){
            case INTERACT_DOOR:
                return DOOR_SLOT;
        }
        return -1;
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        PagedGui<City> gui = event.getPagedGui();
        City city = event.getData();
        for(ClaimPermission permission : ClaimPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                return;
            }
            if(city.hasPermission(permission)){
                gui.setGlowing(slot, true);
                gui.setType(slot + 9, Material.GREEN_CONCRETE);
                gui.setDisplayName(slot + 9, "§aActivé");
            } else {
                gui.setGlowing(slot, false);
                gui.setType(slot + 9, Material.RED_CONCRETE);
                gui.setDisplayName(slot + 9, "§cDésactivé");
            }
        }
    }
    
    private void switchPermission(City city, PagedGui<City> gui, ClaimPermission permission){
        setPermission(city, gui, !city.hasPermission(permission), permission);
    }
    
    private void setPermission(City city, PagedGui<City> gui, boolean activate, ClaimPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            city.addPermission(permission);
            gui.setGlowing(slot, true);
            gui.setType(slot + 9, Material.GREEN_CONCRETE);
            gui.setDisplayName(slot + 9, "§aActivé");
        } else {
            city.removePermission(permission);
            gui.setGlowing(slot, false);
            gui.setType(slot + 9, Material.RED_CONCRETE);
            gui.setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        City city = event.getData();
        switch(event.getSlot()){
            case DOOR_SLOT:
            case DOOR_SLOT + 9:
                switchPermission(city, event.getPagedGui(), ClaimPermission.INTERACT_DOOR);
                break;
        }
    }
}
