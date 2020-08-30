package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class EnderchestCommand extends ConsulatCommand {
    
    public EnderchestCommand(){
        super(ConsulatCore.getInstance(), "enderchest");
        setDescription("Voir l'enderchest d'un joueur").
                setUsage("/ec <joueur> - Voir l'enderchest d'un joueur").
                setAliases("ec").
                setArgsMin(1).
                setRank(Rank.MODO).
                suggest(Arguments.playerList("joueur"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(sender.getRank() == Rank.MODO && (!player.isInModeration() || sender.getPlayer().getGameMode() != GameMode.SPECTATOR)){
            player.sendMessage(Text.NEED_SPECTATOR);
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        sender.getPlayer().openInventory(target.getPlayer().getEnderChest());
    }
}