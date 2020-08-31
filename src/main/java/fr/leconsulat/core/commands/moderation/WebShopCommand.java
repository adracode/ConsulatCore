package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.ConsoleUsable;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.commands.commands.ADebugCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.Fly;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class WebShopCommand extends ConsulatCommand implements ConsoleUsable {
    
    public WebShopCommand(){
        super(ConsulatCore.getInstance(), "boutique");
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        if(!ADebugCommand.UUID_PERMISSION.contains(sender.getUUID())){
            sender.getPlayer().performCommand("help");
            return;
        }
        if(args[0].equals("announce")){
            return;
        }
        perform((SurvivalPlayer)sender, args[0], args[1]);
    }
    
    @Override
    public void onConsoleUse(CommandSender sender, String[] args){
        if(args.length < 2){
            sender.sendMessage("§cErreur");
            return;
        }
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
        if(target == null){
            sender.sendMessage("Erreur");
            return;
        }
        switch(args.length){
            case 2:
                perform(target, args[0]);
                break;
            case 3:
                perform(target, args[0], args[2]);
                break;
            default:
                perform(target, args[0], args[2], args[3]);
                break;
        }
    }
    
    private void perform(SurvivalPlayer target, String subCommand, String... args){
        switch(subCommand.toLowerCase()){
            case "rank":
                String rank = args[0];
                if(rank.equalsIgnoreCase("financeur") ||
                        rank.equalsIgnoreCase("mécène")){
                    Rank newRank = Rank.byName(rank);
                    target.setRank(newRank);
                    target.sendMessage("§7Suite à ton achat, tu es désormais " + newRank.getRankColor() + newRank.getRankName());
                }
                break;
            case "announce":
                Bukkit.broadcastMessage("§7[§aBoutique§7] §a" + target.getName() + "§7 a acheté §a" + args[0] + " " + args[1] + "§7 !");
                break;
            case "home":
                target.incrementLimitHome();
                target.sendMessage("§7Suite à ton achat, tu as un home supplémentaire !");
                break;
            case "up":
                target.setPerkTop(true);
                target.sendMessage("§7Suite à ton achat, tu as accès au /top !");
                break;
            case "fly5":
                target.setFly(Fly.FLY_5);
                target.sendMessage(Text.FLY + "Suite à ton achat tu as maintenant accès au /fly qui dure 5minutes toute les heures !");
                break;
            case "fly25":
                target.setFly(Fly.FLY_25);
                target.sendMessage(Text.FLY + "Suite à ton achat tu as maintenant accès au /fly qui dure 25 minutes toute les heures !");
                break;
            case "infinite":
                target.setFly(Fly.FLY_INFINITE);
                target.sendMessage(Text.FLY + "Suite à ton achat tu as maintenant accès au /fly infini !");
                break;
            case "perso":
                target.setHasCustomRank(true);
                target.sendMessage("§7Suite à ton achat, tu as le grade personnalisé ! Fais /perso et laisse toi guider ;)");
                break;
        }
    }
    
}
