package fr.amisoz.consulatcore.fly;

/**
 * Created by KIZAFOX on 16/03/2020 for ConsulatCore
 */
public enum FlyDurations {

    Fly5(300),
    Fly25(1500),
    FlyInfiny(Integer.MAX_VALUE);
    
    private int duration;

    FlyDurations(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }
}
