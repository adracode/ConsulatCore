package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

public class UnmuteCommand extends ConsulatCommand {
    
    public UnmuteCommand(){
        super("/unmute <Joueur>", 1, Rank.ADMIN);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            ConsulatCore.getInstance().getModerationDatabase().unmute(args[0]);
            if(target != null){
                SurvivalPlayer targetPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(target.getUUID());
                if(targetPlayer.isMuted()){
                    targetPlayer.setMuted(false);
                } else {
                    sender.sendMessage("§cCe joueur n'est pas mute.");
                }
            }
        });
    }
}
