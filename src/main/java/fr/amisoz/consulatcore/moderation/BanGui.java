package fr.amisoz.consulatcore.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BanGui extends GuiListener {
    
    public BanGui(){
        super(null, String.class);
        Gui defaultGui = addGui(null, this, "§c§lBannir§7 §7↠ §4", 3);
        BanEnum[] values = BanEnum.values();
        for(int i = 0; i < values.length; ++i){
            BanEnum ban = values[i];
            GuiItem item = getItem("§c" + ban.getSanctionName(), i, ban.getGuiMaterial(),
                    "§cDurée : §4" + ban.getFormatDuration());
            item.setAttachedObject(ban);
            defaultGui.setItem(i, item);
        }
        setCreateOnOpen(true);
    }
    
    @Override
    public void onCreate(GuiCreateEvent event){
        event.getGui().setName("§c§lBannir§7 §7↠ §4" + ((ConsulatPlayer)event.getKey()).getName());
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
    
    }
    
    @Override
    public void onClose(GuiCloseEvent event){
    
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        long currentTime = System.currentTimeMillis();
        BanEnum ban = (BanEnum)event.getGui().getItem(event.getSlot()).getAttachedObject();
        long durationBan = ban.getDurationSanction() * 1000;
        long resultTime = currentTime + durationBan;
        String targetName = (String)event.getGui().getKey();
        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(targetName);
        ConsulatPlayer banner = event.getPlayer();
        ConsulatPlayer target = CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ConsulatCore.getInstance().getModerationDatabase().addSanction(
                        targetUUID, targetName, banner.getPlayer(), "BAN", ban.getSanctionName(), resultTime, currentTime);
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    if(target != null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(resultTime);
                        Date date = calendar.getTime();
                        target.getPlayer().kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été banni.\n§cRaison : §4" + ban.getSanctionName() + "\n§cJusqu'au : §4" + ConsulatCore.getInstance().DATE_FORMAT.format(date));
                    }
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        Rank onlineRank = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId()).getRank();
                        if(onlineRank.getRankPower() >= Rank.MODO.getRankPower()){
                            sanctionMessage(onlinePlayer, targetName, ban.getSanctionName(), ban.getFormatDuration(), banner.getName());
                        }
                    });
                    Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " " + ChatColor.RED + targetName + ChatColor.DARK_RED + " a été banni.");
    
                });
            } catch(SQLException e){
                banner.sendMessage("§cErreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                e.printStackTrace();
            }
        });
        banner.getPlayer().closeInventory();
    }
    
    private void sanctionMessage(Player playerToSend, String targetName, String sanctionName, String duration, String modName){
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + "§c" + targetName + "§4 a été banni.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Motif : §8" + sanctionName +
                        "§7\nPendant : §8" + duration +
                        "§7\nPar : §8" + modName
                ).create()));
        playerToSend.spigot().sendMessage(textComponent);
    }
    
}
