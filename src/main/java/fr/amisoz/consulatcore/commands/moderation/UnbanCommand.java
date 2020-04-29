package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UnbanCommand extends ConsulatCommand {
    
    public UnbanCommand(){
        super("unban", "/unban <Pseudo>", 1, Rank.RESPONSABLE);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        String playerName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            ConsulatCore.getInstance().getModerationDatabase().unban(playerName);
            sender.sendMessage(Text.MODERATION_PREFIX + "Si le joueur était banni, il a été dé-banni.");
        });
    }
}
