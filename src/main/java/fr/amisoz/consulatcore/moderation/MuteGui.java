package fr.amisoz.consulatcore.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatOffline;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.SQLException;

public class MuteGui extends GuiListener {
    
    public MuteGui(GuiListener father){
        super(father, ConsulatOffline.class);
        Gui defaultGui = addGui(null, this, "§6§lMute§7 ↠ §e", 3);
        MuteEnum[] values = MuteEnum.values();
        for(int i = 0; i < values.length; ++i){
            MuteEnum mute = values[i];
            GuiItem item = getItem("§6" + mute.getSanctionName(), i, mute.getGuiMaterial(),
                    "§6Durée : §e" + mute.getFormatDuration());
            item.setAttachedObject(mute);
            defaultGui.setItem(i, item);
        }
        setCreateOnOpen(true);
    }
    
    @Override
    public void onCreate(GuiCreateEvent event){
        if(event.getKey() == null){
            return;
        }
        event.getGui().setName("§6§lMute§7 ↠ §e" + ((ConsulatOffline)event.getKey()).getName());
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
    
    }
    
    @Override
    public void onClose(GuiCloseEvent event){
        event.setOpenFatherGui(false);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        long currentTime = System.currentTimeMillis();
        MuteEnum muteReason = (MuteEnum)event.getGui().getItem(event.getSlot()).getAttachedObject();
        long durationMute = muteReason.getDurationSanction() * 1000;
        long resultTime = currentTime + durationMute;
        ConsulatOffline offlineTarget = (ConsulatOffline)event.getGui().getKey();
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(offlineTarget.getUUID());
        ConsulatPlayer muter = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ConsulatCore.getInstance().getModerationDatabase().addSanction(
                        offlineTarget.getUUID(), offlineTarget.getName(), muter.getPlayer(),
                        "MUTE", muteReason.getSanctionName(), resultTime, currentTime);
                if(target != null){
                    target.setMuted(true);
                    target.setMuteExpireMillis(resultTime);
                    target.setMuteReason(muteReason.getSanctionName());
                    target.sendMessage("§cTu as été sanctionné. Tu ne peux plus parler pour : §4" + muteReason.getSanctionName());
                }
            } catch(SQLException e){
                muter.sendMessage("§cErreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                e.printStackTrace();
            }
        });
        for(ConsulatPlayer onlinePlayer : CPlayerManager.getInstance().getConsulatPlayers()){
            if(onlinePlayer.hasPower(Rank.MODO)){
                sanctionMessage(onlinePlayer, offlineTarget.getName(), muteReason.getSanctionName(), muteReason.getFormatDuration(), muter.getName());
            }
        }
        Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " §6" + offlineTarget.getName() + " §ea été mute.");
        muter.getPlayer().closeInventory();
    }
    
    private void sanctionMessage(ConsulatPlayer playerToSend, String targetName, String sanctionName, String duration, String modName){
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + ChatColor.YELLOW + targetName + ChatColor.GOLD + " a été mute.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Motif : §8" + sanctionName +
                        "§7\nPendant : §8" + duration +
                        "§7\nPar : §8" + modName
                ).create()));
        playerToSend.sendMessage(textComponent);
    }
    
}
