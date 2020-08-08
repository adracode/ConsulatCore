package fr.amisoz.consulatcore.commands.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class UnclaimCommand extends ConsulatCommand {
    
    public UnclaimCommand(){
        super("consulat.core", "unclaim", "/unclaim", 0, Rank.JOUEUR);
        suggest();
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        Claim claim = player.getClaim();
        if(claim == null || (!claim.isOwner(sender.getUUID()) && !sender.hasPower(Rank.MODPLUS))){
            sender.sendMessage(Text.PREFIX + "§cTu n'es pas dans ton claim.");
            return;
        }
        if(claim.getOwner() instanceof City){
            sender.sendMessage(Text.PREFIX + "§cTu ne peux pas unclaim ce chunk.");
            return;
        }
        ClaimManager.getInstance().unClaim(claim);
        if(claim.isOwner(sender.getUUID())){
            sender.sendMessage(Text.PREFIX + "§aChunk unclaim, tu as récupéré " + ConsulatCore.formatMoney(Claim.REFUND) + ".");
            player.addMoney(Claim.REFUND);
        } else {
            SurvivalPlayer survivalTarget = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(claim.getOwnerUUID());
            if(survivalTarget != null){
                survivalTarget.addMoney(Claim.REFUND);
            } else {
                SPlayerManager.getInstance().addMoney(claim.getOwnerUUID(), Claim.REFUND);
            }
            sender.sendMessage(Text.PREFIX + "§aChunk unclaim.");
        }
    }
}
