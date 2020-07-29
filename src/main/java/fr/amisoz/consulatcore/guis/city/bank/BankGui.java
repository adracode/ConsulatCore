package fr.amisoz.consulatcore.guis.city.bank;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
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
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 37, 38, 39, 40, 41, 42, 43, 44);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case ADD_SLOT:
                GuiManager.getInstance().userInput(event.getPlayer().getPlayer(), (input) -> {
                    SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                    double moneyToGive;
                    try {
                        moneyToGive = Double.parseDouble(input);
                    } catch(NumberFormatException exception){
                        player.sendMessage("§cCe nombre n'est pas valide.");
                        return;
                    }
                    if(moneyToGive <= 0 || moneyToGive > 1_000_000){
                        player.sendMessage(Text.PREFIX + "§cTu ne peux pas donner " + ConsulatCore.formatMoney(moneyToGive) + " à la banque de ta ville.");
                        return;
                    }
                    if(!player.hasMoney(moneyToGive)){
                        player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent !");
                        return;
                    }
                    player.removeMoney(moneyToGive);
                    player.getCity().addMoney(moneyToGive);
                    player.sendMessage("§aTu as ajouté §7" + moneyToGive + " §aà ta ville");
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entrez le montant", "à ajouter"}, 0);
                break;
            case WITHDRAW_SLOT:
                GuiManager.getInstance().userInput(event.getPlayer().getPlayer(), (input) -> {
                    SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                    double moneyToWithdraw;
                    try {
                        moneyToWithdraw = Double.parseDouble(input);
                    } catch(NumberFormatException exception){
                        player.sendMessage("§cCe nombre n'est pas valide.");
                        return;
                    }
                    if(moneyToWithdraw <= 0 || moneyToWithdraw > 1_000_000){
                        player.sendMessage(Text.PREFIX + "§cTu ne peux pas retirer " + ConsulatCore.formatMoney(moneyToWithdraw) + " de la banque de ta ville.");
                        return;
                    }
                    City city = player.getCity();
                    if(!city.hasMoney(moneyToWithdraw)){
                        moneyToWithdraw = city.getMoney();
                    }
                    city.removeMoney(moneyToWithdraw);
                    player.addMoney(moneyToWithdraw);
                    player.sendMessage("§aTu as retiré §7" + moneyToWithdraw + " §ade ta ville");
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entrez le montant", "à retirer"}, 0);
                break;
        }
    }
}
