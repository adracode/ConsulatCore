package fr.amisoz.consulatcore.guis.city.members.member.permissions;

import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Material;

import java.util.UUID;

public class MemberPermissionGui extends DataRelatGui<UUID> {
    
    private static final byte MEMBER_SLOT = 20;
    private static final byte CLAIM_SLOT = 21;
    private static final byte ACCESS_SLOT = 22;
    private static final byte BANK_SLOT = 23;
    private static final byte HOME_SLOT = 24;
    
    public MemberPermissionGui(UUID uuid){
        super(uuid, "Permissions de ville", 5,
                IGui.getItem("§eMembres", MEMBER_SLOT, Material.PLAYER_HEAD, "", "§7Inviter un joueur", "§7Kick un membre"),
                IGui.getItem("§cDésactivé", MEMBER_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eClaims", CLAIM_SLOT, Material.FILLED_MAP, "", "§7Claim un chunk", "§7Unclaim un chunk"),
                IGui.getItem("§cDésactivé", CLAIM_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eAccès", ACCESS_SLOT, Material.BARRIER, "", "§7Gérer les accès aux chunks"),
                IGui.getItem("§cDésactivé", ACCESS_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eBanque", BANK_SLOT, Material.SUNFLOWER, "", "§7Gérer la banque"),
                IGui.getItem("§cDésactivé", BANK_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eBanque", HOME_SLOT, Material.COMPASS, "", "§7Déplacer le home"),
                IGui.getItem("§cDésactivé", HOME_SLOT + 9, Material.RED_CONCRETE)
        );
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onCreate(){
        City city = getPlayerCity();
        for(CityPermission permission : CityPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            if(city.hasPermission(getData(), permission)){
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
        for(CityPermission permission : CityPermission.values()){
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
        City city = getPlayerCity();
        UUID uuid = getData();
        CityPermission permission = null;
        switch(event.getSlot()){
            case MEMBER_SLOT:
            case MEMBER_SLOT + 9:
                permission = CityPermission.MANAGE_PLAYER;
                break;
            case CLAIM_SLOT:
            case CLAIM_SLOT + 9:
                permission = CityPermission.MANAGE_CLAIM;
                break;
            case ACCESS_SLOT:
            case ACCESS_SLOT + 9:
                permission = CityPermission.MANAGE_ACCESS;
                break;
            case BANK_SLOT:
            case BANK_SLOT + 9:
                permission = CityPermission.MANAGE_BANK;
                break;
            case HOME_SLOT:
            case HOME_SLOT + 9:
                permission = CityPermission.MANAGE_HOME;
                break;
        }
        if(permission == null){
            return;
        }
        city.switchPermission(uuid, permission);
    }
    
    public void update(ConsulatPlayer player, boolean allow, int slot){
        if(allow){
            setFakeItem(slot, null, player);
        } else {
            setDescriptionPlayer(slot, player, "", "§cTu ne peux pas", "§cmodifier cette permission");
        }
    }
    
    public void setPermission(boolean activate, CityPermission permission){
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
    
    @SuppressWarnings("unchecked")
    private City getPlayerCity(){
        return ((Datable<City>)getFather().getFather()).getData();
    }
    
    private boolean canSetPermission(ConsulatPlayer player){
        return !getData().equals(player.getUUID());
    }
    
    private byte getSlotPermission(CityPermission permission){
        switch(permission){
            case MANAGE_PLAYER:
                return MEMBER_SLOT;
            case MANAGE_CLAIM:
                return CLAIM_SLOT;
            case MANAGE_ACCESS:
                return ACCESS_SLOT;
            case MANAGE_BANK:
                return BANK_SLOT;
            case MANAGE_HOME:
                return HOME_SLOT;
        }
        return -1;
    }
}
