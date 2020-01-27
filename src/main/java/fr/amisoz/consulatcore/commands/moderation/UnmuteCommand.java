package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UnmuteCommand extends ConsulatCommand {

    private ConsulatCore consulatCore;
    public UnmuteCommand(ConsulatCore consulatCore) {
        super("/unmute <Joueur>", 1, RankEnum.ADMIN);
        this.consulatCore = consulatCore;
    }

    @Override
    public void consulatCommand() {
        String playerName = getArgs()[0];
        Player target = Bukkit.getPlayer((playerName));
        consulatCore.getModerationDatabase().unmute(playerName);
        if(target != null){
            CorePlayer targetPlayer = CoreManagerPlayers.getCorePlayer(target);
            if(targetPlayer.isMuted){
                targetPlayer.isMuted = false;
            }else{
                getPlayer().sendMessage(ChatColor.RED + "Le joueur n'est pas mute.");
            }
        }
    }
}
