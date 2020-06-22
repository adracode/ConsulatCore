package fr.amisoz.consulatcore.guis.city.members.member;

import fr.amisoz.consulatcore.guis.GuiListenerStorage;
import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaimGui;
import fr.amisoz.consulatcore.guis.city.members.member.permissions.MemberPermissionGui;
import fr.amisoz.consulatcore.guis.city.members.member.rank.RankMemberGui;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.UUID;

public class MemberGui extends GuiListener<UUID> {
    
    private static final byte PERMISSION_SLOT = 20;
    private static final byte CLAIM_SLOT = 22;
    private static final byte RANK_SLOT = 24;
    
    public static final String PERMISSION = "city.members.member.permissions";
    public static final String CLAIM = "city.members.member.claims";
    public static final String RANK = "city.members.member.ranks";
    
    private MemberPermissionGui memberPermissionGui = new MemberPermissionGui();
    private AccessibleClaimGui accessibleClaim = new AccessibleClaimGui();
    private RankMemberGui rankMemberGui = new RankMemberGui();
    
    public MemberGui(){
        super(5);
        setTemplate("<membre>",
                getItem("§ePermissions de ville", PERMISSION_SLOT, Material.BOOK, "", "§7Modifier les permissions", "§7de gestion du joueur "),
                getItem("§eClaim accessibles", CLAIM_SLOT, Material.GRASS_BLOCK, "", "§7Modifier les claims", "§7accessibles du joueur"),
                getItem("§eGrade", RANK_SLOT, Material.DIAMOND, "", "§7Modifier le grade", "§7du joueur"))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44);
        GuiListenerStorage storage = GuiListenerStorage.getInstance();
        storage.addListener(PERMISSION, memberPermissionGui);
        storage.addListener(CLAIM, accessibleClaim);
        storage.addListener(RANK, rankMemberGui);
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<UUID> event){
        UUID playerUUID = event.getData();
        event.getPagedGui().setName(Bukkit.getOfflinePlayer(playerUUID).getName());
        event.getGui().prepareChild(PERMISSION, () -> memberPermissionGui.createGui(playerUUID, event.getGui()));
        event.getGui().prepareChild(CLAIM, () -> accessibleClaim.createGui(playerUUID, event.getGui()));
        event.getGui().prepareChild(RANK, () -> rankMemberGui.createGui(playerUUID, event.getGui()));
    }
    
    @Override
    public void onClick(GuiClickEvent<UUID> event){
        switch(event.getSlot()){
            case PERMISSION_SLOT:
                event.getGui().getChild(PERMISSION).open(event.getPlayer());
                break;
            case CLAIM_SLOT:
                event.getGui().getChild(CLAIM).open(event.getPlayer());
                break;
            case RANK_SLOT:
                event.getGui().getChild(RANK).open(event.getPlayer());
                break;
        }
    }
}
