package fr.amisoz.consulatcore.players;

import fr.leconsulat.api.player.Permission;

public enum CityPermission implements Permission {

    MANAGE_PLAYER("consulat.core.city.manage-player"),
    MANAGE_CLAIM("consulat.core.city.manage-claim"),
    MANAGE_ACCESS("consulat.core.city.manage-access"),
    INTERACT("consulat.core.city.interact");
    
    private String permission;
    
    CityPermission(String permission){
        this.permission = permission;
    }
    
    public String getPermission(){
        return permission;
    }
}
