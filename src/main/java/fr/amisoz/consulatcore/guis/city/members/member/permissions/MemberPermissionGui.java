package fr.amisoz.consulatcore.guis.city.members.member.permissions;

import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Material;

import java.util.UUID;

public class MemberPermissionGui extends DataRelatGui<UUID> {
    
    private static final byte MEMBER_SLOT = 19;
    private static final byte CLAIM_SLOT = 21;
    private static final byte ACCESS_SLOT = 23;
    private static final byte BANK_SLOT = 25;
    
    public MemberPermissionGui(UUID uuid){
        super(uuid, "Permissions de ville", 5,
                IGui.getItem("§eMembres", MEMBER_SLOT, Material.PLAYER_HEAD, "", "§7Inviter un joueur", "§7Kick un membre"),
                IGui.getItem("§cDésactivé", MEMBER_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eClaims", CLAIM_SLOT, Material.FILLED_MAP, "", "§7Claim un chunk", "§7Unclaim un chunk"),
                IGui.getItem("§cDésactivé", CLAIM_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eAccès", ACCESS_SLOT, Material.BARRIER, "", "§7Gérer les accès aux chunks"),
                IGui.getItem("§cDésactivé", ACCESS_SLOT + 9, Material.RED_CONCRETE),
                IGui.getItem("§eBanque", BANK_SLOT, Material.SUNFLOWER, "", "§7Gérer la banque"),
                IGui.getItem("§cDésactivé", BANK_SLOT + 9, Material.RED_CONCRETE));
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
        }
        return -1;
    }
    
    private void switchPermission(CityPermission permission){
        setPermission(!getPlayerCity().hasPermission(getData(), permission), permission);
    }
    
    private void setPermission(boolean activate, CityPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        City city = getPlayerCity();
        if(activate){
            city.addPermission(getData(), permission);
            setGlowing(slot, true);
            setType(slot + 9, Material.GREEN_CONCRETE);
            setDisplayName(slot + 9, "§aActivé");
        } else {
            city.removePermission(getData(), permission);
            setGlowing(slot, false);
            setType(slot + 9, Material.RED_CONCRETE);
            setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
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
        }
        if(permission == null){
            return;
        }
        switchPermission(permission);
    }
    
    @SuppressWarnings("unchecked")
    private City getPlayerCity(){
        return ((Datable<City>)getFather().getFather()).getData();
    }
    
}
