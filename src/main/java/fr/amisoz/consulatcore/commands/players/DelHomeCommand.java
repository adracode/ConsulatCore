package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class DelHomeCommand extends ConsulatCommand {
    
    
    public DelHomeCommand(){
        super("/delhome <Nom du home>", 1, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(args.length == 2 && survivalSender.hasPower(Rank.MODPLUS)){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[0]);
                    if(targetUUID != null){
                        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                        if(target != null){
                            if(target.hasHome(args[1])){
                                target.removeHome(args[1]);
                                sender.sendMessage("§aHome supprimé avec succès.");
                            } else {
                                sender.sendMessage("§cCe joueur ne possède pas ce home.");
                            }
                            return;
                        }
                    }
                    //Sera utile lors de la refonte des commandes
                    Map<String, Location> homes = SPlayerManager.getInstance().getHomes(args[0], false);
                    if(!homes.containsKey(args[1].toLowerCase())){
                        sender.sendMessage("§cCe joueur ne possède pas ce home.");
                    } else {
                        if(SPlayerManager.getInstance().removeHome(args[0], args[1])){
                            sender.sendMessage("§aHome supprimé avec succès.");
                        } else {
                            sender.sendMessage("§cCe joueur ne possède pas ce home.");
                        }
                    }
                } catch(SQLException e){
                    sender.sendMessage("§cUne erreur interne est survenue.");
                    e.printStackTrace();
                }
            });
            return;
        }
        if(!survivalSender.hasHome(args[0])){
            sender.sendMessage("§cHome introuvable !");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                survivalSender.removeHome(args[0]);
                sender.sendMessage(Text.PREFIX + "§aTon home a bien été supprimé.");
            } catch(SQLException e){
                sender.sendMessage(Text.PREFIX + "§cErreur lors de la suppression.");
                e.printStackTrace();
            }
        });
    }
}
