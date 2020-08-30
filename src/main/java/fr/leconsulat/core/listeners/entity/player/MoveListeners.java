package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.events.ClaimChangeEvent;
import fr.leconsulat.core.guis.city.CityGui;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MoveListeners implements Listener {
    
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isFrozen()){
            event.setCancelled(true);
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch()){
            survivalPlayer.setLastMove(System.currentTimeMillis());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event){
        if(event.getTo().getChunk() == event.getFrom().getChunk()){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player == null){
            return;
        }
        Location to = event.getTo();
        Location from = event.getFrom();
        if(to.getWorld().equals(ConsulatCore.getInstance().getOverworld()) && from.getWorld().equals(Bukkit.getWorlds().get(1))){
            player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 10));
        }
        player.setOldLocation(event.getFrom());
        player.setLastTeleport(System.currentTimeMillis());
        Chunk chunkTo = to.getChunk();
        Chunk chunkFrom = from.getChunk();
        Bukkit.getPluginManager().callEvent(new ClaimChangeEvent(player,
                ClaimManager.getInstance().getClaim(chunkFrom),
                ClaimManager.getInstance().getClaim(chunkTo)));
    }
    
    @EventHandler
    public void onClaimChange(ClaimChangeEvent event){
        if(event.getPlayer().getCurrentlyOpen() instanceof CityGui){
            SurvivalPlayer player = event.getPlayer();
            CityGui cityGui = (CityGui)player.getCurrentlyOpen();
            Claim claim = event.getClaimTo();
            cityGui.updateHome(player, claim != null && cityGui.getData().isClaim(claim));
        }
    }
}
