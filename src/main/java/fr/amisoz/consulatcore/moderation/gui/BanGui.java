package fr.amisoz.consulatcore.moderation.gui;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.BanEnum;
import fr.amisoz.consulatcore.moderation.MuteEnum;
import fr.amisoz.consulatcore.moderation.SanctionType;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BanGui extends GuiListener {

    public BanGui(GuiListener father) {
        super(father, ConsulatOffline.class);
        addGui(null, this, "§c§lBannir§7 §7↠ §4", 3);
        setCreateOnOpen(true);
    }

    @Override
    public void onCreate(GuiCreateEvent event) {
        if (event.getKey() == null) {
            return;
        }

        event.getGui().setName("§c§lBannir§7 §7↠ §4" + ((ConsulatOffline) event.getKey()).getName());
    }

    @Override
    public void onOpen(GuiOpenEvent event) {
        if (event.getKey() == null) {
            return;
        }

        event.getGui().setName("§c§lBannir§7 §7↠ §4" + ((ConsulatOffline) event.getKey()).getName());

        ConsulatOffline consulatOffline = (ConsulatOffline) event.getKey();
        Player target = Bukkit.getPlayer(consulatOffline.getUUID());
        Player moderator = event.getPlayer().getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            HashMap<BanEnum, Integer> banHistory;

            if (target == null) {
                try {
                    banHistory = getBanHistory(consulatOffline);
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
                banHistory = survivalPlayer.getBanHistory();
            }


            BanEnum[] values = BanEnum.values();
            for (int i = 0; i < values.length; ++i) {
                BanEnum ban = values[i];
                GuiItem item = getItem("§c" + ban.getSanctionName(), i, ban.getGuiMaterial());

                item.setDescription("§cDurée : §4" + ban.getFormatDuration(), "§7Récidive : " + (banHistory.containsKey(ban) ? banHistory.get(ban) : "0"));
                item.setAttachedObject(ban);
                event.getGui().setItem(i, item);
            }
        });
    }

    @Override
    public void onClose(GuiCloseEvent event) {
        event.setOpenFatherGui(false);
    }

    @Override
    public void onClick(GuiClickEvent event) {
        GuiItem item = event.getGui().getItem(event.getSlot());
        List<String> description = item.getDescription();
        String recidive = description.get(1).split(":")[1].trim();
        int recidiveNumber = Integer.parseInt(recidive);
        double multiply = recidiveNumber * 1.5;

        if (multiply == 0) multiply = 1;

        long currentTime = System.currentTimeMillis();
        BanEnum ban = (BanEnum) event.getGui().getItem(event.getSlot()).getAttachedObject();
        double durationBan = (ban.getDurationSanction() * 1000) * multiply;
        long resultTime = currentTime + Math.round(durationBan);
        ConsulatPlayer banner = event.getPlayer();
        ConsulatOffline offlineTarget = (ConsulatOffline) event.getGui().getKey();

        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ConsulatCore.getInstance().getModerationDatabase().addSanction(
                        offlineTarget.getUUID(), offlineTarget.getName(), banner.getPlayer(), "BAN", ban.getSanctionName(), resultTime, currentTime);
                Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                    ConsulatPlayer target = CPlayerManager.getInstance().getConsulatPlayer(offlineTarget.getUUID());
                    if (target != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(resultTime);
                        Date date = calendar.getTime();
                        target.getPlayer().kickPlayer("§7§l§m ----[ §r§6§lLe Consulat §7§l§m]----\n\n§cTu as été banni.\n§cRaison : §4" + ban.getSanctionName() + "\n§cJusqu'au : §4" + ConsulatCore.getInstance().DATE_FORMAT.format(date));
                    }
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
                        if (consulatPlayer != null) {
                            Rank onlineRank = consulatPlayer.getRank();
                            if (onlineRank.getRankPower() >= Rank.MODO.getRankPower()) {
                                long durationRound = Math.round(durationBan);
                                long days = ((durationRound / (1000*60*60*24)));
                                long hours = ((durationRound / (1000 * 60 * 60)) % 24);
                                long minutes = ((durationRound / (1000 * 60)) % 60);
                                sanctionMessage(onlinePlayer, offlineTarget.getName(), ban.getSanctionName(), days + "J" + hours + "H" + minutes + "M", banner.getName(), recidiveNumber);
                            }
                        }
                    });
                    Bukkit.broadcastMessage(Text.ANNOUNCE_PREFIX + " " + ChatColor.RED + offlineTarget.getName() + ChatColor.DARK_RED + " a été banni.");

                });
            } catch (SQLException e) {
                banner.sendMessage("§cErreur lors de l'application de la sanction. (ADD_ANTECEDENTS)");
                e.printStackTrace();
            }
        });
        banner.getPlayer().closeInventory();
    }

    private void sanctionMessage(Player playerToSend, String targetName, String sanctionName, String duration, String modName, int recidive) {
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + "§c" + targetName + "§4 a été banni.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Motif : §8" + sanctionName +
                        "§7\nPendant : §8" + duration +
                        "§7\nPar : §8" + modName +
                        "§7\nRécidive : §8" + recidive
                ).create()));
        playerToSend.spigot().sendMessage(textComponent);
    }

    private HashMap<BanEnum, Integer> getBanHistory(ConsulatOffline consulatOffline) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT reason FROM antecedents WHERE playeruuid = ? AND sanction = 'BAN' AND cancelled = 0");
        preparedStatement.setString(1, consulatOffline.getUUID().toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        HashMap<BanEnum, Integer> banHistory = new HashMap<>();
        while (resultSet.next()) {
            String reason = resultSet.getString("reason");
            BanEnum banReason = Arrays.stream(BanEnum.values()).filter(ban -> ban.getSanctionName().equals(reason)).findFirst().orElse(null);

            if (banReason != null) {
                if (banHistory.containsKey(banReason)) {
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

