package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InvseeCommand extends ConsulatCommand {

    public InvseeCommand() {
        super("/invsee <Joueur>", 1, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        Player target = Bukkit.getPlayer(getArgs()[0]);

        if(target == null){
            getPlayer().sendMessage("§cJoueur invalide");
            return;
        }

        getCorePlayer().seeInv = true;
        getPlayer().openInventory(target.getInventory());
    }
}
