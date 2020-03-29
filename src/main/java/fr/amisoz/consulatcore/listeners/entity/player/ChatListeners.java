package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.amisoz.consulatcore.utils.CustomEnum;
import fr.leconsulat.api.custom.CustomObject;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Calendar;

public class ChatListeners implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();

        if(!ConsulatCore.chat_activated && PlayersManager.getConsulatPlayer(player).getRank().getRankPower() < RankEnum.RESPONSABLE.getRankPower()){
            player.sendMessage("§cChat coupé.");
            event.setCancelled(true);
        }

        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.persoState == CustomEnum.PREFIX){
            event.setCancelled(true);
            String message = event.getMessage();

            if(message.equalsIgnoreCase("cancel")){
                corePlayer.persoNick = "";
                corePlayer.persoState = CustomEnum.START;
                player.sendMessage("§aChangement de grade annulé.");
                event.setCancelled(true);
                return;
            }

            if(message.length() > 10){
                player.sendMessage("§cTon grade doit faire 10 caractères maximum ! Tape §ocancel §r§csi tu veux annuler.");
                event.setCancelled(true);
                return;
            }

            if(ConsulatCore.forbiddenPerso.contains(message)){
                player.sendMessage("§cTu ne peux pas appeler ton grade comme cela ! Tape §ocancel §r§csi tu veux annuler.");
                event.setCancelled(true);
                return;
            }

            if(!event.getMessage().matches("^[a-zA-Z]+$")){
                player.sendMessage("§cTu dois utiliser uniquement des lettres dans ton grade.");
                event.setCancelled(true);
                return;
            }

            corePlayer.persoNick += event.getMessage() + "]";
            corePlayer.persoState = CustomEnum.NAME_COLOR;
            player.sendMessage("§6Voici ton grade : " + ChatColor.translateAlternateColorCodes('&', corePlayer.persoNick));
            player.sendMessage("§7Maintenant, choisis la couleur de ton pseudo :");
            TextComponent[] textComponents = ConsulatCore.textPerso.toArray(new TextComponent[0]);
            player.spigot().sendMessage(textComponents);
        }

        if(corePlayer.isMuted){
            if(!(System.currentTimeMillis() >= corePlayer.muteExpireMillis)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(corePlayer.muteExpireMillis);
                String resultDate = ConsulatCore.DATE_FORMAT.format(calendar.getTime());
                String reason = corePlayer.muteReason;
                player.sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + reason +"\n§4Jusqu'au : §c" + resultDate);
                event.setCancelled(true);
            }
        }

        ConsulatPlayer consulatPlayer =  PlayersManager.getConsulatPlayer(player);
        RankEnum playerRank = consulatPlayer.getRank();
        if(consulatPlayer.isPerso() && consulatPlayer.getPersoPrefix() != null && !consulatPlayer.getPersoPrefix().equalsIgnoreCase("")){
            event.setFormat(ChatColor.translateAlternateColorCodes('&', consulatPlayer.getPersoPrefix()) + "%s" + ChatColor.GRAY + " : " + ChatColor.WHITE + "%s");
        }else{
            event.setFormat(playerRank.getRankColor() + "[" + playerRank.getRankName() + "] " + "%s" + ChatColor.GRAY + " : " + ChatColor.WHITE + "%s");
        }

        if(playerRank.getRankPower() >= RankEnum.MODO.getRankPower()){
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        }

    }

    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] array = event.getMessage().split(" ");
        String command = array[0];

        if(command.equalsIgnoreCase("/w") || command.equalsIgnoreCase("/whisper") ||command.equalsIgnoreCase("/tell") || command.equalsIgnoreCase("/me") || command.contains("bukkit")){
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cCommande désactivée.");
        }
    }
}
