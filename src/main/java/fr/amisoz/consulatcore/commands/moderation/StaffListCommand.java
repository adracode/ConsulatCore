package fr.amisoz.consulatcore.commands.moderation;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class StaffListCommand extends ConsulatCommand {

    public StaffListCommand() {
        super("stafflist", "/stafflist", 0, Rank.MODPLUS);
        suggest(true);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage("§6§uListe du staff en ligne : ");
        for(ConsulatPlayer player : CPlayerManager.getInstance().getConsulatPlayers()){
            if(player.hasPower(Rank.BUILDER)){
                Rank rank = player.getRank();
                sender.sendMessage(rank.getRankColor() + "[" + rank.getRankName() + "] " + player.getName());
            }
        }
    }
}
