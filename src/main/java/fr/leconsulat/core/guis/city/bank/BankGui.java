package fr.leconsulat.core.guis.city.bank;

import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.CityPermission;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.cities.City;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class BankGui extends DataRelatGui<City> {
    
    private static final byte INFO_SLOT = 4;
    private static final byte ADD_SLOT = 20;
    private static final byte WITHDRAW_SLOT = 24;
    
    public BankGui(City city){
        super(city, "Banque", 5,
                IGui.getItem("§eBanque", INFO_SLOT, Material.SUNFLOWER),
                IGui.getItem("§eAjouter de l'argent", ADD_SLOT, Material.ENDER_EYE),
                IGui.getItem("§eRetirer de l'argent", WITHDRAW_SLOT, Material.ENDER_PEARL)
        );
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onCreate(){
        updateBank();
    }
    
    @Override
    public void onOpened(GuiOpenEvent event){
        updateBank(event.getPlayer(),
                getData().hasPermission(event.getPlayer().getUUID(), CityPermission.MANAGE_BANK));
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case ADD_SLOT:
                GuiManager.getInstance().userInput(event.getPlayer(), (input) -> {
                    SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                    double moneyToGive;
                    try {
                        moneyToGive = Double.parseDouble(input);
                    } catch(NumberFormatException exception){
                        player.sendMessage(Text.INVALID_NUMBER);
                        return;
                    }
                    if(moneyToGive <= 0 || moneyToGive > 1_000_000){
                        player.sendMessage(Text.INVALID_MONEY);
                        return;
                    }
                    if(!player.hasMoney(moneyToGive)){
                        player.sendMessage(Text.NOT_ENOUGH_MONEY);
                        return;
                    }
                    player.removeMoney(moneyToGive);
                    player.getCity().addMoney(moneyToGive);
                    player.sendMessage(Text.ADD_MONEY_CITY(moneyToGive));
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        open(player);
                    });
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entre le montant", "à ajouter"}, 0);
                break;
            case WITHDRAW_SLOT:
                if(!getData().hasPermission(event.getPlayer().getUUID(), CityPermission.MANAGE_BANK)){
                    return;
                }
                GuiManager.getInstance().userInput(event.getPlayer(), (input) -> {
                    SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                    double moneyToWithdraw;
                    try {
                        moneyToWithdraw = Double.parseDouble(input);
                    } catch(NumberFormatException exception){
                        player.sendMessage(Text.INVALID_NUMBER);
                        return;
                    }
                    if(moneyToWithdraw <= 0 || moneyToWithdraw > 1_000_000){
                        player.sendMessage(Text.INVALID_MONEY);
                        return;
                    }
                    City city = player.getCity();
                    if(!city.hasMoney(moneyToWithdraw)){
                        player.sendMessage(Text.NOT_ENOUGH_MONEY_CITY);
                        return;
                    }
                    city.removeMoney(moneyToWithdraw);
                    player.addMoney(moneyToWithdraw);
                    player.sendMessage(Text.WITHDRAW_MONEY_CITY(moneyToWithdraw));
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        open(player);
                    });
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entre le montant", "à retirer"}, 0);
                break;
        }
    }
    
    public void updateBank(){
        setDescription(INFO_SLOT, "", "§a" + ConsulatCore.formatMoney(getData().getMoney()));
    }
    
    public void updateBank(ConsulatPlayer player, boolean allow){
        if(allow){
            removeFakeItem(WITHDRAW_SLOT, player);
        } else {
            setDescriptionPlayer(WITHDRAW_SLOT, player, "", "§cTu ne peux pas", "§cretirer de l'argent");
        }
    }
}
