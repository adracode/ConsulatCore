package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.jetbrains.annotations.NotNull;

public class TpmodCommand extends ConsulatCommand {
    
    public TpmodCommand(){
        super(ConsulatCore.getInstance(), "tpmod");
        setDescription("Téléporter des joueurs").
                setUsage("/tpmod <joueur> - Se TP à un joueur\n" +
                        "/tpmod <joueur> <joueur> - TP un joueur à un autre joueur").
                setArgsMin(1).
                setRank(Rank.MODO).
                suggest(Arguments.playerList("joueur").
                        then(Arguments.playerList("joueur")));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(!survivalSender.isInModeration() && !survivalSender.hasPower(Rank.ADMIN)){
            sender.sendMessage(Text.NEED_STAFF_MODE);
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        if(args.length == 2){
            SurvivalPlayer to = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
            if(to == null){
                sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
                return;
            }
            target.getPlayer().teleportAsync(to.getPlayer().getLocation());
            sender.sendMessage(Text.YOU_TELEPORTED_PLAYER_TO(target.getName(), to.getName()));
            return;
        }
        sender.getPlayer().teleportAsync(target.getPlayer().getLocation());
        sender.sendMessage(Text.YOU_TELEPORTED_TO(target.getName()));
    }
}
