package fr.amisoz.consulatcore.commands.economy;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.shop.player.ShopItemType;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.potion.PotionEffectType;

public class ShopCommand extends ConsulatCommand {
    
    private final String help =
            Text.PREFIX + "§cListe des commandes:\n" +
            Text.PREFIX + "§c- §e/shop help §cte permet de savoir comment créer un shop !\n" +
            Text.PREFIX + "§c- §e/shop locate §cte permet de voir la liste des shops d'un certain item, enchantement ou potion !\n" +
            Text.PREFIX + "§c- §e/shop list §cte permet de voir la liste des shops !";
    
    public ShopCommand(){
        super("consulat.core", "shop", "/shop list | help | locate <item>", 0, Rank.JOUEUR);
        LiteralArgumentBuilder<Object> locate = LiteralArgumentBuilder.literal("locate").then(RequiredArgumentBuilder.argument("item", StringArgumentType.word()).suggests(((context, builder) -> {
            for(ShopItemType type : ShopManager.getInstance().getNonEmptyTypes()){
                if(type.toString().startsWith(builder.getRemaining())){
                    builder.suggest(type.toString());
                }
            }
            return builder.buildFuture();
        })));
        suggest(LiteralArgumentBuilder.literal("list"),
                locate,
                LiteralArgumentBuilder.literal("help"),
                LiteralArgumentBuilder.literal("create")
                        .requires((listener) -> {
                            if(!ConsulatAPI.getConsulatAPI().isDebug()){
                                return false;
                            }
                            ConsulatPlayer player = getConsulatPlayer(listener);
                            return player != null && player.hasPower(Rank.MODO);
                        })
                        .then(RequiredArgumentBuilder.argument("nombre", IntegerArgumentType.integer()))
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length == 0){
            sender.sendMessage(help);
            return;
        }
        switch(args[0].toLowerCase()){
            case "list":
                GuiManager.getInstance().getContainer("shop").getGui(ShopItemType.ALL).open(sender);
                return;
            case "help":
                sender.sendMessage(Text.TUTORIAL_SHOP);
                return;
            case "locate":
                if(args.length < 2){
                    break;
                }
                ShopItemType type;
                String itemType = args[1];
                Material material = Material.getMaterial(itemType.toUpperCase());
                if(material == null){
                    itemType = itemType.toLowerCase();
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(itemType));
                    if(enchantment == null){
                        PotionEffectType effectType = PotionEffectType.getByName(itemType);
                        if(effectType == null){
                            sender.sendMessage(Text.INVALID_ITEM(args[1]));
                            return;
                        }
                        type = new ShopItemType.PotionItem(effectType);
                    } else {
                        type = new ShopItemType.EnchantmentItem(enchantment);
                    }
                } else {
                    type = new ShopItemType.MaterialItem(material);
                }
                IGui shop = GuiManager.getInstance().getContainer("shop").getGui(false, type);
                if(shop == null){
                    sender.sendMessage(Text.ITEM_NOT_IN_SELL(args[1]));
                } else {
                    shop.open(sender);
                }
                return;
            case "create":
                if(!ConsulatAPI.getConsulatAPI().isDebug() || !sender.hasPower(Rank.MODO)){
                    break;
                }
                Block block = sender.getPlayer().getLocation().getBlock();
                for(int i = 0, size = Math.min(args.length == 1 ? 1 : Integer.parseInt(args[1]), 20); i < size; ++i){
                    createShop(sender, block.getRelative(i, 0, 0));
                }
                return;
        }
        sender.sendMessage(Text.COMMAND_USAGE(this));
    }
    
    private void createShop(ConsulatPlayer sender, Block block){
        if(block.getType() != Material.AIR){
            sender.sendMessage(Text.BLOCK_HERE);
            return;
        }
        block.setType(Material.CHEST);
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest)block.getState();
        chest.getBlockInventory().addItem(sender.getPlayer().getInventory().getItemInMainHand());
        Block sign = block.getRelative(((Chest)block.getBlockData()).getFacing());
        sign.setType(Material.OAK_WALL_SIGN);
        Sign sign1 = (Sign)sign.getState();
        sign1.setLine(0, "[consulshop]");
        sign1.setLine(1, (ConsulatCore.getRandom().nextInt(10) + 1) + "");
        sign1.update();
        SignChangeEvent event = new SignChangeEvent(sign, sender.getPlayer(), sign1.getLines());
        Bukkit.getServer().getPluginManager().callEvent(event);
        String[] lines = event.getLines();
        for(int i = 0; i < lines.length; i++){
            sign1.setLine(i, lines[i]);
        }
        sign1.update();
    }
    
}
