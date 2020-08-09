package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.GameMode;

public class EnderchestCommand extends ConsulatCommand {
    
    public EnderchestCommand(){
        super("consulat.core", "enderchest", "ec", "/ec <Joueur>", 1, Rank.MODO);
        suggest(Arguments.playerList("joueur"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
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