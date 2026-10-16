package game.ui;

import game.level.BaseLevel;
import game.config.ConfigValues;
import game.config.GameConfig;
import game.rendering.AssetLoader;
import java.awt.*;
import game.logic.effect.EffectManager.Type;

public final class GameHUD {
    private final ProgressBar progress=new ProgressBar();
    public void draw(Graphics2D g,BaseLevel level,ConfigValues settings,AssetLoader assets) {
        g.setFont(new Font("SansSerif",Font.BOLD,16));
        g.setColor(new Color(10,18,30,220));g.fillRect(0,0,1000,76);
        g.setColor(Color.WHITE);g.drawString(level.config().name(),22,27);
        g.drawString("HP: "+level.player().health()+"/"+level.player().maxHealth(),22,55);
        progress.draw(g,level.progress().fraction(),settings,assets);
        if (level.energy().enabled()) {
            g.setColor(new Color(65,55,30));g.fillRect(190,43,180,14);
            g.setColor(Color.ORANGE);g.fillRect(190,43,(int)(180*level.energy().fraction()),14);
            g.setColor(Color.WHITE);g.drawString("Energy "+(int)level.energy().value(),390,56);
        }
        String status=level.effects().active(Type.FREEZE)?"Đóng băng":
                level.effects().active(Type.SNOWBALL_SLOW)?"Chậm do tuyết":
                level.effects().active(Type.SHARK_SLOW)?"Chậm do cá mập":
                level.effects().active(Type.DRAIN)?"Chậm do cống":
                level.effects().active(Type.ENERGY_BOOST)?"Tăng tốc": "";
        if (!status.isEmpty()) {g.setColor(Color.YELLOW);g.drawString(status,520,56);}
        String event=level.event().message();
        if(!event.isEmpty()) {
            g.setFont(new Font("SansSerif",Font.BOLD,16));
            int width=g.getFontMetrics().stringWidth(event)+24,x=(GameConfig.WIDTH-width)/2;
            g.setColor(new Color(15,20,30,220));g.fillRoundRect(x,82,width,30,12,12);
            g.setColor(new Color(255,230,130));g.drawString(event,x+12,103);
        }
    }
}
