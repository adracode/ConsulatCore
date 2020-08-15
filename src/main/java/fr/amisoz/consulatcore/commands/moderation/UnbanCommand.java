package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class UnbanCommand extends ConsulatCommand {
    
    public UnbanCommand(){
        super(ConsulatCore.getInstance(), "unban");
        setDescription("Débannir un joueur").
                setUsage("/unban <joueur> - Débannir un joueur").
                setArgsMin(1).
                setRank(Rank.RESPONSABLE).
                suggest(Arguments.word("joueur"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        String playerName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            ConsulatCore.getInstance().getModerationDatabase().unban(playerName);
            sender.sendMessage(Text.MAYBE_UNBAN_PLAYER);
        });
    }
}
