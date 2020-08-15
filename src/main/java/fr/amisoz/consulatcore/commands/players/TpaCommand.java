package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TpaCommand extends ConsulatCommand {
    
    //Celui qui fait la requête, celui qui l'accepte
    private Map<UUID, UUID> request = new HashMap<>();
    
    public TpaCommand(){
        super(ConsulatCore.getInstance(), "tpa");
        setDescription("Se téléporter à un joueur").
                setUsage("/tpa <joueur> - Envoyer une demande de TP\n" +
                        "/tpa accept <joueur> - Accepter une demande de TP").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(LiteralArgumentBuilder.literal("accept")
                                .then(Arguments.player("joueur").suggests(((context, builder) -> {
                                    List<ConsulatPlayer> playersToAcceptTp = new ArrayList<>();
                                    ConsulatPlayer player = getConsulatPlayerFromContext(context.getSource());
                                    if(player == null){
                                        return builder.buildFuture();
                                    }
                                    for(Map.Entry<UUID, UUID> request : request.entrySet()){
                                        if(request.getValue().equals(player.getUUID())){
                                            playersToAcceptTp.add(CPlayerManager.getInstance().getConsulatPlayer(request.getKey()));
                                        }
                                    }
                                    Arguments.suggest(playersToAcceptTp, ConsulatPlayer::getName, (p) -> true, builder);
                                    return builder.buildFuture();
                                }))),
                        Arguments.playerList("joueur"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        //tpa joueur = envoie une requête
        if(args.length == 1){
            SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
            if(target == null){
                sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
                return;
            }
            if(!survivalSender.hasMoney(10) && !survivalSender.hasPower(Rank.MECENE)){
                sender.sendMessage(Text.NOT_ENOUGH_MONEY(10));
                return;
            }
            if(target.getUUID().equals(request.get(sender.getUUID()))){
                sender.sendMessage(Text.ALREADY_ASK_TPA(target.getName()));
                return;
            }
            request.put(sender.getUUID(), target.getUUID());
            sender.sendMessage(Text.TPA_TO(target.getName()));
            target.sendMessage(Text.TPA_FROM(sender.getName()));
        } else if(args.length == 2){
            if(args[0].equalsIgnoreCase("accept")){
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
                if(target == null){
                    sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
                    return;
                }
                if(!sender.getUUID().equals(request.get(target.getUUID()))){
                    sender.sendMessage(Text.DIDNT_TPA);
                    return;
                }
                if(!target.hasPower(Rank.MECENE)){
                    request.remove(target.getUUID());
                    target.removeMoney(10D);
                    target.getPlayer().teleportAsync(sender.getPlayer().getLocation());
                    target.sendMessage(Text.HAVE_BEEN_TPA(10, sender.getName()));
                } else {
                    target.getPlayer().teleportAsync(sender.getPlayer().getLocation());
                    request.remove(target.getUUID());
                    target.sendMessage(Text.HAVE_BEEN_TPA(sender.getName()));
                }
            } else {
                sender.sendMessage(Text.COMMAND_USAGE(this));
            }
        } else {
            sender.sendMessage(Text.COMMAND_USAGE(this));
        }
    }
}
