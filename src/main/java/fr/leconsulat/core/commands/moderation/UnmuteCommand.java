package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class UnmuteCommand extends ConsulatCommand {
    
    public UnmuteCommand(){
        super(ConsulatCore.getInstance(), "unmute");
        setDescription("Démuter un joueur").
                setUsage("/unmute <joueur> - Démute un joueur").
                setArgsMin(1).
                setRank(Rank.RESPONSABLE).
                suggest(Arguments.playerList("joueur"), Arguments.word("joueur"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
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
