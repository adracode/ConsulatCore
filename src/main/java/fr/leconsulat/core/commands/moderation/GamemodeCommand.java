package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommand extends ConsulatCommand {
    
    public GamemodeCommand(){
        super(ConsulatCore.getInstance(), "gm");
        setDescription("Switcher de gamemode entre survie et spectator").
                setUsage("/gm - Switcher de gamemode").
                setRank(Rank.MODO).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
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
