package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ModerateCommand extends ConsulatCommand {

    private ConsulatCore consulatCore;

    public ModerateCommand(ConsulatCore consulatCore) {
        super("/staff", 0, RankEnum.MODO);
        this.consulatCore = consulatCore;
    }

    @Override
    public void consulatCommand() {

        if(getCorePlayer().isModerate()){
            getPlayer().sendMessage(ModerationUtils.MODERATION_PREFIX + ChatColor.RED + "Tu n'es plus en mode modérateur.");
            ModerationUtils.moderatePlayers.remove(getPlayer());
            ModerationUtils.vanishedPlayers.remove(getPlayer());

            for(PotionEffect effect  : getPlayer().getActivePotionEffects()){
                if(effect.getType().equals(PotionEffectType.NIGHT_VISION)){
                    getPlayer().removePotionEffect(effect.getType());
                }
            }

            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                onlinePlayer.showPlayer(consulatCore, getPlayer());
            });

            getPlayer().getInventory().setContents(getCorePlayer().stockedInventory);

            if(getPlayer().getGameMode() == GameMode.SURVIVAL) {
                getPlayer().setAllowFlight(false);
                getPlayer().setFlying(false);
            }
        }else{
            getPlayer().sendMessage(ModerationUtils.MODERATION_PREFIX + ChatColor.GREEN + "Tu es désormais en mode modérateur.");
            ModerationUtils.moderatePlayers.add(getPlayer());
            ModerationUtils.vanishedPlayers.add(getPlayer());

            getCorePlayer().stockedInventory = getPlayer().getInventory().getContents();

            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if(onlinePlayer != getPlayer()) {
                    ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(onlinePlayer);
                    if (consulatPlayer.getRank().getRankPower() < RankEnum.MODO.getRankPower()) {
                        onlinePlayer.hidePlayer(consulatCore, getPlayer());
                    }
                }
            });

            getPlayer().getInventory().clear();
            getPlayer().getInventory().setHelmet(null);
            getPlayer().getInventory().setChestplate(null);
            getPlayer().getInventory().setLeggings(null);
            getPlayer().getInventory().setBoots(null);

            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2, false, false));
            // Tp aléatoire, vanish/devanish, see inv du joueur sur lequel on clique
            ItemStack randomTeleport = new ItemStack(Material.ENDER_EYE);
            ItemMeta randomMeta = randomTeleport.getItemMeta();
            assert randomMeta != null;
            randomMeta.setDisplayName("§6Se téléporter aléatoirement");
            randomTeleport.setItemMeta(randomMeta);

            ItemStack vanish = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta vanishMeta = vanish.getItemMeta();
            assert vanishMeta != null;
            vanishMeta.setDisplayName("§cChanger son statut d'invisibilité");
            vanish.setItemMeta(vanishMeta);

            ItemStack invsee = new ItemStack(Material.PAPER);
            ItemMeta invseeMeta = invsee.getItemMeta();
            assert invseeMeta != null;
            invseeMeta.setDisplayName("§aVoir l'inventaire");
            invsee.setItemMeta(invseeMeta);

            ItemStack freeze = new ItemStack(Material.PACKED_ICE);
            ItemMeta freezeMeta = freeze.getItemMeta();
            assert freezeMeta != null;
            invseeMeta.setDisplayName("§bFreeze");
            freeze.setItemMeta(invseeMeta);

            getPlayer().getInventory().setItem(3, randomTeleport);
            getPlayer().getInventory().setItem(4, vanish);
            getPlayer().getInventory().setItem(5, invsee);
            getPlayer().getInventory().setItem(6, freeze);

            getPlayer().setAllowFlight(true);
            getPlayer().setFlying(true);
        }


        getCorePlayer().setModerate(!getCorePlayer().isModerate());
    }
}
