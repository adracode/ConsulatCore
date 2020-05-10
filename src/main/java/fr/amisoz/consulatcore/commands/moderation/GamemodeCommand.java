package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeCommand extends ConsulatCommand {
    
    public GamemodeCommand(){
        super("gm", "/gm", 0, Rank.MODO);
        suggest(true);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(!((SurvivalPlayer)sender).isInModeration()){
            sender.sendMessage("§cTu dois être en staff mode.");
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
