package fr.leconsulat.core.zones.cities;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.graph.Graph;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.nbt.CompoundTag;
import fr.leconsulat.api.nbt.ListTag;
import fr.leconsulat.api.nbt.NBTType;
import fr.leconsulat.api.nbt.StringTag;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.guis.city.CityGui;
import fr.leconsulat.core.guis.city.CityInfo;
import fr.leconsulat.core.guis.city.bank.BankGui;
import fr.leconsulat.core.guis.city.members.MembersGui;
import fr.leconsulat.core.guis.city.members.PublicPermissionsGui;
import fr.leconsulat.core.guis.city.members.member.MemberGui;
import fr.leconsulat.core.guis.city.members.member.permissions.MemberPermissionGui;
import fr.leconsulat.core.guis.city.members.member.rank.RankMemberGui;
import fr.leconsulat.core.guis.claims.ManageClaimGui;
import fr.leconsulat.core.players.CityPermission;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.Zone;
import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public class City extends Zone {
    
    public static final Pattern VALID_NAME = Pattern.compile("[a-zA-ZàçéèêîïùÀÇÉÈÊÎÏÙ]{3,16}");
    public static final Pattern VALID_RANK = Pattern.compile("[a-zA-ZàçéèêîïùÀÇÉÈÊÎÏÙ]{3,16}");
    public static final double RENAME_TAX = 5_000;
    public static final double CREATE_TAX = 7_500;
    
    private double bank;
    private long properties = 0;
    private @Nullable Location home;
    private @Nullable String description = null;
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
                new CityRank(0, "Maire", ChatColor.RED, CityPermission.values()),
                new CityRank(1, "Adjoint", ChatColor.AQUA),
                new CityRank(2, "Député", ChatColor.YELLOW),
                new CityRank(3, "Citoyen", ChatColor.GRAY));
    }
    
    @Override
    public boolean addPlayer(@NotNull UUID uuid){
        return addPlayer(uuid, new CityPermission[0]);
    }
    
    @Override
    public boolean removePlayer(@NotNull UUID uuid){
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Player " + uuid + " left the city " + getName());
        }
        if(members.remove(uuid) == null){
            if(ConsulatAPI.getConsulatAPI().isDebug()){
                ConsulatAPI.getConsulatAPI().log(Level.INFO, "Couldn't remove player");
            }
            return false;
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            player.setCity(null);
            channel.removePlayer(player);
        } else {
            ZoneManager.getInstance().setPlayerCity(uuid, null);
        }
        removeAccess(uuid);
        IGui iMembersGui = GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS);
        if(iMembersGui != null){
            ((MembersGui)iMembersGui).removePlayer(uuid);
        }
        IGui iCityInfo = GuiManager.getInstance().getContainer("city-info").getGui(false, this);
        if(iCityInfo != null){
            ((CityInfo)iCityInfo).removePlayer(uuid);
        }
        for(Claim claim : getClaims()){
            claim.removeProtectedContainers(uuid);
        }
        return true;
    }
    
    public boolean isNoDamage(){
        return (properties & 1) == 1;
    }
    
    public void setNoDamage(boolean value){
        if(value){
            properties |= 1;
        } else {
            properties &= Long.MAX_VALUE - 1;
        }
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
    
    @Override
    public void loadNBT(CompoundTag city){
        super.loadNBT(city);
        try {
            for(CompoundTag rankTag : city.<CompoundTag>getList("Ranks", NBTType.COMPOUND)){
                CityRank rank = ranks.get(rankTag.getInt("Id"));
                rank.setRankName(rankTag.getString("Name"));
                for(StringTag permission : rankTag.<StringTag>getList("DefaultPermissions", NBTType.STRING)){
                    rank.addPermission(CityPermission.byPermission(permission.getValue()));
                }
            }
        } catch(IllegalArgumentException e){
            List<StringTag> oldRanks = city.getList("Ranks", NBTType.STRING);
            for(int i = 0; i < oldRanks.size(); i++){
                this.ranks.get(i).setRankName(oldRanks.get(i).getValue());
            }
        }
        List<StringTag> publicPermissions = city.getList("PublicPermissions", NBTType.STRING);
        for(StringTag publicPermission : publicPermissions){
            this.publicPermissions.add(publicPermission.getValue());
        }
        List<CompoundTag> members = city.getList("Members", NBTType.COMPOUND);
        for(CompoundTag member : members){
            int rankIndex = member.getInt("Rank");
            Set<CityPermission> perms = new HashSet<>();
            UUID uuid = member.getUUID("UUID");
            CityPlayer cityPlayer = new CityPlayer(uuid, perms, this.ranks.get(rankIndex));
            List<StringTag> permissions = member.getList("Permissions", NBTType.STRING);
            for(StringTag permission : permissions){
                perms.add(CityPermission.byPermission(permission.getValue()));
            }
            this.members.put(uuid, cityPlayer);
        }
        if(city.has("Home")){
            CompoundTag home = city.getCompound("Home");
            this.home = new Location(
                    ConsulatCore.getInstance().getOverworld(),
                    home.getDouble("x"),
                    home.getDouble("y"),
                    home.getDouble("z"),
                    home.getFloat("yaw"),
                    home.getFloat("pitch")
            );
        }
        if(city.has("Description")){
            this.description = city.getString("Description");
        }
        if(city.has("Properties")){
            this.properties = city.getLong("Properties");
        }
    }
    
    @Override
    public CompoundTag saveNBT(){
        CompoundTag city = super.saveNBT();
        ListTag<CompoundTag> members = new ListTag<>(NBTType.COMPOUND);
        for(Map.Entry<UUID, CityPlayer> memberEntry : this.members.entrySet()){
            CityPlayer member = memberEntry.getValue();
            CompoundTag memberData = new CompoundTag();
            ListTag<StringTag> permissions = new ListTag<>(NBTType.STRING);
            for(CityPermission permission : member.getPermissions()){
                permissions.addTag(new StringTag(permission.getPermission()));
            }
            memberData.putUUID("UUID", memberEntry.getKey());
            memberData.putInt("Rank", getRank(member.getRank().getRankName()));
            memberData.put("Permissions", permissions);
            members.addTag(memberData);
        }
        city.put("Members", members);
        ListTag<CompoundTag> ranks = new ListTag<>(NBTType.COMPOUND);
        for(int i = 0; i < this.ranks.size(); i++){
            CityRank rank = this.ranks.get(i);
            CompoundTag rankTag = new CompoundTag();
            rankTag.putInt("Id", i);
            rankTag.putString("Name", rank.getRankName());
            ListTag<StringTag> defaults = new ListTag<>();
            for(CityPermission permission : rank.getDefaultPermissions()){
                defaults.addTag(new StringTag(permission.getPermission()));
            }
            rankTag.put("DefaultPermissions", defaults);
            ranks.addTag(rankTag);
        }
        city.put("Ranks", ranks);
        ListTag<StringTag> publicPermissions = new ListTag<>(NBTType.STRING);
        for(String publicPermission : this.publicPermissions){
            publicPermissions.addTag(new StringTag(publicPermission));
        }
        city.put("PublicPermissions", publicPermissions);
        if(home != null){
            CompoundTag home = new CompoundTag();
            home.putDouble("x", this.home.getX());
            home.putDouble("y", this.home.getY());
            home.putDouble("z", this.home.getZ());
            home.putFloat("yaw", this.home.getYaw());
            home.putFloat("pitch", this.home.getPitch());
            city.put("Home", home);
        }
        if(description != null){
            city.putString("Description", description);
        }
        if(properties != 0){
            city.putLong("Properties", properties);
        }
        return city;
    }
    
    @Override
    public void setOwner(@NotNull UUID owner){
        UUID old = getOwner();
        setRank(old, getRank(1));
        super.setOwner(owner);
        ZoneManager.getInstance().updateOwner(this);
        setRank(owner, getRank(0));
        SurvivalPlayer oldOwner = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(old);
        if(oldOwner != null){
            IGui gui = oldOwner.getCurrentlyOpen();
            if(gui instanceof CityGui){
                CityGui cityGui = (CityGui)gui;
                cityGui.updateOwner(oldOwner);
            }
        }
        SurvivalPlayer newOwner = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(owner);
        if(newOwner != null){
            IGui gui = newOwner.getCurrentlyOpen();
            if(gui instanceof CityGui){
                CityGui cityGui = (CityGui)gui;
                cityGui.updateOwner(newOwner);
            }
        }
        CityGui cityGui = (CityGui)GuiManager.getInstance().getContainer("city").getGui(false, this);
        if(cityGui != null){
            cityGui.updateOwner();
        }
        IGui cityInfoGui = GuiManager.getInstance().getContainer("city-info").getGui(false, this);
        if(cityInfoGui != null){
            ((CityInfo)cityInfoGui).updateOwner(null);
        }
    }
    
    @Override
    protected @NotNull Graph<Claim> getClaims(){
        return (Graph<Claim>)super.getClaims();
    }
    
    @Override
    public @NotNull String getEnterMessage(){
        return Text.PREFIX + "§cTu entres dans la ville §l" + getName() + ".";
    }
    
    @Override
    public @NotNull String getLeaveMessage(){
        return Text.PREFIX + "§cTu sors de la ville §l" + getName() + ".";
    }
    
    @Override
    public String toString(){
        return super.toString() +
                " City{" +
                "bank=" + bank +
                ", home=" + home +
                ", description='" + description + '\'' +
                ", members=" + members +
                ", channel=" + channel +
                ", publicPermissions=" + publicPermissions +
                ", ranks=" + ranks +
                '}';
    }
    
    public boolean addPlayer(@NotNull UUID uuid, CityPermission... permissions){
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Add player " + uuid + " to city " + getName());
        }
        if(members.containsKey(uuid)){
            if(ConsulatAPI.getConsulatAPI().isDebug()){
                ConsulatAPI.getConsulatAPI().log(Level.INFO, "Can't add player");
            }
            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
            if(player == null || player.belongsToCity()){
                return false;
            }
        }
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            player.setCity(this);
        } else {
            ZoneManager.getInstance().setPlayerCity(uuid, this);
        }
        CityPlayer cityPlayer = new CityPlayer(uuid, ranks.get(ranks.size() - 1));
        members.put(uuid, cityPlayer);
        cityPlayer.addPermission(permissions);
        IGui iMembersGui = GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS);
        if(iMembersGui != null){
            String name = player == null ? Bukkit.getOfflinePlayer(uuid).getName() : player.getName();
            ((MembersGui)iMembersGui).addPlayer(uuid);
        }
        IGui iCityInfo = GuiManager.getInstance().getContainer("city-info").getGui(false, this);
        if(iCityInfo != null){
            ((CityInfo)iCityInfo).addPlayer(uuid);
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
        if(members.isEmpty()){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET city = NULL WHERE city = ?");
                    statement.setString(1, getUniqueId().toString());
                    statement.executeUpdate();
                    statement.close();
                } catch(SQLException e){
                    e.printStackTrace();
                }
            });
            return;
        }
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
    
    public void switchPermission(UUID uuid, CityPermission permission){
        if(hasPermission(uuid, permission)){
            removePermission(uuid, permission);
        } else {
            addPermission(uuid, permission);
        }
    }
    
    public boolean addPermission(@NotNull UUID uuid, @NotNull CityPermission... permissions){
        if(permissions.length == 0){
            return false;
        }
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        boolean result = permissions.length == 1 ? playerPermissions.addPermission(permissions[0]) : playerPermissions.addPermission(permissions);
        if(result){
            updatePermissions(uuid, permissions);
            MemberPermissionGui permissionGui = (MemberPermissionGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS, uuid, MemberGui.PERMISSION);
            if(permissionGui != null){
                for(CityPermission permission : permissions){
                    permissionGui.setPermission(true, permission);
                }
            }
        }
        return result;
    }
    
    public boolean removePermission(@NotNull UUID uuid, @NotNull CityPermission... permissions){
        if(permissions.length == 0){
            return false;
        }
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        boolean result = permissions.length == 1 ? playerPermissions.removePermission(permissions[0]) : playerPermissions.removePermission(permissions);
        if(result){
            updatePermissions(uuid, permissions);
            MemberPermissionGui permissionGui = (MemberPermissionGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS, uuid, MemberGui.PERMISSION);
            if(permissionGui != null){
                for(CityPermission permission : permissions){
                    permissionGui.setPermission(false, permission);
                }
            }
        }
        return result;
    }
    
    public boolean hasPermission(@Nullable UUID uuid, @Nullable CityPermission permission){
        if(permission == null || uuid == null){
            return false;
        }
        CityPlayer playerPermissions = members.get(uuid);
        if(playerPermissions == null){
            return false;
        }
        return playerPermissions.hasPermission(permission);
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
    
    public boolean canWithdraw(UUID uuid){
        return hasPermission(uuid, CityPermission.MANAGE_BANK);
    }
    
    public boolean addPublicPermission(@NotNull ClaimPermission permission){
        boolean result = this.publicPermissions.add(permission.getPermission());
        if(result){
            PublicPermissionsGui permissionGui = (PublicPermissionsGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS, MembersGui.PUBLIC);
            if(permissionGui != null){
                permissionGui.setPermission(true, permission);
            }
        }
        return result;
    }
    
    public boolean removePublicPermission(@NotNull ClaimPermission permission){
        boolean result = this.publicPermissions.remove(permission.getPermission());
        if(result){
            PublicPermissionsGui permissionGui = (PublicPermissionsGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS, MembersGui.PUBLIC);
            if(permissionGui != null){
                permissionGui.setPermission(false, permission);
            }
        }
        return result;
    }
    
    public void switchPermission(ClaimPermission permission){
        if(hasPublicPermission(permission)){
            removePublicPermission(permission);
        } else {
            addPublicPermission(permission);
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
        CityGui cityGui = (CityGui)GuiManager.getInstance().getContainer("city").getGui(false, this);
        if(cityGui != null){
            cityGui.updateBank();
        }
    }
    
    public void removeMoney(double amount){
        addMoney(-amount);
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
    
    public @NotNull CityRank getRank(int index){
        return ranks.get(index);
    }
    
    public void setRank(UUID uuid, CityRank rank){
        CityPlayer player = getCityPlayer(uuid);
        CityRank oldRank = player.getRank();
        player.setRank(rank);
        resetPermissions(uuid);
        MembersGui membersGui = (MembersGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS);
        if(membersGui != null){
            membersGui.updateRanks();
        }
        RankMemberGui rankMemberGui = (RankMemberGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS, uuid, MemberGui.RANK);
        if(rankMemberGui != null){
            rankMemberGui.setRank(oldRank, rank);
        }
        CityInfo cityInfo = (CityInfo)GuiManager.getInstance().getContainer("city-info").getGui(false, this);
        if(cityInfo != null){
            cityInfo.updateRanks();
        }
    }
    
    public boolean setRankName(int index, @NotNull String rank){
        if(rank.isEmpty()){
            return false;
        }
        this.ranks.get(index).setRankName(rank);
        IGui iCityGui = GuiManager.getInstance().getContainer("city").getGui(false, this);
        if(iCityGui != null){
            CityGui cityGui = (CityGui)iCityGui;
            cityGui.updateRank(index);
        }
        IGui iCityInfo = GuiManager.getInstance().getContainer("city-info").getGui(false, this);
        if(iCityInfo != null){
            ((CityInfo)iCityInfo).updateRanks();
        }
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
    
    public @NotNull CityChannel getChannel(){
        return channel;
    }
    
    public @Nullable String getDescription(){
        return description;
    }
    
    public void setDescription(@Nullable String description){
        this.description = description;
    }
    
    public Collection<CityPlayer> getMembers(){
        return Collections.unmodifiableCollection(members.values());
    }
    
    private void updatePermissions(UUID uuid, CityPermission... permissions){
        for(CityPermission permission : permissions){
            switch(permission){
                case MANAGE_ACCESS:
                case MANAGE_CLAIM:
                    updateManageClaim(uuid);
                    break;
                case MANAGE_HOME:
                    updateHome(uuid);
                    break;
                case MANAGE_BANK:
                    updateBank(uuid);
                    break;
                case MANAGE_PLAYER:
                    updateManagePlayer(uuid);
                    break;
            }
        }
    }
    
    private void updateManageClaim(UUID uuid){
        ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            IGui currentlyOpen = player.getCurrentlyOpen();
            if(currentlyOpen instanceof ManageClaimGui){
                currentlyOpen.refresh(player);
            }
        }
    }
    
    private void updateHome(UUID uuid){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            IGui currentlyOpen = player.getCurrentlyOpen();
            if(currentlyOpen instanceof CityGui){
                Claim claim = player.getClaim();
                ((CityGui)currentlyOpen).updateHome(player, claim != null && isClaim(claim));
            }
        }
    }
    
    private void updateBank(UUID uuid){
        ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            IGui currentlyOpen = player.getCurrentlyOpen();
            if(currentlyOpen instanceof BankGui){
                currentlyOpen.refresh(player);
            }
        }
    }
    
    private void updateManagePlayer(UUID uuid){
        ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayer(uuid);
        if(player != null){
            IGui currentlyOpen = player.getCurrentlyOpen();
            if(currentlyOpen instanceof MembersGui){
                currentlyOpen.refresh(player);
            }
        }
    }
    
    private int getRank(String rankName){
        for(int i = 0; i < ranks.size(); i++){
            if(ranks.get(i).getRankName().equals(rankName)){
                return i;
            }
        }
        return -1;
    }
    
    private void resetPermissions(UUID uuid){
        CityPlayer player = getCityPlayer(uuid);
        player.clearPermissions();
        MemberPermissionGui permissionGui = (MemberPermissionGui)GuiManager.getInstance().getContainer("city").getGui(false, this, CityGui.MEMBERS, uuid, MemberGui.PERMISSION);
        if(permissionGui != null){
            for(CityPermission permission : CityPermission.values()){
                permissionGui.setPermission(false, permission);
            }
        }
        addPermission(uuid, player.getRank().getDefaultPermissions().toArray(new CityPermission[0]));
    }
}