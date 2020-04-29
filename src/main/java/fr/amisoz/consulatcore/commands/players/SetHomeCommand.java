package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class SetHomeCommand extends ConsulatCommand {
    
    public SetHomeCommand(){
        super("sethome", "/sethome <Nom du home>", 1, Rank.JOUEUR);
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
        Claim claim = survivalPlayer.getClaimLocation();
        if(claim == null){
            if(!survivalPlayer.hasPower(Rank.MECENE)){
                sender.sendMessage("§cTu dois être dans un claim pour définir ton home.");
                return;
            }
        } else {
            //TODO: enlever les homes lorsque l'accès est supprimé ???
            if(!claim.isAllowed(sender.getUUID())){
                sender.sendMessage("§cTu dois être dans un claim t'appartenant pour définir ton home.");
                return;
            }
        }
        if(!survivalPlayer.canAddNewHome(args[0])){
            sender.sendMessage("§cTu as atteint ta limite de homes, définis la position d'un home existant ou supprime en un.");
            return;
        }
        //TODO: Ajouter pitch et yaw ?
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
