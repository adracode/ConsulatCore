package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.duel.Arena;
import fr.amisoz.consulatcore.duel.ArenaState;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.entity.Player;

public class DuelCommand extends ConsulatCommand {

    public DuelCommand() {
        super("/duel <Joueur> <Mise> ou /duel accept/reject", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        if(getArgs().length == 1){
            if(getCorePlayer().arena == null){
                getPlayer().sendMessage("§cTu n'as pas de demande de duel.");
                return;
            }

            Arena arena = getCorePlayer().arena;
            Player askDuel = arena.getFirstPlayer();

            if(getArgs()[0].equalsIgnoreCase("accept")){

            }else if(getArgs()[0].equalsIgnoreCase("reject")){
                askDuel.sendMessage("§4" + getPlayer().getName() + "§c a refusé ton duel !");
                getPlayer().sendMessage("§cTu as bien refusé le duel !");

                arena.setFirstPlayer(null);
                arena.setSecondPlayer(null);
                arena.setBusy(false);
                arena.setArenaState(ArenaState.FREE);
            }else sendUsage();
        }else if(getArgs().length == 2){

        }else{
            sendUsage();
        }
    }
}
