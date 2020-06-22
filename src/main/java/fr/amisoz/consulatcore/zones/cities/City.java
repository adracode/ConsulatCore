package fr.amisoz.consulatcore.zones.cities;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.guis.GuiListenerStorage;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.members.MembersGui;
import fr.amisoz.consulatcore.guis.city.rank.RankGui;
import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimPermission;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.graph.Graph;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.Permission;
import fr.leconsulat.api.utils.FileUtils;
import fr.leconsulat.api.utils.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jnbt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public class City extends Zone {
    
    public static final int MAX_LENGTH_NAME = 32;
    
    private double bank;
    private @Nullable Location home;
    private @NotNull String description = "Description par défaut";
    private @NotNull Map<UUID, CityPlayer> members = new HashMap<>();
    private @NotNull CityChannel channel;
    private @NotNull Set<String> publicPermissions = new HashSet<>();
    private @NotNull List<CityRank> ranks;
    
    public City(@NotNull UUID uuid, @NotNull String name, @NotNull UUID owner){
        this(uuid, name, owner, 0);
    }
    
    public City(@NotNull UUID uuid, @NotNull String name, @NotNull UUID owner, double bank){
        super(uuid, name, owner, new Graph<>());
        this.bank = bank;
        this.channel = new CityChannel();
        this.ranks = Arrays.asList(
                new CityRank(0, "Maire"),
                new CityRank(1, "Député"),
                new CityRank(2, "Citoyen"));
    }
    
    public boolean addPlayer(@NotNull UUID uuid, CityPermission... permissions){
        if(members.containsKey(uuid)){
            return false;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            player.setCity(this);
        } else {
            ZoneManager.getInstance().setPlayerCity(uuid, this);
        }
        CityPlayer cityPlayer = new CityPlayer(ranks.get(ranks.size() - 1));
        members.put(uuid, cityPlayer);
        for(CityPermission permission : permissions){
            cityPlayer.addPermission(permission.getPermission());
        }
        Gui<City> membersGui = ZoneManager.getInstance().getCityGui().getGui(false, this, CityGui.MEMBERS);
        if(membersGui != null){
            String name = player == null ? Bukkit.getOfflinePlayer(uuid).getName() : player.getName();
            ((MembersGui)membersGui.getListener()).addPlayer(membersGui, uuid, name == null ? "Pseudo" : name);
        }
        return true;
    }
    
    @Override
    public boolean addPlayer(@NotNull UUID uuid){
        return addPlayer(uuid, new CityPermission[0]);
    }
    
    @Override
    public boolean removePlayer(@NotNull UUID uuid){
        if(members.remove(uuid) == null){
            return false;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            player.setCity(null);
        } else {
            ZoneManager.getInstance().setPlayerCity(uuid, null);
        }
        Gui<City> membersGui = ZoneManager.getInstance().getCityGui().getGui(false, this, CityGui.MEMBERS);
        if(membersGui != null){
            ((MembersGui)membersGui.getListener()).removePlayer(membersGui, uuid);
        }
        return true;
    }
    
    public @NotNull CityPlayer getCityPlayer(@NotNull UUID uuid){
        return members.get(uuid);
    }
    
    public boolean isMember(@Nullable UUID uuid){
        if(uuid == null){
            return false;
        }
        return members.containsKey(uuid);
    }
    
    public void removePlayers(){
        List<UUID> offlines = new ArrayList<>(members.size());
        for(Iterator<UUID> iterator = members.keySet().iterator(); iterator.hasNext(); ){
            UUID uuid = iterator.next();
            iterator.remove();
            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
            if(player != null){
                player.setCity(null);
            } else {
                offlines.add(uuid);
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
    
    public boolean addAccess(@NotNull UUID uuid){
        return super.addPlayer(uuid);
    }
    
    public boolean removeAccess(@NotNull UUID uuid){
        return super.removePlayer(uuid);
    }
    
    public boolean addPermission(@NotNull UUID uuid, @NotNull Permission... permissions){
        if(permissions.length == 0){
            return false;
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
    
    public boolean removePermission(@NotNull UUID uuid, @NotNull Permission... permissions){
        if(permissions.length == 0){
            return false;
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
    
    public boolean hasPermission(@Nullable UUID uuid, @Nullable Permission permission){
        if(permission == null || uuid == null){
            return false;
        }
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        return playerPermissions.hasPermission(permission.getPermission());
    }
    
    public boolean canRename(@Nullable UUID uuid){
        return isOwner(uuid);
    }
    
    public boolean canSetSpawn(@Nullable UUID uuid){
        return isOwner(uuid);
    }
    
    public boolean canDisband(@Nullable UUID uuid){
        return isOwner(uuid);
    }
    
    public boolean canKick(@Nullable UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_PLAYER);
    }
    
    public boolean canInvite(@Nullable UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_PLAYER);
    }
    
    public boolean canClaim(@Nullable UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_CLAIM);
    }
    
    public boolean canManageAccesses(@Nullable UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_ACCESS);
    }
    
    public boolean addPublicPermission(@NotNull ClaimPermission permission){
        return this.publicPermissions.add(permission.getPermission());
    }
    
    public boolean removePublicPermission(@NotNull ClaimPermission permission){
        return this.publicPermissions.remove(permission.getPermission());
    }
    
    @Override
    public boolean hasPublicPermission(@Nullable ClaimPermission permission){
        if(permission == null){
            return false;
        }
        return this.publicPermissions.contains(permission.getPermission());
    }
    
    @Override
    public void addClaim(@NotNull Claim claim){
        super.addClaim(claim);
        Graph<Claim> graph = getClaims();
        for(Claim surroundingClaim : Claim.getSurroundingClaims(claim.getX(), claim.getZ())){
            if(isClaim(surroundingClaim)){
                graph.addNeighbours(claim, surroundingClaim);
            }
        }
    }
    
    public boolean areClaimsConnected(@NotNull Claim... withoutClaims){
        return getClaims().isConnected(withoutClaims);
    }
    
    public boolean hasMoney(double amount){
        return bank - amount >= 0;
    }
    
    public void addMoney(double amount){
        bank += amount;
        ZoneManager.getInstance().getCityGui().updateBank(this);
    }
    
    public void removeMoney(double amount){
        addMoney(-amount);
    }
    
    public double getMoney(){
        return bank;
    }
    
    public @NotNull Location getHome(){
        if(home == null){
            throw new IllegalStateException("Use City#hasHome() to check if home is not null");
        }
        return home;
    }
    
    public void setHome(@Nullable Location spawn){
        this.home = spawn;
    }
    
    public boolean hasHome(){
        return home != null;
    }
    
    public boolean isHomeIn(@Nullable Claim claim){
        if(home == null || claim == null){
            return false;
        }
        return claim.getX() == home.getBlockX() >> 4 && claim.getZ() == home.getBlockZ() >> 4;
    }
    
    public boolean isHomeIn(@Nullable Chunk chunk){
        if(home == null || chunk == null){
            return false;
        }
        return chunk.getX() == home.getBlockX() >> 4 && chunk.getZ() == home.getBlockZ() >> 4;
    }
    
    private int getRank(String rankName){
        for(int i = 0; i < ranks.size(); i++){
            if(ranks.get(i).getRankName().equals(rankName)){
                return i;
            }
        }
        return -1;
    }
    
    public @NotNull CityRank getRank(int index){
        return ranks.get(index);
    }
    
    public boolean setRankName(int index, @NotNull String rank){
        if(rank.isEmpty()){
            return false;
        }
        this.ranks.get(index).setRankName(rank);
        CityGui cityGui = ZoneManager.getInstance().getCityGui();
        cityGui.updateRank(this);
        ((RankGui)GuiListenerStorage.getInstance().getListener(CityGui.RANKS)).setRank(cityGui.getGui(false, this, CityGui.RANKS), index);
        return true;
    }
    
    public @NotNull String getRankName(int index){
        return ranks.get(index).getRankName();
    }
    
    public void sendMessage(@Nullable String message){
        this.channel.sendMessage(message);
    }
    
    public void disband(){
        removePlayers();
    }
    
    public @NotNull CityChannel getChannel(){
        return channel;
    }
    
    @Override
    protected @NotNull Graph<Claim> getClaims(){
        return (Graph<Claim>)super.getClaims();
    }
    
    public @NotNull String getDescription(){
        return description;
    }
    
    public @NotNull Set<UUID> getMembers(){
        return Collections.unmodifiableSet(members.keySet());
    }
    
    @Override
    public void loadNBT(){
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + this.getUniqueId() + ".dat");
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
            if(city.containsKey("Home")){
                Map<String, Tag> home = NBTUtils.getChildTag(city, "Home", CompoundTag.class).getValue();
                this.home = new Location(
                        Bukkit.getWorlds().get(0),
                        NBTUtils.getChildTag(home, "x", DoubleTag.class).getValue(),
                        NBTUtils.getChildTag(home, "y", DoubleTag.class).getValue(),
                        NBTUtils.getChildTag(home, "z", DoubleTag.class).getValue(),
                        NBTUtils.getChildTag(home, "yaw", FloatTag.class).getValue(),
                        NBTUtils.getChildTag(home, "pitch", FloatTag.class).getValue()
                );
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    @Override
    public void saveNBT(){
        try {
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + getUniqueId() + ".dat");
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
                membersData.put("rank", new IntTag("Rank", getRank(member.getRank().getRankName())));
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
            if(home != null){
                Map<String, Tag> home = new HashMap<>();
                home.put("x", new DoubleTag("x", this.home.getX()));
                home.put("y", new DoubleTag("y", this.home.getY()));
                home.put("z", new DoubleTag("z", this.home.getZ()));
                home.put("yaw", new FloatTag("yaw", this.home.getYaw()));
                home.put("pitch", new FloatTag("pitch", this.home.getPitch()));
                city.put("home", new CompoundTag("Home", home));
            }
            NBTOutputStream os = new NBTOutputStream(new FileOutputStream(file));
            os.writeTag(new CompoundTag("City", city));
            os.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    
}