package fr.leconsulat.core.zones.claims;

import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.nbt.CompoundTag;
import fr.leconsulat.api.nbt.ListTag;
import fr.leconsulat.api.nbt.NBTType;
import fr.leconsulat.api.nbt.StringTag;
import fr.leconsulat.api.player.Permission;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.chunks.CChunk;
import fr.leconsulat.core.guis.city.CityGui;
import fr.leconsulat.core.guis.city.members.member.MemberGui;
import fr.leconsulat.core.guis.city.members.member.claims.AccessibleClaimGui;
import fr.leconsulat.core.guis.claims.ManageClaimGui;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.utils.CoordinatesUtils;
import fr.leconsulat.core.zones.Zone;
import fr.leconsulat.core.zones.cities.City;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Claim extends CChunk {
    
    public static final String TYPE = "CLAIM";
    
    public static final double BUY_CLAIM = 180;
    public static final double BUY_CITY_CLAIM = BUY_CLAIM;
    public static final double REFUND = BUY_CLAIM * 0.7;
    public static final double SURCLAIM = BUY_CITY_CLAIM - REFUND;
    private final Long2ObjectMap<UUID> protectedContainers = new Long2ObjectOpenHashMap<>();
    private String description;
    private Zone owner;
    private boolean interactSurrounding = false;
    private Map<UUID, Set<String>> permissions = new HashMap<>();
    
    public Claim(long coords){
        super(coords);
    }
    
    Claim(int x, int z, Zone owner, String description){
        super(x, z);
        setOwner(owner);
        this.description = description;
    }
    
    @Override
    public void loadNBT(CompoundTag claim){
        super.loadNBT(claim);
        if(claim.has("Description")){
            this.description = claim.getString("Description");
        }
        List<CompoundTag> members = claim.getList("Members", NBTType.COMPOUND);
        for(CompoundTag member : members){
            Set<String> permissions = new HashSet<>();
            List<StringTag> tagList = member.getList("Permissions", NBTType.STRING);
            for(StringTag permission : tagList){
                permissions.add(permission.getValue());
            }
            this.permissions.put(member.getUUID("UUID"), permissions);
        }
        if(claim.has("ProtectedContainers")){
            List<CompoundTag> protectedContainers = claim.getList("ProtectedContainers", NBTType.COMPOUND);
            for(CompoundTag protectedContainer : protectedContainers){
                this.protectedContainers.put(
                        protectedContainer.getLong("Coords"),
                        protectedContainer.getUUID("Owner"));
            }
        }
        this.interactSurrounding = claim.getByte("InteractSurrounding") == 1;
    }
    
    @Override
    public CompoundTag saveNBT(){
        CompoundTag claim = super.saveNBT();
        if(description != null){
            claim.putString("Description", description);
        }
        ListTag<CompoundTag> members = new ListTag<>(NBTType.COMPOUND);
        for(Map.Entry<UUID, Set<String>> member : this.permissions.entrySet()){
            members.addTag(saveMember(member.getKey()));
        }
        claim.put("Members", members);
        ListTag<CompoundTag> containersTag = new ListTag<>(NBTType.COMPOUND);
        for(Long2ObjectMap.Entry<UUID> container : protectedContainers.long2ObjectEntrySet()){
            CompoundTag containerTag = new CompoundTag();
            containerTag.putLong("Coords", container.getLongKey());
            containerTag.putUUID("Owner", container.getValue());
            containersTag.addTag(containerTag);
        }
        claim.put("ProtectedContainers", containersTag);
        claim.putByte("InteractSurrounding", (byte)(interactSurrounding ? 1 : 0));
        return claim;
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
    
    @Override
    public String toString(){
        return super.toString() +
                ", Claim{" +
                ", description='" + description + '\'' +
                ", owner=" + (owner == null ? "null" : owner.getName()) +
                ", permissions=" + permissions +
                ", protectedContainers=" + protectedContainers +
                '}';
    }
    
    public boolean isOwner(UUID uuid){
        return owner.isOwner(uuid);
    }
    
    public boolean isOwner(Zone zone){
        return owner.equals(zone);
    }
    
    public boolean addPlayer(UUID uuid){
        if(this.permissions.containsKey(uuid)){
            return false;
        }
        this.permissions.put(uuid, new HashSet<>());
        addPermission(uuid, ClaimPermission.values());
        if(owner instanceof City){
            IGui access = GuiManager.getInstance().getContainer("city").getGui(false, owner, CityGui.MEMBERS, uuid, MemberGui.CLAIM);
            if(access != null){
                ((AccessibleClaimGui)access).addItemClaim(this);
            }
        }
        IGui manageClaim = GuiManager.getInstance().getContainer("claim").getGui(false, this);
        if(manageClaim != null){
            ((ManageClaimGui)manageClaim).addPlayerToClaim(uuid);
        }
        return true;
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
    
    public boolean canInteractOther(SurvivalPlayer player){
        return canInteract(player, ClaimPermission.OTHER);
    }
    
    public boolean canDamage(SurvivalPlayer player){
        return canInteract(player, ClaimPermission.DAMAGE);
    }
    
    public boolean canInteract(SurvivalPlayer player){
        return canInteract(player, null);
    }
    
    public boolean canPlace(SurvivalPlayer player){
        return canInteract(player, ClaimPermission.PLACE_BLOCK);
    }
    
    public boolean canInteract(SurvivalPlayer player, ClaimPermission permission){
        if(player == null || !player.isInitialized()){
            return false;
        }
        if(player.hasPermission(ConsulatCore.getInstance().getPermission("interact"))){
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
    
    public boolean protectContainer(long coords, UUID uuid){
        return protectedContainers.putIfAbsent(coords, uuid) == null;
    }
    
    public void freeContainer(Block block){
        protectedContainers.remove(CoordinatesUtils.convertCoordinates(block.getLocation()));
    }
    
    public UUID getProtectedContainer(long coords){
        return protectedContainers.get(coords);
    }
    
    public boolean hasAccess(UUID uuid){
        return isOwner(uuid) || permissions.containsKey(uuid);
    }
    
    public boolean canManageAccesses(UUID uuid){
        return isOwner(uuid) || owner instanceof City && ((City)owner).canManageAccesses(uuid);
    }
    
    public boolean canInteractWith(Claim to){
        /*Si le claim où arrive l'interaction est null,
          alors il n'est pas claim et l'interaction dans ce sens est autorisé*/
        if(to == null){
            return true;
        }
        return interactSurrounding && to.interactSurrounding && isOwner(to.getOwnerUUID());
    }
    
    public Zone getOwner(){
        return owner;
    }
    
    void setOwner(Zone owner){
        if(this.owner != null){
            owner.removeClaim(this);
        }
        this.owner = owner;
        if(owner != null){
            owner.addClaim(this);
        }
        IGui iClaimGui = GuiManager.getInstance().getContainer("claim").getGui(false, this);
        if(iClaimGui != null){
            ((ManageClaimGui)iClaimGui).applyFather();
        }
    }
    
    public UUID getOwnerUUID(){
        return owner.getOwner();
    }
    
    public @Nullable String getDescription(){
        return description == null ? owner instanceof City ? ((City)owner).getDescription() : null : description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public String getOwnerName(){
        return owner.getName();
    }
    
    public Set<UUID> getPlayers(){
        return Collections.unmodifiableSet(permissions.keySet());
    }
    
    public boolean isInteractSurrounding(){
        return interactSurrounding;
    }
    
    public void setInteractSurrounding(boolean interactSurrounding){
        this.interactSurrounding = interactSurrounding;
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
    
    private boolean hasPermission(UUID uuid, String permission){
        Set<String> permissions = this.permissions.get(uuid);
        if(permissions == null){
            return false;
        }
        return permissions.contains(permission);
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
    
    public static boolean canInteract(Chunk from, Chunk to){
        if(from == to){
            return true;
        }
        Claim claimFrom = ClaimManager.getInstance().getClaim(from);
        Claim claimTo = ClaimManager.getInstance().getClaim(to);
        //Si le claim d'où part l'interaction existe, c'est sa méthode qui décide du résultat
        if(claimFrom != null){
            return claimFrom.canInteractWith(claimTo);
        }
        /*Sinon, puisque claimFrom est null, alors si claimTo est null, l'interaction est autorisé,
          sinon l'interaction non-claim -> claim est interdite*/
        return claimTo == null;
    }
    
    public static boolean canInteract(CChunk from, CChunk to){
        if(from == to){
            return true;
        }
        Claim claimFrom = from instanceof Claim ? (Claim)from : null;
        Claim claimTo = to instanceof Claim ? (Claim)to : null;
        //Si le claim d'où part l'interaction existe, c'est sa méthode qui décide du résultat
        if(claimFrom != null){
            return claimFrom.canInteractWith(claimTo);
        }
        /*Sinon, puisque claimFrom est null, alors si claimTo est null, l'interaction est autorisé,
          sinon l'interaction non-claim -> claim est interdite*/
        return claimTo == null;
    }
}
