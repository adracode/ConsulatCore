package fr.amisoz.consulatcore.commands.enchantments;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.enchantments.CEnchantedItem;
import fr.amisoz.consulatcore.enchantments.CEnchantment;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsoleUsable;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CEnchantCommand extends ConsulatCommand implements ConsoleUsable {
    
    public CEnchantCommand(){
        super(ConsulatCore.getInstance(), "cenchant");
        setDescription("Enchanter une armure / livre").
                setUsage("/cenchant <enchant> <niveau> - Enchanter une armure / livre").
                setArgsMin(1).
                setRank(Rank.RESPONSABLE).
                suggest(Arguments.word("enchant").
                        suggests((context, builder) -> {
                            Arguments.suggest(Arrays.asList(CEnchantment.Type.values()),
                                    (type) -> type.name().toLowerCase(), (ignored) -> true, builder);
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder.argument("niveau", IntegerArgumentType.integer(1, 2))));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        CEnchantedItem enchantedItem;
        try {
            enchantedItem = new CEnchantedItem(sender.getPlayer().getInventory().getItemInMainHand());
        } catch(IllegalArgumentException e){
            sender.sendMessage("§cCet item ne peut pas être enchanté.");
            return;
        }
        if(!enchantedItem.addEnchantment(CEnchantment.Type.valueOf(args[0].toUpperCase()), args.length == 1 ? 1 : Integer.parseInt(args[1]))){
            sender.sendMessage("§cL'enchantement " + args[0] + " n'a pas pu être appliqué à cet item.");
        }
    }
    
    @Override
    public void onConsoleUse(CommandSender sender, String[] args){
        if(args.length < 3){
            sender.sendMessage("§c/cenchant <enchant> <niveau> <joueur>");
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[2]);
        if(player == null){
            sender.sendMessage("§cCe joueur n'est pas connecté.");
            return;
        }
        CEnchantedItem enchantedItem = new CEnchantedItem(new ItemStack(Material.ENCHANTED_BOOK));
        if(!enchantedItem.addEnchantment(CEnchantment.Type.valueOf(args[0].toUpperCase()), Integer.parseInt(args[1]))){
            sender.sendMessage("§cL'enchantement " + args[0] + " n'a pas pu être appliqué à cet item.");
        }
        player.getPlayer().getInventory().addItem(enchantedItem.getHandle());
    }
}
