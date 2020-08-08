package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.moderation.AntecedentsGuiContainer;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatOffline;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.UUID;

public class AntecedentsComand extends ConsulatCommand {

    public AntecedentsComand() {
        super("consulat.core", "antecedents", "/antecedents <Joueur>", 1, Rank.RESPONSABLE);
        suggest(Arguments.playerList("joueur"));
        new AntecedentsGuiContainer();
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        UUID uuid = CPlayerManager.getInstance().getPlayerUUID(args[0]);
        if(uuid == null){
            player.sendMessage(Text.PREFIX + "§cCe joueur ne s'est jamais connecté.");
            return;
        }
        GuiManager.getInstance().getContainer("antecedents").getGui(new ConsulatOffline(0, uuid, args[0], Rank.INVITE, null)).open(sender);
    }
}
