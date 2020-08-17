package fr.amisoz.consulatcore.guis.city.members.member.rank;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityRank;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Material;

import java.util.UUID;

public class RankMemberGui extends DataRelatGui<UUID> {
    
    private static final byte CURRENT_SLOT = 4;
    private static final byte RANK2_SLOT = 20;
    private static final byte RANK3_SLOT = 22;
    private static final byte RANK4_SLOT = 24;
    
    private static final String[] GIVE_RANK_DESCRIPTION = new String[]{"", "§aClique §7pour attribuer", "§7ce grade au joueur"};
    
    public RankMemberGui(UUID uuid){
        super(uuid, "Grade", 5,
                IGui.getItem("§eGrade actuel", CURRENT_SLOT, Material.NAME_TAG, "", "§7Assigner un grade à", "§7un membre reset ses", "§7permissions et lui donne", "§7les permissions par défaut", "§7du grade"),
                IGui.getItem("<grade2>", RANK2_SLOT, Material.DIAMOND, GIVE_RANK_DESCRIPTION),
                IGui.getItem("<grade3>", RANK3_SLOT, Material.GOLD_INGOT, GIVE_RANK_DESCRIPTION),
                IGui.getItem("<grade4>", RANK4_SLOT, Material.IRON_INGOT, GIVE_RANK_DESCRIPTION));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onCreate(){
        City city = getCity();
        CityRank rank = city.getCityPlayer(getData()).getRank(), ranks;
        setDescription(CURRENT_SLOT, "§a" + rank.getRankName());
        updateRank(city.getRank(1));
        updateRank(city.getRank(2));
        updateRank(city.getRank(3));
        int slot = slotByRank(rank);
        if(slot == -1){
            return;
        }
        setGlowing(slot, true);
        setDescription(slot);
    }
    
    private int slotByRank(CityRank rank){
        switch(rank.getId()){
            case 1:
                return RANK2_SLOT;
            case 2:
                return RANK3_SLOT;
            case 3:
                return RANK4_SLOT;
        }
        return -1;
    }
    
    public void setRank(CityRank oldRank, CityRank rank){
        int slot = slotByRank(oldRank);
        setGlowing(slot, false);
        setDescription(slot, GIVE_RANK_DESCRIPTION);
        setGlowing(slotByRank(rank), true);
        setDescription(CURRENT_SLOT, "§a" + rank.getRankName());
        setDescription(slotByRank(rank));
    }
    
    public void updateRank(CityRank rank){
        int slot = slotByRank(rank);
        if(slot == -1){
            return;
        }
        setDisplayName(slot, rank.getColor() + rank.getRankName());
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        City city = getCity();
        if(!city.isOwner(event.getPlayer().getUUID())){
            return;
        }
        CityRank rank;
        switch(event.getSlot()){
            case RANK2_SLOT:
                rank = city.getRank(1);
                break;
            case RANK3_SLOT:
                rank = city.getRank(2);
                break;
            case RANK4_SLOT:
                rank = city.getRank(3);
                break;
            default:
                return;
        }
        city.setRank(getData(), rank);
    }
    
    @SuppressWarnings("unchecked")
    private City getCity(){
        return ((Datable<City>)getFather().getFather()).getData();
    }
}
