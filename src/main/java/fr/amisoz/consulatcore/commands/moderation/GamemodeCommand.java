package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeCommand extends ConsulatCommand {
    
    public GamemodeCommand(){
        super("gm", "/gm", 0, Rank.MODO);
        suggest(LiteralArgumentBuilder.literal("gm"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(!ModerationUtils.moderatePlayers.contains(sender.getPlayer())){
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
