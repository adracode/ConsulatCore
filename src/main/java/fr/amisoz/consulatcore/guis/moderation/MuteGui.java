package fr.amisoz.consulatcore.guis.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.MuteReason;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MuteGui extends DataRelatGui<ConsulatOffline> {

    public MuteGui(ConsulatOffline player) {
        super(player, "§6§lMute", 3);
    }

    @Override
    public void onOpen(GuiOpenEvent event) {
       
        ConsulatOffline consulatOffline = getData();
        Player target = Bukkit.getPlayer(consulatOffline.getUUID());
        Player moderator = event.getPlayer().getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            HashMap<MuteReason, Integer> muteHistory;

            if (target == null) {
                try {
                    muteHistory = getMuteHistory(consulatOffline);
                } catch (SQLException e) {
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        moderator.sendMessage(Text.ERROR);
                        moderator.closeInventory();
                        e.printStackTrace();
                    });
                    return;
                }
            } else {
                SurvivalPlayer survivalPlayer = (SurvivalPlayer) CPlayerManager.getInstance().getConsulatPlayer(target.getUniqueId());
                muteHistory = survivalPlayer.getMuteHistory();
            }

            MuteReason[] values = MuteReason.values();
            for (int i = 0; i < values.length; ++i) {
                MuteReason mute = values[i];
                GuiItem item = IGui.getItem("§e" + mute.getSanctionName(), i, mute.getGuiMaterial());

                item.setDescription("§eDurée: §6" + mute.getFormatDuration(), "§7Récidive: " + (muteHistory.containsKey(mute) ? muteHistory.get(mute) : "0"));
                item.setAttachedObject(mute);
                setItem(i, item);
            }
        });
    }

    @Override
    public void onClose(GuiCloseEvent event) {
        event.setOpenFatherGui(false);
    }

    @Override
    public void onClick(GuiClickEvent event) {
        ConsulatOffline offlineTarget = getData();
        SurvivalPlayer target = (SurvivalPlayer) CPlayerManager.getInstance().getConsulatPlayer(offlineTarget.getUUID());
        ConsulatPlayer muter = event.getPlayer();
        if (target != null && target.isMuted()) {
            event.getPlayer().sendMessage(Text.ALREADY_MUTED);
            event.getPlayer().getPlayer().closeInventory();
            return;
        }

        GuiItem item = getItem(event.getSlot());
        List<String> description = item.getDescription();
        String recidive = description.get(1).split(":")[1].trim();
        int recidiveNumber = Integer.parseInt(recidive);
        double multiply = recidiveNumber * 1.5;

        if (multiply == 0) multiply = 1;

        long currentTime = System.currentTimeMillis();
        MuteReason muteReason = (MuteReason) getItem(event.getSlot()).getAttachedObject();
        double durationMute = (muteReason.getDurationSanction() * 1000) * multiply;
        long resultTime = currentTime + Math.round(durationMute);
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ConsulatCore.getInstance().getModerationDatabase().addSanction(
                        offlineTarget.getUUID(), offlineTarget.getName(), muter.getPlayer(),
                        "MUTE", muteReason.getSanctionName(), resultTime, currentTime);
                if (target != null){
                    target.setMuted(true);
                    target.setMuteExpireMillis(resultTime);
                    target.setMuteReason(muteReason.getSanctionName());
                    target.sendMessage("§cTu as été sanctionné. Tu ne peux plus parler pour: §4" + muteReason.getSanctionName());
                    if (target.getMuteHistory().containsKey(muteReason)) {
                        int number = target.getMuteHistory().get(muteReason);
                        target.getMuteHistory().put(muteReason, ++number);
                    } else {
                        target.getMuteHistory().put(muteReason, 1);
                    }
                }
            } catch (SQLException e) {
                muter.sendMessage(Text.ERROR);
                e.printStackTrace();
            }
        });
        for (ConsulatPlayer onlinePlayer : CPlayerManager.getInstance().getConsulatPlayers()) {
            if (onlinePlayer.hasPower(Rank.MODO)) {
                long durationRound = Math.round(durationMute);
                long days = ((durationRound / (1000*60*60*24)));
                long hours = ((durationRound / (1000 * 60 * 60)) % 24);
                long minutes = ((durationRound / (1000 * 60)) % 60);
                onlinePlayer.sendMessage(Text.SANCTION_MUTED(offlineTarget.getName(), muteReason.getSanctionName(), days + "J" + hours + "H" + minutes + "M", muter.getName(), recidiveNumber));
            }
        }
        Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " §6" + offlineTarget.getName() + " §ea été mute.");
        muter.getPlayer().closeInventory();
    }

    private HashMap<MuteReason, Integer> getMuteHistory(ConsulatOffline consulatOffline) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT reason FROM antecedents WHERE playeruuid = ? AND sanction = 'MUTE' AND cancelled = 0");
        preparedStatement.setString(1, consulatOffline.getUUID().toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        HashMap<MuteReason, Integer> muteHistory = new HashMap<>();
        while (resultSet.next()) {
            String reason = resultSet.getString("reason");
            MuteReason muteReason = Arrays.stream(MuteReason.values()).filter(ban -> ban.getSanctionName().equals(reason)).findFirst().orElse(null);

            if (muteReason != null) {
                if (muteHistory.containsKey(muteReason)) {
                    int number = muteHistory.get(muteReason);
                    muteHistory.put(muteReason, ++number);
                } else {
                    muteHistory.put(muteReason, 1);
                }
            }
        }

        return muteHistory;
    }
}
