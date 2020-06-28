package fr.amisoz.consulatcore.guis.city.members.member.rank;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.amisoz.consulatcore.zones.cities.CityRank;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Material;

import java.util.UUID;

//TODO: changer le nom lors d'une update
public class RankMemberGui extends DataRelatGui<UUID> {
    
    private static final byte CURRENT_SLOT = 4;
    private static final byte RANK2_SLOT = 20;
    private static final byte RANK3_SLOT = 22;
    private static final byte RANK4_SLOT = 24;
    
    private static final String[] GIVE_RANK_DESCRIPTION = new String[]{"", "§aCliquez §7pour attribuer", "§7ce grade au joueur"};
    
    public RankMemberGui(UUID uuid){
        super(uuid, "Grade", 5,
                IGui.getItem("§eGrade actuel", CURRENT_SLOT, Material.NAME_TAG),
                IGui.getItem("<grade2>", RANK2_SLOT, Material.DIAMOND, GIVE_RANK_DESCRIPTION),
                IGui.getItem("<grade3>", RANK3_SLOT, Material.GOLD_INGOT, GIVE_RANK_DESCRIPTION),
                IGui.getItem("<grade4>", RANK4_SLOT, Material.IRON_INGOT, GIVE_RANK_DESCRIPTION));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onCreate(){
        City city = getCity();
        CityRank rank = city.getCityPlayer(getData()).getRank();
        setDescription(CURRENT_SLOT, "§a" + rank.getRankName());
        setDisplayName(RANK2_SLOT, "§b" + city.getRankName(1));
        setDisplayName(RANK3_SLOT, "§e" + city.getRankName(2));
        setDisplayName(RANK4_SLOT, "§7" + city.getRankName(3));
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
    
    public void setRank(CityRank rank){
        City city = getCity();
        UUID uuid = getData();
        CityPlayer cityPlayer = city.getCityPlayer(uuid);
        CityRank currentRank = cityPlayer.getRank();
        int slot = slotByRank(currentRank);
        setGlowing(slot, false);
        setDescription(slot, GIVE_RANK_DESCRIPTION);
        setGlowing(slotByRank(rank), true);
        setDescription(CURRENT_SLOT, "§a" + rank.getRankName());
        setDescription(slotByRank(rank));
        cityPlayer.setRank(rank);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        City city = getCity();
        CityRank rank = null;
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
        }
        if(rank != null){
            setRank(rank);
        }
    }
    
    @SuppressWarnings("unchecked")
    private City getCity(){
        return ((Datable<City>)getFather().getFather()).getData();
    }
}
