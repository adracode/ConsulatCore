package fr.amisoz.consulatcore.zones.claims;

import fr.leconsulat.api.player.Permission;

public enum ClaimPermission implements Permission {
    
    INTERACT_DOOR("consulat.core.claim.interact-door"),
    PLACE_BLOCK("consulat.core.claim.place-block"),
    BREAK_BLOCK("consulat.core.claim.break-block"),
    OPEN_CONTAINER("consulat.core.claim.open-container"),
    INTERACT_REDSTONE("consulat.core.claim.interact-redstone"),
    FLY("consulat.core.claim.fly"),
    COLLIDE("consulat.core.claim.collide");
    
    private String permission;
    
    ClaimPermission(String permission){
        this.permission = permission;
    }
    
    @Override
    public String getPermission(){
        return permission;
    }
}
