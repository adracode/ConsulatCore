package fr.leconsulat.core.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.commands.commands.ADebugCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.chunks.ChunkManager;
import fr.leconsulat.core.players.Fly;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.shop.ShopManager;
import fr.leconsulat.core.shop.player.PlayerShop;
import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.cities.City;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.UUID;

public class CDebugCommand extends ConsulatCommand {
    
    public CDebugCommand(){
        super(ConsulatCore.getInstance(), "cdebug");
        setDescription("Survie debug").
                setUsage("/cdebug ...").
                suggest(listener -> {
                            ConsulatPlayer player = getConsulatPlayer(listener);
                            return player != null && ADebugCommand.UUID_PERMISSION.contains(player.getUUID());
                        },
                        LiteralArgumentBuilder.literal("chunk"),
                        LiteralArgumentBuilder.literal("city").
                                then(LiteralArgumentBuilder.literal("lead").
                                        then(RequiredArgumentBuilder.argument("city", StringArgumentType.word()))).
                                then(LiteralArgumentBuilder.literal("join").
                                        then(RequiredArgumentBuilder.argument("city", StringArgumentType.word()))).
                                then(LiteralArgumentBuilder.literal("add").
                                        then(Arguments.playerList("player"))).
                                then(LiteralArgumentBuilder.literal("kick").
                                        then(Arguments.playerList("player"))),
                        LiteralArgumentBuilder.literal("fly").then(LiteralArgumentBuilder.literal("reset")),
                        LiteralArgumentBuilder.literal("shop"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        if(!ADebugCommand.UUID_PERMISSION.contains(sender.getUUID())){
            return;
        }
        if(args.length > 0){
            switch(args[0]){
                case "chunk":
                    sender.sendMessage(ChunkManager.getInstance().getChunk(sender.getPlayer().getChunk()).toString());
                    break;
                case "city":
                    switch(args[1]){
                        case "join":{
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
                        case "add":{
                            City city = ((SurvivalPlayer)sender).getCity();
                            if(city == null){
                                return;
                            }
                            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                            city.addPlayer(target.getUniqueId());
                            city.sendMessage(Text.HAS_JOINED_CITY(city, target.getName()));
                        }
                        break;
                        case "kick":{
                            City city = ((SurvivalPlayer)sender).getCity();
                            if(city == null){
                                return;
                            }
                            UUID uuid;
                            try {
                                uuid = UUID.fromString(args[2]);
                            } catch(IllegalArgumentException e){
                                uuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                            }
                            city.removePlayer(uuid);
                            city.sendMessage(Text.YOU_KICKED_PLAYER);
                        }
                        break;
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
