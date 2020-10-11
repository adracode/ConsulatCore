package fr.leconsulat.core.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.jetbrains.annotations.NotNull;

public class FlyCommand extends ConsulatCommand {
    
    public FlyCommand(){
        super(ConsulatCore.getInstance(), "fly");
        setDescription("Gérer son fly").
                setUsage("/fly info - Voir les informations sur ton fly\n" +
                        "/fly start - Activer le fly\n" +
                        "/fly stop - Désactiver le fly").
                setArgsMin(1).
                suggest((listener) -> {
                            SurvivalPlayer player = (SurvivalPlayer)getConsulatPlayer(listener);
                            return player != null && player.hasFly();
                        },
                        LiteralArgumentBuilder.literal("start"),
                        LiteralArgumentBuilder.literal("stop"),
                        LiteralArgumentBuilder.literal("info"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(!player.hasFly()){
            sender.sendMessage(Text.NO_FLY);
            return;
        }
        if(sender.getPlayer().getWorld() != ConsulatCore.getInstance().getOverworld()){
            sender.sendMessage(Text.CANT_FLY_DIMENSION);
            return;
        }
        if(player.isInModeration()){
            sender.sendMessage(Text.CANT_USE_COMMAND_STAFF_MODE);
            return;
        }
        if(player.isInCombat()){
            player.sendMessage(Text.IN_COMBAT);
            return;
        }
        switch(args[0].toLowerCase()){
            case "start":
                if(player.isFlying()){
                    sender.sendMessage(Text.FLY_ALREADY_ON);
                    return;
                }
                if(!player.isFlyAvailable()){
                    long timeWait = (player.getFlyReset() - System.currentTimeMillis()) / 1000;
                    long minutes = ((timeWait / 60) % 60);
                    long seconds = (timeWait % 60);
                    player.sendMessage(Text.WAIT_FLY(minutes, seconds));
                    return;
                }
                if(!player.canFlyHere()){
                    sender.sendMessage(Text.CANT_FLY_HERE);
                    return;
                }
                player.enableFly();
                sender.sendMessage(Text.FLY_ON);
                break;
            case "info":{
                if(player.hasInfiniteFly()){
                    player.sendMessage(Text.INFINITE_FLY);
                    return;
                }
                int timeLeft;
                if(player.getFlyReset() < System.currentTimeMillis()){
                    timeLeft = player.getFlyTime();
                } else {
                    timeLeft = player.getFlyTimeLeft();
                }
                long minutes = ((timeLeft / 60) % 60);
                long seconds = timeLeft % 60;
                sender.sendMessage(Text.FLY_INFO(minutes, seconds));
            }
            break;
            case "stop":{
                if(!player.isFlying()){
                    sender.sendMessage(Text.NOT_IN_FLY);
                    return;
                }
                player.disableFly();
                sender.sendMessage(Text.FLY + (player.hasInfiniteFly() ? Text.FLY_INFINITE_OFF : Text.FLY_OFF));
            }
            break;
            default:
                sender.sendMessage(Text.COMMAND_USAGE(this));
                break;
        }
    }
}
