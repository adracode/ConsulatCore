package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KickCommand extends ConsulatCommand {

    public KickCommand() {
        super("/kick <Joueur> <Raison>", 2, Rank.MODO);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cJoueur hors-ligne");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder() ;
        for(int i = 1; i < args.length; i++){
            stringBuilder.append(" ").append(args[i]);
        }
        String reason = stringBuilder.toString();
        target.getPlayer().kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été exclu.\n§cRaison : §4" + reason);
        sender.sendMessage("§aJoueur exclu !");
    }
}
