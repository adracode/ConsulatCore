package fr.amisoz.consulatcore.duel;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public class Arena {

    private boolean busy;
    private Location firstSpawn;
    private Location secondSpawn;
    private Location spectateLocation;
    private ArenaState arenaState;
    private Player firstPlayer;
    private Player secondPlayer;
    private Player victoryPlayer;
    public int bet;

    public Arena(Location firstSpawn, Location secondSpawn, Location spectateLocation) {
        this.busy = false;
        this.firstSpawn = firstSpawn;
        this.secondSpawn = secondSpawn;
        this.spectateLocation = spectateLocation;
        this.arenaState = ArenaState.FREE;
    }

    public Player getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(Player firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public Player getVictoryPlayer() {
        return victoryPlayer;
    }

    public void setVictoryPlayer(Player victoryPlayer) {
        this.victoryPlayer = victoryPlayer;
    }

    public Player getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(Player secondPlayer) {
        this.secondPlayer = secondPlayer;
    }

    public ArenaState getArenaState() {
        return arenaState;
    }

    public void setArenaState(ArenaState arenaState) {
        this.arenaState = arenaState;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Location getFirstSpawn() {
        return firstSpawn;
    }

    public Location getSecondSpawn() {
        return secondSpawn;
    }

    public Location getSpectateLocation() {
        return spectateLocation;
    }

    public void setSpectateLocation(Location spectateLocation) {
        this.spectateLocation = spectateLocation;
    }
}
