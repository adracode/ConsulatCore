package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.*;

public class TpaCommand extends ConsulatCommand {
    
    //Celui qui fait la requête, celui qui l'accepte
    private Map<UUID, UUID> request = new HashMap<>();
    
    public TpaCommand(){
        super("tpa", "/tpa <Joueur> | /tpa accept", 1, Rank.JOUEUR);
        suggest(true, LiteralArgumentBuilder.literal("accept")
                        .then(Arguments.player("joueur").suggests(((context, builder) -> {
                            List<ConsulatPlayer> playersToAcceptTp = new ArrayList<>();
                            ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayerFromContextSource(context.getSource());
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
                Arguments.playerList("joueur")
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        //tpa joueur = envoie une requête
        if(args.length == 1){
            SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
            if(target == null){
                sender.sendMessage(Text.PREFIX + "§cLe joueur n'est pas connecté.");
                return;
            }
            if(!survivalSender.hasMoney(10.0) && !survivalSender.hasPower(Rank.MECENE)){
                sender.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent.");
                return;
            }
            if(target.getUUID().equals(request.get(sender.getUUID()))){
                sender.sendMessage(Text.PREFIX + "§cTu as déjà fait une demande de téléportation à " + target.getName());
                return;
            }
            request.put(sender.getUUID(), target.getUUID());
            sender.sendMessage(Text.PREFIX + "§aTu as fait une demande de téléportation à " + target.getName());
            target.sendMessage(Text.PREFIX + "§eTu as reçu une demande de téléportation de " + sender.getName());
            target.sendMessage(Text.PREFIX + "§eFais /tpa accept " + sender.getName());
        } else if(args.length == 2){
            if(args[0].equalsIgnoreCase("accept")){
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
                if(target == null){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur n'est pas connecté.");
                    return;
                }
                if(!sender.getUUID().equals(request.get(target.getUUID()))){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur ne t'a pas demandé en téléportation.");
                    return;
                }
                if(!target.hasPower(Rank.MECENE)){
                    request.remove(target.getUUID());
                    target.removeMoney(10D);
                    target.getPlayer().teleportAsync(sender.getPlayer().getLocation());
                    target.sendMessage("§aTu as été téléporté à " + sender.getName() + " pour 10 €.");
                } else {
                    target.getPlayer().teleportAsync(sender.getPlayer().getLocation());
                    request.remove(target.getUUID());
                    target.sendMessage("§aTu as été téléporté à " + sender.getName() + ".");
                }
            } else {
                sender.sendMessage(Text.PREFIX + "§c/tpa accept <joueur>");
            }
        } else {
            sender.sendMessage("§c" + getUsage());
        }
    }
}
