package fr.amisoz.consulatcore.guis.city.members.member;

import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaimGui;
import fr.amisoz.consulatcore.guis.city.members.member.permissions.MemberPermissionGui;
import fr.amisoz.consulatcore.guis.city.members.member.rank.RankMemberGui;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MemberGui extends DataRelatGui<UUID> {
    
    private static final byte PERMISSION_SLOT = 20;
    private static final byte CLAIM_SLOT = 22;
    private static final byte RANK_SLOT = 24;
    
    public static final String PERMISSION = "city.members.member.permissions";
    public static final String CLAIM = "city.members.member.claims";
    public static final String RANK = "city.members.member.ranks";
    
    public MemberGui(UUID uuid){
        super(uuid, Bukkit.getOfflinePlayer(uuid).getName(), 5,
                IGui.getItem("§ePermissions de ville", PERMISSION_SLOT, Material.BOOK, "", "§7Modifier les permissions", "§7de gestion du joueur "),
                IGui.getItem("§eClaim accessibles", CLAIM_SLOT, Material.GRASS_BLOCK, "", "§7Modifier les claims", "§7accessibles du joueur"),
                IGui.getItem("§eGrade", RANK_SLOT, Material.DIAMOND, "", "§7Modifier le grade", "§7du joueur"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof String){
            switch((String)key){
                case PERMISSION:
                    return new MemberPermissionGui(getData());
                case CLAIM:
                    return new AccessibleClaimGui(getData());
                case RANK:
                    return new RankMemberGui(getData());
            }
        }
        return super.createChild(key);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case PERMISSION_SLOT:
                getChild(PERMISSION).open(event.getPlayer());
                break;
            case CLAIM_SLOT:
                getChild(CLAIM).open(event.getPlayer());
                break;
            case RANK_SLOT:
                getChild(RANK).open(event.getPlayer());
                break;
        }
    }
}
