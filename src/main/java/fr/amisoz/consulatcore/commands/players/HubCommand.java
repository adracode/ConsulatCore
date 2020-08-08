package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class HubCommand extends ConsulatCommand {

    public HubCommand() {
        super("consulat.core", "hub", "/hub", 0, Rank.JOUEUR);
        suggest();
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        ConsulatCore.getInstance().getHub().connectPlayer(sender);
    }
}
