package fr.amisoz.consulatcore.players;

import java.util.Objects;

public class Fly {
    
    public static final Fly FLY_5 = new Fly(5 * 60, System.currentTimeMillis(), 5 * 60);
    public static final Fly FLY_25 = new Fly(25 * 60, System.currentTimeMillis(), 25 * 60);
    public static final Fly FLY_INFINITE = new Fly(-1, -1, -1);
    
    private boolean flying = false;
    private int flyTime;
    private long reset;
    private int timeLeft;
    
    public Fly(int flyTime, long reset, int timeLeft){
        this.flyTime = flyTime;
        this.reset = reset;
        this.timeLeft = timeLeft;
    }
    
    public Fly(Fly fly){
        this(fly.flyTime, fly.reset, fly.timeLeft);
    }
    
    public void decrementTimeLeft(){
        --this.timeLeft;
    }
    
    public boolean canFly(){
        return hasInfiniteFly() || reset < System.currentTimeMillis() || timeLeft > 0;
    }
    
    public boolean hasInfiniteFly(){
        return flyTime == -1;
    }
    
    public int getFlyTime(){
        return flyTime;
    }
    
    public long getReset(){
        return reset;
    }
    
    public int getTimeLeft(){
        return timeLeft;
    }
    
    public boolean isFlying(){
        return flying;
    }
    
    public void setFlying(boolean flying){
        this.flying = flying;
        if(flying && !hasInfiniteFly() && reset < System.currentTimeMillis()){
            reset = System.currentTimeMillis() + 3_600_000;
            timeLeft = flyTime;
        }
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(flying, flyTime, reset, timeLeft);
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof Fly)){
            return false;
        }
        Fly fly = (Fly)o;
        return flying == fly.flying &&
                flyTime == fly.flyTime &&
                reset == fly.reset &&
                timeLeft == fly.timeLeft;
    }
}
