package game.logic.progress;
public final class ProgressManager {
    private final double duration;
    private double effectiveTime, hitTime;
    public ProgressManager(double duration) {
        if (duration<=0 || !Double.isFinite(duration)) throw new IllegalArgumentException("Invalid duration"); this.duration=duration;
    }
    public void hit(double seconds) { hitTime=Math.max(hitTime,seconds); }
    public void update(double dt,double normal,double hitMultiplier,double effectMultiplier) {
        double penalized=Math.min(dt,hitTime);
        effectiveTime=Math.min(duration,effectiveTime+((dt-penalized)+penalized*hitMultiplier)*normal*effectMultiplier);
        hitTime=Math.max(0,hitTime-dt);
    }
    public double fraction() { return Math.max(0,Math.min(1,effectiveTime/duration)); }
    public boolean complete() { return fraction()>=1-1e-10; }
    public void reset() { effectiveTime=0; hitTime=0; }
}
