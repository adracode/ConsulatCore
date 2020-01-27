package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;

public class BackCommand extends ConsulatCommand {

    public BackCommand() {
        super("/back", 0, RankEnum.MECENE);
    }

    @Override
    public void consulatCommand() {
        Bukkit.getWorlds().get(0).getChunkAt(getCorePlayer().oldLocation).load(true);
        getPlayer().teleport(getCorePlayer().oldLocation);
        getPlayer().sendMessage("§aTu as été téléporté ! ");
    }
}
