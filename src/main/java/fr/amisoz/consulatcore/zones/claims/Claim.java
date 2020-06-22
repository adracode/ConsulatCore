package fr.amisoz.consulatcore.zones.claims;

import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.claimlist.ClaimsGui;
import fr.amisoz.consulatcore.guis.city.claimlist.claims.ManageClaimGui;
import fr.amisoz.consulatcore.guis.city.members.member.MemberGui;
import fr.amisoz.consulatcore.guis.city.members.member.claims.AccessibleClaimGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.Permission;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.utils.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jnbt.*;

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
    private Gui<Claim> manageClaim = null;
    
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
                CityGui cityGui = ZoneManager.getInstance().getCityGui();
                Gui<UUID> access = cityGui.getGui(false, (City)owner, CityGui.MEMBERS, uuid, MemberGui.CLAIM);
                if(access != null){
                    ((AccessibleClaimGui)access.getListener()).addItemClaim(access, this);
                }
            }
            if(manageClaim != null){
                ((ManageClaimGui)manageClaim.getListener()).addPlayerToClaim(manageClaim, uuid, Bukkit.getOfflinePlayer(uuid).getName());
            }
        }
        return added;
    }
    
    public boolean removePlayer(UUID uuid){
        boolean removed = this.permissions.remove(uuid) != null;
        if(removed){
            if(owner instanceof City){
                CityGui cityGui = ZoneManager.getInstance().getCityGui();
                Gui<UUID> access = cityGui.getGui(false, (City)owner, CityGui.MEMBERS, uuid, MemberGui.CLAIM);
                if(access != null){
                    ((AccessibleClaimGui)access.getListener()).removeItemClaim(access, this);
                }
            }
            if(manageClaim != null){
                ((ManageClaimGui)manageClaim.getListener()).removePlayerFromClaim(manageClaim, uuid);
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
        if(manageClaim != null){
            if(owner instanceof City){
                CityGui gui = ZoneManager.getInstance().getCityGui();
                Gui<City> cityGui = gui.getGui((City)owner);
                manageClaim.setFather(cityGui.getChild(CityGui.CLAIMS));
                addClaimToList();
            } else {
                manageClaim.setFather(null);
            }
        }
        this.owner = owner;
        permissions.clear();
    }
    
    void addClaimToList(){
        if(owner instanceof City){
            Gui<City> claimGui = ZoneManager.getInstance().getCityGui().getGui(false, (City)owner, CityGui.CLAIMS);
            if(claimGui != null){
                ((ClaimsGui)claimGui.getListener()).addItemClaim(claimGui, this);
            }
        }
    }
    
    public void openManageClaim(ConsulatPlayer player){
        getGui(true).open(player);
    }
    
    private Gui<Claim> createGui(){
        CityGui cityGui = ZoneManager.getInstance().getCityGui();
        return manageClaim = ZoneManager.getInstance().getManageClaimGui().createGui(this,
                owner instanceof City ? cityGui.getGui(true, (City)owner, CityGui.CLAIMS) : null);
    }
    
    @Nullable
    public Gui<Claim> getGui(boolean create){
        return manageClaim == null ? create ? createGui() : null : manageClaim;
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
        Map<String, Tag> member = new HashMap<>();
        List<Tag> permissions = new ArrayList<>();
        for(String permission : this.permissions.get(uuid)){
            permissions.add(new StringTag("", permission));
        }
        member.put("uuid", new StringTag("UUID", uuid.toString()));
        member.put("permissions", new ListTag("Permissions", StringTag.class, permissions));
        return new CompoundTag("", member);
    }
    
    public void loadNBT(CompoundTag root){
        Map<String, Tag> claimMap = root.getValue();
        if(claimMap.containsKey("Description")){
            this.description = NBTUtils.getChildTag(claimMap, "Description", StringTag.class).getValue();
        }
        List<Tag> membersList = NBTUtils.getChildTag(claimMap, "Members", ListTag.class).getValue();
        for(Tag tag : membersList){
            Map<String, Tag> memberMap = ((CompoundTag)tag).getValue();
            Set<String> permissions = new HashSet<>();
            for(Tag permissionTag : NBTUtils.getChildTag(memberMap, "Permissions", ListTag.class).getValue()){
                permissions.add(((StringTag)permissionTag).getValue());
            }
            this.permissions.put(
                    UUID.fromString(NBTUtils.getChildTag(memberMap, "UUID", StringTag.class).getValue()),
                    permissions);
        }
        if(claimMap.containsKey("ProtectedContainers")){
            List<Tag> protectedList = NBTUtils.getChildTag(claimMap, "ProtectedContainers", ListTag.class).getValue();
            for(Tag tag : protectedList){
                Map<String, Tag> protectedContainer = ((CompoundTag)tag).getValue();
                protectedContainers.put(
                        NBTUtils.getChildTag(protectedContainer, "Coords", LongTag.class).getValue(),
                        UUID.fromString(NBTUtils.getChildTag(protectedContainer, "Owner", StringTag.class).getValue()));
            }
        }
    }
    
    public CompoundTag saveNBT(){
        Map<String, Tag> claim = new HashMap<>();
        claim.put("coords", new LongTag("Coords", coords));
        if(description != null){
            claim.put("description", new StringTag("Description", description));
        }
        List<Tag> members = new ArrayList<>();
        for(Map.Entry<UUID, Set<String>> member : this.permissions.entrySet()){
            members.add(saveMember(member.getKey()));
        }
        claim.put("members", new ListTag("Members", CompoundTag.class, members));
        List<Tag> containersTag = new ArrayList<>();
        for(Map.Entry<Long, UUID> container : protectedContainers.entrySet()){
            Map<String, Tag> containerMap = new HashMap<>();
            containerMap.put("coords", new LongTag("Coords", container.getKey()));
            containerMap.put("owner", new StringTag("Owner", container.getValue().toString()));
            containersTag.add(new CompoundTag("", containerMap));
        }
        claim.put("containers", new ListTag("ProtectedContainers", CompoundTag.class, containersTag));
        return new CompoundTag("", claim);
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
                ", manageClaim=" + manageClaim +
                ", protectedContainers=" + protectedContainers +
                '}';
    }
    
    public boolean hasAccess(UUID uuid){
        return permissions.containsKey(uuid);
    }
}
