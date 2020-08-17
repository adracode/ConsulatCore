package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class TestCommand extends ConsulatCommand {
    
    public TestCommand(){
        super(ConsulatCore.getInstance(), "test");
        setDescription("Commande de dev").
                setUsage("/test city lead <ville> - Faire un coup d'état\n" +
                        "/test claim - Switch les permissions de claim").
                setRank(Rank.DEVELOPPEUR).
                suggest(
                        LiteralArgumentBuilder.literal("claim"),
                        LiteralArgumentBuilder.literal("city").
                                then(LiteralArgumentBuilder.literal("lead").
                                        then(RequiredArgumentBuilder.argument("city", StringArgumentType.word()))));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length > 0){
            switch(args[0]){
                case "city":
                    switch(args[1]){
                        case "lead":
                            City city = ZoneManager.getInstance().getCity(args[2]);
                            if(city == null){
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
            }
        }
    }
}
