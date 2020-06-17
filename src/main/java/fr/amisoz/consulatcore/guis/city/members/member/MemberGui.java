package fr.amisoz.consulatcore.guis.city.members.member;

import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaim;
import fr.amisoz.consulatcore.guis.city.members.member.permissions.MemberPermission;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.UUID;

public class MemberGui extends GuiListener<UUID> {
    
    public static final String PERMISSION = "Permission";
    public static final String CLAIM = "Claim";
    
    private static final byte PERMISSION_SLOT = 10;
    private static final byte CLAIM_SLOT = 11;
    private static final byte RANK_SLOT = 12;
    
    private MemberPermission memberPermissionGui = new MemberPermission();
    private AccessibleClaim accessibleClaim = new AccessibleClaim();
    
    public MemberGui(){
        super(6);
        setTemplate("<membre>",
                getItem("§7Permissions de ville", PERMISSION_SLOT, Material.BOOK),
                getItem("§7Claim accessibles", CLAIM_SLOT, Material.GRASS_BLOCK),
                getItem("§7Grade", RANK_SLOT, Material.DIAMOND))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<UUID> event){
        UUID playerUUID = event.getData();
        event.getPagedGui().setName(Bukkit.getOfflinePlayer(playerUUID).getName());
        event.getGui().prepareChild(PERMISSION, () -> memberPermissionGui.createGui(playerUUID, event.getGui()));
        event.getGui().prepareChild(CLAIM, () -> accessibleClaim.createGui(playerUUID, event.getGui()));
    }
    
    @Override
    public void onClick(GuiClickEvent<UUID> event){
        switch(event.getSlot()){
            case PERMISSION_SLOT:
                event.getGui().getChild(PERMISSION).open(event.getPlayer());
                break;
            case CLAIM_SLOT:
                event.getGui().getChild(CLAIM).open(event.getPlayer());
            case RANK_SLOT:
                return;
        }
    }
}
