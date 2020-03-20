package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.fly.FlySQL;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.amisoz.consulatcore.runnable.FlyRunnable;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by KIZAFOX on 03/03/2020 for ConsulatCore
 */
public class CommandFly extends ConsulatCommand {

    public CommandFly() {
        super("/fly [start/info/infini]", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        Player player = getPlayer();
        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
        FlySQL flySQL = ConsulatCore.INSTANCE.getFlySQL();
        ClaimObject chunk = consulatPlayer.claimedChunk;

        if(getArgs()[0].equalsIgnoreCase("start")){
            if (flySQL.canFly(player)) {
                if ((System.currentTimeMillis() - flySQL.getLastTime(player)) / 1000 >= 3600) {
                    if (chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))) {
                        if(!FlyRunnable.flyMap.containsKey(player)){
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            player.sendMessage(ChatColor.GREEN + "Tu as activé ton fly !");
                            flySQL.setLastTime(player, 0);
                            FlyRunnable.flyMap.put(player, (System.currentTimeMillis()));
                        }else{
                            player.sendMessage(ChatColor.RED + "Erreur | Ton fly est déjà actif !");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Erreur | Tu ne peux pas fly dans un autre claim que le tien ou ceux que tu as accès !");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Erreur | Tu n'as pas attendu assez longtemps ! (" + (flySQL.getLastTime(player) > 60 ? "" + flySQL.getLastTime(player) / 60 + " minutes)" : " secondes)"));
                }
            }else{
                player.sendMessage(ChatColor.RED + "Erreur | Tu n'as pas acheté le fly !");
            }
        }else if(getArgs()[0].equalsIgnoreCase("info")){
            if(!FlyRunnable.flyMap.containsKey(player)){
                player.sendMessage(ChatColor.RED + "Erreur | Tu n'as pas encore activé ton fly !");
                return;
            }
            player.sendMessage(ChatColor.BLUE+"Tu as encore ton fly pendant " + (FlyRunnable.getDuration() >= 60 ? FlyRunnable.getDuration()/60 + " minutes !" : FlyRunnable.getDuration() + " secondes !"));
        }
    }
}
