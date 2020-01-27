package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SeenCommand extends ConsulatCommand {

    public SeenCommand() {
        super("/seen <Pseudo>", 1, RankEnum.ADMIN);
    }

    @Override
    public void consulatCommand() {
        try{

            getPlayer().sendMessage("§6Connexions récentes de : §o" + getArgs()[0]);
            getPlayer().sendMessage("§7------------------------------");
            PreparedStatement preparedStatement = ConsulatCore.INSTANCE.getDatabaseConnection().prepareStatement("SELECT * FROM connections INNER JOIN players ON connections.player_id = players.id WHERE players.player_name = ? ORDER BY connections.id DESC LIMIT 5 ");
            preparedStatement.setString(1, getArgs()[0]);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                getPlayer().sendMessage("§7Date : §6" + resultSet.getString("connection_date") + " §7 | IP : §6" + resultSet.getString("player_ip"));
            }

            preparedStatement.close();
            resultSet.close();
            getPlayer().sendMessage("§7------------------------------");
        }catch(SQLException e){
            getPlayer().sendMessage("§cErreur lors de la requête à la BDD : " + e.getMessage());
        }
    }


}
