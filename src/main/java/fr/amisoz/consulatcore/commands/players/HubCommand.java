package fr.amisoz.consulatcore.commands.players;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class HubCommand extends ConsulatCommand {

    public HubCommand() {
        super("hub", "/hub", 0, Rank.JOUEUR);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby");
        sender.getPlayer().sendPluginMessage(ConsulatCore.getInstance(), "BungeeCord", out.toByteArray());
    }
}
