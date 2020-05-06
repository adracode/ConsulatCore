package fr.amisoz.consulatcore.moderation;

import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import org.bukkit.ChatColor;

public class MuteGui extends GuiListener {
    
    public MuteGui(){
        super(null, String.class);
    }
    
    @Override
    public void onCreate(GuiCreateEvent guiCreateEvent){
    
    }
    
    @Override
    public void onOpen(GuiOpenEvent guiOpenEvent){
    
    }
    
    @Override
    public void onClose(GuiCloseEvent guiCloseEvent){
    
    }
    
    @Override
    public void onClick(GuiClickEvent guiClickEvent){
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
    }
}
