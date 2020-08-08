package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class SetHomeCommand extends ConsulatCommand {
    
    public SetHomeCommand(){
        super("consulat.core", "sethome", "/sethome <Nom du home>", 1, Rank.JOUEUR);
        suggest(Arguments.word("home"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args[0].length() > 10){
            sender.sendMessage("§cNom de home trop long");
            return;
        }
        if(sender.getPlayer().getWorld() != Bukkit.getWorlds().get(0)){
            sender.sendMessage("§cTu dois être dans le monde de base afin de sethome.");
            return;
        }
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)sender;
        Claim claim = survivalPlayer.getClaim();
        if(claim == null){
            if(!survivalPlayer.hasPower(Rank.MECENE)){
                sender.sendMessage("§cTu dois être dans un claim pour définir ton home.");
                return;
            }
        } else {
            if(!claim.canInteract((SurvivalPlayer)sender)){
                sender.sendMessage("§cTu dois être dans un claim t'appartenant pour définir ton home.");
                return;
            }
        }
        if(!survivalPlayer.canAddNewHome(args[0])){
            sender.sendMessage("§cTu as atteint ta limite de homes, définis la position d'un home existant ou supprime en un.");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                survivalPlayer.addNewHome(args[0], sender.getPlayer().getLocation());
                sender.sendMessage("§aHome sauvegardé.");
            } catch(SQLException e){
                sender.sendMessage("§cUne erreur interne est survenue.");
                e.printStackTrace();
            }
        });
        
    }
}
