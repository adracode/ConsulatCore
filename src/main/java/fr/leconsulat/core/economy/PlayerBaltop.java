package fr.leconsulat.core.economy;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.core.players.SurvivalOffline;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PlayerBaltop extends Baltop<SurvivalOffline> {
    
    public PlayerBaltop(){
        super(10, SurvivalOffline::getMoney);
    }
    
    @Override
    public Collection<SurvivalOffline> getMoneyOwners(){
        try {
            PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement(
                    "SELECT id, player_name, money FROM players WHERE player_rank != 'Admin' AND player_rank != 'Superviseur' AND player_rank != 'Développeur' ORDER BY money DESC limit ?;");
            preparedStatement.setInt(1, max);
            preparedStatement.executeQuery();
            ResultSet result = preparedStatement.executeQuery();
            List<SurvivalOffline> rank = new ArrayList<>();
            while(result.next()){
                String name = result.getString("player_name");
                if(name == null){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Player name in null at id " + result.getInt("id"));
                    continue;
                }
                rank.add(new SurvivalOffline(
                        0,
                        null,
                        result.getString("player_name"),
                        null,
                        null,
                        result.getDouble("money"),
                        null
                ));
            }
            preparedStatement.close();
            return rank;
        } catch(SQLException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
