package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.runnable.FlyRunnable;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KIZAFOX on 03/03/2020 for ConsulatCore
 */
public class CommandFly extends ConsulatCommand {

    public static List<Player> fly = new ArrayList<>();
    public static Map<String, Long> cooldowns = new HashMap<>();

    public CommandFly() {
        super("/fly [start/info]", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {

        Player player = getPlayer();

        if(ConsulatCore.INSTANCE.getFlySQL().canFly(player)){
            player.sendMessage(ChatColor.RED+"Tu n'as pas encore acheté de fly ! Tu peux en acheter un sur la boutique ou le shop en jeu !");
        }else{
            if(getArgs().length == 0){
                player.sendMessage(ChatColor.BLUE+"Usage: /fly [start/info]");
                return ;
            }

            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
            ClaimObject chunk = consulatPlayer.claimedChunk;

            switch (getArgs()[0]){
                case "start":
                    int cooldownTime = 3600;
                    if(cooldowns.containsKey(player.getName())) {
                        long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                        if(secondsLeft > 0) {
                            player.sendMessage(ChatColor.RED+"Tu ne peux utiliser ton fly que dans " + (secondsLeft > 60 ? "" + secondsLeft / 60 + " minutes" : " secondes"));
                            return;
                        }
                    }

                    if(chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))){
                        if(fly.contains(player)){
                            player.sendMessage(ChatColor.RED + "Ton fly est déjà actif !");
                            return;
                        }
                        new FlyRunnable(player, ConsulatCore.INSTANCE.getFlySQL().getDuration(player)).runTaskTimer(ConsulatCore.INSTANCE, 0, 20);
                    }else{
                        player.sendMessage(ChatColor.RED+"Il faut être dans son claim ou y avoir accès pour faire cette commande !");
                        return;
                    }
                    break;
                case "info":
                    if(!fly.contains(player)){
                        player.sendMessage(ChatColor.RED + "Tu n'as pas encore activé ton fly !");
                        return;
                    }

                    player.sendMessage(ChatColor.BLUE+"Tu as encore ton fly pendant " + (FlyRunnable.duration >= 60 ? FlyRunnable.duration/60 + " minutes !" : FlyRunnable.duration + " secondes !"));
                    break;
                default: break;
            }
        }
    }
}
