package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.BanEnum;
import fr.amisoz.consulatcore.moderation.InventorySanction;
import fr.amisoz.consulatcore.moderation.MuteEnum;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import java.util.UUID;

public class InventoryListeners implements Listener {
    
    public InventoryListeners(ConsulatCore consulatCore){
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPick(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isInModeration() || survivalPlayer.isLookingInventory()){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(!(event.getPlayer() instanceof Player)){
            return;
        }
        Player player = (Player)event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer != null){
            survivalPlayer.setLookingInventory(false);
        }
    }
    
    //A refaire
    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player)event.getWhoClicked();
        
        if(event.getClickedInventory() == null) return;
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        SurvivalPlayer moderator = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(!moderator.hasPower(Rank.MODO)){
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        InventoryView inventoryView = event.getView();
        String inventoryName = inventoryView.getTitle();
        if(moderator.isLookingInventory()){
            event.setCancelled(true);
            return;
        }
        String targetName = moderator.getSanctionTarget();
        if(targetName == null){
            return;
        }
        UUID uuidTarget = CPlayerManager.getInstance().getPlayerUUID(targetName);
        if(uuidTarget == null){
            return;
        }
        if(inventoryName.contains("Sanction")){
            event.setCancelled(true);
            Material material = clickedItem.getType();
            if(material.equals(Material.REDSTONE_BLOCK)){
                // Bannir
                player.openInventory(InventorySanction.banInventory(targetName));
            } else if(material.equals(Material.PAPER)){
                // Mute
                player.openInventory(InventorySanction.muteInventory(targetName));
            }
            return;
        }
        ItemMeta itemMeta = clickedItem.getItemMeta();
        if(!(inventoryName.contains("Sanction")) && !(inventoryName.contains("Mute")) && !(inventoryName.contains("Bannir"))){
            if(moderator.isInModeration()){
                event.setCancelled(true);
            }
            return;
        }
        if(CPlayerManager.getInstance().getPlayerUUID(targetName) == null){
            player.sendMessage("§cLe joueur ne s'est jamais connecté au serveur.");
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetName);
        Long currentTime = System.currentTimeMillis();
        if(inventoryName.contains("Mute")){
            event.setCancelled(true);
            String motifName = Objects.requireNonNull(itemMeta.getLore()).get(1);
            MuteEnum muteReason = MuteEnum.valueOf(motifName);
            Long durationMute = muteReason.getDurationSanction() * 1000;
            Long resultTime = currentTime + durationMute;
            if(target == null){
                try {
                    ConsulatCore.getInstance().getModerationDatabase().addSanction(
                            uuidTarget,
                            targetName, player, "MUTE", muteReason.getSanctionName(),
                            resultTime, currentTime);
                } catch(SQLException e){
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            } else {
                target.setMuted(true);
                target.setMuteExpireMillis(resultTime);
                target.setMuteReason(muteReason.getSanctionName());
    
                target.sendMessage(ChatColor.RED + "Tu as été sanctionné. Tu ne peux plus parler pour : " + ChatColor.DARK_RED + muteReason.getSanctionName());
                try {
                    ConsulatCore.getInstance().getModerationDatabase().addSanction(target.getUUID(), targetName, player, "MUTE", muteReason.getSanctionName(), resultTime, currentTime);
                } catch(SQLException e){
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            }
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                Rank onlineRank = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId()).getRank();
                if(onlineRank.getRankPower() >= Rank.MODO.getRankPower()){
                    sanctionMessage(onlinePlayer, targetName, muteReason.getSanctionName(), muteReason.getFormatDuration(), player.getName(), false);
                }
            });
            Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " " + ChatColor.GOLD + targetName + ChatColor.YELLOW + " a été mute.");
            player.closeInventory();
            return;
        }
        if(inventoryName.contains("Bannir")){
            event.setCancelled(true);
            String motifName = Objects.requireNonNull(itemMeta.getLore()).get(1);
            BanEnum banReason = BanEnum.valueOf(motifName);
            Long durationBan = banReason.getDurationSanction() * 1000;
            Long resultTime = currentTime + durationBan;
            if(target == null){
                try {
                    ConsulatCore.getInstance().getModerationDatabase().addSanction
                            (uuidTarget, targetName, player, "BAN", banReason.getSanctionName(), resultTime, currentTime);
                } catch(SQLException e){
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            } else {
                try {
                    ConsulatCore.getInstance().getModerationDatabase().addSanction(
                            target.getUUID(), targetName, player, "BAN", banReason.getSanctionName(), resultTime, currentTime);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(resultTime);
                    Date date = calendar.getTime();
    
                    target.getPlayer().kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été banni.\n§cRaison : §4" + banReason.getSanctionName() + "\n§cJusqu'au : §4" + ConsulatCore.getInstance().DATE_FORMAT.format(date));
                } catch(SQLException e){
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                    e.printStackTrace();
                    return;
                }
            }
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                Rank onlineRank = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId()).getRank();
                if(onlineRank.getRankPower() >= Rank.MODO.getRankPower()){
                    sanctionMessage(onlinePlayer, targetName, banReason.getSanctionName(), banReason.getFormatDuration(), player.getName(), true);
                }
            });
            Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " " + ChatColor.RED + targetName + ChatColor.DARK_RED + " a été banni.");
            player.closeInventory();
        }
    }
    
    private void sanctionMessage(Player playerToSend, String targetName, String sanctionName, String duration, String modName, boolean isBanned){
        TextComponent textComponent;
        
        if(isBanned){
            textComponent = new TextComponent(Text.MODERATION_PREFIX + ChatColor.RED + targetName + ChatColor.DARK_RED + " a été banni.");
        } else {
            textComponent = new TextComponent(Text.MODERATION_PREFIX + ChatColor.YELLOW + targetName + ChatColor.GOLD + " a été mute.");
        }
        
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GRAY + "Motif : " + ChatColor.DARK_GRAY + sanctionName +
                        ChatColor.GRAY + "\nPendant : " + ChatColor.DARK_GRAY + duration +
                        ChatColor.GRAY + "\nPar : " + ChatColor.DARK_GRAY + modName
                ).create()));
        playerToSend.spigot().sendMessage(textComponent);
    }
}
