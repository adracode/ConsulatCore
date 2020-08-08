package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class TpmodCommand extends ConsulatCommand {
    
    public TpmodCommand(){
        super("consulat.core", "tpmod", "/tpmod <Joueur>", 1, Rank.MODO);
        suggest(Arguments.playerList("joueur").then(Arguments.playerList("joueur")));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(!survivalSender.isInModeration() && !survivalSender.hasPower(Rank.ADMIN)){
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
            target.getPlayer().teleportAsync(to.getPlayer().getLocation());
            sender.sendMessage("§aTu as téléporté " + target.getName() + " à " + to.getName());
            return;
        }
        sender.getPlayer().teleportAsync(target.getPlayer().getLocation());
        sender.sendMessage("§aTu t'es téléporté à " + args[0] + ".");
    }
}
