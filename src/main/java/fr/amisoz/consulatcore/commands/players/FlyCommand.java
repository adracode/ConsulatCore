package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by KIZAFOX on 03/03/2020 for ConsulatCore
 */
public class FlyCommand extends ConsulatCommand {


    public FlyCommand() {
        super("/fly [start/info/infini]", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        Player player = getPlayer();
        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
        ClaimObject chunk = consulatPlayer.claimedChunk;
        if (!getCorePlayer().canFly) {
            player.sendMessage(FlyManager.flyPrefix + "Erreur | Tu n'as pas acheté le fly !");
            return;
        }

        if (getArgs()[0].equalsIgnoreCase("start")) {
            if (getCorePlayer().flyTime == -1) {
                getPlayer().sendMessage(FlyManager.flyPrefix + "Tu dois utiliser /fly infini.");
                return;
            }

            if ((System.currentTimeMillis() - getCorePlayer().lastTime) / 1000 < 3600 && getCorePlayer().isFinished) {
                long timeWait = (getCorePlayer().lastTime + 3600000) - (System.currentTimeMillis());
                long minutes = ((timeWait / (1000 * 60)) % 60);
                long seconds = ((timeWait / 1000) % 60);
                player.sendMessage(FlyManager.flyPrefix + "Erreur | Tu n'as pas attendu assez longtemps ! Tu dois encore attendre " + minutes + "M" + seconds + "S.");
                return;
            }

            if (checkFly(player, chunk)) {
                player.sendMessage(FlyManager.flyPrefix + "Erreur | Tu ne peux pas fly dans un autre claim que le tien ou ceux que tu as accès !");
                return;
            }

            if (FlyManager.flyMap.containsKey(player)) {
                player.sendMessage(FlyManager.flyPrefix + "Erreur | Ton fly est déjà actif !");
                return;
            }

            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(FlyManager.flyPrefix + "Tu as activé ton fly !");
            FlyManager.flyMap.put(player, System.currentTimeMillis());

        } else if (getArgs()[0].equalsIgnoreCase("info")) {
            if (!FlyManager.flyMap.containsKey(player)) {
                player.sendMessage(FlyManager.flyPrefix + "Erreur | Tu n'as pas encore activé ton fly !");
                return;
            }

            long startFly = FlyManager.flyMap.get(player);
            long timeLeft = getCorePlayer().flyTime - (System.currentTimeMillis() - startFly) / 1000;
            long minutes = ((timeLeft / 60) % 60);
            long seconds = timeLeft % 60;

            player.sendMessage(FlyManager.flyPrefix + "Tu as encore ton fly pendant " + minutes + "M" + seconds + "S.");

        } else if (getArgs()[0].equalsIgnoreCase("infini")) {
            if (checkFly(player, chunk)) {
                player.sendMessage(FlyManager.flyPrefix + "Erreur | Tu ne peux pas fly dans un autre claim que le tien ou ceux que tu as accès !");
                return;
            }

            if (!FlyManager.infiniteFly.contains(player)) {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage(FlyManager.flyPrefix + "Tu as activé ton fly infini !");
                FlyManager.infiniteFly.add(player);
            } else {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage(FlyManager.flyPrefix + "Tu as enlevé ton fly infini !");
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
                FlyManager.infiniteFly.remove(player);
            }
        }
    }

    public boolean checkFly(Player player, ClaimObject chunk) {
        if (chunk == null) {
            return true;
        }

        return !chunk.getPlayerUUID().equalsIgnoreCase(getPlayer().getUniqueId().toString()) && !chunk.access.contains(player.getUniqueId().toString());
    }
}
