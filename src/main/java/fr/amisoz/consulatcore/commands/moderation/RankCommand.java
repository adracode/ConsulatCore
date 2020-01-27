package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import fr.leconsulat.api.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RankCommand extends ConsulatCommand {

    private ConsulatCore consulatCore;

    public RankCommand(ConsulatCore consulatCore) {
        super("/rank <Joueur> <Rang>", 2, RankEnum.RESPONSABLE);
        this.consulatCore = consulatCore;
    }


    @Override
    public void consulatCommand() {
        RankEnum playerRank = getConsulatPlayer().getRank();

        Player target = Bukkit.getPlayer(getArgs()[0]);
        if(target == null){
            getPlayer().sendMessage(ChatColor.RED + "Joueur ciblé introuvable ! " + ChatColor.GRAY + "( " + getArgs()[0] + " )");
            return;
        }

        String newRankName = getArgs()[1];
        RankEnum newRank = RankManager.getRankByName(newRankName);
        if(newRank == null){
            getPlayer().sendMessage(ChatColor.RED + "Une erreur s'est produite. Le nouveau rang est peut-être invalide : " + newRankName);
            Arrays.stream(RankEnum.values()).forEach(rank -> getPlayer().sendMessage(rank.getRankColor() + rank.getRankName() + " : " + rank.getRankPower()));
            return;
        }

        if(getConsulatPlayer().getRank().getRankPower() < newRank.getMinPower()){
            getPlayer().sendMessage("§cTu ne peux pas ajouter ce grade");
            return;
        }

        if(consulatCore.getRankManager().changeRank(target, newRank)){
            getPlayer().sendMessage(ChatColor.GREEN + target.getName() + ChatColor.GRAY + " est désormais " + newRank.getRankColor() + newRank.getRankName());
        }else{
            getPlayer().sendMessage(ChatColor.RED + "Une erreur s\'est produite.");
        }

    }


}
