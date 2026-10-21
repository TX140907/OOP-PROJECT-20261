package game.rendering;

import game.config.*;
import game.engine.*;
import game.level.BaseLevel;
import game.model.obstacle.Obstacle;
import game.ui.GameHUD;
import java.awt.*;

public final class GameRenderer {
    private final AssetLoader assets=new AssetLoader();
    private final EntityRenderer entities=new EntityRenderer(assets);
    private final AirBossRenderer bosses=new AirBossRenderer(assets);
    private final GameHUD hud=new GameHUD();
    private final GlareEffect glare=new GlareEffect();
    private final IceRoadRenderer iceRoad=new IceRoadRenderer();
    private final ConfigValues settings=AppSettings.load();
    public void draw(Graphics2D g,Game game) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(game.state()==GameState.MAIN_MENU) {g.setColor(new Color(12,23,43));g.fillRect(0,0,1000,700);return;}
        BaseLevel level=game.level();var c=level.config().values();
        g.setColor(c.color("level.backgroundColor"));g.fillRect(0,0,1000,700);
        var background=assets.image(c.text("sprite.background",""));
        if (background!=null) g.drawImage(background,0,0,1000,700,null);
        if (!level.config().helicopter()) {
            var road=assets.image(c.text("sprite.road",""));
            if (road!=null) g.drawImage(road,140,0,720,700,null);
            else {g.setColor(c.color("level.roadColor"));g.fillRect(140,0,720,700);}
            if (road==null || c.bool("road.overlayLines")) {
                g.setColor(new Color(230,230,200));g.fillRect(140,0,3,700);g.fillRect(857,0,3,700);
                for (int lane=1;lane<3;lane++) for (int y=-80;y<700;y+=80)
                    g.fillRect(140+lane*240-2,y+(int)(level.scroll()%80),4,40);
            }
        } else {
            g.setColor(new Color(255,255,255,100));
            for (int i=0;i<6;i++) g.fillOval((i*193)%1000,(int)((i*137+level.scroll())%800)-80,140,45);
        }
        iceRoad.draw(g,level);
        for (Obstacle o:level.obstacles()) {
            float alpha=glare.alpha(o,c,level.event().glareStrength());
            g.setComposite(AlphaComposite.SrcOver.derive(alpha)); entities.obstacle(g,o,level);
        }
        g.setComposite(AlphaComposite.SrcOver);glare.drawHaze(g,c,level.event().glareStrength());bosses.world(g,level);entities.player(g,level);
        entities.foregroundWarnings(g,level);hud.draw(g,level,settings,assets);bosses.hud(g,level);
        if (game.state()==GameState.TRANSITION) {
            g.setColor(new Color(0,0,0,(int)(255*game.fadeAlpha())));g.fillRect(0,0,1000,700);
        }
    }
}
