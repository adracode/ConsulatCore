package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeCommand extends ConsulatCommand {
    
    public GamemodeCommand(){
        super("consulat.core", "gm", "/gm", 0, Rank.MODO);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(!((SurvivalPlayer)sender).isInModeration()){
            sender.sendMessage(Text.NEED_STAFF_MODE);
            return;
        }
        Player bukkitPlayer = sender.getPlayer();
        if(bukkitPlayer.getGameMode() == GameMode.SURVIVAL){
            bukkitPlayer.setGameMode(GameMode.SPECTATOR);
        } else if(bukkitPlayer.getGameMode() == GameMode.SPECTATOR){
            bukkitPlayer.setGameMode(GameMode.SURVIVAL);
            bukkitPlayer.setAllowFlight(true);
            bukkitPlayer.setFlying(true);
        }
    }
}
