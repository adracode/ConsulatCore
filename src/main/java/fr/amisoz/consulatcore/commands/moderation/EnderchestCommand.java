package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.Collections;

public class EnderchestCommand extends ConsulatCommand {

    public EnderchestCommand() {
        super("enderchest", Collections.singletonList("ec"), "/ec <Joueur>", 1, Rank.MODPLUS);
        suggest(true,
                Arguments.playerList("joueur"));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args) {
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cJoueur invalide.");
            return;
        }
        sender.getPlayer().openInventory(target.getPlayer().getEnderChest());
    }
}