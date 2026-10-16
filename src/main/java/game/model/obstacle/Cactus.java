package game.model.obstacle;

import game.config.ConfigValues;
import game.config.GameConfig;
import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.logic.movement.ScrollingMovement;
import java.awt.Shape;
import java.awt.geom.*;

/** Chính cây trên đường đứng, báo vùng đổ rồi xoay quanh gốc; không sinh thêm một loại cây khác. */
public final class Cactus extends Obstacle {
    private final boolean fallEnabled,fallsRight;
    private final double standingSeconds,warningSeconds,fallSeconds;
    public Cactus(ObstacleSpec spec,double x,double y,double scrollSpeed) {this(spec,x,y,scrollSpeed,null,true);}
    public Cactus(ObstacleSpec spec,double x,double y,double scrollSpeed,ConfigValues config,boolean fallsRight) {
        super(spec,new ScrollingMovement(x,y,0,scrollSpeed));
        this.fallsRight=fallsRight;
        fallEnabled=config!=null && Boolean.parseBoolean(config.text("cactus.fallEnabled","false"));
        standingSeconds=config==null?1.2:config.number("cactus.standingSeconds",1.2);
        warningSeconds=config==null?1.3:config.number("cactus.warningSeconds",1.3);
        fallSeconds=config==null?.3:config.number("cactus.fallSeconds",.3);
    }
    public boolean fallsRight() {return fallsRight;}
    public boolean telegraphing() {return fallEnabled && age()+1e-9>=standingSeconds && !fallenAt(0);}
    public double fallProgress() {return fallEnabled?Math.max(0,Math.min(1,(age()-standingSeconds-warningSeconds)/fallSeconds)):0;}
    private boolean fallenAt(double future) {return fallEnabled && age()+future+1e-9>=standingSeconds+warningSeconds+fallSeconds;}
    public Shape standingShape() {
        double x=x(),y=y(),w=width(),h=height();
        Area body=new Area(new RoundRectangle2D.Double(x+w/3,y,w/3,h,12,12));
        body.add(new Area(new Rectangle2D.Double(x,y+h/3,w,h/4)));
        return body;
    }
    @Override public Shape collisionShape() {
        return AffineTransform.getRotateInstance((fallsRight?1:-1)*Math.PI/2*fallProgress(),
                x()+width()/2,y()+height()).createTransformedShape(standingShape());
    }
    private Rectangle2D fallenBounds(Rectangle2D upright) {
        double rootX=upright.getCenterX(),rootY=upright.getMaxY();
        return new Rectangle2D.Double(fallsRight?rootX:rootX-height(),rootY-width()/2,height(),width());
    }
    public Rectangle2D fallTarget() {return fallenBounds(super.predictedHitbox(0));}
    @Override public Rectangle2D predictedHitbox(double future) {
        Rectangle2D upright=super.predictedHitbox(future);
        if(!fallEnabled)return upright;
        Rectangle2D ground=fallenBounds(upright);
        return fallenAt(future)?ground:upright.createUnion(ground);
    }
    @Override public Rectangle2D laneReservation() {return predictedHitbox(0);}
    @Override public boolean activeAt(double future) {
        return super.activeAt(future) && (!fallEnabled || age()+future<standingSeconds || fallenAt(future));
    }
    @Override public boolean occupiesAt(double future) {return super.activeAt(future);}
    @Override public void update(double dt,BaseLevel world) {
        super.update(dt,world);
        if(fallenAt(0) && fallTarget().getMinY()>GameConfig.HEIGHT+world.config().values().number("spawn.margin"))remove();
    }
}
