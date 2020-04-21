package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Arrays;

public class RankCommand extends ConsulatCommand {
    
    public RankCommand(){
        super("/rank <Joueur> <Rang>", 2, Rank.RESPONSABLE);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cJoueur ciblé introuvable ! §7( " + args[0] + " )");
            return;
        }
        String newRankName = args[1];
        Rank newRank = Rank.byName(newRankName);
        if(newRank == null){
            sender.sendMessage("§cUne erreur s'est produite. Le nouveau rang est peut-être invalide : " + newRankName);
            Arrays.stream(Rank.values()).forEach(rank -> sender.sendMessage(rank.getRankColor() + rank.getRankName() + " : " + rank.getRankPower()));
            return;
        }
        if(!sender.hasPower(newRank)){
            sender.sendMessage("§cTu ne peux pas ajouter ce grade");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                target.setRank(newRank);
                sender.sendMessage("§a" + target.getName() + "§7 est désormais " + newRank.getRankColor() + newRank.getRankName());
            } catch(SQLException e){
                sender.sendMessage("§cUne erreur s'est produite.");
                e.printStackTrace();
            }
        });
    }
}
