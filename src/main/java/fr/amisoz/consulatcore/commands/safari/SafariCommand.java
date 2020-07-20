package fr.amisoz.consulatcore.commands.safari;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.stream.PlayerOutputStream;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.redis.RedisManager;

public class SafariCommand extends ConsulatCommand {
    
    public SafariCommand(){
        super("safari", "/safari", 0, Rank.RESPONSABLE);
        setPermission("consulat.core.command.safari");
        suggest(false);
    }
    
    @Override
    public void onCommand(ConsulatPlayer player, String[] args){
        RedisManager.getInstance().getRedis().getTopic(ConsulatAPI.getConsulatAPI().isDevelopment() ? "LoadPlayerDataTestSafari" : "LoadPlayerDataSafari").publish(
                new PlayerOutputStream(player.getPlayer()).writeLevel().writeInventory().send());
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(ConsulatAPI.getConsulatAPI().isDevelopment() ? "testsafari" : "safari");
        player.getPlayer().sendPluginMessage(ConsulatCore.getInstance(), "BungeeCord", out.toByteArray());
    }
}
