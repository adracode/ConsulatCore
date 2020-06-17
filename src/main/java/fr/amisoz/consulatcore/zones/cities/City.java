package fr.amisoz.consulatcore.zones.cities;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.graph.Graph;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.Permission;
import fr.leconsulat.api.utils.FileUtils;
import fr.leconsulat.api.utils.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class City extends Zone {
    
    private double bank;
    private Location spawn;
    private String description = "Description par défaut";
    private Map<UUID, CityPlayer> members = new HashMap<>();
    private CityChannel channel;
    private Set<String> publicPermissions = new HashSet<>();
    private List<CityRank> ranks;
    
    public City(UUID uuid, String name, UUID owner){
        this(uuid, name, owner, 0, null);
    }
    
    public City(UUID uuid, String name, UUID owner, double bank, Location spawn){
        super(uuid, name, owner, new Graph<>());
        this.bank = bank;
        this.spawn = spawn;
        this.channel = new CityChannel();
        this.ranks = new ArrayList<>(Arrays.asList(new CityRank("Maire"), new CityRank("Député"), new CityRank("Citoyen")));
        this.members.put(owner, new CityPlayer(
                new HashSet<>(Arrays.asList(
                        CityPermission.INTERACT.getPermission(),
                        CityPermission.MANAGE_ACCESS.getPermission(),
                        CityPermission.MANAGE_CLAIM.getPermission(),
                        CityPermission.MANAGE_PLAYER.getPermission())),
                ranks.get(0))
        );
    }
    
    public CityChannel getChannel(){
        return channel;
    }
    
    public void sendMessage(String message){
        this.channel.sendMessage(message);
    }
    
    public boolean isMember(UUID uuid){
        return members.containsKey(uuid);
    }
    
    public boolean addAccess(UUID uuid){
        return super.addPlayer(uuid);
    }
    
    public boolean removeAccess(UUID uuid){
        return super.removePlayer(uuid);
    }
    
    @Override
    public boolean addPlayer(UUID uuid){
        if(members.put(uuid, new CityPlayer(ranks.get(ranks.size() - 1))) != null){
            return false;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            if(player.belongsToCity()){
                return false;
            }
            player.setCity(this);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET city = ? WHERE player_uuid = ?");
                    statement.setString(1, this.getUUID().toString());
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch(SQLException e){
                    e.printStackTrace();
                }
            });
        }
        ZoneManager.getInstance().getCityGui().addPlayerCityPermissions(this, uuid, player == null ? Bukkit.getOfflinePlayer(uuid).getName() : player.getName());
        return true;
    }
    
    @Override
    public boolean removePlayer(UUID uuid){
        if(members.remove(uuid) == null){
            return false;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            player.setCity(null);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET city = NULL WHERE player_uuid = ?");
                    statement.setString(1, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch(SQLException e){
                    e.printStackTrace();
                }
            });
        }
        ZoneManager.getInstance().getCityGui().removePlayerCityPermissions(this, uuid);
        return true;
    }
    
    public boolean addPermission(ClaimPermission permission){
        return this.publicPermissions.add(permission.getPermission());
    }
    
    public boolean removePermission(ClaimPermission permission){
        return this.publicPermissions.remove(permission.getPermission());
    }
    
    public boolean hasPermission(ClaimPermission permission){
        if(permission == null){
            return false;
        }
        return this.publicPermissions.contains(permission.getPermission());
    }
    
    public boolean addPermission(UUID uuid, Permission... permissions){
        if(permissions.length == 0){
            return true;
        }
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        if(permissions.length == 1){
            return playerPermissions.addPermission(permissions[0].getPermission());
        }
        return playerPermissions.addPermission(Permission.toStringArray(permissions));
    }
    
    public boolean removePermission(UUID uuid, Permission... permissions){
        if(permissions.length == 0){
            return true;
        }
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        if(permissions.length == 1){
            return playerPermissions.removePermission(permissions[0].getPermission());
        }
        return playerPermissions.removePermission(Permission.toStringArray(permissions));
    }
    
    public boolean hasPermission(UUID uuid, Permission permission){
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        return playerPermissions.hasPermission(permission.getPermission());
    }
    
    public void removePlayers(){
        List<UUID> offlines = new ArrayList<>(members.size());
        for(Iterator<UUID> iterator = members.keySet().iterator(); iterator.hasNext(); ){
            UUID uuid = iterator.next();
            if(members.containsKey(uuid)){
                iterator.remove();
                SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
                if(player != null){
                    player.setCity(null);
                } else {
                    offlines.add(uuid);
                }
            }
        }
        if(offlines.size() != 0){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET city = NULL WHERE player_uuid = ?");
                    for(UUID uuid : offlines){
                        statement.setString(1, uuid.toString());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    statement.close();
                } catch(SQLException e){
                    e.printStackTrace();
                }
                
            });
        }
    }
    
    @Override
    public void addClaim(Claim claim){
        super.addClaim(claim);
        Graph<Claim> graph = (Graph<Claim>)getClaims();
        for(Claim surroundingClaim : Claim.getSurroundingClaims(claim.getX(), claim.getZ())){
            if(isClaim(surroundingClaim)){
                graph.addNeighbours(claim, surroundingClaim);
            }
        }
    }
    
    public boolean hasMoney(double amount){
        return bank - amount >= 0;
    }
    
    public void addMoney(double amount){
        bank += amount;
        ZoneManager.getInstance().getCityGui().setBank(this);
    }
    
    public void removeMoney(double amount){
        addMoney(-amount);
    }
    
    public boolean hasSpawn(){
        return spawn != null;
    }
    
    public Location getHome(){
        return spawn;
    }
    
    public void setHome(Location spawn){
        this.spawn = spawn;
    }
    
    public void disband(){
        removePlayers();
    }
    
    public boolean canRename(UUID uuid){
        return isOwner(uuid);
    }
    
    public boolean canSetSpawn(UUID uuid){
        return isOwner(uuid);
    }
    
    public boolean canDisband(UUID uuid){
        return isOwner(uuid);
    }
    
    public boolean canKick(UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_PLAYER);
    }
    
    public boolean canInvite(UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_PLAYER);
    }
    
    public boolean canClaim(UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_CLAIM);
    }
    
    public boolean canManageAccesses(UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_ACCESS);
    }
    
    public boolean areChunksConnected(Claim... claimRemoved){
        return ((Graph<Claim>)getClaims()).isConnected(claimRemoved);
    }
    
    public boolean isSpawnIn(Claim claim){
        if(spawn == null){
            return false;
        }
        return claim.getX() == spawn.getBlockX() >> 4 && claim.getZ() == spawn.getBlockZ() >> 4;
    }
    
    public boolean isSpawnIn(Chunk chunk){
        return chunk.getX() == spawn.getBlockX() >> 4 && chunk.getZ() == spawn.getBlockZ() >> 4;
    }
    
    public double getMoney(){
        return bank;
    }
    
    public String getDescription(){
        return description;
    }
    
    public Set<UUID> getMembers(){
        return Collections.unmodifiableSet(members.keySet());
    }
    
    public String getRank(int index){
        return ranks.get(index).getRankName();
    }
    
    private int getRank(String rankName){
        for(int i = 0; i < ranks.size(); i++){
            if(ranks.get(i).getRankName().equals(rankName)){
                return i;
            }
        }
        return -1;
    }
    
    public void loadNBT(){
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + this.getUUID() + ".dat");
            if(!file.exists()){
                return;
            }
            NBTInputStream is = new NBTInputStream(new FileInputStream(file));
            Map<String, Tag> city = ((CompoundTag)is.readTag()).getValue();
            is.close();
            List<Tag> ranks = NBTUtils.getChildTag(city, "Ranks", ListTag.class).getValue();
            for(int i = 0; i < ranks.size(); i++){
                this.ranks.get(i).setRankName(((StringTag)ranks.get(i)).getValue());
            }
            List<Tag> publicPermissions = NBTUtils.getChildTag(city, "PublicPermissions", ListTag.class).getValue();
            for(Tag publicPermission : publicPermissions){
                this.publicPermissions.add(((StringTag)publicPermission).getValue());
            }
            for(Tag memberTag : NBTUtils.getChildTag(city, "Members", ListTag.class).getValue()){
                Map<String, Tag> member = ((CompoundTag)memberTag).getValue();
                int rankIndex = NBTUtils.getChildTag(member, "Rank", IntTag.class).getValue();
                Set<String> perms = new HashSet<>();
                CityPlayer cityPlayer = new CityPlayer(perms, rankIndex == -1 ? null : this.ranks.get(rankIndex));
                for(Tag permissionTag : NBTUtils.getChildTag(member, "Permissions", ListTag.class).getValue()){
                    perms.add(((StringTag)permissionTag).getValue());
                }
                this.members.put(UUID.fromString(NBTUtils.getChildTag(member, "UUID", StringTag.class).getValue()), cityPlayer);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void saveNBT(){
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + getUUID() + ".dat");
            Map<String, Tag> city = new HashMap<>();
            if(!file.exists()){
                if(!file.createNewFile()){
                    throw new IOException("Couldn't create file.");
                }
            }
            List<Tag> membersTag = new ArrayList<>();
            for(Map.Entry<UUID, CityPlayer> memberEntry : this.members.entrySet()){
                CityPlayer member = memberEntry.getValue();
                Map<String, Tag> membersData = new HashMap<>();
                List<Tag> permissionsTag = new ArrayList<>();
                for(String permission : memberEntry.getValue().getPermissions()){
                    permissionsTag.add(new StringTag("", permission));
                }
                membersData.put("uuid", new StringTag("UUID", memberEntry.getKey().toString()));
                membersData.put("rank", new IntTag("Rank", member.getRank() == null ? -1 : getRank(member.getRank().getRankName())));
                membersData.put("permissions", new ListTag("Permissions", StringTag.class, permissionsTag));
                membersTag.add(new CompoundTag("", membersData));
            }
            city.put("members", new ListTag("Members", CompoundTag.class, membersTag));
            List<Tag> ranksTag = new ArrayList<>();
            for(CityRank rank : this.ranks){
                ranksTag.add(new StringTag("", rank.getRankName()));
            }
            city.put("ranks", new ListTag("Ranks", StringTag.class, ranksTag));
            List<Tag> publicPermissionsTag = new ArrayList<>();
            for(String publicPermission : this.publicPermissions){
                publicPermissionsTag.add(new StringTag("", publicPermission));
            }
            city.put("public", new ListTag("PublicPermissions", StringTag.class, publicPermissionsTag));
            NBTOutputStream os = new NBTOutputStream(new FileOutputStream(file));
            os.writeTag(new CompoundTag("City", city));
            os.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void setRank(int index, String rank){
        this.ranks.get(index).setRankName(rank);
        CityGui cityGui = ZoneManager.getInstance().getCityGui();
        cityGui.setRanks(this);
        cityGui.getRankGui().setRanks(cityGui.getGui(false, this));
    }
    
    public CityPlayer getMember(UUID uuid){
        return members.get(uuid);
    }
    
    public CityPlayer getCityPlayer(UUID uuid){
        return members.get(uuid);
    }
}