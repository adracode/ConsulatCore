package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class TestCommand extends ConsulatCommand {
    
    public TestCommand(){
        super(ConsulatCore.getInstance(), "test");
        setDescription("Commande de dev").
                setUsage("/test city lead - Faire un coup d'état\n" +
                        "/test city join <ville> - Rejoindre une ville\n" +
                        "/test money <montant> - Se donner de l'argent\n" +
                        "/test claim - Switch les permissions de claim").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(
                        LiteralArgumentBuilder.literal("claim"),
                        LiteralArgumentBuilder.literal("money").then(RequiredArgumentBuilder.argument("montant", DoubleArgumentType.doubleArg(0, 1000000))),
                        LiteralArgumentBuilder.literal("city").
                                then(LiteralArgumentBuilder.literal("join").
                                        then(RequiredArgumentBuilder.argument("city", StringArgumentType.word()))).
                                then(LiteralArgumentBuilder.literal("lead").
                                        then(RequiredArgumentBuilder.argument("city", StringArgumentType.word()))));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length > 0){
            switch(args[0]){
                case "city":
                    if(args.length < 2){
                        sender.sendMessage("§cPas assez d'arguments");
                        return;
                    }
                    switch(args[1]){
                        case "join":{
                            if(args.length < 3){
                                sender.sendMessage("§cPas assez d'arguments");
                                return;
                            }
                            City city = ZoneManager.getInstance().getCity(args[2]);
                            if(city == null){
                                return;
                            }
                            if(!ZoneManager.getInstance().invitePlayer(city, sender.getUUID())){
                                sender.sendMessage(Text.ALREADY_INVITED_CITY);
                                return;
                            }
                            sender.sendMessage(Text.YOU_BEEN_INVITED_TO_CITY(city.getName(), sender.getName()));
                        }
                        break;
                        case "lead":
                            City city = ((SurvivalPlayer)sender).getCity();
                            if(city == null){
                                sender.sendMessage(Text.YOU_NOT_IN_CITY);
                                return;
                            }
                            city.setOwner(sender.getUUID());
                            city.sendMessage(Text.PREFIX_CITY(city) + "§a" + sender.getName() + " a fait un coup d'état, il devient propriétaire.");
                            break;
                    }
                    break;
                case "claim":
                    if(sender.hasPermission(ConsulatCore.getInstance().getPermission("interact"))){
                        sender.removePermission(ConsulatCore.getInstance().getPermission("interact"));
                        sender.sendMessage("§cTu ne peux plus interagir avec tous les claims.");
                    } else {
                        sender.addPermission(ConsulatCore.getInstance().getPermission("interact"));
                        sender.sendMessage("§aTu peux interagir avec tous les claims.");
                    }
                    break;
                case "money":
                    if(args.length < 2){
                        sender.sendMessage("§cPas assez d'arguments");
                        return;
                    }
                    double money = Math.min(Math.abs(Double.parseDouble(args[1])), 1000000);
                    ((SurvivalPlayer)sender).addMoney(money);
                    sender.sendMessage(Text.YOU_RECEIVED_MONEY_FROM(money, sender.getName()));
                    break;
            }
        }
    }
}
