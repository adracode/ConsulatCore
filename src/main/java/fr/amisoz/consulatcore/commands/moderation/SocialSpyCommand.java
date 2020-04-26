package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class SocialSpyCommand extends ConsulatCommand {
    
    public SocialSpyCommand(){
        super("/socialspy", 0, Rank.RESPONSABLE);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.isSpying()){
            sender.sendMessage(Text.PREFIX + "Tu ne vois plus les messages.");
        } else {
            sender.sendMessage(Text.PREFIX + "Tu vois désormais les messages.");
        }
        player.setSpying(!player.isSpying());
    }
}
