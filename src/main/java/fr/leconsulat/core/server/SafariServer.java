package fr.leconsulat.core.server;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.stream.OfflinePlayerOutputStream;
import fr.leconsulat.api.player.stream.PlayerOutputStream;
import fr.leconsulat.api.redis.RedisManager;
import fr.leconsulat.api.server.Server;
import fr.leconsulat.core.ConsulatCore;
import org.bukkit.plugin.Plugin;
import org.redisson.api.RTopic;

import java.util.Base64;
import java.util.UUID;

public class SafariServer extends Server {
    
    private RTopic loadOnSafari = RedisManager.getInstance().getRedis().getTopic(
            ConsulatAPI.getConsulatAPI().isDevelopment() ? "LoadPlayerDataTestsafari" : "LoadPlayerDataSafari");
    
    public SafariServer(){
        super(ConsulatAPI.getConsulatAPI().isDevelopment() ? "testsafari" : "safari", true);
        RedisManager.getInstance().register(ConsulatAPI.getConsulatAPI().isDevelopment() ? "LoadPlayerDataTestsurvie" : "LoadPlayerDataSurvie",
                String.class, (channel, data) -> CPlayerManager.getInstance().loadPlayerData(Base64.getDecoder().decode(data)));
        RedisManager.getInstance().register(ConsulatAPI.getConsulatAPI().isDevelopment() ? "SavePlayerDataTestsurvie" : "SavePlayerDataSurvie",
                String.class, (channel, data) -> CPlayerManager.getInstance().savePlayerData(Base64.getDecoder().decode(data)));
        RedisManager.getInstance().register("AskPlayerData" + (ConsulatAPI.getConsulatAPI().isDevelopment() ? "Testsurvie" : "Survie"),
                String.class, (channel, uuid) -> loadOnSafari.publishAsync(Base64.getEncoder().encodeToString(
                        new OfflinePlayerOutputStream(UUID.fromString(uuid)).writeLevel().writeInventory().send())));
    }
    
    @Override
    public Plugin getPlugin(){
        return ConsulatCore.getInstance();
    }
    
    @Override
    public void onPlayerConnect(ConsulatPlayer player){
        player.setDisconnectHandled(true);
        player.setInventoryBlocked(true);
        loadOnSafari.publishAsync(Base64.getEncoder().encodeToString(new PlayerOutputStream(player.getPlayer()).writeLevel().writeInventory().send()));
    }
    
    @Override
    public void onConnectionRefused(ConsulatPlayer player){
        player.setDisconnectHandled(false);
        player.setInventoryBlocked(false);
    }
}
