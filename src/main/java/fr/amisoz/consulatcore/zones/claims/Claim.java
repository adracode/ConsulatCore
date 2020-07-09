package fr.amisoz.consulatcore.zones.claims;

import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.members.member.MemberGui;
import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaimGui;
import fr.amisoz.consulatcore.guis.claims.ManageClaimGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.nbt.CompoundTag;
import fr.leconsulat.api.nbt.ListTag;
import fr.leconsulat.api.nbt.NBTType;
import fr.leconsulat.api.nbt.StringTag;
import fr.leconsulat.api.player.Permission;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

public class Claim implements Comparable<Claim> {
    
    public static final double BUY_CLAIM = 180;
    public static final double BUY_CITY_CLAIM = BUY_CLAIM;
    public static final double REFUND = BUY_CLAIM * 0.7;
    
    private static final int SHIFT = 25 - 4; //Max coordonnées MC - Taille du chunk
    private static final int LIMIT_X = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int LIMIT_Z = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int CONVERT = (1 << SHIFT + 1) - 1; //1111111111111111111111
    
    private long coords;
    private String description;
    private Zone owner;
    private Map<UUID, Set<String>> permissions = new HashMap<>();
    
    Claim(int x, int z, Zone owner, String description){
        setOwner(owner);
        setCoords(x, z);
        this.description = description;
    }
    
    private void setCoords(int x, int z){
        if(x < -LIMIT_X || x > LIMIT_X || z < -LIMIT_Z || z > LIMIT_Z){
            throw new IllegalArgumentException("Les coordonnées d'un chunk ne peuvent dépasse les limites");
        }
        coords = Claim.convert(x, z);
    }
    
    public int getX(){
        return (int)((coords & CONVERT) - LIMIT_X);
    }
    
    public int getZ(){
        return (int)((coords >> SHIFT + 1) - LIMIT_Z);
    }
    
    public Zone getOwner(){
        return owner;
    }
    
    public UUID getOwnerUUID(){
        return owner.getOwner();
    }
    
    public boolean isOwner(UUID uuid){
        return owner.isOwner(uuid);
    }
    
    public boolean isOwner(Zone zone){
        return owner.equals(zone);
    }
    
    public String getDescription(){
        return description;
    }
    
    public long getCoordinates(){
        return coords;
    }
    
    public boolean addPlayer(UUID uuid){
        boolean added = this.permissions.put(uuid, new HashSet<>()) == null;
        if(added){
            addPermission(uuid, ClaimPermission.values());
            if(owner instanceof City){
                IGui access = GuiManager.getInstance().getContainer("city").getGui(false, owner, CityGui.MEMBERS, uuid, MemberGui.CLAIM);
                if(access != null){
                    ((AccessibleClaimGui)access).addItemClaim(this);
                }
            }
            IGui manageClaim = GuiManager.getInstance().getContainer("claim").getGui(false, this);
            if(manageClaim != null){
                ((ManageClaimGui)manageClaim).addPlayerToClaim(uuid, Bukkit.getOfflinePlayer(uuid).getName());
            }
        }
        return added;
    }
    
    public boolean removePlayer(UUID uuid){
        boolean removed = this.permissions.remove(uuid) != null;
        if(removed){
            if(owner instanceof City){
                IGui access = GuiManager.getInstance().getContainer("city").getGui(false, owner, CityGui.MEMBERS, uuid, MemberGui.CLAIM);
                if(access != null){
                    ((AccessibleClaimGui)access).removeItemClaim(this);
                }
            }
            IGui manageClaim = GuiManager.getInstance().getContainer("claim").getGui(false, this);
            if(manageClaim != null){
                ((ManageClaimGui)manageClaim).removePlayerFromClaim(uuid);
            }
        }
        return removed;
    }
    
    public boolean canInteractDispenser(SurvivalPlayer player){
        return canInteract(player);
    }
    
    public boolean canInteract(SurvivalPlayer player){
        return canInteract(player, null);
    }
    
    public boolean canInteract(SurvivalPlayer player, ClaimPermission permission){
        if(player == null || !player.isInitialized()){
            return false;
        }
        if(player.hasPermission("consulat.core.interact")){
            return true;
        }
        return player.hasPower(Rank.MODPLUS) ||
                owner.hasPublicPermission(permission) ||
                canInteract(player.getUUID(), permission);
    }
    
    public boolean canInteract(UUID uuid, Permission permission){
        return isOwner(uuid) || hasPermission(uuid, permission);
    }
    
    public boolean addPermission(UUID uuid, Permission... permissions){
        Set<String> perms = this.permissions.get(uuid);
        if(perms == null){
            return false;
        }
        if(permissions.length == 0){
            return true;
        } else if(permissions.length == 1){
            return perms.add(permissions[0].getPermission());
        }
        return perms.addAll(Arrays.asList(Permission.toStringArray(permissions)));
    }
    
    public boolean removePermission(UUID uuid, Permission... permissions){
        if(permissions.length == 0){
            return true;
        }
        Set<String> perms = this.permissions.get(uuid);
        if(perms == null){
            return false;
        }
        if(permissions.length == 1){
            return perms.remove(permissions[0].getPermission());
        }
        return perms.removeAll(Arrays.asList(Permission.toStringArray(permissions)));
    }
    
    public boolean hasPermission(UUID uuid, Permission permission){
        if(permission == null){
            return false;
        }
        return hasPermission(uuid, permission.getPermission());
    }
    
    private boolean hasPermission(UUID uuid, String permission){
        Set<String> permissions = this.permissions.get(uuid);
        if(permissions == null){
            return false;
        }
        return permissions.contains(permission);
    }
    
    public void setDescription(String description) throws SQLException{
        ClaimManager.getInstance().setDescriptionDatabase(this, description);
        this.description = description;
    }
    
    public String getOwnerName(){
        return owner.getName();
    }
    
    public static Claim[] getSurroundingClaims(int x, int z){
        ClaimManager claimManager = ClaimManager.getInstance();
        return new Claim[]{
                claimManager.getClaim(x, z - 1),
                claimManager.getClaim(x, z + 1),
                claimManager.getClaim(x - 1, z),
                claimManager.getClaim(x + 1, z)
        };
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim)o;
        return coords == claim.coords;
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    public static long convert(int x, int z){
        return (((long)z + LIMIT_Z) << SHIFT + 1) | (x + LIMIT_X);
    }
    
    public boolean canFertilize(SurvivalPlayer player){
        return canInteract(player);
    }
    
    public boolean canIgnite(SurvivalPlayer player){
        return canInteract(player);
    }
    
    public boolean canUseCauldron(SurvivalPlayer player){
        return canInteract(player);
    }
    
    public boolean canFormBlock(SurvivalPlayer player){
        return canInteract(player);
    }
    
    public boolean canReceivePotion(SurvivalPlayer player){
        return canInteract(player);
    }
    
    void setOwner(Zone owner){
        this.owner = owner;
        permissions.clear();
        IGui iClaimGui = GuiManager.getInstance().getContainer("claim").getGui(false, this);
        if(iClaimGui != null){
            ((ManageClaimGui)iClaimGui).applyFather();
        }
    }
    
    private final Map<Long, UUID> protectedContainers = new HashMap<>();
    
    public Map<Long, UUID> getProtectedContainers(){
        return Collections.unmodifiableMap(protectedContainers);
    }
    
    public boolean protectContainer(Block block, UUID uuid){
        return protectContainer(CoordinatesUtils.convertCoordinates(block.getLocation()), uuid);
    }
    
    public boolean protectContainer(long coords, UUID uuid){
        return protectedContainers.putIfAbsent(coords, uuid) == null;
    }
    
    public void freeContainer(Block block){
        protectedContainers.remove(CoordinatesUtils.convertCoordinates(block.getLocation()));
    }
    
    public UUID getProtectedContainer(long coords){
        return protectedContainers.get(coords);
    }
    
    protected CompoundTag saveMember(UUID uuid){
        CompoundTag member = new CompoundTag();
        ListTag<StringTag> permissions = new ListTag<>(NBTType.STRING);
        for(String permission : this.permissions.get(uuid)){
            permissions.addTag(new StringTag(permission));
        }
        member.putUUID("UUID", uuid);
        member.put("Permissions", permissions);
        return member;
    }
    
    public void loadNBT(CompoundTag claim){
        if(claim.has("Description")){
            this.description = claim.getString("Description");
        }
        List<CompoundTag> members = claim.getList("Members", CompoundTag.class);
        for(CompoundTag member : members){
            Set<String> permissions = new HashSet<>();
            for(StringTag permission : member.getList("Permissions", StringTag.class)){
                permissions.add(permission.getValue());
            }
            this.permissions.put(member.getUUID("UUID"), permissions);
        }
        if(claim.has("ProtectedContainers")){
            List<CompoundTag> protectedContainers = claim.getList("ProtectedContainers", CompoundTag.class);
            for(CompoundTag protectedContainer : protectedContainers){
                this.protectedContainers.put(
                        protectedContainer.getLong("Coords"),
                        protectedContainer.getUUID("Owner"));
            }
        }
    }
    
    public CompoundTag saveNBT(){
        CompoundTag claim = new CompoundTag();
        claim.putLong("Coords", coords);
        if(description != null){
            claim.putString("Description", description);
        }
        ListTag<CompoundTag> members = new ListTag<>(NBTType.COMPOUND);
        for(Map.Entry<UUID, Set<String>> member : this.permissions.entrySet()){
            members.addTag(saveMember(member.getKey()));
        }
        claim.put("Members", members);
        ListTag<CompoundTag> containersTag = new ListTag<>(NBTType.COMPOUND);
        for(Map.Entry<Long, UUID> container : protectedContainers.entrySet()){
            CompoundTag containerTag = new CompoundTag();
            containerTag.putLong("Coords", container.getKey());
            containerTag.putUUID("Owner", container.getValue());
            containersTag.addTag(containerTag);
        }
        claim.put("Containers", containersTag);
        return claim;
    }
    
    @Override
    public int compareTo(@NotNull Claim o){
        return Long.compare(coords, o.coords);
    }
    
    public Set<UUID> getPlayers(){
        return Collections.unmodifiableSet(permissions.keySet());
    }
    
    @Override
    public String toString(){
        return "Claim{" +
                "coords=" + coords +
                ", description='" + description + '\'' +
                ", owner=" + owner.getName() +
                ", permissions=" + permissions +
                ", protectedContainers=" + protectedContainers +
                '}';
    }
    
    public boolean hasAccess(UUID uuid){
        return permissions.containsKey(uuid);
    }
    
    public boolean canManageAccesses(UUID uuid){
        return isOwner(uuid) || owner instanceof City && ((City)owner).canManageAccesses(uuid);
    }
    
}
