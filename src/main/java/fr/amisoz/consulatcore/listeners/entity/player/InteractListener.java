package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Random;

public class InteractListener implements Listener {

    private ConsulatCore consulatCore;

    public InteractListener(ConsulatCore consulatCore) {
        this.consulatCore = consulatCore;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() != Material.OAK_WALL_SIGN) {
                return;
            }
            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] lines = sign.getLines();
            if (lines[0].contains("§9[Téléportation]")) {
                try {
                    int x = Integer.parseInt(lines[1]);
                    int y = Integer.parseInt(lines[2]);
                    int z = Integer.parseInt(lines[3]);
                    Location result = new Location(Bukkit.getWorlds().get(0), x, y, z);
                    player.teleport(result);
                    player.sendMessage("§aTu as été téléporté à la zone.");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cErreur de coordonnées");
                }
            }
        }
        if(event.getItem() == null) return;
        if(event.getItem().getItemMeta() == null) return;

        ItemStack item = event.getItem();
        ItemMeta itemMeta = item.getItemMeta();

        if(corePlayer.isModerate()){
            Action action = event.getAction();

            if(itemMeta.getDisplayName().contains("Se téléporter aléatoirement")) {
                ArrayList<Player> playersOnline = new ArrayList<>(Bukkit.getOnlinePlayers());
                playersOnline.remove(player);
                Player resultedPlayer = playersOnline.get(new Random().nextInt(playersOnline.size()));
                player.teleport(resultedPlayer);
                player.sendMessage(ChatColor.GREEN + "Tu as été téléporté à : " + resultedPlayer.getName());
                event.setCancelled(true);
            }

            if(itemMeta.getDisplayName().contains("Changer son statut d'invisibilité")) {
                if(ModerationUtils.vanishedPlayers.contains(player)){
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        onlinePlayer.showPlayer(consulatCore, player);
                    });
                    ModerationUtils.vanishedPlayers.remove(player);
                    player.sendMessage("§aTu es désormais visible.");
                }else{
                    ModerationUtils.vanishedPlayers.add(player);
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        if(PlayersManager.getConsulatPlayer(player).getRank().getRankPower() < RankEnum.MODO.getRankPower()) {
                            onlinePlayer.hidePlayer(consulatCore, player);
                        }
                    });
                    player.sendMessage("§cTu es désormais invisible.");
                }
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent event){
        if(!(event.getRightClicked() instanceof Player)) return;
        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();

        if(!ModerationUtils.moderatePlayers.contains(player)) return;

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(itemStack.getItemMeta() == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();

        if(itemMeta.getDisplayName().contains("Voir l'inventaire")) {
            Inventory inventory = target.getInventory();
            player.openInventory(inventory);
        }

        if(itemMeta.getDisplayName().contains("Freeze")) {
            CorePlayer coreTarget = CoreManagerPlayers.getCorePlayer(target);
            if(coreTarget.isFreezed){
                target.sendMessage(ModerationUtils.ANNOUNCE_PREFIX + "Tu as été un-freeze.");
                player.sendMessage(ModerationUtils.ANNOUNCE_PREFIX + "Joueur un-freeze");
            }else{
                target.sendMessage(ModerationUtils.ANNOUNCE_PREFIX + "Tu as été freeze par un modérateur.");
                player.sendMessage(ModerationUtils.ANNOUNCE_PREFIX + "Joueur freeze");
            }
            coreTarget.isFreezed = !coreTarget.isFreezed;
        }
    }

}
