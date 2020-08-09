package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class SetHomeCommand extends ConsulatCommand {
    
    public static final Pattern VALID_HOME = Pattern.compile("[a-zA-Z0-9àçéèêîïùÀÇÉÈÊÎÏÙ]{3,10}");
    
    public SetHomeCommand(){
        super("consulat.core", "sethome", "/sethome <Nom du home>", 1, Rank.JOUEUR);
        suggest(Arguments.word("home"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(!VALID_HOME.matcher(args[0]).matches()){
            sender.sendMessage(Text.INVALID_HOME_NAME);
            return;
        }
        if(sender.getPlayer().getWorld() != ConsulatCore.getInstance().getOverworld()){
            sender.sendMessage(Text.DIMENSION_HOME);
            return;
        }
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)sender;
        Claim claim = survivalPlayer.getClaim();
        if(claim == null){
            if(!survivalPlayer.hasPower(Rank.MECENE)){
                sender.sendMessage(Text.NOT_IN_YOUR_CLAIM);
                return;
            }
        } else {
            if(!claim.canInteract((SurvivalPlayer)sender)){
                sender.sendMessage(Text.NOT_IN_YOUR_CLAIM);
                return;
            }
        }
        if(!survivalPlayer.canAddNewHome(args[0])){
            sender.sendMessage(Text.NO_MORE_HOME_AVAILABLE);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                survivalPlayer.addNewHome(args[0], sender.getPlayer().getLocation());
                sender.sendMessage(Text.HOME_SET);
            } catch(SQLException e){
                sender.sendMessage(Text.ERROR);
                e.printStackTrace();
            }
        });
        
    }
}
