package fr.amisoz.consulatcore.commands.manager;

import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class ConsulatCommand implements CommandExecutor {

    private String usage;
    private int argsMin;
    private RankEnum rankMinimum;

    private Player player;
    private CorePlayer corePlayer;
    private String[] args;

    public ConsulatCommand(String usage, int argsMin, RankEnum rankMinimum) {
        this.usage = usage;
        this.argsMin = argsMin;
        this.rankMinimum = rankMinimum;
    }

    public abstract void consulatCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.args = args;

        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Il faut être en jeu pour éxecuter cette commande.");
            return false;
        }

        if(args.length < argsMin){
            sender.sendMessage(ChatColor.RED + usage);
            return false;
        }

        player = (Player) sender;
        corePlayer = CoreManagerPlayers.getCorePlayer(player);
        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);

        if(consulatPlayer.getRank().getRankPower() < rankMinimum.getRankPower()){
            player.sendMessage(ChatColor.RED + "Tu n'as pas le power requis.");
            return false;
        }

        consulatCommand();
        return true;
    }

    public ConsulatPlayer getConsulatPlayer(){
        return PlayersManager.getConsulatPlayer(player);
    }
    public CorePlayer getCorePlayer() {
        return corePlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public String[] getArgs() {
        return args;
    }
}
