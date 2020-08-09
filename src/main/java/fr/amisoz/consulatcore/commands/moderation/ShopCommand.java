package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.Fly;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class ShopCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args){
        if(commandSender instanceof Player){
            ((Player)commandSender).performCommand("help");
            return false;
        }
        if(args.length < 2){
            commandSender.sendMessage("§cErreur");
            return false;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
        if(target == null){
            commandSender.sendMessage("Erreur");
            return false;
        }
        switch(args[0].toLowerCase()){
            case "rank":
                String rank = args[2];
                if(rank.equalsIgnoreCase("financeur") ||
                        rank.equalsIgnoreCase("mécène")){
                    Rank newRank = Rank.byName(rank);
                    target.setRank(newRank);
                    target.sendMessage("§7Suite à ton achat, tu es désormais " + newRank.getRankColor() + newRank.getRankName());
                }
                break;
            case "announce":
                Bukkit.broadcastMessage("§7[§aBoutique§7] §a" + args[1] + "§7 a acheté §a" + args[2] + " " + args[3] + "§7 !");
                break;
            case "home":
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        target.incrementLimitHome();
                        target.sendMessage("§7Suite à ton achat, tu as un home supplémentaire ! Afin de l'activer, déconnecte et reconnecte toi.");
                    } catch(SQLException e){
                        target.sendMessage("§cUne erreur s'est produite lors de l'achat de ton home, préviens un administrateur !");
                        e.printStackTrace();
                    }
                });
                break;
            case "up":
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        target.setPerkTop(true);
                        target.sendMessage("§7Suite à ton achat, tu as accès au /top !");
                    } catch(SQLException e){
                        target.sendMessage("§cUne erreur s'est produite lors de l'achat de ton /up, préviens un administrateur !");
                        e.printStackTrace();
                    }
                });
                break;
            case "fly5":
                target.setFly(Fly.FLY_5);
                target.sendMessage(Text.FLY + "Suite à ton achat tu as maintenant accès au /fly qui dure 5minutes toute les heures !");
                break;
            case "fly25":
                target.setFly(Fly.FLY_25);
                target.sendMessage(Text.FLY + "Suite à ton achat tu as maintenant accès au /fly qui dure 25 minutes toute les heures !");
                break;
            case "infinite":
                target.setFly(Fly.FLY_INFINITE);
                target.sendMessage(Text.FLY + "Suite à ton achat tu as maintenant accès au /fly infini !");
                break;
            case "perso":
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        target.setHasCustomRank(true);
                        target.sendMessage("§7Suite à ton achat, tu as le grade personnalisé ! Fais /perso et laisse toi guider ;)");
                    } catch(SQLException e){
                        target.sendMessage("§cUne erreur s'est produite lors de l'achat de grade personnalisé, préviens un administrateur !");
                        e.printStackTrace();
                    }
                });
                break;
        }
        return false;
    }
}
