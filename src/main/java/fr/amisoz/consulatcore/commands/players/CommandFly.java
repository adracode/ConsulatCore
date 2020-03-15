package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.amisoz.consulatcore.runnable.FlyRunnable;
import fr.amisoz.consulatcore.runnable.FlyRunnableBoutique;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KIZAFOX on 03/03/2020 for ConsulatCore
 */
public class CommandFly extends ConsulatCommand {

    public static List<Player> fly5 = new ArrayList<>();
    public static List<Player> fly25 = new ArrayList<>();
    public static List<Player> infinite = new ArrayList<>();
    public static Map<String, Long> cooldowns = new HashMap<>();

    public CommandFly() {
        super("/fly [start/info/stop]", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {

        Player player = getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.canFly){
            player.sendMessage(ChatColor.RED+"Tu n'as pas encore acheté de fly ! Tu peux en acheter un sur la boutique ou le shop en jeu !");
        }else{
            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
            ClaimObject chunk = consulatPlayer.claimedChunk;

            if(ConsulatCore.INSTANCE.getFlySQL().getDuration(player) == 300){//300 = 5 min
                if(getArgs().length == 0){
                    player.sendMessage(usage(player));
                    return ;
                }
                if(getArgs()[0].equalsIgnoreCase("start")){
                    int cooldownTime = 3600;
                    if(cooldowns.containsKey(player.getName())) {
                        long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                        if(secondsLeft > 0) {
                            player.sendMessage(ChatColor.RED+"Tu ne peux utiliser ton fly que dans " + (secondsLeft > 60 ? "" + secondsLeft / 60 + " minutes" : " secondes"));
                            return;
                        }
                    }
                    if(chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))){
                        if(fly5.contains(player)){
                            player.sendMessage(ChatColor.RED + "Ton fly est déjà actif !");
                            return;
                        }
                        new FlyRunnable(player, ConsulatCore.INSTANCE.getFlySQL().getDuration(player), 300).runTaskTimer(ConsulatCore.INSTANCE, 0, 20);
                    }else{
                        player.sendMessage(ChatColor.RED+"Il faut être dans son claim ou y avoir accès pour faire cette commande !");
                    }
                }else if(getArgs()[0].equalsIgnoreCase("info")){
                    if(!fly5.contains(player)){
                        player.sendMessage(ChatColor.RED + "Tu n'as pas encore activé ton fly !");
                        return;
                    }
                    player.sendMessage(ChatColor.BLUE+"Tu as encore ton fly pendant " + (FlyRunnable.duration >= 60 ? FlyRunnable.duration/60 + " minutes !" : FlyRunnable.duration + " secondes !"));
                }
            }else if(ConsulatCore.INSTANCE.getFlySQL().getDuration(player) == 1500){//1500 = 25 min
                if(getArgs().length == 0){
                    player.sendMessage(usage(player));
                    return ;
                }
                if(getArgs()[0].equalsIgnoreCase("start")){
                    int cooldownTime = 3600;
                    if(cooldowns.containsKey(player.getName())) {
                        long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                        if(secondsLeft > 0) {
                            player.sendMessage(ChatColor.RED+"Tu ne peux utiliser ton fly que dans " + (secondsLeft > 60 ? "" + secondsLeft / 60 + " minutes" : " secondes"));
                            return;
                        }
                    }
                    if(chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))){
                        if(fly25.contains(player)){
                            player.sendMessage(ChatColor.RED + "Ton fly est déjà actif !");
                            return;
                        }
                        new FlyRunnableBoutique(player, ConsulatCore.INSTANCE.getFlySQL().getDuration(player), 1500).runTaskTimer(ConsulatCore.INSTANCE, 0, 20);
                    }else{
                        player.sendMessage(ChatColor.RED+"Il faut être dans son claim ou y avoir accès pour faire cette commande !");
                    }
                }else if(getArgs()[0].equalsIgnoreCase("info")){
                    if(!fly25.contains(player)){
                        player.sendMessage(ChatColor.RED + "Tu n'as pas encore activé ton fly !");
                        return;
                    }
                    player.sendMessage(ChatColor.BLUE+"Tu as encore ton fly pendant " + (FlyRunnable.duration >= 60 ? FlyRunnable.duration/60 + " minutes !" : FlyRunnable.duration + " secondes !"));
                }
            }else if(ConsulatCore.INSTANCE.getFlySQL().getDuration(player) == FlyRunnable.INFINITY){
                if(getArgs().length == 0){
                    player.sendMessage(usage(player));
                    return;
                }
                if(getArgs()[0].equalsIgnoreCase("start")){
                    int cooldownTime = 3600;
                    if(cooldowns.containsKey(player.getName())) {
                        long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
                        if(secondsLeft > 0) {
                            player.sendMessage(ChatColor.RED+"Tu ne peux utiliser ton fly que dans " + (secondsLeft > 60 ? "" + secondsLeft / 60 + " minutes" : " secondes"));
                            return;
                        }
                    }
                    if(chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))){
                        if(infinite.contains(player)){
                            player.sendMessage(ChatColor.RED + "Ton fly est déjà actif !");
                            return;
                        }
                        infinite.add(player);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        player.sendMessage(ChatColor.GREEN+"Tu viens d'activer ton fly !");
                    }else{
                        player.sendMessage(ChatColor.RED+"Il faut être dans son claim ou y avoir accès pour faire cette commande !");
                    }
                }else if(getArgs()[0].equalsIgnoreCase("stop")) {
                    if(!infinite.contains(player)){
                        player.sendMessage(ChatColor.RED + "Tu n'as pas encore activé ton fly !");
                        return;
                    }
                    ConsulatCore.INSTANCE.getFlySQL().setDuration(player, Integer.MAX_VALUE);
                    infinite.remove(player);
                    cooldowns.put(player.getName(), System.currentTimeMillis());
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
                    player.sendMessage(ChatColor.RED+"Tu as arrêté ton fly !");
                }
            }
        }
    }

    private String usage(Player player){
        switch (ConsulatCore.INSTANCE.getFlySQL().getDuration(player)){
            case 300:
                player.sendMessage(ChatColor.BLUE+"Usage: /fly [start/info] (5minutes)");
                break;
            case 1500:
                player.sendMessage(ChatColor.BLUE+"Usage: /fly [start/info] (25minutes)");
                break;
            case Integer.MAX_VALUE:
                player.sendMessage(ChatColor.BLUE+"Usage: /fly [start/stop] (illimité)");
                break;
            default:break;
        }
        return ChatColor.RED+"No usage...";
    }
}
