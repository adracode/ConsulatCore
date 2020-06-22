package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

public class BackCommand extends ConsulatCommand {

    public BackCommand() {
        super("back", "/back", 0, Rank.MECENE);
        suggest(true);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(player.getOldLocation() == null){
            player.sendMessage("§cTu n'as pas encore été téléporté.");
            return;
        }
        Bukkit.getWorlds().get(0).getChunkAt(player.getOldLocation()).load(true);
        sender.getPlayer().teleportAsync(player.getOldLocation());
        sender.sendMessage("§aTu as été téléporté ! ");
    }
}
