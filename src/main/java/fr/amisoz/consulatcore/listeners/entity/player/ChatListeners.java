package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.utils.CustomEnum;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.Calendar;

public class ChatListeners implements Listener {
    
    public ChatListeners(){
    }
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(!ConsulatCore.getInstance().isChatActivated() && !player.hasPower(Rank.RESPONSABLE)){
            player.sendMessage("§cChat coupé.");
            event.setCancelled(true);
        }
        if(player.getPersoState() == CustomEnum.PREFIX){
            event.setCancelled(true);
            String message = event.getMessage();
            if(message.equalsIgnoreCase("cancel")){
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        player.resetCustomRank();
                    } catch(SQLException e){
                        e.printStackTrace();
                    }
                });
                player.setPersoState(CustomEnum.START);
                player.sendMessage("§aChangement de grade annulé.");
                event.setCancelled(true);
                return;
            }
            if(message.length() > 10){
                player.sendMessage("§cTon grade doit faire 10 caractères maximum ! Tape §ocancel §r§csi tu veux annuler.");
                event.setCancelled(true);
                return;
            }
            if(ConsulatCore.getInstance().isCustomRankForbidden(event.getMessage())){
                player.sendMessage("§cTu ne peux pas appeler ton grade comme cela ! Tape §ocancel §r§csi tu veux annuler.");
                event.setCancelled(true);
                return;
            }
            if(!event.getMessage().matches("^[a-zA-Z]+$")){
                player.sendMessage("§cTu dois utiliser uniquement des lettres dans ton grade.");
                event.setCancelled(true);
                return;
            }
            player.setPrefix(event.getMessage());
            player.setPersoState(CustomEnum.NAME_COLOR);
            player.sendMessage("§6Voici ton grade : " + ChatColor.translateAlternateColorCodes('&', player.getCustomPrefix()));
            player.sendMessage("§7Maintenant, choisis la couleur de ton pseudo :");
            TextComponent[] textComponents = ConsulatCore.getInstance().getTextPerso().toArray(new TextComponent[0]);
            player.sendMessage(textComponents);
        }
        if(player.isMuted()){
            if(!(System.currentTimeMillis() >= player.getMuteExpireMillis())){
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(player.getMuteExpireMillis());
                String resultDate = ConsulatCore.getInstance().DATE_FORMAT.format(calendar.getTime());
                String reason = player.getMuteReason();
                player.sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + reason + "\n§4Jusqu'au : §c" + resultDate);
                event.setCancelled(true);
            }
        }
        Rank playerRank = player.getRank();
        if(player.getCustomRank() != null){
            event.setFormat(ChatColor.translateAlternateColorCodes('&', player.getCustomRank()) + "%s" + ChatColor.GRAY + " : " + ChatColor.WHITE + "%s");
        } else {
            event.setFormat(playerRank.getRankColor() + "[" + playerRank.getRankName() + "] " + "%s" + ChatColor.GRAY + " : " + ChatColor.WHITE + "%s");
        }
        if(player.hasPower(Rank.MODO)){
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        }
    }
}
