package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ModerateCommand extends ConsulatCommand {
    
    public ModerateCommand(){
        super(ConsulatCore.getInstance(), "staff");
        setDescription("Switcher de mode entre joueur et staff").
                setUsage("/staff - Switcher de mode").
                setAliases("moderate").
                setRank(Rank.MODO).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        Player bukkitPlayer = sender.getPlayer();
        player.setInModeration(!player.isInModeration());
        if(!player.isInModeration()){
            sender.sendMessage(Text.NO_MORE_IN_STAFF_MODE);
            for(PotionEffect effect : bukkitPlayer.getActivePotionEffects()){
                if(effect.getType().equals(PotionEffectType.NIGHT_VISION) || effect.getType().equals(PotionEffectType.INVISIBILITY)){
                    bukkitPlayer.removePotionEffect(effect.getType());
                }
            }
            for(Player onlinePlayers : Bukkit.getOnlinePlayers()){
                onlinePlayers.showPlayer(ConsulatCore.getInstance(), bukkitPlayer);
            }
            bukkitPlayer.getInventory().setContents(player.getStockedInventory());
            if(bukkitPlayer.getGameMode() == GameMode.SURVIVAL){
                bukkitPlayer.setAllowFlight(false);
                bukkitPlayer.setFlying(false);
            }
        } else {
            sender.sendMessage(Text.NOW_IN_STAFF_MODE);
            player.setVanished(true);
            player.setStockedInventory(bukkitPlayer.getInventory().getContents());
            for(ConsulatPlayer onlinePlayer : CPlayerManager.getInstance().getConsulatPlayers()){
                if(!onlinePlayer.hasPower(Rank.MODO)){
                    onlinePlayer.getPlayer().hidePlayer(ConsulatCore.getInstance(), bukkitPlayer);
                }
            }
            bukkitPlayer.getInventory().clear();
            bukkitPlayer.getInventory().setHelmet(null);
            bukkitPlayer.getInventory().setChestplate(null);
            bukkitPlayer.getInventory().setLeggings(null);
            bukkitPlayer.getInventory().setBoots(null);
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2, false, false));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false));
            // Tp aléatoire, vanish/devanish, see inv du joueur sur lequel on clique
            ItemStack randomTeleport = new ItemStack(Material.ENDER_EYE);
            ItemMeta randomMeta = randomTeleport.getItemMeta();
            randomMeta.setDisplayName("§6Se téléporter aléatoirement");
            randomTeleport.setItemMeta(randomMeta);
            
            ItemStack vanish = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta vanishMeta = vanish.getItemMeta();
            vanishMeta.setDisplayName("§cChanger son statut d'invisibilité");
            vanish.setItemMeta(vanishMeta);
            
            ItemStack invsee = new ItemStack(Material.PAPER);
            ItemMeta invseeMeta = invsee.getItemMeta();
            invseeMeta.setDisplayName("§aVoir l'inventaire");
            invsee.setItemMeta(invseeMeta);
            
            ItemStack freeze = new ItemStack(Material.PACKED_ICE);
            ItemMeta freezeMeta = freeze.getItemMeta();
            freezeMeta.setDisplayName("§bFreeze");
            freeze.setItemMeta(freezeMeta);
            
            ItemStack seeBelow16 = new ItemStack(Material.DIAMOND_ORE);
            ItemMeta seeBelow16Meta = seeBelow16.getItemMeta();
            seeBelow16Meta.setDisplayName("§eJoueurs < 16");
            seeBelow16.setItemMeta(seeBelow16Meta);

            bukkitPlayer.getInventory().setItem(2, freeze);
            bukkitPlayer.getInventory().setItem(3, randomTeleport);
            bukkitPlayer.getInventory().setItem(4, vanish);
            bukkitPlayer.getInventory().setItem(5, invsee);
            bukkitPlayer.getInventory().setItem(6, seeBelow16);

            if(player.isFlying()){
                player.disableFly();
            } else {
                bukkitPlayer.setAllowFlight(true);
                bukkitPlayer.setFlying(true);
            }
        }
    }
}
