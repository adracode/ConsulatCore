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
        super("/duel spectate ou /duel <Joueur> <Mise> ou /duel accept/reject ou /duel annonce on/off", 1, RankEnum.JOUEUR);
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

                if(!arena.getArenaState().equals(ArenaState.DUEL_ASKED)){
                    getPlayer().sendMessage(ChatColor.RED + "Un duel a déjà été accepté.");
                    return;
                }

                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(!DuelManager.removeAnnounces.contains(player)){
                        player.sendMessage("§7[§b§lDuel§r§7] §c"  + arena.getFirstPlayer().getName() + " vs " + arena.getSecondPlayer().getName()  +" ! Mise de chaque participant : §6" +  arena.bet + "€ §c! Pour y être téléporté afin de regarder le duel, /duel spectate");
                    }
                });

                arena.setArenaState(ArenaState.DUEL_ACCEPTED);

                arena.firstBefore = arena.getFirstPlayer().getLocation();
                arena.secondBefore = arena.getSecondPlayer().getLocation();

                arena.getFirstPlayer().teleport(arena.getFirstSpawn());
                arena.getSecondPlayer().teleport(arena.getSecondSpawn());

                arena.getFirstPlayer().sendMessage("§7Le combat commence dans 15 secondes.");
                arena.getSecondPlayer().sendMessage("§7Le combat commence dans 15 secondes.");

                PlayersManager.getConsulatPlayer(arena.getFirstPlayer()).removeMoney((double) arena.bet);
                PlayersManager.getConsulatPlayer(arena.getSecondPlayer()).removeMoney((double) arena.bet);

                getCorePlayer().isFighting = true;
                CoreManagerPlayers.getCorePlayer(arena.getFirstPlayer()).isFighting = true;
                DuelManager.askedDuels.remove(arena.getFirstPlayer().getUniqueId().toString());

                Bukkit.getScheduler().runTaskLater(ConsulatCore.INSTANCE, () -> {

                    if(arena.getArenaState() == ArenaState.DUEL_ACCEPTED) {
                        arena.setArenaState(ArenaState.IN_FIGHT);
                        Bukkit.broadcastMessage("§7[§b§lDuel§r§7] §cQue le duel commence !");
                    }
                }, 20*15);

            }else if(getArgs()[0].equalsIgnoreCase("reject")){
                if(!arena.getArenaState().equals(ArenaState.DUEL_ASKED)){
                    getPlayer().sendMessage(ChatColor.RED + "Un duel a déjà été accepté.");
                    return;
                }

                askDuel.sendMessage("§4" + getPlayer().getName() + "§c a refusé ton duel !");
                getPlayer().sendMessage("§cTu as bien refusé le duel !");

                DuelManager.askedDuels.remove(arena.getFirstPlayer().getUniqueId().toString());

                arena.setFirstPlayer(null);
                arena.setSecondPlayer(null);
                arena.setBusy(false);
                arena.setArenaState(ArenaState.FREE);

            }else sendUsage();
        }else if(getArgs().length == 2){
            if(getArgs()[0].equalsIgnoreCase("annonce")){
                String result = getArgs()[1];
                String uuid = getPlayer().getUniqueId().toString();
                if(result.equalsIgnoreCase("on")){
                    if(DuelManager.removeAnnounces.contains(uuid)){
                        getPlayer().sendMessage("§aLes annonces des duels sont à nouveau activées.");
                        DuelManager.removeAnnounces.remove(uuid);
                    }else{
                        getPlayer().sendMessage("§cLes annonces sont déjà activées !");
                    }
                }else if(result.equalsIgnoreCase("off")){
                    if(DuelManager.removeAnnounces.contains(uuid)){
                        getPlayer().sendMessage("§cLes annonces sont déjà désactivées !");
                        DuelManager.removeAnnounces.remove(uuid);
                    }else{
                        getPlayer().sendMessage("§aLes annonces sont désormais désactivées. !");
                        DuelManager.removeAnnounces.add(uuid);
                    }
                }else sendUsage();

                return;
            }

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

            if(PlayersManager.getConsulatPlayer(getPlayer()).getMoney() < bet){
                getPlayer().sendMessage(ChatColor.RED + "Tu n'as pas assez d'argent !");
                return;
            }

            if(PlayersManager.getConsulatPlayer(target).getMoney() < bet){
                getPlayer().sendMessage(ChatColor.RED + "Le joueur n'a pas assez d'argent !");
                return;
            }

            Arena arena = null;
            for(Arena arenaLoop : DuelManager.arenas){
                if(!arenaLoop.isBusy() && arena == null){
                    arena = arenaLoop;
                }
            }

            if(arena == null){
                getPlayer().sendMessage(ChatColor.RED + "Aucune arène n'est libre.");
                return;
            }

            if(DuelManager.askedDuels.containsKey(getPlayer().getUniqueId().toString())){
                getPlayer().sendMessage(ChatColor.RED + "Tu as déjà fait une demande, tu dois attendre qu'elle expire.");
                return;
            }

            arena.bet = bet;

            getPlayer().sendMessage(ChatColor.GREEN + "Demande envoyée à " + ChatColor.DARK_GREEN + target.getName());
            CorePlayer coreTarget = CoreManagerPlayers.getCorePlayer(target);

            if(coreTarget == null){
                getPlayer().sendMessage(ChatColor.RED + "Erreur avec l'adversaire.");
                return;
            }


            arena.setFirstPlayer(getPlayer());
            arena.setSecondPlayer(target);
            arena.setArenaState(ArenaState.DUEL_ASKED);
            arena.setBusy(true);
            coreTarget.arena = arena;
            getCorePlayer().arena = arena;

            DuelManager.askedDuels.put(getPlayer().getUniqueId().toString(), arena);

            target.sendMessage(ChatColor.RED + "Tu as reçu une demande de duel par " + ChatColor.DARK_RED + getPlayer().getName());
            target.sendMessage(ChatColor.RED + "La mise est de " + ChatColor.DARK_RED + bet + ChatColor.RED + "€");
            target.sendMessage(ChatColor.GREEN + "Fais /duel accept pour accepter et /duel reject pour refuser !");
            target.sendMessage(ChatColor.GRAY + "Sache que tout ton stuff sera perdu si tu meurs. Ta mise également ! En revanche, si tu gagnes, tu gagneras " + (bet*2) + "€, ainsi que le stuff de l'adversaire.");

            Bukkit.getScheduler().runTaskLater(ConsulatCore.INSTANCE, () -> {
                Arena laterArena = DuelManager.askedDuels.get(getPlayer().getUniqueId().toString());
                if(laterArena.getArenaState() == ArenaState.DUEL_ASKED) {
                    laterArena.setArenaState(ArenaState.FREE);
                    laterArena.setBusy(false);
                    laterArena.setFirstPlayer(null);
                    laterArena.setSecondPlayer(null);
                    DuelManager.askedDuels.remove(getPlayer().getUniqueId().toString());
                    getPlayer().sendMessage(ChatColor.GRAY + "La demande de duel n'a pas reçu de réponse.");
                }
            }, 30*20);
        }else{
            sendUsage();
        }
    }
}