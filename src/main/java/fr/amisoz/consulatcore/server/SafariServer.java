package fr.amisoz.consulatcore.server;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.stream.PlayerOutputStream;
import fr.leconsulat.api.redis.RedisManager;
import fr.leconsulat.api.server.Server;
import org.redisson.api.RTopic;

public class SafariServer extends Server {
    
    private RTopic loadOnSafari = RedisManager.getInstance().getRedis().getTopic(
            ConsulatAPI.getConsulatAPI().isDevelopment() ? "LoadPlayerDataTestSafari" : "LoadPlayerDataSafari");
    
    public SafariServer(){
        super(ConsulatAPI.getConsulatAPI().isDevelopment() ? "testsafari" : "safari", true);
        RedisManager.getInstance().register(ConsulatAPI.getConsulatAPI().isDevelopment() ? "LoadPlayerDataTestSurvie" : "LoadPlayerDataSurvie",
                byte[].class, (channel, data) -> CPlayerManager.getInstance().loadPlayerData(data));
        RedisManager.getInstance().register(ConsulatAPI.getConsulatAPI().isDevelopment() ? "SavePlayerDataTestSurvie" : "SavePlayerDataSurvie",
                byte[].class, (channel, data) -> CPlayerManager.getInstance().savePlayerData(data));
    }
    
    @Override
    public void onPlayerConnect(ConsulatPlayer player){
        player.setDisconnectHandled(true);
        loadOnSafari.publishAsync(new PlayerOutputStream(player.getPlayer()).writeLevel().writeInventory().send());
    }
}
