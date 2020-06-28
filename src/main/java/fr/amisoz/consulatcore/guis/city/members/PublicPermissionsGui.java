package fr.amisoz.consulatcore.guis.city.members;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Material;

public class PublicPermissionsGui extends DataRelatGui<City> {
    
    private static final byte DOOR_SLOT = 22;
    
    public PublicPermissionsGui(City city){
        super(city, "Publiques", 6,
                IGui.getItem("§7Interaction", DOOR_SLOT, Material.OAK_DOOR, "§7Interagir avec", "§7- Portes", "§7- Trappes", "§7- Portillon"),
                IGui.getItem("§cDésactivé", DOOR_SLOT + 9, Material.RED_CONCRETE));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    }
    
    @Override
    public void onCreate(){
        for(ClaimPermission permission : ClaimPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                return;
            }
            if(getData().hasPublicPermission(permission)){
                setGlowing(slot, true);
                setType(slot + 9, Material.GREEN_CONCRETE);
                setDisplayName(slot + 9, "§aActivé");
            } else {
                setGlowing(slot, false);
                setType(slot + 9, Material.RED_CONCRETE);
                setDisplayName(slot + 9, "§cDésactivé");
            }
        }
    }
    
    private byte getSlotPermission(ClaimPermission permission){
        switch(permission){
            case INTERACT_DOOR:
                return DOOR_SLOT;
        }
        return -1;
    }
    
    private void switchPermission(ClaimPermission permission){
        setPermission(!getData().hasPublicPermission(permission), permission);
    }
    
    private void setPermission(boolean activate, ClaimPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            getData().addPublicPermission(permission);
            setGlowing(slot, true);
            setType(slot + 9, Material.GREEN_CONCRETE);
            setDisplayName(slot + 9, "§aActivé");
        } else {
            getData().removePublicPermission(permission);
            setGlowing(slot, false);
            setType(slot + 9, Material.RED_CONCRETE);
            setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        City city = getData();
        switch(event.getSlot()){
            case DOOR_SLOT:
            case DOOR_SLOT + 9:
                switchPermission(ClaimPermission.INTERACT_DOOR);
                break;
        }
    }
}
