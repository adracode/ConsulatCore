package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EnderchestCommand extends ConsulatCommand {

    public EnderchestCommand() {
        super("/ec <Joueur>", 1, RankEnum.RESPONSABLE);
    }

    @Override
    public void consulatCommand() {
        Player target = Bukkit.getPlayer(getArgs()[0]);

        if(target == null){
            getPlayer().sendMessage("§cJoueur invalide");
            return;
        }

        getPlayer().openInventory(target.getEnderChest());
    }
}
