package game.engine;

public final class TransitionManager {
    private final double outSeconds,inSeconds;
    private double time;
    private boolean fadingIn;
    public TransitionManager(double outSeconds,double inSeconds) {
        if (outSeconds<=0 || inSeconds<=0) throw new IllegalArgumentException("Invalid fade duration");
        this.outSeconds=outSeconds; this.inSeconds=inSeconds;
    }
    public void beginOut() { time=0; fadingIn=false; }
    public void beginIn() { time=0; fadingIn=true; }
    public boolean update(double dt) { time+=dt; return time+1e-9>=(fadingIn?inSeconds:outSeconds); }
    public double alpha() { double p=Math.max(0,Math.min(1,time/(fadingIn?inSeconds:outSeconds))); return fadingIn?1-p:p; }
    public boolean fadingIn() { return fadingIn; }
}
