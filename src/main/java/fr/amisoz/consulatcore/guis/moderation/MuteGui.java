package fr.amisoz.consulatcore.guis.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.MuteEnum;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MuteGui extends GuiListener<ConsulatOffline> {

    public MuteGui() {
        super(3);
        setTemplate("§6§lMute");
    }

    @Override
    public void onPageCreate(PagedGuiCreateEvent<ConsulatOffline> event) {
        event.getPagedGui().setName("§6§lMute" + event.getData().getName());
    }

    @Override
    public void onOpen(GuiOpenEvent<ConsulatOffline> event) {
        if (event.getData() == null) {
            return;
        }
        ConsulatOffline consulatOffline = event.getData();
        Player target = Bukkit.getPlayer(consulatOffline.getUUID());
        Player moderator = event.getPlayer().getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            HashMap<MuteEnum, Integer> muteHistory;

            if (target == null) {
                try {
                    muteHistory = getMuteHistory(consulatOffline);
                } catch (SQLException e) {
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        moderator.sendMessage(ChatColor.RED + "Erreur lors du chargement du menu.");
                        moderator.closeInventory();
                        e.printStackTrace();
                    });
                    return;
                }
            } else {
                SurvivalPlayer survivalPlayer = (SurvivalPlayer) CPlayerManager.getInstance().getConsulatPlayer(target.getUniqueId());
                muteHistory = survivalPlayer.getMuteHistory();
            }

            MuteEnum[] values = MuteEnum.values();
            for (int i = 0; i < values.length; ++i) {
                MuteEnum mute = values[i];
                GuiItem item = getItem("§e" + mute.getSanctionName(), i, mute.getGuiMaterial());

                item.setDescription("§eDurée : §6" + mute.getFormatDuration(), "§7Récidive : " + (muteHistory.containsKey(mute) ? muteHistory.get(mute) : "0"));
                item.setAttachedObject(mute);
                event.getPagedGui().setItem(i, item);
            }
        });
    }

    @Override
    public void onClose(GuiCloseEvent<ConsulatOffline> event) {
        event.setOpenFatherGui(false);
    }

    @Override
    public void onClick(GuiClickEvent<ConsulatOffline> event) {
        ConsulatOffline offlineTarget = event.getData();
        SurvivalPlayer target = (SurvivalPlayer) CPlayerManager.getInstance().getConsulatPlayer(offlineTarget.getUUID());
        ConsulatPlayer muter = event.getPlayer();

        if (target.isMuted()) {
            event.getPlayer().sendMessage("§cJoueur déjà mute.");
            event.getPlayer().getPlayer().closeInventory();
            return;
        }

        GuiItem item = event.getPagedGui().getItem(event.getSlot());
        List<String> description = item.getDescription();
        String recidive = description.get(1).split(":")[1].trim();
        int recidiveNumber = Integer.parseInt(recidive);
        double multiply = recidiveNumber * 1.5;

        if (multiply == 0) multiply = 1;

        long currentTime = System.currentTimeMillis();
        MuteEnum muteReason = (MuteEnum) event.getPagedGui().getItem(event.getSlot()).getAttachedObject();
        double durationMute = (muteReason.getDurationSanction() * 1000) * multiply;
        long resultTime = currentTime + Math.round(durationMute);
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ConsulatCore.getInstance().getModerationDatabase().addSanction(
                        offlineTarget.getUUID(), offlineTarget.getName(), muter.getPlayer(),
                        "MUTE", muteReason.getSanctionName(), resultTime, currentTime);
                if (target != null) {
                    target.setMuted(true);
                    target.setMuteExpireMillis(resultTime);
                    target.setMuteReason(muteReason.getSanctionName());
                    target.sendMessage("§cTu as été sanctionné. Tu ne peux plus parler pour : §4" + muteReason.getSanctionName());
                }

                if (target.getMuteHistory().containsKey(muteReason)) {
                    int number = target.getMuteHistory().get(muteReason);
                    target.getMuteHistory().put(muteReason, ++number);
                } else {
                    target.getMuteHistory().put(muteReason, 1);
                }
            } catch (SQLException e) {
                muter.sendMessage("§cErreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                e.printStackTrace();
            }
        });
        for (ConsulatPlayer onlinePlayer : CPlayerManager.getInstance().getConsulatPlayers()) {
            if (onlinePlayer.hasPower(Rank.MODO)) {
                long durationRound = Math.round(durationMute);
                long days = ((durationRound / (1000*60*60*24)));
                long hours = ((durationRound / (1000 * 60 * 60)) % 24);
                long minutes = ((durationRound / (1000 * 60)) % 60);
                sanctionMessage(onlinePlayer, offlineTarget.getName(), muteReason.getSanctionName(), days + "J" + hours + "H" + minutes + "M", muter.getName(), recidiveNumber);
            }
        }
        Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " §6" + offlineTarget.getName() + " §ea été mute.");
        muter.getPlayer().closeInventory();
    }

    private void sanctionMessage(ConsulatPlayer playerToSend, String targetName, String sanctionName, String duration, String modName, int recidive) {
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + ChatColor.YELLOW + targetName + ChatColor.GOLD + " a été mute.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Motif : §8" + sanctionName +
                        "§7\nPendant : §8" + duration +
                        "§7\nPar : §8" + modName +
                        "§7\nRécidive : §8" + recidive
                ).create()));
        playerToSend.sendMessage(textComponent);
    }

    private HashMap<MuteEnum, Integer> getMuteHistory(ConsulatOffline consulatOffline) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT reason FROM antecedents WHERE playeruuid = ? AND sanction = 'MUTE' AND cancelled = 0");
        preparedStatement.setString(1, consulatOffline.getUUID().toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        HashMap<MuteEnum, Integer> muteHistory = new HashMap<>();
        while (resultSet.next()) {
            String reason = resultSet.getString("reason");
            MuteEnum muteReason = Arrays.stream(MuteEnum.values()).filter(ban -> ban.getSanctionName().equals(reason)).findFirst().orElse(null);

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
