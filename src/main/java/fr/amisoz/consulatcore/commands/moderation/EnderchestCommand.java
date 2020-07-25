package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.GameMode;

import java.util.Collections;

public class EnderchestCommand extends ConsulatCommand {
    
    public EnderchestCommand(){
        super("enderchest", Collections.singletonList("ec"), "/ec <Joueur>", 1, Rank.MODO);
        suggest(true, Arguments.playerList("joueur"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(sender.getRank() == Rank.MODO && (!player.isInModeration() || sender.getPlayer().getGameMode() != GameMode.SPECTATOR)){
            player.sendMessage("§cTu dois être en spectateur pour regarder les enderchest.");
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cJoueur invalide.");
            return;
        }
        sender.getPlayer().openInventory(target.getPlayer().getEnderChest());
    }
}