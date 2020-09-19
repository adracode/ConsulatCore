package fr.leconsulat.core.listeners.entity.player;

import fr.leconsulat.api.events.ConsulatPlayerLeaveEvent;
import fr.leconsulat.api.events.ConsulatPlayerLoadedEvent;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.events.ClaimChangeEvent;
import fr.leconsulat.core.guis.city.CityGui;
import fr.leconsulat.core.guis.moderation.XRayHelperGui;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MovementChecker implements Listener {
    
    private static MovementChecker instance;
    
    private XRayHelperGui gui = new XRayHelperGui();
    
    public MovementChecker(){
        if(instance != null){
            throw new IllegalStateException();
        }
        instance = this;
    }
    
    private void removeBelow16(ConsulatPlayer player){
        gui.removePlayer(player);
    }
    
    private void addBelow16(ConsulatPlayer player){
        gui.addPlayer(player);
    }
    
    @EventHandler
    public void onJoin(ConsulatPlayerLoadedEvent event){
        if(event.getPlayer().getPlayer().getLocation().getY() <= 16){
            addBelow16(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onQuit(ConsulatPlayerLeaveEvent event){
        removeBelow16(event.getPlayer());
    }
    
    private void checkY(double yFrom, double yTo, ConsulatPlayer player){
        if(yFrom <= 16){
            if(yTo > 16){
                removeBelow16(player);
            }
        } else {
            if(yTo <= 16){
                addBelow16(player);
            }
        }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player.isFrozen()){
            event.setCancelled(true);
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch()){
            player.setLastMove(System.currentTimeMillis());
        }
        checkY(event.getFrom().getY(), event.getTo().getY(), player);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player == null){
            return;
        }
        Location to = event.getTo();
        Location from = event.getFrom();
        checkY(from.getY(), to.getY(), player);
        if(to.getChunk() == from.getChunk()){
            return;
        }
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
    
    public XRayHelperGui getGui(){
        return gui;
    }
    
    public static MovementChecker getInstance(){
        return instance;
    }
}
