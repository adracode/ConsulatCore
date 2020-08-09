package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

public class UnmuteCommand extends ConsulatCommand {
    
    public UnmuteCommand(){
        super("consulat.core", "unmute", "/unmute <Joueur>", 1, Rank.RESPONSABLE);
        suggest(Arguments.playerList("joueur"), Arguments.word("joueur"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            ConsulatCore.getInstance().getModerationDatabase().unmute(args[0]);
            SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
            if(target != null){
                if(target.isMuted()){
                    target.setMuted(false);
                    sender.sendMessage(Text.UNMUTE_PLAYER);
                } else {
                    sender.sendMessage(Text.PLAYER_NOT_MUTE);
                }
            } else {
                sender.sendMessage(Text.MAYBE_UNMUTE_PLAYER);
            }
        });
    }
}
