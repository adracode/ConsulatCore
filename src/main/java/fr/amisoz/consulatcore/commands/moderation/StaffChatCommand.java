package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class StaffChatCommand extends ConsulatCommand {


    public StaffChatCommand() {
        super("/sc <Message>", 1, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        String message = StringUtils.join(getArgs(), " ");
        Bukkit.getOnlinePlayers().forEach(player -> {
            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
            if(consulatPlayer != null && consulatPlayer.getRank().getRankPower() >= RankEnum.MODO.getRankPower()){
                player.sendMessage("§2(Staff)§a " + getPlayer().getName() + "§7 : " + message);
            }
        });
    }
}
