package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeCommand extends ConsulatCommand {

    public HomeCommand() {
        super("/home <Nom du home>", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        RankEnum playerRank = getConsulatPlayer().getRank();

        if (getArgs().length == 0) {
            StringBuilder result = new StringBuilder();
            for (String key : getCorePlayer().homes.keySet()) {
                result.append(key).append(", ");
            }

            if (result.length() != 0) {
                getPlayer().sendMessage(ConsulatCore.PREFIX + "§eVoici la liste de tes homes: " + result.substring(0, result.length() - 2));
            } else {
                getPlayer().sendMessage(ConsulatCore.PREFIX + "§cTu ne possèdes aucun home.");
                getPlayer().sendMessage(ConsulatCore.PREFIX + "§eFais: §c/sethome <Nom du home> §epour poser un home.");
            }
            return;
        }

        String homeName = getArgs()[0];

        if(playerRank.getRankPower() >= RankEnum.MODPLUS.getRankPower()){
            if(homeName.endsWith(":")){
                homeName = homeName.substring(0, homeName.length()-1);
                getPlayer().sendMessage("§6Liste des homes de : §c" + homeName);
                getPlayer().sendMessage("§7---------------------------------");
                try {
                    getHomes(getPlayer(), homeName);
                } catch (SQLException e) {
                    getPlayer().sendMessage(ChatColor.RED + "Erreur");
                }
                return;
            }
        }

        if (getCorePlayer().homes.containsKey(homeName)) {
            getCorePlayer().oldLocation = getPlayer().getLocation();
            Location home = new Location(Bukkit.getWorlds().get(0), getCorePlayer().homes.get(homeName).getX(), getCorePlayer().homes.get(homeName).getY(), getCorePlayer().homes.get(homeName).getZ());
            getPlayer().teleport(home);
            getPlayer().sendMessage(ConsulatCore.PREFIX + "§aTu as bien été téléporté à ton home : §2" + homeName);
        } else {
            StringBuilder result = new StringBuilder();
            for (String key : getCorePlayer().homes.keySet()) {
                result.append(key).append(", ");
            }
            if(getCorePlayer().homes.size() == 0) getPlayer().sendMessage(ConsulatCore.PREFIX + "§cTu n'as pas de home défini.");
            else getPlayer().sendMessage(ConsulatCore.PREFIX + "§cHome inconnu, voici la liste : " + result.substring(0, result.length() - 2));
        }
    }

    private void getHomes(Player player, String playerName) throws SQLException {
        PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM homes INNER JOIN players ON players.id = homes.idplayer WHERE players.player_name = ?");
        preparedStatement.setString(1, playerName);

        ResultSet resultSet = preparedStatement.executeQuery();
        int homeNumber = 0;
        while(resultSet.next()){
            homeNumber++;
            TextComponent textComponent = new TextComponent("§a" + resultSet.getString("home_name") + " §7| §cX§7:§6" + resultSet.getInt("x") + " §cY§7:§6" + resultSet.getInt("y") + " §cZ§7:§6" + resultSet.getInt("z"));
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour t'y téléporter.").create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + resultSet.getInt("x") + " " + resultSet.getInt("y") + " " + resultSet.getInt("z")));
            getPlayer().spigot().sendMessage(textComponent);
        }

        if(homeNumber == 0) getPlayer().sendMessage(ChatColor.RED + "Ce joueur n'a pas de home.");

        resultSet.close();
        preparedStatement.close();
    }
}