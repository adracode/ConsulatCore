package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.moderation.SanctionGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatOffline;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.UUID;

public class SanctionCommand extends ConsulatCommand {
    
    private SanctionGui sanctionGui;
    
    public SanctionCommand(){
        super("sanction", "/sanction <Joueur>", 1, Rank.MODO);
        suggest(true, Arguments.playerList("joueur"));
        sanctionGui = new SanctionGui();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        UUID uuid = CPlayerManager.getInstance().getPlayerUUID(args[0]);
        if(uuid == null){
            player.sendMessage(Text.PREFIX + "§cCe joueur ne s'est jamais connecté.");
            return;
        }
        sanctionGui.getGui(new ConsulatOffline(0, uuid, args[0], Rank.INVITE, null)).open(player);
    }
}