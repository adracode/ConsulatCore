package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.runnable.FlyRunnable;
import fr.amisoz.consulatcore.utils.Title;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KIZAFOX on 03/03/2020 for ConsulatCore
 */
public class CommandFly implements CommandExecutor {

    public static List<Player> fly = new ArrayList<>();
    public static Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)){
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("five.fly.perm") || !player.hasPermission("twentyfive.fly.perm") || !player.hasPermission("infinity.fly.perm")){
            player.sendMessage(ChatColor.RED+"Tu n'as pas encore acheté de fly ! Tu peux en acheter un sur la boutique ou le shop en jeu !");
            return true;
        }else{
            if(player.hasPermission("five.fly.perm")){
                if(args.length == 0){
                    player.sendMessage(ChatColor.BLUE+"Usage: /fly [start/info]");
                    return true;
                }

                ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
                ClaimObject chunk = consulatPlayer.claimedChunk;

                switch (args[0]){
                    case "start":
                        int cooldownTime = 3600;
                        if(cooldowns.containsKey(player.getName())) {
                            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                            if(secondsLeft > 0) {
                                player.sendMessage(ChatColor.RED+"Tu ne peux utiliser ton fly que dans " + (secondsLeft > 60 ? "" + secondsLeft / 60 + " minutes" : " secondes"));
                                return true;
                            }
                        }

                        if(chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))){
                            if(fly.contains(player)){
                                player.sendMessage(ChatColor.RED + "Ton fly est déjà actif !");
                                return true;
                            }
                            new FlyRunnable(player).runTaskTimer(ConsulatCore.INSTANCE, 0, 20);
                        }else{
                            player.sendMessage(ChatColor.RED+"Il faut être dans son claim ou y avoir accès pour faire cette commande !");
                            return true;
                        }
                        break;
                    case "info":
                        if(!fly.contains(player)){
                            player.sendMessage(ChatColor.RED + "Tu n'as pas encore activé ton fly !");
                            return true;
                        }

                        if(FlyRunnable.getDuration() >= 60){
                            player.sendMessage(ChatColor.RED+"Tu as encore ton fly pendant " + FlyRunnable.getDuration()/60 + " minutes !");
                        }else {
                            player.sendMessage(ChatColor.RED+"Tu as encore ton fly pendant " + FlyRunnable.getDuration() + " secondes !");
                        }
                        break;
                    default: break;
                }
            }else{
                player.sendMessage(ChatColor.RED+"Vous n'avez pas la permission !");
                return true;
            }
        }
        return false;
    }
}
