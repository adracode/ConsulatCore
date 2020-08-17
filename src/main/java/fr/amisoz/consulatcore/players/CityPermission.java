package fr.amisoz.consulatcore.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.player.Permission;

public enum CityPermission implements Permission {
    
    MANAGE_PLAYER(ConsulatCore.getInstance().getPermission("city.manage-player")),
    MANAGE_CLAIM(ConsulatCore.getInstance().getPermission("city.manage-claim")),
    MANAGE_ACCESS(ConsulatCore.getInstance().getPermission("city.manage-access")),
    MANAGE_BANK(ConsulatCore.getInstance().getPermission("city.manage-bank")),
    MANAGE_HOME(ConsulatCore.getInstance().getPermission("city.manage-home")),
    INTERACT(ConsulatCore.getInstance().getPermission("city.interact"));
    
    private String permission;
    
    CityPermission(String permission){
        this.permission = permission;
    }
    
    public String getPermission(){
        return permission;
    }
    
    public static CityPermission byPermission(String permission){
        for(CityPermission claimPermission : values()){
            if(claimPermission.getPermission().equals(permission)){
                return claimPermission;
            }
        }
        throw new IllegalArgumentException();
    }
}
