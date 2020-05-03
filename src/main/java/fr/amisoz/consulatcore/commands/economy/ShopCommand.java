package fr.amisoz.consulatcore.commands.economy;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

import java.util.Collections;

public class ShopCommand extends ConsulatCommand {
    
    public ShopCommand(){
        super("shop", "/shop list|help", 0, Rank.JOUEUR);
        suggest(LiteralArgumentBuilder.literal("shop")
                .then(LiteralArgumentBuilder.literal("list"))
                .then(LiteralArgumentBuilder.literal("help")));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length == 0){
            sender.sendMessage(Text.PREFIX + "§cListe des commandes:");
            sender.sendMessage(Text.PREFIX + "§c- §e/shop list §cte permet de voir la liste des shops !");
            sender.sendMessage(Text.PREFIX + "§c- §e/shop help §cte permet de savoir comment créer un shop !");
            return;
        }
        switch(args[0].toLowerCase()){
            case "list":
                if(!GuiManager.getInstance().getRootGui("shop").open(sender, 1)){
                    sender.sendMessage(Text.PREFIX + "§cIl n'y a aucun shop.");
                }
                break;
            case "help":
                ShopManager.getInstance().tutorial((SurvivalPlayer)sender);
                break;
        }
    }
}
