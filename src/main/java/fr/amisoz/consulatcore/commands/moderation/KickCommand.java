package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KickCommand extends ConsulatCommand {

    public KickCommand() {
        super("/kick <Joueur> <Raison>", 2, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        Player target = Bukkit.getPlayer(getArgs()[0]);
        if(target == null){
            getPlayer().sendMessage("§cJoueur hors-ligne");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder() ;
        for(int i = 1; i < getArgs().length; i++){
            stringBuilder.append(" ").append(getArgs()[i]);
        }
        String reason = stringBuilder.toString();

        target.kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été exclu.\n§cRaison : §4" + reason);
        getPlayer().sendMessage("§aJoueur exclu !");
    }
}
