package fr.amisoz.consulatcore.zones.claims;

import fr.amisoz.consulatcore.chunks.CChunk;
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
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import java.util.*;

public class Claim extends CChunk {
    
    public static final String TYPE = "CLAIM";
    
    public static final double BUY_CLAIM = 180;
    public static final double BUY_CITY_CLAIM = BUY_CLAIM;
    public static final double REFUND = BUY_CLAIM * 0.7;
    
    private String description;
    private Zone owner;
    private Map<UUID, Set<String>> permissions = new HashMap<>();
    
    public Claim(long coords){
        super(coords);
    }
    
    Claim(int x, int z, Zone owner, String description){
        super(x, z);
        setOwner(owner);
        this.description = description;
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
    
    public void setDescription(String description){
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
        if(this.owner != null){
            owner.removeClaim(this);
        }
        this.owner = owner;
        if(owner != null){
            owner.addClaim(this);
        }
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
    
    @Override
    public void loadNBT(CompoundTag claim){
        super.loadNBT(claim);
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
        for(Map.Entry<Long, UUID> container : protectedContainers.entrySet()){
            CompoundTag containerTag = new CompoundTag();
            containerTag.putLong("Coords", container.getKey());
            containerTag.putUUID("Owner", container.getValue());
            containersTag.addTag(containerTag);
        }
        claim.put("Containers", containersTag);
        return claim;
    }
    
    public Set<UUID> getPlayers(){
        return Collections.unmodifiableSet(permissions.keySet());
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
    
    public boolean hasAccess(UUID uuid){
        return permissions.containsKey(uuid);
    }
    
    public boolean canManageAccesses(UUID uuid){
        return isOwner(uuid) || owner instanceof City && ((City)owner).canManageAccesses(uuid);
    }
    
    @Override
    public String getType(){
        return TYPE;
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
    
    public boolean canInteractWith(Claim to){
        /*Si le claim où arrive l'interaction est null,
          alors il n'est pas claim et l'interaction dans ce sens est autorisé*/
        if(to == null){
            return true;
        }
        return isOwner(to.getOwnerUUID());
    }
    
}
