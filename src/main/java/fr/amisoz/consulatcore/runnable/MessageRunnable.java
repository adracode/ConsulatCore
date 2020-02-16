package fr.amisoz.consulatcore.runnable;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class MessageRunnable implements Runnable {

    private List<String> messageList = new ArrayList<>();
    private int index = 0;

    public MessageRunnable() {
        messageList.add("N'oublies pas de t'inscrire sur le site, pour y accéder tu peux faire /site.");
        messageList.add("N'oublies pas de rejoindre le discord via le /discord.");
        messageList.add("L'end et le nether sont reset approximativement toutes les 2 semaines.");
        messageList.add("Le /help et /news est ton ami si tu as une question");
        messageList.add("Cheater et insulter ne sert à rien !");
        messageList.add("Tu peux acheter et vendre des items à l'admin shop au spawn !");
        messageList.add("Tu peux vendre et acheter des items à d'autres joueurs, /shop help !");
    }

    @Override
    public void run() {
        String message = messageList.get(index);
        Bukkit.broadcastMessage("§7[§6Annonce§7]§6 " + message);
        index++;
    }
}