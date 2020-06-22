package fr.amisoz.consulatcore.zones.cities;

import org.jetbrains.annotations.NotNull;

public class CityRank {

    private final int id;
    @NotNull private String rankName;

    public CityRank(int id, @NotNull String rankName){
        this.id = id;
        this.rankName = rankName;
    }

    @NotNull
    public String getRankName(){
        return rankName;
    }

    public void setRankName(@NotNull String rankName){
        this.rankName = rankName;
    }
    
    public int getId(){
        return id;
    }
}
