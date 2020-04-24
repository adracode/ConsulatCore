package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InvseeCommand extends ConsulatCommand {

    public InvseeCommand() {
        super("/invsee <Joueur>", 1, Rank.MODO);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cJoueur invalide.");
            return;
        }
        ((SurvivalPlayer)sender).setLookingInventory(true);
        sender.getPlayer().openInventory(target.getPlayer().getInventory());
    }
}
