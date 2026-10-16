package game.model.obstacle;

import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.DelayedLinearMovement;
import java.awt.geom.Rectangle2D;

/** Đích được chốt khi cảnh báo bắt đầu: ném tại hàng người chơi hoặc bay dọc làn. */
public final class BossProjectile extends Obstacle {
    private final double warningSeconds,targetX,targetY,sourceX,sourceY,lifetimeSeconds;
    private final boolean rowStrike,aimed;
    private final Object volley;
    public BossProjectile(ObstacleSpec spec,double x,double y,double targetY,double speed,double warning,double activeSeconds,boolean rowStrike) {
        this(spec,x+spec.width()/2,y+spec.height()/2,x+spec.width()/2,targetY,0,speed,warning,
                rowStrike?warning+activeSeconds:Double.POSITIVE_INFINITY,rowStrike,false,null);
    }
    private BossProjectile(ObstacleSpec spec,double sourceX,double sourceY,double targetX,double targetY,
            double vx,double vy,double warning,double lifetime,boolean rowStrike,boolean aimed,Object volley) {
        super(spec,new DelayedLinearMovement(sourceX-spec.width()/2,sourceY-spec.height()/2,vx,vy,warning));
        this.warningSeconds=warning;this.targetX=targetX;this.targetY=targetY;
        this.sourceX=sourceX;this.sourceY=sourceY;this.lifetimeSeconds=lifetime;this.rowStrike=rowStrike;this.aimed=aimed;this.volley=volley;
    }
    /** Khóa đích khi ngắm; hết cảnh báo mới bay thẳng từ tay boss qua ô đã chọn. */
    public static BossProjectile aimed(ObstacleSpec spec,double sourceX,double sourceY,double targetX,double targetY,
            double speed,double warning,double afterTargetSeconds) {
        return aimed(spec,sourceX,sourceY,targetX,targetY,speed,warning,afterTargetSeconds,null);
    }
    public static BossProjectile aimed(ObstacleSpec spec,double sourceX,double sourceY,double targetX,double targetY,
            double speed,double warning,double afterTargetSeconds,Object volley) {
        double dx=targetX-sourceX,dy=targetY-sourceY,distance=Math.hypot(dx,dy);
        if(speed<=0 || distance==0)throw new IllegalArgumentException("Invalid aimed projectile trajectory");
        return new BossProjectile(spec,sourceX,sourceY,targetX,targetY,dx/distance*speed,dy/distance*speed,warning,
                warning+distance/speed+afterTargetSeconds,true,true,volley);
    }
    public boolean warning() { return age()+1e-9<warningSeconds; }
    public boolean rowStrike() { return rowStrike; }
    public boolean aimed() { return aimed; }
    public double sourceX() { return sourceX; }
    public double sourceY() { return sourceY; }
    public double lifetimeSeconds() { return lifetimeSeconds; }
    @Override public boolean blocksPlayerRocket() { return false; }
    /** Hai viên cùng loạt được xuất phát chung một tay; vẫn kiểm tra đường né của cả hai. */
    @Override public boolean allowsOverlap(Obstacle other) {
        return volley!=null && other instanceof BossProjectile projectile && volley==projectile.volley;
    }
    public Rectangle2D targetArea(double size) { return new Rectangle2D.Double(targetX-size/2,targetY-size/2,size,size); }
    @Override public boolean activeAt(double future) {
        double time=age()+future;
        return super.activeAt(future) && time+1e-9>=warningSeconds && time<lifetimeSeconds-1e-9;
    }
    @Override public boolean occupiesAt(double future) {
        return aimed?activeAt(future):!removed() && age()+future<lifetimeSeconds;
    }
    @Override public void update(double dt,BaseLevel world) {
        super.update(dt,world);
        if(age()+1e-9>=lifetimeSeconds) remove();
    }
}
