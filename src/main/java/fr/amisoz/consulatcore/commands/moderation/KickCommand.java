package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

public class KickCommand extends ConsulatCommand {
    
    public KickCommand(){
        super(ConsulatCore.getInstance(), "kick");
        setDescription("Expulser un joueur du serveur").
                setUsage("/kick <joueur> <raison> - Expulser un joueur").
                setArgsMin(2).
                setRank(Rank.MODO).
                suggest(Arguments.playerList("joueur")
                        .then(RequiredArgumentBuilder.argument("raison", StringArgumentType.greedyString())));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < args.length; i++){
            stringBuilder.append(" ").append(args[i]);
        }
        String reason = stringBuilder.toString();
        target.getPlayer().kickPlayer(Text.KICK_PLAYER(reason));
        sender.sendMessage(Text.YOU_KICKED_PLAYER);
    }
}
