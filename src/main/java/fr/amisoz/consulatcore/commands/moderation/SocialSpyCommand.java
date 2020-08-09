package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class SocialSpyCommand extends ConsulatCommand {
    
    public SocialSpyCommand(){
        super("consulat.core", "socialspy", "/socialspy", 0, Rank.RESPONSABLE);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.isSpying()){
            sender.sendMessage(Text.PREFIX + Text.NO_MORE_IN_SPY);
        } else {
            sender.sendMessage(Text.PREFIX + Text.NOW_IN_SPY);
        }
        player.setSpying(!player.isSpying());
    }
}
