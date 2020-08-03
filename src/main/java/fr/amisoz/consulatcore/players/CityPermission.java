package fr.amisoz.consulatcore.players;

import fr.leconsulat.api.player.Permission;

public enum CityPermission implements Permission {

    MANAGE_PLAYER("consulat.core.city.manage-player"),
    MANAGE_CLAIM("consulat.core.city.manage-claim"),
    MANAGE_ACCESS("consulat.core.city.manage-access"),
    MANAGE_BANK("consulat.core.city.manage-bank"),
    MANAGE_HOME("consulat.core.city.manage-home"),
    INTERACT("consulat.core.city.interact");
    
    private String permission;
    
    CityPermission(String permission){
        this.permission = permission;
    }
    
    public static CityPermission byPermission(String permission){
        for(CityPermission claimPermission : values()){
            if(claimPermission.getPermission().equals(permission)){
                return claimPermission;
            }
        }
        throw new IllegalArgumentException();
    }
    
    public String getPermission(){
        return permission;
    }
}
