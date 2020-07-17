package fr.amisoz.consulatcore.commands.enchantments;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.enchantments.CEnchantedItem;
import fr.amisoz.consulatcore.enchantments.CEnchantment;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.Arrays;

public class CEnchantCommand extends ConsulatCommand {
    
    public CEnchantCommand(){
        super("cenchant", "/cenchant", 2, Rank.ADMIN);
        setPermission("consulat.core.command.cenchant");
        suggest(false,
                Arguments.word("enchant").suggests((context, builder) -> {
                    Arguments.suggest(Arrays.asList(CEnchantment.Type.values()),
                            (type) -> type.name().toLowerCase(), (ignored) -> true, builder);
                    return builder.buildFuture();
                }).then(RequiredArgumentBuilder.argument("level", IntegerArgumentType.integer(1, 2))));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        CEnchantedItem enchantedItem = new CEnchantedItem(sender.getPlayer().getInventory().getItemInMainHand());
        enchantedItem.addEnchantment(CEnchantment.Type.valueOf(args[0].toUpperCase()), Integer.parseInt(args[1]));
    }
}
