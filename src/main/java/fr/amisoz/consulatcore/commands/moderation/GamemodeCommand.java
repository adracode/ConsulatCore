package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.GameMode;

public class GamemodeCommand extends ConsulatCommand {

    public GamemodeCommand() {
        super("/gm", 0, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        if(!ModerationUtils.moderatePlayers.contains(getPlayer())){
            getPlayer().sendMessage("§cTu dois être en staff mdoe.");
            return;
        }

        if(getPlayer().getGameMode().equals(GameMode.SURVIVAL)){
            getPlayer().setGameMode(GameMode.SPECTATOR);
        }else if(getPlayer().getGameMode().equals(GameMode.SPECTATOR)){
            getPlayer().setGameMode(GameMode.SURVIVAL);
            getPlayer().setAllowFlight(true);
            getPlayer().setFlying(true);
        }
    }
}
