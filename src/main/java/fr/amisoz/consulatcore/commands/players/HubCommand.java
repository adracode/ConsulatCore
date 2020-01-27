package fr.amisoz.consulatcore.commands.players;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

public class HubCommand extends ConsulatCommand {

    public HubCommand() {
        super("/hub", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby");
        getPlayer().sendPluginMessage(ConsulatCore.INSTANCE, "BungeeCord", out.toByteArray());
    }
}
