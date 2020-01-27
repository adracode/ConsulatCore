package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.moderation.BanEnum;
import fr.amisoz.consulatcore.moderation.InventorySanction;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.moderation.MuteEnum;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class InventoryListeners implements Listener {

    private ConsulatCore consulatCore;

    public InventoryListeners(ConsulatCore consulatCore) {
        this.consulatCore = consulatCore;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isModerate()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPick(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isModerate()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isModerate()){
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if(event.getClickedInventory() == null) return;
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        CorePlayer moderator = CoreManagerPlayers.getCorePlayer(player);
        InventoryView inventoryView = event.getView();
        String inventoryName = inventoryView.getTitle();


        String targetName = moderator.getSanctionTarget();

        if(inventoryName.contains("Sanction")){
            event.setCancelled(true);
            Material material = clickedItem.getType();
            if(material.equals(Material.REDSTONE_BLOCK)){
                // Bannir
                player.openInventory(InventorySanction.banInventory(targetName));
            }else if(material.equals(Material.PAPER)){
                // Mute
                player.openInventory(InventorySanction.muteInventory(targetName));
            }

            return;
        }

        ItemMeta itemMeta = clickedItem.getItemMeta();

        if(!(inventoryName.contains("Sanction")) && !(inventoryName.contains("Mute")) && !(inventoryName.contains("Bannir"))) {
            if(moderator.isModerate()){
                event.setCancelled(true);
            }
            return;
        }

        try {
            if(!consulatCore.getRankDatabase().hasAccount(consulatCore.getRankDatabase().getUUID(targetName))){
                player.sendMessage(ChatColor.RED + "Le joueur ne s'est jamais connecté au serveur.");
                return;
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (RETRIEVE_DB_INFO)");
            e.printStackTrace();
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        Long actuelTime = System.currentTimeMillis();

        if(inventoryName.contains("Mute")){
            event.setCancelled(true);
            String motifName = Objects.requireNonNull(itemMeta.getLore()).get(1);
            MuteEnum muteReason = MuteEnum.valueOf(motifName);


            Long durationMute = muteReason.getDurationSanction() * 1000;
            Long resultTime = actuelTime + durationMute;

            if(targetPlayer == null){
                try {
                    consulatCore.getModerationDatabase().addSanction(consulatCore.getRankDatabase().getUUID(targetName), targetName, player, "MUTE", muteReason.getSanctionName(),resultTime , actuelTime);
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            }else{
                CorePlayer targetConsulat = CoreManagerPlayers.getCorePlayer(targetPlayer);
                targetConsulat.isMuted = true;
                targetConsulat.muteExpireMillis = resultTime;
                targetConsulat.muteReason = muteReason.getSanctionName();

                targetPlayer.sendMessage(ChatColor.RED + "Tu as été sanctionné. Tu ne peux plus parler pour : " + ChatColor.DARK_RED + muteReason.getSanctionName());
                try {
                    consulatCore.getModerationDatabase().addSanction(targetPlayer.getUniqueId().toString(), targetName, player, "MUTE", muteReason.getSanctionName(),resultTime , actuelTime);
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            }

            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                RankEnum onlineRank = PlayersManager.getConsulatPlayer(onlinePlayer).getRank();
                if(onlineRank.getRankPower() >= RankEnum.MODO.getRankPower()){
                    sanctionMessage(onlinePlayer, targetName, muteReason.getSanctionName(), muteReason.getFormatDuration(), player.getName(), false);
                }
            });
            Bukkit.broadcastMessage(ModerationUtils.ANNOUNCE_PREFIX + " " + ChatColor.GOLD + targetName + ChatColor.YELLOW + " a été mute.");
            player.closeInventory();
            return;
        }

        if(inventoryName.contains("Bannir")){
            event.setCancelled(true);
            String motifName = Objects.requireNonNull(itemMeta.getLore()).get(1);
            BanEnum banReason = BanEnum.valueOf(motifName);

            Long durationBan = banReason.getDurationSanction() * 1000;
            Long resultTime = actuelTime + durationBan;

            if(targetPlayer == null){
                try {
                    consulatCore.getModerationDatabase().addSanction(consulatCore.getRankDatabase().getUUID(targetName), targetName, player, "BAN", banReason.getSanctionName(),resultTime , actuelTime);
                } catch (SQLException e) {
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            }else{
                try {
                    consulatCore.getModerationDatabase().addSanction(targetPlayer.getUniqueId().toString(), targetName, player, "BAN", banReason.getSanctionName(),resultTime , actuelTime);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(resultTime);
                    Date date = calendar.getTime();

                    targetPlayer.kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été banni.\n§cRaison : §4" + banReason.getSanctionName() + "\n§cJusqu'au : §4" + ConsulatCore.DATE_FORMAT.format(date));
                }catch (SQLException e)  {
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            }
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                RankEnum onlineRank = PlayersManager.getConsulatPlayer(onlinePlayer).getRank();
                if(onlineRank.getRankPower() >= RankEnum.MODO.getRankPower()){
                    sanctionMessage(onlinePlayer, targetName, banReason.getSanctionName(), banReason.getFormatDuration(), player.getName(), true);
                }
            });
            Bukkit.broadcastMessage(ModerationUtils.ANNOUNCE_PREFIX + " " + ChatColor.RED + targetName + ChatColor.DARK_RED + " a été banni.");
            player.closeInventory();
        }
    }

    private void sanctionMessage(Player playerToSend, String targetName, String sanctionName, String duration, String modName, boolean isBanned){
        TextComponent textComponent;

        if(isBanned){
            textComponent = new TextComponent(ModerationUtils.MODERATION_PREFIX + ChatColor.RED + targetName + ChatColor.DARK_RED + " a été banni.");
        }else{
            textComponent = new TextComponent(ModerationUtils.MODERATION_PREFIX + ChatColor.YELLOW + targetName + ChatColor.GOLD + " a été mute.");
        }

        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GRAY + "Motif : " + ChatColor.DARK_GRAY + sanctionName +
                        ChatColor.GRAY + "\nPendant : " + ChatColor.DARK_GRAY + duration +
                        ChatColor.GRAY + "\nPar : " + ChatColor.DARK_GRAY + modName
                ).create()));
        playerToSend.spigot().sendMessage(textComponent);
    }
}
