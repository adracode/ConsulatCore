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
import fr.leconsulat.api.player.ConsulatOffline;
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

public class BanGui extends GuiListener {
    
    public BanGui(GuiListener father){
        super(father, ConsulatOffline.class);
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
        if(event.getKey() == null){
            return;
        }
        event.getGui().setName("§c§lBannir§7 §7↠ §4" + ((ConsulatOffline)event.getKey()).getName());
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
        BanEnum ban = (BanEnum)event.getGui().getItem(event.getSlot()).getAttachedObject();
        long durationBan = ban.getDurationSanction() * 1000;
        long resultTime = currentTime + durationBan;
        ConsulatPlayer banner = event.getPlayer();
        ConsulatOffline offlineTarget = (ConsulatOffline)event.getGui().getKey();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ConsulatCore.getInstance().getModerationDatabase().addSanction(
                        offlineTarget.getUUID(), offlineTarget.getName(), banner.getPlayer(), "BAN", ban.getSanctionName(), resultTime, currentTime);
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    ConsulatPlayer target = CPlayerManager.getInstance().getConsulatPlayer(offlineTarget.getUUID());
                    if(target != null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(resultTime);
                        Date date = calendar.getTime();
                        target.getPlayer().kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été banni.\n§cRaison : §4" + ban.getSanctionName() + "\n§cJusqu'au : §4" + ConsulatCore.getInstance().DATE_FORMAT.format(date));
                    }
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                        if(consulatPlayer != null){
                            Rank onlineRank = consulatPlayer.getRank();
                            if(onlineRank.getRankPower() >= Rank.MODO.getRankPower()){
                                sanctionMessage(onlinePlayer, offlineTarget.getName(), ban.getSanctionName(), ban.getFormatDuration(), banner.getName());
                            }
                        }
                    });
                    Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " " + ChatColor.RED + offlineTarget.getName() + ChatColor.DARK_RED + " a été banni.");
    
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
