package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.custom.CustomDatabase;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import fr.leconsulat.api.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ShopCommand implements CommandExecutor {

    ConsulatCore consulatCore;

    public ShopCommand(ConsulatCore consulatCore) {
        this.consulatCore = consulatCore;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player) {
            commandSender.sendMessage("§cTu ne peux pas executer cette commande.");
            return false;
        }

        if (args.length < 2) {
            commandSender.sendMessage("§cErreur");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            commandSender.sendMessage("Erreur");
            return false;
        }

        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(target);

        if (args[0].equalsIgnoreCase("rank")) {
            String rank = args[2];
            if (rank.equalsIgnoreCase("financeur") || rank.equalsIgnoreCase("mécène")) {
                RankEnum newRank = RankManager.getRankByName(rank);
                if (consulatCore.getRankManager().changeRank(target, newRank)) {
                    target.sendMessage("§7Suite à ton achat, tu es désormais " + newRank.getRankColor() + newRank.getRankName());
                } else {
                    target.sendMessage("§cUne erreur s'est produite lors de l'achat de ton grade " + rank + ", préviens un administrateur !");
                }
            }
        }

        if (args[0].equalsIgnoreCase("announce")) {
            String playerName = args[1];
            String number = args[2];
            String article = args[3];
            Bukkit.broadcastMessage("§7[§aBoutique§7] §a" + playerName + "§7 a acheté §a" + number + " " + article + "§7 !");
        }

        if (args[0].equalsIgnoreCase("home")) {
            try {
                addHome(target.getName());
                target.sendMessage("§7Suite à ton achat, tu as un home supplémentaire ! Afin de l'activer, déconnecte et reconnecte toi.");
            } catch (SQLException e) {
                target.sendMessage("§cUne erreur s'est produite lors de l'achat de ton home, préviens un administrateur !");
                e.printStackTrace();
            }
        }


        if (args[0].equalsIgnoreCase("perso")) {
            try {
                CustomDatabase.activePerso(target);
                consulatPlayer.setHasPerso(true);
                target.sendMessage("§7Suite à ton achat, tu as le grade personnalisé ! Fais /perso et laisse toi guider ;)");
            } catch (SQLException e) {
                target.sendMessage("§cUne erreur s'est produite lors de l'achat de grade personnalisé, préviens un administrateur !");
                e.printStackTrace();
            }
        }
        if (args[0].equals("fly5")) {
            CoreManagerPlayers.getCorePlayer(target).canFly = true;
            CoreManagerPlayers.getCorePlayer(target).flyTime = 300;
            target.sendMessage(FlyManager.flyPrefix + "Suite à ton achat tu as maintenant accès au /fly qui dure 5minutes toute les heures !");

            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    ConsulatCore.INSTANCE.getFlySQL().updateFlyTime(target, 300L);
                } catch (SQLException e) {
                    target.sendMessage(FlyManager.flyPrefix + "La sauvegarde de ton Fly a échoué, contacte un administrateur.");
                }
            });
        }


        if (args[0].equals("fly25")) {
            CoreManagerPlayers.getCorePlayer(target).canFly = true;
            CoreManagerPlayers.getCorePlayer(target).flyTime = 1500;
            target.sendMessage(FlyManager.flyPrefix + "Suite à ton achat tu as maintenant accès au /fly qui dure 25 minutes toute les heures !");

            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    ConsulatCore.INSTANCE.getFlySQL().updateFlyTime(target, 1500L);
                } catch (SQLException e) {
                    target.sendMessage(FlyManager.flyPrefix + "La sauvegarde de ton Fly a échoué, contacte un administrateur.");
                }
            });
        }

        if (args[0].equals("infinite")) {
            CoreManagerPlayers.getCorePlayer(target).canFly = true;
            CoreManagerPlayers.getCorePlayer(target).flyTime = -1;
            target.sendMessage(FlyManager.flyPrefix + "Suite à ton achat tu as maintenant accès au /fly infini !");

            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                try {
                    ConsulatCore.INSTANCE.getFlySQL().updateFlyTime(target, -1L);
                } catch (SQLException e) {
                    target.sendMessage(FlyManager.flyPrefix + "La sauvegarde de ton Fly a échoué, contacte un administrateur.");
                }
            });
        }

        return false;
    }

    private void addHome(String playerName) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET moreHomes = moreHomes + 1 WHERE player_name = ?");
        preparedStatement.setString(1, playerName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
