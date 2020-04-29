package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SeenCommand extends ConsulatCommand {
    
    public SeenCommand(){
        super("seen", "/seen <Pseudo>", 1, Rank.ADMIN);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                sender.sendMessage("§6Connexions récentes de : §o" + args[0]);
                sender.sendMessage("§7------------------------------");
                PreparedStatement preparedStatement = ConsulatCore.getInstance().getDatabaseConnection().prepareStatement("SELECT * FROM connections INNER JOIN players ON connections.player_id = players.id WHERE players.player_name = ? ORDER BY connections.id DESC LIMIT 5 ");
                preparedStatement.setString(1, args[0]);
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()){
                    sender.sendMessage("§7Date : §6" +
                            resultSet.getString("connection_date") + " §7 | IP : §6" +
                            resultSet.getString("player_ip"));
                }
                preparedStatement.close();
                resultSet.close();
                sender.sendMessage("§7------------------------------");
            } catch(SQLException e){
                sender.sendMessage("§cErreur lors de la requête à la BDD : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    
}
