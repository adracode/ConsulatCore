package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TpmodCommand extends ConsulatCommand {

    public TpmodCommand() {
        super("/tpmod <Joueur>", 1, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        Player target = Bukkit.getPlayer(getArgs()[0]);
        if(target == null) {
            getPlayer().sendMessage(ChatColor.RED + "Cible introuvable.");
            return;
        }

        if(getArgs().length == 2){
            Player to = Bukkit.getPlayer(getArgs()[1]);
            if(to == null) {
                getPlayer().sendMessage(ChatColor.RED + "Cible introuvable.");
                return;
            }
            target.teleport(to);
            getPlayer().sendMessage("§aTu as téléporté " + target.getName() + " à " + to.getName());
            return;
        }

        if(!ModerationUtils.moderatePlayers.contains(getPlayer()) && getConsulatPlayer().getRank().getRankPower() < RankEnum.ADMIN.getRankPower()){
            getPlayer().sendMessage(ChatColor.RED + "Tu dois être en mode modérateur.");
            return;
        }

        getPlayer().teleport(target);
        getPlayer().sendMessage(ChatColor.GREEN + "Tu t'es téléporté à " + getArgs()[0] + ".");
    }
}
