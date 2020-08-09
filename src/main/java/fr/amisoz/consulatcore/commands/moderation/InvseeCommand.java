package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class InvseeCommand extends ConsulatCommand {
    
    public InvseeCommand(){
        super("consulat.core", "invsee", "/invsee <Joueur>", 1, Rank.MODO);
        suggest(Arguments.playerList("joueur"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        ((SurvivalPlayer)sender).setLookingInventory(true);
        sender.getPlayer().openInventory(target.getPlayer().getInventory());
    }
}
