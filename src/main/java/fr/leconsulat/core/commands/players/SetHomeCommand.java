package fr.leconsulat.core.commands.players;

import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.claims.Claim;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class SetHomeCommand extends ConsulatCommand {
    
    public static final Pattern VALID_HOME = Pattern.compile("[a-zA-Z0-9àçéèêîïùÀÇÉÈÊÎÏÙ]{1,10}");
    
    public SetHomeCommand(){
        super(ConsulatCore.getInstance(), "sethome");
        setDescription("Créer un home").
                setUsage("/sethome <home> - Créer un home").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(Arguments.word("home"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
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
            if(!claim.hasAccess(sender.getUUID())){
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
