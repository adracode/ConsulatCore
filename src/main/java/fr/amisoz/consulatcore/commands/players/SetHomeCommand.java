package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.economy.AccountLoader;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetHomeCommand extends ConsulatCommand {
    
    
    public SetHomeCommand(){
        super("/sethome <Nom du home>", 1, RankEnum.JOUEUR);
    }
    
    @Override
    public void consulatCommand(){
        int moreHome = 0;
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getPlayer().getName());
            moreHome = AccountLoader.hasSupplementHome(offlinePlayer);
        } catch(SQLException e){
            e.printStackTrace();
        }
        
        if(getArgs()[0].length() > 10){
            getPlayer().sendMessage("§cNom de home trop long");
            return;
        }
        
        if(getPlayer().getWorld() != Bukkit.getWorlds().get(0)){
            getPlayer().sendMessage("§cTu dois être dans le monde de base afin de sethome.");
            return;
        }
        
        Claim claim = ClaimManager.getInstance().getClaim(getPlayer());
        RankEnum playerRank = getConsulatPlayer().getRank();
        
        if(claim == null){
            if(playerRank.getRankPower() < RankEnum.MECENE.getRankPower()){
                getPlayer().sendMessage("§cTu dois être dans un claim pour définir ton home.");
                return;
            }
        } else {
            if(!claim.getOwner().equals(getPlayer().getUniqueId()) && !claim.isAllowed(getPlayer().getUniqueId())){
                getPlayer().sendMessage("§cTu dois être dans un claim t'appartenant pour définir ton home.");
                return;
            }
        }
        
        int homeNumber;
    
        switch(playerRank){
            case JOUEUR:
                homeNumber = 1;
                break;
            case TOURISTE:
                homeNumber = 2;
                break;
            case FINANCEUR:
                homeNumber = 3;
                break;
            default:
                homeNumber = 4;
                break;
        }
        
        homeNumber += moreHome;
        
        if(getCorePlayer().homes.size() == homeNumber){
            if(!(getCorePlayer().homes.containsKey(getArgs()[0]))){
                getPlayer().sendMessage("§cTu as atteint ta limite de homes, définis la position d'un home existant ou supprime en un.");
                return;
            }
            
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    updateHome(getPlayer(), getArgs()[0], getPlayer().getLocation());
                    getCorePlayer().homes.put(getArgs()[0], getPlayer().getLocation());
                    getPlayer().sendMessage("§aHome sauvegardé");
                } catch(SQLException e){
                    getPlayer().sendMessage("§cErreur lors de la sauvegarde");
                    e.printStackTrace();
                }
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    insertHome(getPlayer(), getArgs()[0], getPlayer().getLocation());
                    getCorePlayer().homes.put(getArgs()[0], getPlayer().getLocation());
                    getPlayer().sendMessage("§aHome sauvegardé.");
                } catch(SQLException e){
                    getPlayer().sendMessage("§cErreur lors de la sauvegarde");
                    e.printStackTrace();
                }
            });
        }
    }
    
    public void updateHome(Player player, String homeName, Location location) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE homes SET x = ?, y = ?, z = ? WHERE home_name = ? AND idplayer = ?");
        preparedStatement.setDouble(1, location.getX());
        preparedStatement.setDouble(2, location.getY());
        preparedStatement.setDouble(3, location.getZ());
        preparedStatement.setString(4, homeName);
        preparedStatement.setInt(5, PlayersManager.getConsulatPlayer(player).getIdPlayer());
        
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
    
    public void insertHome(Player player, String homeName, Location location) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO homes(x, y, z, idplayer, home_name) VALUES(?, ?, ?, ?, ?)");
        preparedStatement.setDouble(1, location.getX());
        preparedStatement.setDouble(2, location.getY());
        preparedStatement.setDouble(3, location.getZ());
        preparedStatement.setInt(4, PlayersManager.getConsulatPlayer(player).getIdPlayer());
        preparedStatement.setString(5, homeName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
