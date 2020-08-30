package fr.leconsulat.core.economy;

import fr.leconsulat.core.ConsulatCore;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.function.ToDoubleFunction;

public abstract class Baltop<T> implements Listener {
    
    protected final int max;
    private List<T> rank = new ArrayList<>();
    private long lastUpdate = System.currentTimeMillis();
    private int timeBetweenUpdate = 5 * 60 * 1000;
    private Comparator<T> sort;
    private boolean updating = false;
    
    public Baltop(int max, ToDoubleFunction<T> getMoney){
        this.max = max;
        this.sort = (moneyOwner1, moneyOwner2) ->
                -Double.compare(getMoney.applyAsDouble(moneyOwner1), getMoney.applyAsDouble(moneyOwner2));
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), this::updateBaltop);
    }
    
    public List<T> getBaltop(){
        List<T> baltop = Collections.unmodifiableList(rank);
        if(System.currentTimeMillis() - lastUpdate > timeBetweenUpdate && !updating){
            updating = true;
            lastUpdate = System.currentTimeMillis();
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), this::updateBaltop);
        }
        return baltop;
    }
    
    private void updateBaltop(){
        TreeSet<T> sort = new TreeSet<>(this.sort);
        sort.addAll(getMoneyOwners());
        Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
            int i = -1;
            this.rank.clear();
            for(T toSort : sort){
                if(++i == max){
                    break;
                }
                this.rank.add(toSort);
            }
            updating = false;
        });
    }
    
    public abstract Collection<T> getMoneyOwners();
    
}
