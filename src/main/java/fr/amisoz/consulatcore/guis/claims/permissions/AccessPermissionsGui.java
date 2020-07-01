package fr.amisoz.consulatcore.guis.claims.permissions;

import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AccessPermissionsGui extends DataRelatGui<UUID> {
    
    private static final byte INTERACT_DOOR_SLOT = 29;
    private static final byte BREAK_SLOT = 30;
    private static final byte PLACE_SLOT = 31;
    private static final byte CONTAINER_SLOT = 32;
    private static final byte REDSTONE_SLOT = 33;
    private static final byte GIVE_ALL_SLOT = 1;
    private static final byte REMOVE_ALL_SLOT = 2;
    private static final byte KICK_SLOT = 7;
    
    private IGui link;
    
    public IGui getLink(){
        return link;
    }
    
    public void setLink(IGui link){
        this.link = link;
    }
    
    @Override
    public @NotNull IGui setItem(@NotNull GuiItem item){
        if(link != null){
            link.setItem(item);
        }
        return super.setItem(item);
    }
    
    @Override
    public @NotNull IGui setItem(int slot, @Nullable GuiItem item){
        if(link != null){
            link.setItem(slot, item);
        }
        return super.setItem(slot, item);
    }
    
    public AccessPermissionsGui(UUID uuid){
        super(uuid, Bukkit.getOfflinePlayer(uuid).getName(), 6,
                IGui.getItem("§eTout activer", GIVE_ALL_SLOT, Material.TOTEM_OF_UNDYING),
                IGui.getItem("§eTout désactiver", REMOVE_ALL_SLOT, Material.BARRIER),
                IGui.getItem("§cRévoquer l'accès", KICK_SLOT, Material.TRIDENT),
                IGui.getItem("§eInteraction", INTERACT_DOOR_SLOT, Material.OAK_DOOR, "§7Interagir avec", "§7- Portes", "§7- Trappes", "§7- Portillon"),
                IGui.getItem("§cDésactivé", INTERACT_DOOR_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eDétruire", BREAK_SLOT, Material.DIAMOND_PICKAXE, "§7Détruire des blocs"),
                IGui.getItem("§cDésactivé", BREAK_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§ePlacer", PLACE_SLOT, Material.BRICK, "§7Placer des blocs"),
                IGui.getItem("§cDésactivé", PLACE_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eRedstone", REDSTONE_SLOT, Material.REDSTONE, "§7Utiliser la redstone", "§7- Leviers", "§7- Boutons", "§7- Plaques de pressions", "§7- Fils tendues"),
                IGui.getItem("§cDésactivé", REDSTONE_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eAccès", CONTAINER_SLOT, Material.CHEST, "§7Utiliser les",
                        "§7- Coffres, barrils, shulkers",
                        "§7- Fours, haut fourneaux, fumoirs",
                        "§7- Alambics",
                        "§7- Distributeurs, droppers",
                        "§7- Entonnoirs"),
                IGui.getItem("§cDésactivé", CONTAINER_SLOT + 9, Material.RED_CONCRETE));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void setDisplayName(int slot, @NotNull String name){
        if(link != null){
            link.setDisplayName(slot, name);
        }
        super.setDisplayName(slot, name);
    }
    
    @Override
    public void setDescription(int slot, @NotNull String... description){
        if(link != null){
            link.setDescription(slot, description);
        }
        super.setDescription(slot, description);
    }
    
    @Override
    public void setType(int slot, @NotNull Material material){
        if(link != null){
            link.setType(slot, material);
        }
        super.setType(slot, material);
    }
    
    @Override
    public void setGlowing(int slot, boolean glow){
        if(link != null){
            link.setGlowing(slot, glow);
        }
        super.setGlowing(slot, glow);
    }
    
    @Override
    public void removeItem(int slot){
        if(link != null){
            link.removeItem(slot);
        }
        super.removeItem(slot);
    }
    
    @Override
    public void onCreate(){
        Claim claim = getClaim();
        for(ClaimPermission permission : ClaimPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            if(claim.hasPermission(getData(), permission)){
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
    
    private void switchPermission(ClaimPermission permission){
        setPermission(!getClaim().hasPermission(getData(), permission), permission);
    }
    
    private void setPermission(boolean activate, ClaimPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            getClaim().addPermission(getData(), permission);
            setGlowing(slot, true);
            setType(slot + 9, Material.GREEN_CONCRETE);
            setDisplayName(slot + 9, "§aActivé");
        } else {
            getClaim().removePermission(getData(), permission);
            setGlowing(slot, false);
            setType(slot + 9, Material.RED_CONCRETE);
            setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        Claim claim = getClaim();
        switch(event.getSlot()){
            case GIVE_ALL_SLOT:
                for(ClaimPermission permission : ClaimPermission.values()){
                    setPermission(true, permission);
                }
                break;
            case REMOVE_ALL_SLOT:
                for(ClaimPermission permission : ClaimPermission.values()){
                    setPermission(false, permission);
                }
                break;
            case KICK_SLOT:
                claim.removePlayer(getData());
                event.getPlayer().getPlayer().closeInventory();
                break;
            case INTERACT_DOOR_SLOT:
            case INTERACT_DOOR_SLOT + 9:
                switchPermission(ClaimPermission.INTERACT_DOOR);
                break;
            case BREAK_SLOT:
            case BREAK_SLOT + 9:
                switchPermission(ClaimPermission.BREAK_BLOCK);
                break;
            case PLACE_SLOT:
            case PLACE_SLOT + 9:
                switchPermission(ClaimPermission.PLACE_BLOCK);
                break;
            case CONTAINER_SLOT:
            case CONTAINER_SLOT + 9:
                switchPermission(ClaimPermission.OPEN_CONTAINER);
                break;
            case REDSTONE_SLOT:
            case REDSTONE_SLOT + 9:
                switchPermission(ClaimPermission.INTERACT_REDSTONE);
                break;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Claim getClaim(){
        return ((Datable<Claim>)getFather()).getData();
    }
    
}
