package fr.leconsulat.core.runnable;

import fr.leconsulat.core.ConsulatCore;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class MessageRunnable implements Runnable {
    
    private List<String> messageList = new ArrayList<>();
    private int index = 0;
    
    public MessageRunnable(){
        messageList.add("N'oublie pas de t'inscrire sur le site, pour y accéder tu peux faire /site.");
        messageList.add("N'oublie pas de rejoindre le discord via le /discord.");
        //messageList.add("L'end et le nether sont reset approximativement toutes les 2 semaines.");
        messageList.add("Le /help est ton ami si tu as une question");
        messageList.add("Cheater et insulter ne sert à rien !");
        messageList.add("Tu peux acheter et vendre des items à l'admin shop au spawn !");
        messageList.add("Tu peux vendre et acheter des items à d'autres joueurs, /shop help !");
        messageList.add("Un nouveau grade a fait son apparition, il est disponible pour " + ConsulatCore.formatMoney(15_000) + "au shop admin ! (Prix actuellement réduit)");
    }
    
    @Override
    public void run(){
        String message = messageList.get(index);
        Bukkit.broadcastMessage("§7[§6Annonce§7]§6 " + message);
        index++;
        if(messageList.size() == index){
            index = 0;
        }
    }
}
