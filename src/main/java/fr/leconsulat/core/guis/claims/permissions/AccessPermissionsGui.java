package fr.leconsulat.core.guis.claims.permissions;

import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class AccessPermissionsGui extends DataRelatGui<UUID> {
    
    private static final byte INTERACT_DOOR_SLOT = 28;
    private static final byte BREAK_SLOT = 29;
    private static final byte PLACE_SLOT = 30;
    private static final byte CONTAINER_SLOT = 31;
    private static final byte REDSTONE_SLOT = 32;
    private static final byte DAMAGE_SLOT = 33;
    private static final byte OTHER_SLOT = 34;
    private static final byte GIVE_ALL_SLOT = 1;
    private static final byte REMOVE_ALL_SLOT = 2;
    private static final byte KICK_SLOT = 7;
    
    private IGui link;
    
    public AccessPermissionsGui(UUID uuid){
        super(uuid, Bukkit.getOfflinePlayer(uuid).getName(), 6,
                IGui.getItem("§eTout activer", GIVE_ALL_SLOT, Material.TOTEM_OF_UNDYING),
                IGui.getItem("§eTout désactiver", REMOVE_ALL_SLOT, Material.BARRIER),
                IGui.getItem("§cRévoquer l'accès", KICK_SLOT, Material.TRIDENT),
                IGui.getItem("§eInteraction", INTERACT_DOOR_SLOT, Material.OAK_DOOR, "§7Interagir avec", "§7- Portes", "§7- Trappes", "§7- Portillon"),
                IGui.getItem("§cDésactivé", INTERACT_DOOR_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eDétruire", BREAK_SLOT, Material.DIAMOND_PICKAXE, "§7Détruire des blocs"),
                IGui.getItem("§cDésactivé", BREAK_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§ePlacer", PLACE_SLOT, Material.BRICK, "§7Placer", "§7- Blocs", "§7- Engrais", "§7- Seaux", "§7- Cadres, peintures, stands..."),
                IGui.getItem("§cDésactivé", PLACE_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eDégâts", DAMAGE_SLOT, Material.DIAMOND_SWORD, "§7Infliger des dégâts"),
                IGui.getItem("§cDésactivé", DAMAGE_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eAutre", OTHER_SLOT, Material.MINECART, "§7Autre", "§7- Autre interactions", "§7- ..."),
                IGui.getItem("§cDésactivé", OTHER_SLOT + 9, Material.RED_CONCRETE),
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
    
    @Override
    public void onOpened(GuiOpenEvent event){
        ConsulatPlayer player = event.getPlayer();
        if(canSetPermission(event.getPlayer())){
            setFakeItem(GIVE_ALL_SLOT, null, player);
            setFakeItem(REMOVE_ALL_SLOT, null, player);
            setFakeItem(KICK_SLOT, null, player);
        } else {
            setDescriptionPlayer(GIVE_ALL_SLOT, player, "", "§cTu ne peux pas", "§cfaire cette action");
            setDescriptionPlayer(REMOVE_ALL_SLOT, player, "", "§cTu ne peux pas", "§cfaire cette action");
            setDescriptionPlayer(KICK_SLOT, player, "", "§cTu ne peux pas", "§cfaire cette action");
        }
        for(ClaimPermission permission : ClaimPermission.values()){
            int slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            update(event.getPlayer(), canSetPermission(player), slot + 9);
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        if(!canSetPermission(event.getPlayer())){
            return;
        }
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
            case DAMAGE_SLOT:
            case DAMAGE_SLOT + 9:
                switchPermission(ClaimPermission.DAMAGE);
                break;
            case OTHER_SLOT:
            case OTHER_SLOT + 9:
                switchPermission(ClaimPermission.OTHER);
                break;
        }
    }
    
    @Override
    public void setDescriptionPlayer(int slot, ConsulatPlayer player, String... description){
        if(link != null){
            link.setDescriptionPlayer(slot, player, description);
        }
        super.setDescriptionPlayer(slot, player, description);
    }
    
    @Override
    public void setDescriptionPlayer(int slot, ConsulatPlayer player, List<String> description){
        if(link != null){
            link.setDescriptionPlayer(slot, player, description);
        }
        super.setDescriptionPlayer(slot, player, description);
    }
    
    public void update(ConsulatPlayer player, boolean allow, int slot){
        if(allow){
            setFakeItem(slot, null, player);
        } else {
            setDescriptionPlayer(slot, player, "", "§cTu ne peux pas", "§cmodifier cette permission");
        }
    }
    
    public IGui getLink(){
        return link;
    }
    
    public void setLink(IGui link){
        this.link = link;
    }
    
    @SuppressWarnings("unchecked")
    private Claim getClaim(){
        return ((Datable<Claim>)getFather()).getData();
    }
    
    private boolean canSetPermission(ConsulatPlayer player){
        return !getData().equals(player.getUUID());
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
            case DAMAGE:
                return DAMAGE_SLOT;
            case OTHER:
                return OTHER_SLOT;
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
    
}
