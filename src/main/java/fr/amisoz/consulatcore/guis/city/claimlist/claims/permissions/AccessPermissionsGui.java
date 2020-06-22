package fr.amisoz.consulatcore.guis.city.claimlist.claims.permissions;

import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.UUID;

public class AccessPermissionsGui extends GuiListener<UUID> {
    
    private static final byte INTERACT_DOOR_SLOT = 29;
    private static final byte BREAK_SLOT = 30;
    private static final byte PLACE_SLOT = 31;
    private static final byte CONTAINER_SLOT = 32;
    private static final byte REDSTONE_SLOT = 33;
    private static final byte GIVE_ALL_SLOT = 1;
    private static final byte REMOVE_ALL_SLOT = 2;
    private static final byte KICK_SLOT = 7;
    
    public AccessPermissionsGui(){
        super(6);
        GuiItem deactivate = new GuiItem("§cDésactivé", (byte)-1, Material.RED_CONCRETE);
        setTemplate("<joueur>",
                getItem("§eTout activer", GIVE_ALL_SLOT, Material.TOTEM_OF_UNDYING),
                getItem("§eTout désactiver", REMOVE_ALL_SLOT, Material.BARRIER),
                getItem("§cRévoquer l'accès", KICK_SLOT, Material.TRIDENT),
                getItem("§eInteraction", INTERACT_DOOR_SLOT, Material.OAK_DOOR, "§7Interagir avec", "§7- Portes", "§7- Trappes", "§7- Portillon"),
                getItem(deactivate, INTERACT_DOOR_SLOT + 9),
                getItem("§eDétruire", BREAK_SLOT, Material.DIAMOND_PICKAXE, "§7Détruire des blocs"),
                getItem(deactivate, BREAK_SLOT + 9),
                getItem("§ePlacer", PLACE_SLOT, Material.BRICK, "§7Placer des blocs"),
                getItem(deactivate, PLACE_SLOT + 9),
                getItem("§eRedstone", REDSTONE_SLOT, Material.REDSTONE, "§7Utiliser la redstone", "§7- Leviers", "§7- Boutons", "§7- Plaques de pressions", "§7- Fils tendues"),
                getItem(deactivate, REDSTONE_SLOT + 9),
                getItem("§eAccès", CONTAINER_SLOT, Material.CHEST, "§7Utiliser les",
                        "§7- Coffres, barrils, shulkers",
                        "§7- Fours, haut fourneaux, fumoirs",
                        "§7- Alambics",
                        "§7- Distributeurs, droppers",
                        "§7- Entonnoirs"),
                getItem(deactivate, CONTAINER_SLOT + 9))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setCreateOnOpen(true);
    }
    
    private byte getSlotPermission(ClaimPermission permission){
        switch(permission){
            case INTERACT_DOOR:
                return INTERACT_DOOR_SLOT;
            case BREAK_BLOCK:
                return BREAK_SLOT;
            case PLACE_BLOCK:
                return PLACE_SLOT;
            case OPEN_CONTAINER:
                return CONTAINER_SLOT;
            case INTERACT_REDSTONE:
                return REDSTONE_SLOT;
        }
        return -1;
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<UUID> event){
        PagedGui<UUID> gui = event.getPagedGui();
        UUID uuid = event.getData();
        gui.setName(Bukkit.getOfflinePlayer(uuid).getName());
        Claim claim = getClaim(event.getGui());
        for(ClaimPermission permission : ClaimPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            if(claim.hasPermission(uuid, permission)){
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
    
    private void switchPermission(Claim claim, UUID uuid, PagedGui<UUID> gui, ClaimPermission permission){
        setPermission(claim, uuid, gui, !claim.hasPermission(uuid, permission), permission);
    }
    
    private void setPermission(Claim claim, UUID uuid, PagedGui<UUID> gui, boolean activate, ClaimPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            claim.addPermission(uuid, permission);
            gui.setGlowing(slot, true);
            gui.setType(slot + 9, Material.GREEN_CONCRETE);
            gui.setDisplayName(slot + 9, "§aActivé");
        } else {
            claim.removePermission(uuid, permission);
            gui.setGlowing(slot, false);
            gui.setType(slot + 9, Material.RED_CONCRETE);
            gui.setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<UUID> event){
        Claim claim = getClaim(event.getGui());
        switch(event.getSlot()){
            case GIVE_ALL_SLOT:
                for(ClaimPermission permission : ClaimPermission.values()){
                    setPermission(claim, event.getData(), event.getPagedGui(), true, permission);
                }
                break;
            case REMOVE_ALL_SLOT:
                for(ClaimPermission permission : ClaimPermission.values()){
                    setPermission(claim, event.getData(), event.getPagedGui(), false, permission);
                }
                break;
            case KICK_SLOT:
                claim.removePlayer(event.getData());
                event.getPlayer().getPlayer().closeInventory();
                break;
            case INTERACT_DOOR_SLOT:
            case INTERACT_DOOR_SLOT + 9:
                switchPermission(claim, event.getData(), event.getPagedGui(), ClaimPermission.INTERACT_DOOR);
                break;
            case BREAK_SLOT:
            case BREAK_SLOT + 9:
                switchPermission(claim, event.getData(), event.getPagedGui(), ClaimPermission.BREAK_BLOCK);
                break;
            case PLACE_SLOT:
            case PLACE_SLOT + 9:
                switchPermission(claim, event.getData(), event.getPagedGui(), ClaimPermission.PLACE_BLOCK);
                break;
            case CONTAINER_SLOT:
            case CONTAINER_SLOT + 9:
                switchPermission(claim, event.getData(), event.getPagedGui(), ClaimPermission.OPEN_CONTAINER);
                break;
            case REDSTONE_SLOT:
            case REDSTONE_SLOT + 9:
                switchPermission(claim, event.getData(), event.getPagedGui(), ClaimPermission.INTERACT_REDSTONE);
                break;
        }
    }
    
    private Claim getClaim(Gui<UUID> current){
        return (Claim)current.getFather().getData();
        
    }
    
}
