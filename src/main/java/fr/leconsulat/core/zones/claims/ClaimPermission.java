package fr.leconsulat.core.zones.claims;

import fr.leconsulat.api.player.Permission;
import fr.leconsulat.core.ConsulatCore;

public enum ClaimPermission implements Permission {
    
    INTERACT_DOOR(ConsulatCore.getInstance().getPermission("claim.interact-door")),
    PLACE_BLOCK(ConsulatCore.getInstance().getPermission("claim.place-block")),
    BREAK_BLOCK(ConsulatCore.getInstance().getPermission("claim.break-block")),
    OPEN_CONTAINER(ConsulatCore.getInstance().getPermission("claim.open-container")),
    INTERACT_REDSTONE(ConsulatCore.getInstance().getPermission("claim.interact-redstone")),
    DAMAGE(ConsulatCore.getInstance().getPermission("claim.damage")),
    OTHER(ConsulatCore.getInstance().getPermission("claim.interact-other")),
    FLY(ConsulatCore.getInstance().getPermission("claim.fly")),
    COLLIDE(ConsulatCore.getInstance().getPermission("claim.collide"));
    
    private String permission;
    
    ClaimPermission(String permission){
        this.permission = permission;
    }
    
    @Override
    public String getPermission(){
        return permission;
    }
}
