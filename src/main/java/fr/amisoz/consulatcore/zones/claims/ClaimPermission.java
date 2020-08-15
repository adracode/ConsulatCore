package fr.amisoz.consulatcore.zones.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.player.Permission;

public enum ClaimPermission implements Permission {
    
    INTERACT_DOOR(ConsulatCore.getInstance().getPermission("claim.interact-door")),
    PLACE_BLOCK(ConsulatCore.getInstance().getPermission("claim.place-block")),
    BREAK_BLOCK(ConsulatCore.getInstance().getPermission("claim.break-block")),
    OPEN_CONTAINER(ConsulatCore.getInstance().getPermission("claim.open-container")),
    INTERACT_REDSTONE(ConsulatCore.getInstance().getPermission("claim.interact-redstone")),
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
