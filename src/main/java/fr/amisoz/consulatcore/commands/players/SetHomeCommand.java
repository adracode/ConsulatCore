package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.claim.ClaimObject;
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


    public SetHomeCommand() {
        super("/sethome <Nom du home>", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        int moreHome = 0;
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getPlayer().getName());
            moreHome = AccountLoader.hasSupplementHome(offlinePlayer);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (getArgs()[0].length() > 10) {
            getPlayer().sendMessage("§cNom de home trop long");
            return;
        }

        ClaimObject claim = getConsulatPlayer().claimedChunk;
        RankEnum playerRank = getConsulatPlayer().getRank();

        if (getPlayer().getWorld() != Bukkit.getWorlds().get(0)) {
            getPlayer().sendMessage("§cTu dois être dans le monde de base afin de sethome.");
            return;
        }

        if (claim == null) {
            if (playerRank.getRankPower() < RankEnum.MECENE.getRankPower()) {
                getPlayer().sendMessage("§cTu dois etre dans un claim pour définir ton home.");
                return;
            }
        } else {
            if (!claim.getPlayerUUID().equalsIgnoreCase(getPlayer().getUniqueId().toString()) && !claim.access.contains(getPlayer().getUniqueId().toString())) {
                getPlayer().sendMessage("§cTu dois être dans un claim t'appartenant pour définir ton home.");
                return;
            }
        }

        int homeNumber;

        if (playerRank.equals(RankEnum.JOUEUR)) {
            homeNumber = 1;
        } else if (playerRank.equals(RankEnum.TOURISTE)) {
            homeNumber = 2;
        } else if (playerRank.equals(RankEnum.FINANCEUR)) {
            homeNumber = 3;
        } else {
            homeNumber = 4;
        }

        homeNumber += moreHome;

        if (getCorePlayer().homes.size() == homeNumber) {
            if (!(getCorePlayer().homes.containsKey(getArgs()[0]))) {
                getPlayer().sendMessage("§cTu as atteint ta limite de homes, définis la position d'un home existant ou supprime en un.");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    updateHome(getPlayer(), getArgs()[0], getPlayer().getLocation());
                    getCorePlayer().homes.put(getArgs()[0], getPlayer().getLocation());
                    getPlayer().sendMessage("§aHome sauvegardé");
                } catch (SQLException e) {
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
                } catch (SQLException e) {
                    getPlayer().sendMessage("§cErreur lors de la sauvegarde");
                    e.printStackTrace();
                }
            });
        }
    }

    public void updateHome(Player player, String homeName, Location location) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE homes SET x = ?, y = ?, z = ? WHERE home_name = ? AND idplayer = ?");
        preparedStatement.setInt(1, location.getBlockX());
        preparedStatement.setInt(2, location.getBlockY());
        preparedStatement.setInt(3, location.getBlockZ());
        preparedStatement.setString(4, homeName);
        preparedStatement.setInt(5, PlayersManager.getConsulatPlayer(player).getIdPlayer());

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void insertHome(Player player, String homeName, Location location) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO homes(x, y, z, idplayer, home_name) VALUES(?, ?, ?, ?, ?)");
        preparedStatement.setInt(1, location.getBlockX());
        preparedStatement.setInt(2, location.getBlockY());
        preparedStatement.setInt(3, location.getBlockZ());
        preparedStatement.setInt(4, PlayersManager.getConsulatPlayer(player).getIdPlayer());
        preparedStatement.setString(5, homeName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
