package fr.amisoz.consulatcore.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.duel.Arena;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.utils.CustomEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Calendar;
import java.util.HashMap;

public class CorePlayer {

    public Location oldLocation = ConsulatCore.spawnLocation;
    private String sanctionTarget = null;
    private boolean isModerate = false;
    public ItemStack[] stockedInventory;

    public boolean isMuted;
    public Long muteExpireMillis;
    public String muteReason;

    public Long advertDelay = 0L;

    public Long lastMove = 0L;
    public Long lastTeleport = 0L;

    public boolean isFreezed;

    public Player lastPrivate;

    public boolean seeInv;

    public CustomEnum persoState = CustomEnum.START;
    public String persoNick = "";

    public boolean canFly = false;
    public long flyTime = 0;
    public long lastTime = 0;
    public long timeLeft = 0;

    public Arena arena;
    public boolean isFighting;

    public boolean canUp = false;
    public boolean isSpy = false;

    public HashMap<String, Location> homes = new HashMap<>();

    public String getSanctionTarget() {
        return sanctionTarget;
    }

    public void setSanctionTarget(String sanctionTarget) {
        this.sanctionTarget = sanctionTarget;
    }

    public boolean isModerate() {
        return isModerate;
    }

    public void setModerate(boolean moderate) {
        isModerate = moderate;
    }

    public MuteObject getMute(){
        if(System.currentTimeMillis() < muteExpireMillis) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(muteExpireMillis);
            String resultDate = ConsulatCore.DATE_FORMAT.format(calendar.getTime());
            String reason = muteReason;
            return new MuteObject(reason, resultDate);
        }
        return null;
    }
}
