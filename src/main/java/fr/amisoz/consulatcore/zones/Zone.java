package fr.amisoz.consulatcore.zones;

import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.database.Saveable;
import fr.leconsulat.api.utils.FileUtils;
import fr.leconsulat.api.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Zone implements Saveable {
    
    @NotNull private final UUID uuid;
    @NotNull private final UUID owner;
    @NotNull private String name;
    @NotNull private Set<Claim> claims;
    @NotNull private final Set<String> publicPermissions = new HashSet<>();
    
    public Zone(@NotNull UUID uuid, @Nullable String name, @NotNull UUID owner){
        this(uuid, name == null || name.isEmpty() ? "null" : name, owner, new HashSet<>());
    }
    
    protected Zone(@NotNull UUID uuid, @NotNull String name, @NotNull UUID owner, @NotNull Set<Claim> claims){
        this.uuid = Objects.requireNonNull(uuid);
        this.name = Objects.requireNonNull(name);
        this.owner = Objects.requireNonNull(owner);
        this.claims = Objects.requireNonNull(claims);
        addPermission(owner, ClaimPermission.values());
    }
    
    public boolean isOwner(UUID uuid){
        return owner.equals(uuid);
    }
    
    public boolean isClaim(Claim claim){
        if(claim == null){
            return false;
        }
        return this.equals(claim.getOwner());
    }
    
    public boolean hasClaims(){
        return !claims.isEmpty();
    }
    
    public boolean addPlayer(UUID uuid){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.addPlayer(uuid);
        }
        return result;
    }
    
    public boolean removePlayer(UUID uuid){
        boolean result = false;
        for(Claim claim : claims){
             result |= claim.removePlayer(uuid);
        }
        return result;
    }
    
    public boolean hasPermission(ClaimPermission permission){
        return false;
    }
    
    public boolean addPermission(UUID uuid, ClaimPermission... permission){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.addPermission(uuid, permission);
        }
        return result;
    }
    
    public boolean removePermission(UUID uuid, ClaimPermission... permission){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.removePermission(uuid, permission);
        }
        return result;
    }
    
    public void addClaim(Claim claim){
        claims.add(claim);
    }
    
    public void removeClaim(Claim claim){
        claims.remove(claim);
    }
    
    public UUID getUUID(){
        return uuid;
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof Zone)){
            return false;
        }
        return uuid.equals(((Zone)o).uuid);
    }
    
    @Override
    public int hashCode(){
        return uuid.hashCode();
    }
    
    @NotNull
    public UUID getOwner(){
        return owner;
    }
    
    @NotNull
    protected Set<Claim> getClaims(){
        return claims;
    }
    
    @NotNull
    public Set<Claim> getZoneClaims(){
        return Collections.unmodifiableSet(claims);
    }
    
    public void rename(String name){
        this.name = name;
    }
    
    @NotNull
    public String getName(){
        return name;
    }
    
    public String getType(){
        return "PLAYER";
    }
    
    public void loadNBT(){
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "zones/" + uuid + ".dat");
            if(!file.exists()){
                return;
            }
            NBTInputStream is = new NBTInputStream(new FileInputStream(file));
            Map<String, Tag> zoneMap = ((CompoundTag)is.readTag()).getValue();
            is.close();
            this.name = NBTUtils.getChildTag(zoneMap, "Name", StringTag.class).getValue();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void saveNBT(){
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "zones/" + uuid + ".dat");
            Map<String, Tag> zone = new HashMap<>();
            if(!file.exists()){
                if(!file.createNewFile()){
                    throw new IOException("Couldn't create file.");
                }
            }
            zone.put("name", new StringTag("Name", name));
            NBTOutputStream os = new NBTOutputStream(new FileOutputStream(file));
            os.writeTag(new CompoundTag("City", zone));
            os.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
