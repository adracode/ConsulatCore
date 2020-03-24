package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelHomeCommand extends ConsulatCommand {


    public DelHomeCommand() {
        super("/delhome <Nom du home>", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {

        if (getArgs().length == 2 && getConsulatPlayer().getRank().getRankPower() >= RankEnum.RESPONSABLE.getRankPower()) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(getArgs()[0]);
            if (!target.hasPlayedBefore()) {
                getPlayer().sendMessage(ChatColor.RED + "Joueur inexistant.");
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    if (delHomeModeration(target.getName(), getArgs()[1]) > 0) {
                        if (target.isOnline()) {
                            Player onlineTarget = target.getPlayer();
                            if (onlineTarget == null) return;

                            getCorePlayer().homes.remove(getArgs()[1]);

                        }
                        getPlayer().sendMessage(ChatColor.GREEN + "Home supprimé.");
                    } else {
                        getPlayer().sendMessage(ChatColor.RED + "Ce home n'existe pas.");
                    }
                } catch (SQLException e) {
                    getPlayer().sendMessage(ChatColor.RED + "Erreur lors de la suppression.");
                    e.printStackTrace();
                }
            });

            return;
        }

        String homeName = getArgs()[0];
        if (!getCorePlayer().homes.containsKey(homeName)) {
            getPlayer().sendMessage(ChatColor.RED + "Home introuvable !");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
            try {
                delHome(getPlayer(), getArgs()[0]);
                getCorePlayer().homes.remove(getArgs()[0]);
                getPlayer().sendMessage(ConsulatCore.PREFIX + "§aTon home a bien été supprimé.");
            } catch (SQLException e) {
                getPlayer().sendMessage(ConsulatCore.PREFIX + "§cErreur lors de la suppression.");
                e.printStackTrace();
            }
        });
    }

    public void delHome(Player player, String homeName) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM `homes` WHERE `idplayer` = ? AND `home_name` = ?");
        preparedStatement.setInt(1, PlayersManager.getConsulatPlayer(player).getIdPlayer());
        preparedStatement.setString(2, homeName);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private int delHomeModeration(String playerName, String homeName) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("DELETE h FROM homes h INNER JOIN players ON players.id = h.idplayer WHERE players.player_name = ? AND h.home_name = ?");
        preparedStatement.setString(1, playerName);
        preparedStatement.setString(2, homeName);
        int updated = preparedStatement.executeUpdate();

        preparedStatement.close();
        return updated;
    }
}
