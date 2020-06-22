package fr.amisoz.consulatcore.guis.city.members.member.permissions;

import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Material;

import java.util.UUID;

public class MemberPermissionGui extends GuiListener<UUID> {
    
    private static final byte MEMBER_SLOT = 20;
    private static final byte CLAIM_SLOT = 22;
    private static final byte ACCESS_SLOT = 24;
    
    public MemberPermissionGui(){
        super(5);
        GuiItem deactivate = new GuiItem("§cDésactivé", (byte)-1, Material.RED_CONCRETE);
        setTemplate("Permissions de ville",
                getItem("§eMembres", MEMBER_SLOT, Material.PLAYER_HEAD, "", "§7Inviter un joueur", "§7Kick un membre"),
                getItem(deactivate, MEMBER_SLOT + 9),
                getItem("§eClaims", CLAIM_SLOT, Material.FILLED_MAP, "", "§7Claim un chunk", "§7Unclaim un chunk"),
                getItem(deactivate, CLAIM_SLOT + 9),
                getItem("§eAccès", ACCESS_SLOT, Material.BARRIER, "", "§7Gérer les accès aux chunks"),
                getItem(deactivate, ACCESS_SLOT + 9))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    private byte getSlotPermission(CityPermission permission){
        switch(permission){
            case MANAGE_PLAYER:
                return MEMBER_SLOT;
            case MANAGE_CLAIM:
                return CLAIM_SLOT;
            case MANAGE_ACCESS:
                return ACCESS_SLOT;
        }
        return -1;
    }
    
    private void switchPermission(City city, UUID uuid, PagedGui<UUID> gui, CityPermission permission){
        setPermission(city, uuid, gui, !city.hasPermission(uuid, permission), permission);
    }
    
    private void setPermission(City city, UUID uuid, PagedGui<UUID> gui, boolean activate, CityPermission permission){
        byte slot = getSlotPermission(permission);
        if(slot == -1){
            return;
        }
        if(activate){
            city.addPermission(uuid, permission);
            gui.setGlowing(slot, true);
            gui.setType(slot + 9, Material.GREEN_CONCRETE);
            gui.setDisplayName(slot + 9, "§aActivé");
        } else {
            city.removePermission(uuid, permission);
            gui.setGlowing(slot, false);
            gui.setType(slot + 9, Material.RED_CONCRETE);
            gui.setDisplayName(slot + 9, "§cDésactivé");
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<UUID> event){
        PagedGui<UUID> gui = event.getPagedGui();
        UUID uuid = event.getData();
        City city = getPlayerCity(event.getGui());
        for(CityPermission permission : CityPermission.values()){
            byte slot = getSlotPermission(permission);
            if(slot == -1){
                continue;
            }
            if(city.hasPermission(uuid, permission)){
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
    
    @Override
    public void onClick(GuiClickEvent<UUID> event){
        City city = getPlayerCity(event.getGui());
        UUID uuid = event.getData();
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
        }
        if(permission == null){
            return;
        }
        switchPermission(city, uuid, event.getPagedGui(), permission);
    }
    
    private City getPlayerCity(Gui<UUID> current){
        return (City)current.getFather().getFather().getData();
    }
    
}
