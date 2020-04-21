package fr.amisoz.consulatcore.players;

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
    
    public int getFlyTime(){
        return flyTime;
    }
    
    public long getReset(){
        return reset;
    }
    
    public int getTimeLeft(){
        return timeLeft;
    }
    
    public void decrementTimeLeft(){
        --this.timeLeft;
    }
    
    public boolean canFly(){
        return hasInfiniteFly() || timeLeft > 0;
    }
    
    public boolean hasInfiniteFly(){
        return flyTime == -1;
    }
    
    public boolean isFlying(){
        return flying;
    }
    
    public void setFlying(boolean flying){
        this.flying = flying;
        if(flying && flyTime != -1 && flyTime == timeLeft){
            reset = System.currentTimeMillis() + 3_600_000;
        }
    }
}
