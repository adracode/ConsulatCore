package fr.amisoz.consulatcore.zones;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.database.Saveable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Zone implements Saveable {
    
    @NotNull private final UUID uuid;
    @NotNull private UUID owner;
    @NotNull private final Set<Claim> claims;
    @NotNull private String name;
    
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
    
    public boolean addPlayer(@NotNull UUID uuid){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.addPlayer(uuid);
        }
        return result;
    }
    
    public boolean removePlayer(@NotNull UUID uuid){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.removePlayer(uuid);
        }
        return result;
    }
    
    public boolean hasPublicPermission(@Nullable ClaimPermission permission){
        return false;
    }
    
    public boolean addPermission(@NotNull UUID uuid, @NotNull ClaimPermission... permission){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.addPermission(uuid, permission);
        }
        return result;
    }
    
    public boolean removePermission(@NotNull UUID uuid, @NotNull ClaimPermission... permission){
        boolean result = false;
        for(Claim claim : claims){
            result |= claim.removePermission(uuid, permission);
        }
        return result;
    }
    
    public boolean isClaim(@Nullable Claim claim){
        if(claim == null){
            return false;
        }
        return this.equals(claim.getOwner());
    }
    
    public boolean hasClaims(){
        return !claims.isEmpty();
    }
    
    public void addClaim(@NotNull Claim claim){
        if(!this.equals(claim.getOwner())){
            throw new IllegalArgumentException("Cannot add claim to zone " + name + ", claim is owned by " + claim.getOwner().getName());
        }
        claims.add(claim);
    }
    
    public void removeClaim(@Nullable Claim claim){
        if(claim == null){
            return;
        }
        claims.remove(claim);
    }
    
    public void rename(@NotNull String name){
        if(name.isEmpty()){
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }
    
    public @NotNull UUID getUniqueId(){
        return uuid;
    }
    
    public @NotNull UUID getOwner(){
        return owner;
    }
    
    public void setOwner(@NotNull UUID owner){
        this.owner = owner;
    }
    
    public boolean isOwner(@Nullable UUID uuid){
        return owner.equals(uuid);
    }
    
    protected @NotNull Set<Claim> getClaims(){
        return claims;
    }
    
    public @NotNull Set<Claim> getZoneClaims(){
        return Collections.unmodifiableSet(claims);
    }
    
    public @NotNull String getName(){
        return name;
    }
    
    public @NotNull String getType(){
        return "PLAYER";
    }
    
    public @NotNull String getEnterMessage(){
        return Text.PREFIX + "§cTu entres dans la zone de §l" + getName() + ".";
    }
    
    public @NotNull String getLeaveMessage(){
        return Text.PREFIX + "§cTu sors de la zone de §l" + getName() + ".";
    }
    
    public void loadNBT(){
        /*try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "zones/" + uuid + ".dat");
            if(!file.exists()){
                return;
            }
            NBTInputStream is = new NBTInputStream(new FileInputStream(file));
            Map<String, Tag> zoneMap = ((CompoundTag)is.readTag()).getValue();
            is.close();
        } catch(IOException e){
            e.printStackTrace();
        }*/
    }
    
    public void saveNBT(){
        /*try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "zones/" + uuid + ".dat");
            Map<String, Tag> zone = new HashMap<>();
            if(!file.exists()){
                if(!file.createNewFile()){
                    throw new IOException("Couldn't create file.");
                }
            }
            NBTOutputStream os = new NBTOutputStream(new FileOutputStream(file));
            os.writeTag(new CompoundTag("City", zone));
            os.close();
        } catch(IOException e){
            e.printStackTrace();
        }*/
    }
    
    @Override
    public boolean equals(@Nullable Object o){
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
    
    @Override
    public String toString(){
        return getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)) +
                '{' +
                "uuid=" + uuid +
                ", owner=" + owner +
                ", claims=" + claims +
                ", name='" + name + '\'' +
                '}';
    }
}
