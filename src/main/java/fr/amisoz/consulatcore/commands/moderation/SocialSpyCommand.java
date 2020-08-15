package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

public class SocialSpyCommand extends ConsulatCommand {
    
    public SocialSpyCommand(){
        super(ConsulatCore.getInstance(), "socialspy");
        setDescription("Voir les MPs et les chat de villes").
                setUsage("/socialspy - Activer / désactiver").
                setRank(Rank.RESPONSABLE).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.isSpying()){
            sender.sendMessage(Text.PREFIX + Text.NO_MORE_IN_SPY);
        } else {
            sender.sendMessage(Text.PREFIX + Text.NOW_IN_SPY);
        }
        player.setSpying(!player.isSpying());
    }
}
