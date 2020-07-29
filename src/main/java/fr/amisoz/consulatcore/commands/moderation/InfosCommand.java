package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalOffline;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

public class InfosCommand extends ConsulatCommand {
    
    public InfosCommand(){
        super("infos", Collections.emptyList(), "/infos <Joueur>", 1, Rank.RESPONSABLE);
        suggest(true, Arguments.playerList("joueur"),
                Arguments.word("joueur"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        
        player.sendMessage(ChatColor.GRAY + "========" + ChatColor.YELLOW + " INFOS " + ChatColor.GRAY + "========");
        player.sendMessage(ChatColor.GRAY + "Pseudo ⤗ " + ChatColor.AQUA + args[0]);
        
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        player.sendMessage(ChatColor.GRAY + "Statut ⤗ " + (offlinePlayer.isOnline() ? ChatColor.GREEN + "Connecté" : ChatColor.RED + "Déconnecté"));
        if(!offlinePlayer.hasPlayedBefore()){
            player.sendMessage(ChatColor.GRAY + "Le joueur ne s'est jamais connecté.");
            return;
        }
        
        Date date = new Date(offlinePlayer.getFirstPlayed());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm:ss");
        String firstPlayed = simpleDateFormat.format(date);
        
        player.sendMessage(ChatColor.GRAY + "Première connexion ⤗ " + ChatColor.DARK_PURPLE + firstPlayed);
        
        if(offlinePlayer.isOnline()){
            Player target = offlinePlayer.getPlayer();
            SurvivalPlayer survivalTarget = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
            int flyTime = survivalTarget.getFlyTimeLeft();
            
            if(survivalTarget.hasInfiniteFly()){
                player.sendMessage(ChatColor.GRAY + "Fly ⤗ " + ChatColor.YELLOW + "Infini" + ChatColor.GRAY + " • " + (survivalTarget.isFlying() ? ChatColor.GREEN + "Fly activé" : ChatColor.RED + "Fly désactivé"));
            } else {
                player.sendMessage(ChatColor.GRAY + "Fly ⤗ " + ChatColor.YELLOW + (flyTime / 60) + " minutes" + ChatColor.GRAY + " • " + (survivalTarget.isFlying() ? ChatColor.GREEN + "Fly activé" : ChatColor.RED + "Fly désactivé"));
            }
            
            player.sendMessage(ChatColor.GRAY + "Grade ⤗ " + survivalTarget.getRank().getRankColor() + survivalTarget.getRank().getRankName() + ChatColor.GRAY + " ↭ Argent ⤗ " + ChatColor.BLUE + ConsulatCore.formatMoney(survivalTarget.getMoney()));
        }
        
        
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                player.sendMessage(ChatColor.GRAY + "Dernières connexions : ");
                sendConnections(args[0], player.getPlayer());
                if(!offlinePlayer.isOnline()){
                    Optional<SurvivalOffline> offlineConsulat = SPlayerManager.getInstance().fetchOffline(args[0]);
                    if(!offlineConsulat.isPresent()){
                        return;
                    }
                    SurvivalOffline offlineTarget = offlineConsulat.get();
                    sendBan(args[0], player.getPlayer());
                    player.sendMessage(ChatColor.GRAY + "Grade ⤗ " + offlineTarget.getRank().getRankColor() + offlineTarget.getRank().getRankName() + ChatColor.GRAY + " ↭ Argent ⤗ " + ChatColor.BLUE + ConsulatCore.formatMoney(offlineTarget.getMoney()));
                }
            } catch(SQLException e){
                player.sendMessage(ChatColor.RED + "Erreur lors de la récupération de certaines informations.");
                e.printStackTrace();
            }
            
            TextComponent textComponent = new TextComponent(ChatColor.GRAY + "[" + ChatColor.GOLD + "Voir les homes" + ChatColor.GRAY + "]");
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Clique pour voir les homes !").create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + args[0] + ":"));
            
            TextComponent antecedentsComponent = new TextComponent(ChatColor.GRAY + "[" + ChatColor.GOLD + "Voir les antécédents" + ChatColor.GRAY + "]");
            antecedentsComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Clique pour voir les antécédents !").create()));
            antecedentsComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/antecedents " + args[0]));
            
            player.getPlayer().spigot().sendMessage(textComponent, antecedentsComponent);
        });
    }
    
    private void sendConnections(String playerName, Player moderator) throws SQLException{
        PreparedStatement preparedStatement = ConsulatCore.getInstance().getDatabaseConnection().prepareStatement("SELECT connection_date, player_ip FROM connections INNER JOIN players ON connections.player_id = players.id WHERE players.player_name = ? ORDER BY connections.id DESC LIMIT 3");
        preparedStatement.setString(1, playerName);
        ResultSet resultSet = preparedStatement.executeQuery();
        
        while(resultSet.next()){
            moderator.sendMessage(ChatColor.GRAY + "Connexion ⤗ " + ChatColor.GOLD + resultSet.getString("connection_date") + ChatColor.GRAY + " ↭ " + ChatColor.GOLD + resultSet.getString("player_ip"));
        }
        
        resultSet.close();
        preparedStatement.close();
    }
    
    private void sendBan(String playerName, Player moderator) throws SQLException{
        PreparedStatement preparedStatement = ConsulatCore.getInstance().getDatabaseConnection().prepareStatement("SELECT * FROM antecedents WHERE playername = ? AND sanction = 'BAN' AND active = '1'");
        preparedStatement.setString(1, playerName);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            long expireBan = resultSet.getLong("expire");
            String reason = resultSet.getString("reason");
            
            resultSet.close();
            preparedStatement.close();
            if(System.currentTimeMillis() > expireBan){
                moderator.sendMessage(ChatColor.GRAY + "Banni ⤗ " + ChatColor.GREEN + "Non");
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(expireBan);
                String endBan = new SimpleDateFormat("dd/MM/yyyy 'à' kk:mm").format(calendar.getTime());
                moderator.sendMessage(ChatColor.GRAY + "Banni ⤗ " + ChatColor.RED + "Oui" + ChatColor.GRAY + " • " + ChatColor.YELLOW + reason + ChatColor.GRAY + " ↭ Jusqu'au " + ChatColor.BLUE + endBan);
            }
        } else {
            moderator.sendMessage(ChatColor.GRAY + "Banni ⤗ " + ChatColor.GREEN + "Non");
        }
    }
}
