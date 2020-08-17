package fr.amisoz.consulatcore.guis.city.members.member;

import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaimGui;
import fr.amisoz.consulatcore.guis.city.members.member.permissions.MemberPermissionGui;
import fr.amisoz.consulatcore.guis.city.members.member.rank.RankMemberGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MemberGui extends DataRelatGui<UUID> {
    
    public static final String PERMISSION = "city.members.member.permissions";
    public static final String CLAIM = "city.members.member.claims";
    public static final String RANK = "city.members.member.ranks";
    private static final byte PERMISSION_SLOT = 20;
    private static final byte CLAIM_SLOT = 22;
    private static final byte RANK_SLOT = 24;
    
    public MemberGui(UUID uuid){
        super(uuid, Bukkit.getOfflinePlayer(uuid).getName(), 5,
                IGui.getItem("§ePermissions de ville", PERMISSION_SLOT, Material.BOOK),
                IGui.getItem("§eClaim accessibles", CLAIM_SLOT, Material.GRASS_BLOCK),
                IGui.getItem("§eGrade", RANK_SLOT, Material.DIAMOND));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onCreate(){
        City city = getCity();
        if(city.isOwner(getData())){
            setDescription(PERMISSION_SLOT, "", "§aCe joueur à", "§atoutes les permissions");
            setDescription(CLAIM_SLOT, "", "§aCe joueur à", "§atous les accès");
            setDescription(RANK_SLOT, "", "§aCe joueur est propriétaire");
        } else {
            setDescription(PERMISSION_SLOT, "", "§7Modifier les permissions", "§7de gestion du joueur ");
            setDescription(CLAIM_SLOT, "", "§7Modifier les claims", "§7accessibles du joueur");
            setDescription(RANK_SLOT, "", "§7Modifier le grade", "§7du joueur");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        City city = getCity();
        switch(event.getSlot()){
            case PERMISSION_SLOT:
                if(city.isOwner(getData())){
                    return;
                }
                getChild(PERMISSION).getGui().open(event.getPlayer());
                break;
            case CLAIM_SLOT:
                if(city.isOwner(getData())){
                    return;
                }
                getChild(CLAIM).getGui().open(event.getPlayer());
                break;
            case RANK_SLOT:
                if(city.isOwner(getData())){
                    return;
                }
                getChild(RANK).getGui().open(event.getPlayer());
                break;
        }
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
    
    @SuppressWarnings("unchecked")
    private City getCity(){
        return ((Datable<City>)getFather()).getData();
    }
    
}
