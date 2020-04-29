package fr.amisoz.consulatcore.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;

public class DuelCommand extends ConsulatCommand {

    public DuelCommand() {
        super("duel", "/duel spectate ou /duel <Joueur> <Mise> ou /duel accept/reject ou /duel annonce on/off", 0, Rank.JOUEUR);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage("§7[§b§lDuel§r§7] §cLes duels sont actuellement en cours de maintenance.");
        return;

        /*if(args.length == 1){

            if(args[0].equalsIgnoreCase("spectate")){
                Arena arena = null;
                for(Arena arenaLoop : DuelManager.arenas){
                    if(arenaLoop.isBusy()){
                        arena = arenaLoop;
                        break;
                    }
                }

                if(arena == null){
                    sender.sendMessage(ChatColor.RED + "Aucun combat n'est en cours.");
                }else{
                    sender.teleport(arena.getSpectateLocation());
                }
                return;
            }

            if(getCorePlayer().arena == null){
                sender.sendMessage("§cTu n'as pas de demande de duel.");
                return;
            }

            Arena arena = getCorePlayer().arena;
            Player askDuel = arena.getFirstPlayer();

            if(args[0].equalsIgnoreCase("accept")){

                if(!arena.getArenaState().equals(ArenaState.DUEL_ASKED)){
                    sender.sendMessage(ChatColor.RED + "Un duel a déjà été accepté.");
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

            }else if(args[0].equalsIgnoreCase("reject")){
                if(!arena.getArenaState().equals(ArenaState.DUEL_ASKED)){
                    sender.sendMessage(ChatColor.RED + "Un duel a déjà été accepté.");
                    return;
                }

                askDuel.sendMessage("§4" + sender.getName() + "§c a refusé ton duel !");
                sender.sendMessage("§cTu as bien refusé le duel !");

                DuelManager.askedDuels.remove(arena.getFirstPlayer().getUniqueId().toString());

                arena.setFirstPlayer(null);
                arena.setSecondPlayer(null);
                arena.setBusy(false);
                arena.setArenaState(ArenaState.FREE);

            }else sendUsage();
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("annonce")){
                String result = args[1];
                String uuid = sender.getUniqueId().toString();
                if(result.equalsIgnoreCase("on")){
                    if(DuelManager.removeAnnounces.contains(uuid)){
                        sender.sendMessage("§aLes annonces des duels sont à nouveau activées.");
                        DuelManager.removeAnnounces.remove(uuid);
                    }else{
                        sender.sendMessage("§cLes annonces sont déjà activées !");
                    }
                }else if(result.equalsIgnoreCase("off")){
                    if(DuelManager.removeAnnounces.contains(uuid)){
                        sender.sendMessage("§cLes annonces sont déjà désactivées !");
                        DuelManager.removeAnnounces.remove(uuid);
                    }else{
                        sender.sendMessage("§aLes annonces sont désormais désactivées. !");
                        DuelManager.removeAnnounces.add(uuid);
                    }
                }else sendUsage();

                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            int bet;

            if(target == null){
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return;
            }

            try{
                bet = Integer.parseInt(args[1]);
            }catch(NumberFormatException exception){
                sender.sendMessage(ChatColor.RED + "La mise est incorrect.");
                return;
            }

            if(bet < 100){
                sender.sendMessage(ChatColor.RED + "La mise doit être d'au moins 100€.");
                return;
            }

            if(sender == target){
                sender.sendMessage(ChatColor.RED + "Tu ne peux pas te duel toi même.");
                return;
            }

            if(PlayersManager.getConsulatPlayer(sender).getMoney() < bet){
                sender.sendMessage(ChatColor.RED + "Tu n'as pas assez d'argent !");
                return;
            }

            if(PlayersManager.getConsulatPlayer(target).getMoney() < bet){
                sender.sendMessage(ChatColor.RED + "Le joueur n'a pas assez d'argent !");
                return;
            }

            Arena arena = null;
            for(Arena arenaLoop : DuelManager.arenas){
                if(!arenaLoop.isBusy() && arena == null){
                    arena = arenaLoop;
                }
            }

            if(arena == null){
                sender.sendMessage(ChatColor.RED + "Aucune arène n'est libre.");
                return;
            }

            if(DuelManager.askedDuels.containsKey(sender.getUniqueId().toString())){
                sender.sendMessage(ChatColor.RED + "Tu as déjà fait une demande, tu dois attendre qu'elle expire.");
                return;
            }

            arena.bet = bet;

            sender.sendMessage(ChatColor.GREEN + "Demande envoyée à " + ChatColor.DARK_GREEN + target.getName());
            CorePlayer coreTarget = CoreManagerPlayers.getCorePlayer(target);

            if(coreTarget == null){
                sender.sendMessage(ChatColor.RED + "Erreur avec l'adversaire.");
                return;
            }


            arena.setFirstPlayer(sender);
            arena.setSecondPlayer(target);
            arena.setArenaState(ArenaState.DUEL_ASKED);
            arena.setBusy(true);
            coreTarget.arena = arena;
            getCorePlayer().arena = arena;

            DuelManager.askedDuels.put(sender.getUniqueId().toString(), arena);

            target.sendMessage(ChatColor.RED + "Tu as reçu une demande de duel par " + ChatColor.DARK_RED + sender.getName());
            target.sendMessage(ChatColor.RED + "La mise est de " + ChatColor.DARK_RED + bet + ChatColor.RED + "€");
            target.sendMessage(ChatColor.GREEN + "Fais /duel accept pour accepter et /duel reject pour refuser !");
            target.sendMessage(ChatColor.GRAY + "Sache que tout ton stuff sera perdu si tu meurs. Ta mise également ! En revanche, si tu gagnes, tu gagneras " + (bet*2) + "€, ainsi que le stuff de l'adversaire.");

            Bukkit.getScheduler().runTaskLater(ConsulatCore.INSTANCE, () -> {
                Arena laterArena = DuelManager.askedDuels.get(sender.getUniqueId().toString());
                if(laterArena.getArenaState() == ArenaState.DUEL_ASKED) {
                    laterArena.setArenaState(ArenaState.FREE);
                    laterArena.setBusy(false);
                    laterArena.setFirstPlayer(null);
                    laterArena.setSecondPlayer(null);
                    DuelManager.askedDuels.remove(sender.getUniqueId().toString());
                    sender.sendMessage(ChatColor.GRAY + "La demande de duel n'a pas reçu de réponse.");
                }
            }, 30*20);
        }else{
            sendUsage();
        }*/
    }
}
