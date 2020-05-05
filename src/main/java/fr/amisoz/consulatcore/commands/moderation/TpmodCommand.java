package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class TpmodCommand extends ConsulatCommand {
    
    public TpmodCommand(){
        super("tpmod", "/tpmod <Joueur>", 1, Rank.MODO);
        suggest(true,
                Arguments.player("joueur"),
                Arguments.player("joueur").then(Arguments.player("joueur")));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(!ModerationUtils.moderatePlayers.contains(sender.getPlayer()) && !survivalSender.hasPower(Rank.ADMIN)){
            sender.sendMessage("§cTu dois être en mode modérateur.");
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cCe joueur n'est pas connecté.");
            return;
        }
        if(args.length == 2){
            SurvivalPlayer to = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
            if(to == null){
                sender.sendMessage("§cCe joueur n'est pas connecté.");
                return;
            }
            target.getPlayer().teleport(to.getPlayer());
            sender.sendMessage("§aTu as téléporté " + target.getName() + " à " + to.getName());
            return;
        }
        sender.getPlayer().teleport(target.getPlayer());
        sender.sendMessage("§aTu t'es téléporté à " + args[0] + ".");
    }
}
