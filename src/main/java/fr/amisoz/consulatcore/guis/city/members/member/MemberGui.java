package fr.amisoz.consulatcore.guis.city.members.member;

import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaimGui;
import fr.amisoz.consulatcore.guis.city.members.member.permissions.MemberPermissionGui;
import fr.amisoz.consulatcore.guis.city.members.member.rank.RankMemberGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
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
    public void onOpened(GuiOpenEvent event){
        ConsulatPlayer player = event.getPlayer();
        City city = getCity();
        updatePermissions(player, city.isOwner(player.getUUID()));
        updateAccess(player, city.canManageAccesses(player.getUUID()));
        updateRank(player, city.isOwner(player.getUUID()));
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
    
    public void updatePermissions(ConsulatPlayer player, boolean allow){
        if(allow){
            setFakeItem(PERMISSION_SLOT, null, player);
        } else {
            setDescriptionPlayer(PERMISSION_SLOT, player, "", "§cTu ne peux pas", "§cmodifier les permissions");
        }
    }
    
    public void updateAccess(ConsulatPlayer player, boolean allow){
        if(allow){
            setFakeItem(CLAIM_SLOT, null, player);
        } else {
            setDescriptionPlayer(CLAIM_SLOT, player, "", "§cTu ne peux pas", "§cmodifier les accès");
        }
    }
    
    public void updateRank(ConsulatPlayer player, boolean allow){
        if(allow){
            setFakeItem(RANK_SLOT, null, player);
        } else {
            setDescriptionPlayer(RANK_SLOT, player, "", "§cTu ne peux pas", "§cmodifier le grade");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        City city = getCity();
        ConsulatPlayer player = event.getPlayer();
        switch(event.getSlot()){
            case PERMISSION_SLOT:
                if(!city.isOwner(player.getUUID())){
                    return;
                }
                getChild(PERMISSION).open(event.getPlayer());
                break;
            case CLAIM_SLOT:
                if(!city.canManageAccesses(player.getUUID())){
                    return;
                }
                getChild(CLAIM).open(event.getPlayer());
                break;
            case RANK_SLOT:
                if(!city.isOwner(player.getUUID())){
                    return;
                }
                getChild(RANK).open(event.getPlayer());
                break;
        }
    }
    
    @SuppressWarnings("unchecked")
    private City getCity(){
        return ((Datable<City>)getFather()).getData();
    }
    
}
