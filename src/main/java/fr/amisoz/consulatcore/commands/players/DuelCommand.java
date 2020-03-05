package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.duel.Arena;
import fr.amisoz.consulatcore.duel.ArenaState;
import fr.amisoz.consulatcore.duel.DuelManager;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DuelCommand extends ConsulatCommand {

    public DuelCommand() {
        super("/sduel spectate ou /duel <Joueur> <Mise> ou /duel accept/reject", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        if(getArgs().length == 1){

            if(getArgs()[0].equalsIgnoreCase("spectate")){
                Arena arena = null;
                for(Arena arenaLoop : DuelManager.arenas){
                    if(arenaLoop.isBusy()){
                        arena = arenaLoop;
                        break;
                    }
                }

                if(arena == null){
                    getPlayer().sendMessage(ChatColor.RED + "Aucun combat n'est en cours.");
                }else{
                    getPlayer().teleport(arena.getSpectateLocation());
                }
                return;
            }

            if(getCorePlayer().arena == null){
                getPlayer().sendMessage("§cTu n'as pas de demande de duel.");
                return;
            }

            Arena arena = getCorePlayer().arena;
            Player askDuel = arena.getFirstPlayer();

            if(getArgs()[0].equalsIgnoreCase("accept")){
                Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §4Un duel va avoir lieu ! La mise de chaque participant est de " +  arena.bet + "€ ! Pour y être téléporté afin de regarder le duel, /duel spectate");
                arena.setArenaState(ArenaState.DUEL_ACCEPTED);
                arena.getFirstPlayer().teleport(arena.getFirstSpawn());
                arena.getSecondPlayer().teleport(arena.getSecondSpawn());

                arena.getFirstPlayer().sendMessage("§7Le combat commence dans 30 secondes.");
                arena.getSecondPlayer().sendMessage("§7Le combat commence dans 30 secondes.");

                PlayersManager.getConsulatPlayer(arena.getFirstPlayer()).removeMoney((double) arena.bet);
                PlayersManager.getConsulatPlayer(arena.getSecondPlayer()).removeMoney((double) arena.bet);

                getCorePlayer().isFighting = true;
                CoreManagerPlayers.getCorePlayer(arena.getFirstPlayer()).isFighting = true;

                Bukkit.getScheduler().runTaskLater(ConsulatCore.INSTANCE, () -> {

                    arena.setArenaState(ArenaState.IN_FIGHT);
                    Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §4Que le duel commence !");

                }, 20*30);

            }else if(getArgs()[0].equalsIgnoreCase("reject")){
                askDuel.sendMessage("§4" + getPlayer().getName() + "§c a refusé ton duel !");
                getPlayer().sendMessage("§cTu as bien refusé le duel !");

                arena.setFirstPlayer(null);
                arena.setSecondPlayer(null);
                arena.setBusy(false);
                arena.setArenaState(ArenaState.FREE);
            }else sendUsage();
        }else if(getArgs().length == 2){
            Player target = Bukkit.getPlayer(getArgs()[0]);
            int bet;

            if(target == null){
                getPlayer().sendMessage(ChatColor.RED + "Joueur introuvable.");
                return;
            }

            try{
                bet = Integer.parseInt(getArgs()[1]);
            }catch(NumberFormatException exception){
                getPlayer().sendMessage(ChatColor.RED + "La mise est incorrect.");
                return;
            }

            if(bet < 100){
                getPlayer().sendMessage(ChatColor.RED + "La mise doit être d'au moins 100€.");
                return;
            }

            if(getPlayer() == target){
                getPlayer().sendMessage(ChatColor.RED + "Tu ne peux pas te duel toi même.");
                return;
            }

            Arena arena = null;
            for(Arena arenaLoop : DuelManager.arenas){
                if(!arenaLoop.isBusy()){
                    arena = arenaLoop;
                    break;
                }
            }

            if(arena == null){
                getPlayer().sendMessage(ChatColor.RED + "Aucune arène n'est libre.");
                return;
            }

            arena.bet = bet;

            getPlayer().sendMessage(ChatColor.GREEN + "Demande envoyée à " + ChatColor.DARK_GREEN + target.getName());
            CorePlayer coreTarget = CoreManagerPlayers.getCorePlayer(target);

            arena.setFirstPlayer(getPlayer());
            arena.setSecondPlayer(target);
            arena.setArenaState(ArenaState.DUEL_ASKED);
            arena.setBusy(true);
            coreTarget.arena = arena;
            getCorePlayer().arena = arena;

            target.sendMessage(ChatColor.RED + "Tu as reçu une demande de duel par " + ChatColor.DARK_RED + getPlayer().getName());
            target.sendMessage(ChatColor.RED + "La mise est de " + ChatColor.DARK_RED + bet + ChatColor.RED + "€");
            target.sendMessage(ChatColor.GREEN + "Fais /duel accept pour accepter et /duel reject pour refuser !");
            target.sendMessage(ChatColor.GRAY + "Sache que tout ton stuff sera perdu si tu meurs. Ta mise également ! En revanche, si tu gagnes, tu gagneras " + (bet*2) + "€, ainsi que le stuff de l'adversaire.");
        }else{
            sendUsage();
        }
    }
}
