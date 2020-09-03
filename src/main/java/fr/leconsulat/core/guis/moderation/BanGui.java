package fr.leconsulat.core.guis.moderation;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCloseEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatOffline;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.moderation.BanReason;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BanGui extends DataRelatGui<ConsulatOffline> {
    
    public BanGui(ConsulatOffline player){
        super(player, "§c§lBannir", 3);
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
        ConsulatOffline consulatOffline = getData();
        Player target = Bukkit.getPlayer(consulatOffline.getUUID());
        Player moderator = event.getPlayer().getPlayer();
        
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            HashMap<BanReason, Integer> banHistory;
            
            if(target == null){
                try {
                    banHistory = getBanHistory(consulatOffline);
                } catch(SQLException e){
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        moderator.sendMessage(Text.ERROR);
                        moderator.closeInventory();
                        e.printStackTrace();
                    });
                    return;
                }
            } else {
                SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(target.getUniqueId());
                banHistory = survivalPlayer.getBanHistory();
            }
            
            
            BanReason[] values = BanReason.values();
            for(int i = 0; i < values.length; ++i){
                BanReason ban = values[i];
                GuiItem item = IGui.getItem("§c" + ban.getSanctionName(), i, ban.getGuiMaterial());
                
                item.setDescription("§cDurée: §4" + ban.getFormatDuration(), "§7Récidive: " + (banHistory.containsKey(ban) ? banHistory.get(ban) : "0"));
                item.setAttachedObject(ban);
                setItem(i, item);
            }
        });
    }
    
    @Override
    public void onClose(GuiCloseEvent event){
        event.setOpenFatherGui(false);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        GuiItem item = getItem(event.getSlot());
        List<String> description = item.getDescription();
        String recidive = description.get(1).split(":")[1].trim();
        int recidiveNumber = Integer.parseInt(recidive);
        double multiply = recidiveNumber * 1.5;
        
        if(multiply == 0) multiply = 1;
        
        long currentTime = System.currentTimeMillis();
        BanReason ban = (BanReason)getItem(event.getSlot()).getAttachedObject();
        double durationBan = (ban.getDurationSanction() * 1000) * multiply;
        long resultTime = currentTime + Math.round(durationBan);
        ConsulatPlayer banner = event.getPlayer();
        ConsulatOffline offlineTarget = getData();
        
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
                        target.getPlayer().kickPlayer(Text.KICK_PLAYER("§4" + ban.getSanctionName() + "\n§cJusqu'au: §4" + ConsulatCore.getInstance().DATE_FORMAT.format(date)));
                    }
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                        if(consulatPlayer != null){
                            Rank onlineRank = consulatPlayer.getRank();
                            if(onlineRank.getRankPower() >= Rank.MODO.getRankPower()){
                                long durationRound = Math.round(durationBan);
                                long days = ((durationRound / (1000 * 60 * 60 * 24)));
                                long hours = ((durationRound / (1000 * 60 * 60)) % 24);
                                long minutes = ((durationRound / (1000 * 60)) % 60);
                                consulatPlayer.sendMessage(Text.SANCTION_BANNED(offlineTarget.getName(), ban.getSanctionName(), days + "J" + hours + "H" + minutes + "M", banner.getName(), recidiveNumber));
                            }
                        }
                    });
                    Bukkit.broadcastMessage(Text.PLAYER_BANNED(offlineTarget.getName()));
                });
            } catch(SQLException e){
                banner.sendMessage(Text.ERROR);
                e.printStackTrace();
            }
        });
        banner.getPlayer().closeInventory();
    }
    
    private HashMap<BanReason, Integer> getBanHistory(ConsulatOffline consulatOffline) throws SQLException{
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT reason FROM antecedents WHERE playeruuid = ? AND sanction = 'BAN' AND cancelled = 0");
        preparedStatement.setString(1, consulatOffline.getUUID().toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        
        HashMap<BanReason, Integer> banHistory = new HashMap<>();
        while(resultSet.next()){
            String reason = resultSet.getString("reason");
            BanReason banReason = Arrays.stream(BanReason.values()).filter(ban -> ban.getSanctionName().equals(reason)).findFirst().orElse(null);
            
            if(banReason != null){
                if(banHistory.containsKey(banReason)){
                    int number = banHistory.get(banReason);
                    banHistory.put(banReason, ++number);
                } else {
                    banHistory.put(banReason, 1);
                }
            }
        }
        
        return banHistory;
    }
}

