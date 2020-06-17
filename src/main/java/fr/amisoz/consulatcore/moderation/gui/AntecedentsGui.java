package fr.amisoz.consulatcore.moderation.gui;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.moderation.SanctionObject;
import fr.amisoz.consulatcore.moderation.SanctionType;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.player.ConsulatOffline;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AntecedentsGui extends GuiContainer<ConsulatOffline> {

    public AntecedentsGui() {
        super(3);
        setTemplate("§6§lAntécédents §7↠ §e");
    }

    @Override
    public void onPageCreate(PagedGuiCreateEvent<ConsulatOffline> event) {
        ConsulatOffline consulatOffline = event.getData();
        event.getPagedGui().setName(event.getGui().getName() + consulatOffline.getName());
    }

    @Override
    public void onOpen(GuiOpenEvent<ConsulatOffline> event) {
        ConsulatOffline consulatOffline = event.getData();
        Player player = event.getPlayer().getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                List<SanctionObject> sanctions = getAntecedents(consulatOffline.getUUID().toString());

                Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                    if (sanctions.size() == 0) {
                        player.closeInventory();
                        player.sendMessage("§cCe joueur n'a pas d'antécédents.");
                        return;
                    }

                    for (int i = 0; i < sanctions.size(); i++) {
                        SanctionObject sanctionObject = sanctions.get(i);
                        GuiItem item = getItem("§cSANCTION", i, sanctionObject.getSanctionType().getMaterial(),
                                "§6Le : §e" + sanctionObject.getSanctionAt(),
                                "§6Jusqu'au : §e" + sanctionObject.getExpire(),
                                "§6Motif : §e" + sanctionObject.getSanctionName(),
                                "§6Modérateur : §e" + sanctionObject.getMod_name(),
                                "§6Annulé : §e" + (sanctionObject.isCancelled() ? "Oui" : "Non"),
                                "§6Actif : §e" + (sanctionObject.isActive() ? "Oui" : "Non"));

                        event.getPagedGui().setItem(item);
                    }
                });
            } catch (SQLException e) {
                Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                    player.closeInventory();
                    player.sendMessage("§cUne erreur s'est produite.");
                    e.printStackTrace();
                });
            }
        });
    }

    private List<SanctionObject> getAntecedents(String uuid) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM antecedents WHERE playeruuid = ?");
        preparedStatement.setString(1, uuid);
        ResultSet resultSet = preparedStatement.executeQuery();

        ArrayList<SanctionObject> sanctions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        while (resultSet.next()) {
            SanctionType sanctionType = SanctionType.valueOf(resultSet.getString("sanction"));
            String sanctionName = resultSet.getString("reason");

            calendar.setTimeInMillis(resultSet.getLong("applicated"));
            String sanctionAt = new SimpleDateFormat("dd/MM/yyyy 'à' kk:mm").format(calendar.getTime());

            calendar.setTimeInMillis(resultSet.getLong("expire"));
            String expire = new SimpleDateFormat("dd/MM/yyyy 'à' kk:mm").format(calendar.getTime());
            String moderatorName = resultSet.getString("modname");
            boolean isActive = resultSet.getBoolean("active");
            boolean isCancel = resultSet.getBoolean("cancelled");

            sanctions.add(new SanctionObject(sanctionType, sanctionName, sanctionAt, expire, moderatorName, isActive, isCancel));
        }

        return sanctions;
    }
}