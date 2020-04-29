package fr.amisoz.consulatcore.commands.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.text.DecimalFormat;

public class UnclaimCommand extends ConsulatCommand {
    
    private DecimalFormat shapePrice = new DecimalFormat();
    
    public UnclaimCommand(){
        super("unclaim", "/unclaim", 0, Rank.JOUEUR);
        shapePrice.setMaximumFractionDigits(2);
        shapePrice.setMinimumFractionDigits(2);
        shapePrice.setDecimalSeparatorAlwaysShown(true);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        Claim claim = survivalSender.getClaimLocation();
        if(claim == null || (!claim.isOwner(sender) && !sender.hasPower(Rank.MODPLUS))){
            sender.sendMessage(Text.PREFIX + "§cTu n'es pas dans ton claim.");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                ClaimManager.getInstance().unClaim(claim.getX(), claim.getZ());
                if(claim.isOwner(sender)){
                    sender.sendMessage(Text.PREFIX + "§aChunk unclaim, tu as récupéré " + shapePrice.format(Claim.REFUND) + " €.");
                    survivalSender.addMoney(Claim.REFUND);
                } else {
                    SurvivalPlayer survivalTarget = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(claim.getOwner());
                    if(survivalTarget != null){
                        survivalTarget.addMoney(Claim.REFUND);
                    } else {
                        SPlayerManager.getInstance().addMoney(claim.getOwner(), Claim.REFUND);
                    }
                    sender.sendMessage(Text.PREFIX + "§aChunk unclaim.");
                }
            } catch(SQLException e){
                sender.sendMessage(Text.PREFIX + "§cErreur lors de l'unclaim.");
                e.printStackTrace();
            }
        });
    }
}
