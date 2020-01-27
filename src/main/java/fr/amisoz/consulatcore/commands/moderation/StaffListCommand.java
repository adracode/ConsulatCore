package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;

public class StaffListCommand extends ConsulatCommand {

    public StaffListCommand() {
        super("/stafflist", 0, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        getPlayer().sendMessage("§6§uListe du staff en ligne : ");
        Bukkit.getOnlinePlayers().forEach(player -> {
            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
            RankEnum rank = consulatPlayer.getRank();
            if(rank.getRankPower() >= RankEnum.BUILDER.getRankPower()){
                getPlayer().sendMessage(rank.getRankColor() + "[" + rank.getRankName() + "] " + player.getName());
            }
        });
    }
}
