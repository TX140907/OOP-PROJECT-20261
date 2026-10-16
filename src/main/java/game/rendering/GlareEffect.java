package game.rendering;
import game.config.ConfigValues;
import game.config.GameConfig;
import game.model.obstacle.Obstacle;
import java.awt.*;
/** Environmental presentation only, with a visible minimum alpha and a clear near field. */
public final class GlareEffect {
    public float alpha(Obstacle obstacle,ConfigValues c) {
        return alpha(obstacle,c,1);
    }
    public float alpha(Obstacle obstacle,ConfigValues c,double strength) {
        if (!c.bool("glare.enabled") || !(","+c.text("glare.types")+",").contains(","+obstacle.spec().type().key()+",")) return 1;
        double distance=Math.max(0,Math.min(1,obstacle.y()/c.number("glare.clearY")));
        distance=Math.pow(distance,c.number("glare.revealExponent",1));
        double opacity=c.number("glare.minAlpha")+(1-c.number("glare.minAlpha"))*distance;
        return (float)(1-Math.max(0,Math.min(1,strength))*(1-opacity));
    }
    /** Lớp chói phủ cả mặt đường, mờ dần trước nhân vật; HUD và cảnh báo được vẽ sau. */
    public void drawHaze(Graphics2D graphics,ConfigValues c) {
        drawHaze(graphics,c,1);
    }
    public void drawHaze(Graphics2D graphics,ConfigValues c,double strength) {
        double alpha=c.number("glare.hazeAlpha",0)*Math.max(0,Math.min(1,strength));
        if(!c.bool("glare.enabled") || alpha<=0)return;
        Color color=c.color("glare.hazeColor");
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setPaint(new GradientPaint(0,0,new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)(255*alpha)),
                    0,(float)c.number("glare.clearY"),new Color(color.getRed(),color.getGreen(),color.getBlue(),0)));
            g.fillRect(GameConfig.ROAD_LEFT,0,GameConfig.ROAD_RIGHT-GameConfig.ROAD_LEFT,(int)Math.ceil(c.number("glare.clearY")));
        } finally {g.dispose();}
    }
}
