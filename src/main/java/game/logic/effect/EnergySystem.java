package game.logic.effect;
import game.config.ConfigValues;
public final class EnergySystem {
    private final boolean enabled;
    private final double max,drain;
    private double value;
    public EnergySystem(ConfigValues c) {
        enabled=c.bool("energy.enabled"); max=c.number("energy.max"); drain=c.number("energy.drainPerSecond"); value=c.number("energy.initial");
    }
    public void update(double dt) { if (enabled) value=Math.max(0,value-drain*dt); }
    public void recover(double amount) { if (enabled) value=Math.min(max,value+amount); }
    public boolean empty() { return enabled && value<=0; }
    public boolean enabled() { return enabled; }
    public double value() { return value; }
    public double fraction() { return value/max; }
}
