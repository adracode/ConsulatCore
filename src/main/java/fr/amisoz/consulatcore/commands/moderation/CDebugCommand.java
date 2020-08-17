package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.amisoz.consulatcore.players.Fly;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.shop.player.PlayerShop;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;

import java.lang.reflect.Field;
import java.util.UUID;

public class CDebugCommand extends ConsulatCommand {
    
    public static final UUID UUID_PERMS = UUID.fromString("43da311c-d869-4e88-9b78-f1d4fc193ed4");
    
    public CDebugCommand(){
        super(ConsulatCore.getInstance(), "cdebug");
        setDescription("Survie debug").
                setUsage("/cdebug ...").
                suggest(listener -> {
                            ConsulatPlayer player = getConsulatPlayer(listener);
                            return player != null && player.getUUID().equals(UUID_PERMS);
                        },
                        LiteralArgumentBuilder.literal("chunk"),
                        LiteralArgumentBuilder.literal("city").
                                then(LiteralArgumentBuilder.literal("join").
                                        then(RequiredArgumentBuilder.argument("city", StringArgumentType.word()))),
                        LiteralArgumentBuilder.literal("fly").then(LiteralArgumentBuilder.literal("reset")),
                        LiteralArgumentBuilder.literal("shop"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(!sender.getUUID().equals(UUID_PERMS)){
            return;
        }
        if(args.length > 0){
            switch(args[0]){
                case "chunk":
                    sender.sendMessage(ChunkManager.getInstance().getChunk(sender.getPlayer().getChunk()).toString());
                    break;
                case "city":
                    switch(args[1]){
                        case "join":
                            City city = ZoneManager.getInstance().getCity(args[2]);
                            if(city == null){
                                return;
                            }
                            if(!ZoneManager.getInstance().invitePlayer(city, sender.getUUID())){
                                sender.sendMessage(Text.ALREADY_INVITED_CITY);
                                return;
                            }
                            sender.sendMessage(Text.YOU_BEEN_INVITED_TO_CITY(city.getName(), sender.getName()));
                            break;
                    }
                    break;
                case "shop":
                    PlayerShop shop = ShopManager.getInstance().getPlayerShop(sender.getPlayer().getTargetBlock(5).getLocation());
                    if(shop == null){
                        return;
                    }
                    sender.sendMessage("Shop x = " + shop.getX() + " y = " + shop.getY() + " z = " + shop.getZ());
                    sender.sendMessage("Restant: " + shop.getAmount());
                    sender.sendMessage("Item: " + shop.getItem());
                    sender.sendMessage("ItemFrame: " + shop.getItemFrame().getLocation() + ", Facing: " + shop.getItemFrame().getFacing());
                    break;
                case "fly":
                    if(args.length < 2){
                        return;
                    }
                    switch(args[1]){
                        case "reset":
                            SurvivalPlayer player = (SurvivalPlayer)sender;
                            try {
                                Field field = Fly.class.getDeclaredField("reset");
                                field.setAccessible(true);
                                field.set(player.getFly(), System.currentTimeMillis());
                            } catch(IllegalAccessException | NoSuchFieldException e){
                                e.printStackTrace();
                            }
                            break;
                    }
                
            }
        }
    }
}
