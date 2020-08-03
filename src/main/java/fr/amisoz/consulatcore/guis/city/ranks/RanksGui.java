package fr.amisoz.consulatcore.guis.city.ranks;

import fr.amisoz.consulatcore.guis.city.ranks.rank.RankGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityRank;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class RanksGui extends DataRelatGui<City> {
    
    private static final byte RANK1_SLOT = 19;
    private static final byte RANK2_SLOT = 21;
    private static final byte RANK3_SLOT = 23;
    private static final byte RANK4_SLOT = 25;
    
    public RanksGui(City city){
        super(city, "Grades", 5,
                IGui.getItem("grade1", RANK1_SLOT, Material.BEACON),
                IGui.getItem("grade2", RANK2_SLOT, Material.DIAMOND),
                IGui.getItem("grade3", RANK3_SLOT, Material.GOLD_INGOT),
                IGui.getItem("grade4", RANK4_SLOT, Material.IRON_INGOT));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof CityRank){
            return new RankGui((CityRank)key);
        }
        return super.createChild(key);
    }
    
    @Override
    public void onCreate(){
        setRank(0);
        setRank(1);
        setRank(2);
        setRank(3);
    }
    
    public void setRank(int index){
        City city = getData();
        CityRank rank;
        switch(index){
            case 0:
                setDisplayName(RANK1_SLOT, (rank = city.getRank(0)).getColor() + rank.getRankName());
                break;
            case 1:
                setDisplayName(RANK2_SLOT, (rank = city.getRank(1)).getColor() + rank.getRankName());
                break;
            case 2:
                setDisplayName(RANK3_SLOT, (rank = city.getRank(2)).getColor() + rank.getRankName());
                break;
            case 3:
                setDisplayName(RANK4_SLOT, (rank = city.getRank(3)).getColor() + rank.getRankName());
                break;
        }
    }
    
    private CityRank getRank(int slot){
        switch(slot){
            case RANK1_SLOT:
                return getData().getRank(0);
            case RANK2_SLOT:
                return getData().getRank(1);
            case RANK3_SLOT:
                return getData().getRank(2);
            case RANK4_SLOT:
                return getData().getRank(3);
        }
        return null;
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case RANK1_SLOT:
            case RANK2_SLOT:
            case RANK3_SLOT:
            case RANK4_SLOT:
                getChild(getRank(event.getSlot())).open(event.getPlayer());
                break;
        }
    }
}
