package fr.leconsulat.core.guis.city;

import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.utils.StringUtils;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.guis.city.bank.BankGui;
import fr.leconsulat.core.guis.city.changehome.ChangeHomeGui;
import fr.leconsulat.core.guis.city.claimlist.ClaimsGui;
import fr.leconsulat.core.guis.city.members.MembersGui;
import fr.leconsulat.core.guis.city.members.PublicPermissionsGui;
import fr.leconsulat.core.guis.city.members.member.MemberGui;
import fr.leconsulat.core.guis.city.members.member.rank.RankMemberGui;
import fr.leconsulat.core.guis.city.ranks.RanksGui;
import fr.leconsulat.core.players.CityPermission;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.cities.CityRank;
import fr.leconsulat.core.zones.claims.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CityGui extends DataRelatGui<City> {
    
    public static final String CLAIMS = "city.claims";
    public static final String HOME = "city.home";
    public static final String MEMBERS = "city.members";
    public static final String RANKS = "city.ranks";
    public static final String BANK = "city.bank";
    private static final byte CITY_SLOT = 4;
    private static final byte CLAIM_SLOT = 19;
    private static final byte HOME_SLOT = 21;
    private static final byte BANK_SLOT = 23;
    private static final byte RANK_SLOT = 25;
    private static final byte PERMISSION_SLOT = 40;
    private static final byte DISBAND_SLOT = 43;
    
    public CityGui(City city){
        super(city, "<ville>", 6,
                IGui.getItem("§e<nom>", CITY_SLOT, Material.PAPER),
                IGui.getItem("§eClaims", CLAIM_SLOT, Material.FILLED_MAP, "", "§7Voir les claims", "§7de la ville"),
                IGui.getItem("§eHome", HOME_SLOT, Material.COMPASS),
                IGui.getItem("§eBanque", BANK_SLOT, Material.SUNFLOWER),
                IGui.getItem("§eGrades", RANK_SLOT, Material.OAK_SIGN),
                IGui.getItem("§eMembres", PERMISSION_SLOT, Material.PLAYER_HEAD, "", "§7Gérer les membres", "§7de la ville"),
                IGui.getItem("§eDestruction", DISBAND_SLOT, Material.BARRIER, "", "§cDétruire la ville"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8);
    }
    
    @Override
    public void onCreate(){
        updateName();
        updateOwner();
        updateBank();
        updateRank();
    }
    
    @Override
    public void onOpened(GuiOpenEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        Claim claim = player.getClaim();
        City city = getData();
        updateInfo(player, city.canRename(player.getUUID()));
        updateHome(player, claim != null && city.isClaim(claim));
        updateRank(player, city.isOwner(player.getUUID()));
        updateDisband(player, city.canDisband(player.getUUID()));
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        City city = getData();
        switch(event.getSlot()){
            case CLAIM_SLOT:{
                getChild(CLAIMS).getGui().open(player);
            }
            break;
            case HOME_SLOT:{
                if(!city.hasPermission(player.getUUID(), CityPermission.MANAGE_HOME)){
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    return;
                }
                if(!city.hasHome()){
                    ZoneManager.getInstance().setHome(city, player.getPlayer().getLocation());
                    player.sendMessage(Text.YOU_SET_HOME_CITY);
                } else {
                    confirmSethome(player);
                }
            }
            break;
            case RANK_SLOT:{
                if(!city.isOwner(player.getUUID())){
                    return;
                }
                getChild(RANKS).getGui().open(player);
            }
            break;
            case PERMISSION_SLOT:{
                getChild(MEMBERS).getGui().open(player);
            }
            break;
            case BANK_SLOT:{
                getChild(BANK).getGui().open(player);
            }
            break;
            case CITY_SLOT:{
                if(!city.canRename(player.getUUID())){
                    return;
                }
                if(!getData().hasMoney(City.RENAME_TAX)){
                    player.getPlayer().closeInventory();
                    player.sendMessage(Text.NOT_ENOUGH_MONEY_CITY(City.RENAME_TAX));
                    return;
                }
                GuiManager.getInstance().userInput(player, (input) -> {
                    if(!City.VALID_NAME.matcher(input).matches()){
                        player.sendMessage(Text.INVALID_CITY_NAME);
                        return;
                    }
                    input = StringUtils.capitalize(input);
                    if(ZoneManager.getInstance().getCity(input) != null){
                        player.sendMessage(Text.CITY_ALREADY_EXISTS);
                        return;
                    }
                    player.sendMessage(Text.CITY_RENAMED(city.getName(), input));
                    ZoneManager.getInstance().renameCity(city, input);
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        open(player);
                    });
                }, new String[]{"", "", "^^^^^^^^^^^^^^", "Nouveau nom"}, 0, 1);
            }
            break;
            case DISBAND_SLOT:{
                if(!city.canDisband(player.getUUID())){
                    return;
                }
                GuiManager.getInstance().getContainer("city-disband").getGui(getData()).open(player);
            }
            break;
        }
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof String){
            switch((String)key){
                case CLAIMS:
                    return new ClaimsGui(getData());
                case HOME:
                    return new ChangeHomeGui(getData());
                case MEMBERS:
                    return new MembersGui(getData());
                case RANKS:
                    return new RanksGui(getData());
                case BANK:
                    return new BankGui(getData());
            }
            throw new IllegalArgumentException();
        }
        return super.createChild(key);
    }
    
    public void updateDisband(SurvivalPlayer player, boolean allow){
        if(allow){
            setFakeItem(DISBAND_SLOT, null, player);
        } else {
            setDescriptionPlayer(DISBAND_SLOT, player, "", "§cTu ne peux pas", "§cdétruire la ville");
        }
    }
    
    public void updateInfo(SurvivalPlayer player, boolean allow){
        if(allow){
            setFakeItem(CITY_SLOT, null, player);
        } else {
            setDescriptionPlayer(CITY_SLOT, player, "", "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(getData().getOwner()).getName());
        }
    }
    
    public void updateName(){
        City city = getData();
        setName(city.getName());
        setDisplayName(CITY_SLOT, "§e" + city.getName());
    }
    
    public void updateOwner(){
        setDescription(CITY_SLOT, "", "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(getData().getOwner()).getName(),
                "", "§7§oClique pour renommer", "§7§ota ville (" + ConsulatCore.formatMoney(City.RENAME_TAX) + ")");
        MembersGui membersGui = (MembersGui)getLegacyChild(MEMBERS);
        if(membersGui != null){
            membersGui.refresh();
            PublicPermissionsGui publicPermissionsGui = (PublicPermissionsGui)getLegacyChild(MembersGui.PUBLIC);
            if(publicPermissionsGui != null){
                membersGui.refresh();
            }
            for(Relationnable child : membersGui.getChildren()){
                child.getGui().refresh();
                if(child instanceof MemberGui){
                    ((MemberGui)child).onCreate();
                }
            }
        }
        
    }
    
    public void confirmSethome(ConsulatPlayer player){
        getChild(HOME).getGui().open(player);
    }
    
    public void updateHome(){
        City city = getData();
        for(HumanEntity player : getInventory().getViewers()){
            SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
            Claim claim = survivalPlayer.getClaim();
            updateHome(survivalPlayer, claim != null && city.isClaim(claim));
        }
        ChangeHomeGui changeHomeGui = (ChangeHomeGui)getLegacyChild(HOME);
        if(changeHomeGui != null){
            for(HumanEntity player : changeHomeGui.getInventory().getViewers()){
                player.sendMessage(Text.CITY_HOME_CHANGED);
            }
        }
    }
    
    public void updateHome(ConsulatPlayer player, boolean allow){
        City city = getData();
        if(!city.hasPermission(player.getUUID(), CityPermission.MANAGE_HOME)){
            allow = false;
        }
        if(!city.hasHome()){
            if(allow){
                setDescriptionPlayer(HOME_SLOT, player, "§7Aucun home défini", "",
                        "§aClique §7pour", "§7définir le home §aici");
            } else {
                setDescriptionPlayer(HOME_SLOT, player, "§7Aucun home défini");
            }
        } else {
            Location home = city.getHome();
            if(allow){
                setDescriptionPlayer(HOME_SLOT, player, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ(), "",
                        "§aClique §7pour", "§7définir le home §aici");
            } else {
                setDescriptionPlayer(HOME_SLOT, player, "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            }
        }
    }
    
    public void updateBank(){
        setDescription(BANK_SLOT, "", "§a" + ConsulatCore.formatMoney(getData().getMoney()), "", "§7Gérer la banque");
        BankGui bankGui = (BankGui)getLegacyChild(BANK);
        if(bankGui != null){
            bankGui.updateBank();
        }
    }
    
    public void updateRank(){
        City city = getData();
        setDescription(RANK_SLOT, "",
                "§c" + city.getRankName(0),
                "§b" + city.getRankName(1),
                "§e" + city.getRankName(2),
                "§7" + city.getRankName(3), "", "§7Gérer les grades de ville");
    }
    
    public void updateRank(int index){
        CityRank rank = getData().getRank(index);
        updateRank();
        RanksGui ranksGui = (RanksGui)getLegacyChild(CityGui.RANKS);
        if(ranksGui != null){
            ranksGui.setRank(index);
        }
        MembersGui cityMembers = (MembersGui)getLegacyChild(CityGui.MEMBERS);
        if(cityMembers != null){
            cityMembers.updateRanks();
            Collection<Relationnable> children = cityMembers.getChildren();
            for(Relationnable child : children){
                if(child instanceof MemberGui){
                    RankMemberGui rankMemberGui = (RankMemberGui)child.getLegacyChild(MemberGui.RANK);
                    if(rankMemberGui != null){
                        rankMemberGui.updateRank(rank);
                    }
                }
            }
        }
    }
    
    public void updateRank(SurvivalPlayer player, boolean allow){
        if(allow){
            setFakeItem(RANK_SLOT, null, player);
        } else {
            City city = getData();
            setDescriptionPlayer(RANK_SLOT, player, "",
                    "§c" + city.getRankName(0),
                    "§b" + city.getRankName(1),
                    "§e" + city.getRankName(2),
                    "§7" + city.getRankName(3),
                    "", "§cTu ne peux pas", "§cgérer les grades de ville");
        }
    }
    
    public void updateOwner(SurvivalPlayer player){
        boolean isOwner = getData().isOwner(player.getUUID());
        updateRank(player, isOwner);
        updateInfo(player, isOwner);
        updateDisband(player, isOwner);
        if(player.getCurrentlyOpen() instanceof RankMemberGui){
            player.getCurrentlyOpen().refresh(player);
        }
    }
    
    public static class Container extends GuiContainer<City> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("city", this);
        }
        
        @Override
        public Datable<City> createGui(City city){
            return new CityGui(city);
        }
    }
    
}
