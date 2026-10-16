package game.model.obstacle;
import game.config.ConfigValues;
import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.config.GameConfig;
import game.logic.movement.DelayedLinearMovement;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
/** Máy bay dùng chung hình/độ bền, bay xuống hoặc bay ngược sau thời gian cảnh báo. */
public final class EnemyPlane extends Obstacle {
    private final ConfigValues config;
    private final boolean armed;
    private final Object formation;
    private final double speed,launchDelay,flightSeconds,warningStartSeconds;
    private final int hitsToDestroy;
    private int rocketHits;
    private double hitFlashRemaining;
    private double shotTime;
    public EnemyPlane(ObstacleSpec spec,double x,double y,double speed,ConfigValues config) {
        this(spec,x,y,speed,config,true,null);
    }
    public EnemyPlane(ObstacleSpec spec,double x,double y,double speed,ConfigValues config,boolean armed,Object formation) {
        this(spec,x,y,speed,config,armed,formation,0,Double.POSITIVE_INFINITY);
    }
    private EnemyPlane(ObstacleSpec spec,double x,double y,double speed,ConfigValues config,boolean armed,Object formation,double delay,double flight) {
        this(spec,x,y,speed,config,armed,formation,delay,flight,0);
    }
    private EnemyPlane(ObstacleSpec spec,double x,double y,double speed,ConfigValues config,boolean armed,Object formation,double delay,double flight,double warningStart) {
        super(spec,new DelayedLinearMovement(x,y,0,speed,delay));this.config=config;
        this.armed=armed;this.formation=formation;
        this.speed=speed;launchDelay=delay;flightSeconds=flight;warningStartSeconds=warningStart;
        hitsToDestroy=config.integer("enemyPlane.hitsToDestroy");
        shotTime=config.number("enemyPlane.firstShotDelaySeconds");
    }
    /** Bay ngược sau cảnh báo; quãng đường gồm cả thân máy bay để rời hẳn màn trong thời gian cấu hình. */
    public static EnemyPlane reverse(ObstacleSpec spec,double x,ConfigValues config,Object formation) {
        return reverse(spec,x,config,formation,0);
    }
    public static EnemyPlane reverse(ObstacleSpec spec,double x,ConfigValues config,Object formation,double warningStart) {
        if(warningStart<0)throw new IllegalArgumentException("Reverse warning start must not be negative");
        double margin=config.number("boss.reverse.spawnMargin"),flight=config.number("boss.reverse.flightSeconds");
        double speed=-(GameConfig.HEIGHT+spec.height()+2*margin)/flight;
        return new EnemyPlane(spec,x,GameConfig.HEIGHT+margin,speed,config,false,formation,
                warningStart+config.number("boss.reverse.warningSeconds"),flight,warningStart);
    }
    /** Dùng chung đường viền cho hình tạm và va chạm, bỏ vùng trống quanh cánh và mũi. */
    @Override public Shape collisionShape() {
        double x=x(),y=y(),w=width(),h=height();
        Path2D outline=new Path2D.Double();
        outline.moveTo(x+w/2,y+h);
        outline.lineTo(x+w,y+h/3);
        outline.lineTo(x+w*3/5,y+h/3);
        outline.lineTo(x+w/2,y);
        outline.lineTo(x+w*2/5,y+h/3);
        outline.lineTo(x,y+h/3);
        outline.closePath();
        return reverseFlight()?AffineTransform.getRotateInstance(Math.PI,x+w/2,y+h/2).createTransformedShape(outline):outline;
    }
    @Override public void update(double dt,BaseLevel world) {
        super.update(dt,world);shotTime-=dt;hitFlashRemaining=Math.max(0,hitFlashRemaining-dt);
        if(age()+1e-9>=launchDelay+flightSeconds)remove();
        if(armed && world.allowsPlaneFire() && !removed() && shotTime<=0 && y()>=config.number("enemyPlane.minFireY") && y()<=config.number("enemyPlane.maxFireY")) {
            var encounter=world.bossEncounter();
            boolean laneAttack=encounter!=null && encounter.active()
                    && encounter.action()==game.logic.boss.AirBossEncounter.Action.LANE_ATTACK;
            boolean fired=world.fireMissile(this);
            shotTime=config.number(laneAttack?(fired?"boss.lane.planeShotIntervalSeconds":"boss.lane.planeShotRetrySeconds")
                    :"enemyPlane.shotIntervalSeconds");
        }
    }
    public boolean armed() { return armed; }
    public boolean reverseFlight() { return speed<0; }
    public boolean awaitingWarning() { return age()+1e-9<warningStartSeconds; }
    public boolean warning() { return !awaitingWarning() && age()+1e-9<launchDelay; }
    public double warningElapsedSeconds() { return Math.max(0,age()-warningStartSeconds); }
    @Override public boolean activeAt(double future) {
        double time=age()+future;
        return super.activeAt(future) && time+1e-9>=launchDelay && time+1e-9<launchDelay+flightSeconds;
    }
    @Override public boolean occupiesAt(double future) { return activeAt(future); }
    @Override public void hitByPlayerRocket() {
        if(removed())return;
        rocketHits++;hitFlashRemaining=config.number("enemyPlane.hitFlashSeconds");
        if(rocketHits>=hitsToDestroy)remove();
    }
    public boolean hitFlashing() { return hitFlashRemaining>0; }
    /** Tạo vị trí sinh thay thế, giữ nguyên vũ khí/tốc độ/đội hình; không dời vật đang bay. */
    public EnemyPlane atSpawnX(double x) {
        if(age()!=0)throw new IllegalStateException("Cannot relocate a plane after spawning");
        EnemyPlane shifted=new EnemyPlane(spec(),x,y(),speed,config,armed,formation,launchDelay,flightSeconds,warningStartSeconds);
        shifted.rocketHits=rocketHits;shifted.hitFlashRemaining=hitFlashRemaining;shifted.shotTime=shotTime;
        return shifted;
    }
    /** Hai máy bay cùng đội hình ở hai làn riêng được phép sát mép nhau. */
    @Override public boolean allowsOverlap(Obstacle other) {
        return formation!=null && other instanceof EnemyPlane plane && formation==plane.formation;
    }
}
