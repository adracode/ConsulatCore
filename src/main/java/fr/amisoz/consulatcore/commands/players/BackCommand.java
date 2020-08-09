package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class BackCommand extends ConsulatCommand {

    public BackCommand() {
        super("consulat.core", "back", "/back", 0, Rank.MECENE);
        suggest();
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.getOldLocation() == null){
            player.sendMessage(Text.NOT_YET_TELEPORTED);
            return;
        }
        sender.getPlayer().teleportAsync(player.getOldLocation());
        sender.sendMessage(Text.TELEPORTATION);
    }
}
