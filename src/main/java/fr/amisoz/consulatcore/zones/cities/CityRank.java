package fr.amisoz.consulatcore.zones.cities;

import org.jetbrains.annotations.NotNull;

public class CityRank {

    @NotNull private String rankName;

    public CityRank(@NotNull String rankName){
        this.rankName = rankName;
    }

    @NotNull
    public String getRankName(){
        return rankName;
    }

    public void setRankName(@NotNull String rankName){
        this.rankName = rankName;
    }
}
