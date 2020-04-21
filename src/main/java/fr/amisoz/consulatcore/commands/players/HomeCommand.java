package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class HomeCommand extends ConsulatCommand {
    
    public HomeCommand(){
        super("/home <Nom du home>", 0, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)sender;
        if(args.length == 0){
            Set<String> homes = survivalPlayer.getNameHomes();
            if(homes.size() == 0){
                sender.sendMessage(Text.PREFIX + "§cTu ne possèdes aucun home.");
                sender.sendMessage(Text.PREFIX + "§eFais: §c/sethome <Nom du home> §epour poser un home.");
                return;
            }
            StringBuilder result = new StringBuilder();
            for(String key : homes){
                result.append(key).append(", ");
            }
            sender.sendMessage(Text.PREFIX + "§eVoici la liste de tes homes: " + result.substring(0, result.length() - 2));
            return;
        }
        String homeName = args[0];
        if(survivalPlayer.hasPower(Rank.MODPLUS) && homeName.endsWith(":")){
            homeName = homeName.substring(0, homeName.length() - 1);
            String player = homeName;
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    Map<String, Location> homes = SPlayerManager.getInstance().getHomes(player, true);
                    if(homes.isEmpty()){
                        sender.sendMessage("§cCe joueur n'a pas de homes");
                    } else {
                        sender.sendMessage("§6Liste des homes de : §c" + player);
                        sender.sendMessage("§7---------------------------------");
                        for(Map.Entry<String, Location> home : homes.entrySet()){
                            TextComponent textComponent = new TextComponent("§a" + home.getKey() + " §7| §cX§7:§6" +
                                    home.getValue().getBlockX() + " §cY§7:§6" +
                                    home.getValue().getBlockY() + " §cZ§7:§6" +
                                    home.getValue().getBlockZ());
                            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour t'y téléporter.").create()));
                            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " +
                                    home.getValue().getBlockX() + " " +
                                    home.getValue().getBlockY() + " " +
                                    home.getValue().getBlockZ()));
                            sender.sendMessage(textComponent);
                        }
                    }
                } catch(SQLException e){
                    sender.sendMessage("§cUne erreur interne est survenue.");
                }
            });
            return;
        }
        Location home = survivalPlayer.getHome(homeName);
        if(home != null){
            survivalPlayer.setOldLocation(sender.getPlayer().getLocation());
            sender.getPlayer().teleport(home);
            sender.sendMessage(Text.PREFIX + "§aTu as bien été téléporté à ton home : §2" + homeName);
        } else {
            Set<String> names = survivalPlayer.getNameHomes();
            if(names.size() == 0){
                sender.sendMessage(Text.PREFIX + "§cTu n'as pas de home défini.");
                return;
            }
            StringBuilder result = new StringBuilder();
            for(String key : survivalPlayer.getNameHomes()){
                result.append(key).append(", ");
            }
            sender.sendMessage(Text.PREFIX + "§cHome inconnu, voici la liste : " + result.substring(0, result.length() - 2));
        }
    }
}