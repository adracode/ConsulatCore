package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.runnable.FlyRunnable;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        ClaimObject chunk = consulatPlayer.claimedChunk;

        if(!getCorePlayer().canFly){
            player.sendMessage(ChatColor.RED + "Erreur | Tu n'as pas acheté le fly !");
            return;
        }

        if(getCorePlayer().flyTime == 300 || getCorePlayer().flyTime == 1500){
            if(getArgs()[0].equalsIgnoreCase("start")){
                if ((System.currentTimeMillis() - getCorePlayer().lastTime) / 1000 >= 3600) {
                    if (chunk != null && (chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getName()))) {
                        if(!FlyManager.flyMap.containsKey(player)){
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            player.sendMessage(ChatColor.GREEN + "Tu as activé ton fly !");
                            FlyManager.flyMap.put(player, (System.currentTimeMillis()));
                        }else{
                            player.sendMessage(ChatColor.RED + "Erreur | Ton fly est déjà actif !");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Erreur | Tu ne peux pas fly dans un autre claim que le tien ou ceux que tu as accès !");
                    }
                } else {
                    long timeWait = (getCorePlayer().lastTime+3600000) - (System.currentTimeMillis());
                    long minutes = ((timeWait / (1000*60)) % 60);
                    long seconds = ((timeWait / 1000) % 60);
                    player.sendMessage(ChatColor.RED + "Erreur | Tu n'as pas attendu assez longtemps ! Tu dois encore attendre " + minutes + "M" + seconds + "S.");
                }
            }else if(getArgs()[0].equalsIgnoreCase("info")){
                if(!FlyManager.flyMap.containsKey(player)){
                    player.sendMessage(ChatColor.RED + "Erreur | Tu n'as pas encore activé ton fly !");
                    return;
                }

                long startFly = FlyManager.flyMap.get(player);
                long timeLeft = getCorePlayer().flyTime -  (System.currentTimeMillis() - startFly)/1000;

                player.sendMessage(ChatColor.BLUE+"Tu as encore ton fly pendant " + (timeLeft >= 60 ? timeLeft/60 + " minutes !" : timeLeft + " secondes !"));
            }
        }else if(getCorePlayer().flyTime == -1){
            if (getArgs()[0].equalsIgnoreCase("infini")) {
                if(!FlyManager.infiniteFly.contains(player)){
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage(ChatColor.GREEN + "Tu as activé ton fly infini !");
                    FlyManager.infiniteFly.add(player);
                }else{
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.sendMessage(ChatColor.RED + "Tu as enlevé ton fly infini !");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
                    FlyManager.infiniteFly.remove(player);
                }
            }
        }else{
            getPlayer().sendMessage(ChatColor.RED + "Tu n'as pas de fly.");
        }
    }
}
