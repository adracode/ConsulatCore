package fr.leconsulat.core.guis.city.members;

import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.claims.ClaimPermission;
import org.bukkit.Material;

public class PublicPermissionsGui extends DataRelatGui<City> {
    
    private static final byte DOOR_SLOT = 21;
    private static final byte REDSTONE_SLOT = 23;
    
    public PublicPermissionsGui(City city){
        super(city, "Publiques", 6,
                IGui.getItem("§eInteraction", DOOR_SLOT, Material.OAK_DOOR, "§7Interagir avec", "§7- Portes", "§7- Trappes", "§7- Portillon"),
                IGui.getItem("§cDésactivé", DOOR_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eRedstone", REDSTONE_SLOT, Material.REDSTONE, "§7Utiliser la redstone", "§7- Leviers", "§7- Boutons", "§7- Plaques de pressions", "§7- Fils tendues"),
                IGui.getItem("§cDésactivé", REDSTONE_SLOT + 9, Material.RED_CONCRETE));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    }
    
    @Override
    public void onCreate(){
        for(ClaimPermission permission : ClaimPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
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
    
    @Override
    public void onOpened(GuiOpenEvent event){
        for(ClaimPermission permission : ClaimPermission.values()){
            int slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            update(event.getPlayer(), canSetPermission(event.getPlayer()), slot + 9);
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        if(!canSetPermission(event.getPlayer())){
            return;
        }
        City city = getData();
        switch(event.getSlot()){
            case DOOR_SLOT:
            case DOOR_SLOT + 9:
                city.switchPermission(ClaimPermission.INTERACT_DOOR);
                break;
            case REDSTONE_SLOT:
            case REDSTONE_SLOT + 9:
                city.switchPermission(ClaimPermission.INTERACT_REDSTONE);
                break;
        }
    }
    
    public void update(ConsulatPlayer player, boolean allow, int slot){
        if(allow){
            setFakeItem(slot, null, player);
        } else {
            setDescriptionPlayer(slot, player, "", "§cTu ne peux pas", "§cmodifier cette permission");
        }
    }
    
    public void setPermission(boolean activate, ClaimPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            setGlowing(slot, true);
            setType(slot + 9, Material.GREEN_CONCRETE);
            setDisplayName(slot + 9, "§aActivé");
        } else {
            setGlowing(slot, false);
            setType(slot + 9, Material.RED_CONCRETE);
            setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    private boolean canSetPermission(ConsulatPlayer player){
        return getData().isOwner(player.getUUID());
    }
    
    private byte getSlotPermission(ClaimPermission permission){
        switch(permission){
            case INTERACT_DOOR:
                return DOOR_SLOT;
            case INTERACT_REDSTONE:
                return REDSTONE_SLOT;
        }
        return -1;
    }
}
