package game.ui;

import java.awt.*;
import game.config.ConfigValues;
import game.rendering.AssetLoader;

public final class ProgressBar {
    public void draw(Graphics2D g,double progress,ConfigValues settings,AssetLoader assets) {
        int x=settings.integer("hud.progressX"),y=settings.integer("hud.progressY"),w=settings.integer("hud.progressWidth"),h=settings.integer("hud.progressHeight");
        int size=settings.integer("hud.avatarSize");
        double p=Math.max(0,Math.min(1,progress));
        g.setColor(new Color(20,24,32));g.fillRoundRect(x,y,w,h,8,8);
        g.setColor(new Color(80,210,170));g.fillRoundRect(x,y,(int)(w*p),h,8,8);
        int head=Math.max(x,Math.min(x+w-size,x+(int)(w*p)-size/2));
        var face=assets.image(settings.text("sprite.hudAvatar"));
        if (face!=null) g.drawImage(face,head,y+(h-size)/2,size,size,null);
        else { g.setColor(new Color(255,215,120));g.fillRect(head,y+(h-size)/2,size,size);g.setColor(Color.BLACK);g.drawString("T",head+size/3,y+h-2); }
        g.setColor(Color.WHITE);g.drawString(String.format("%.0f%%",100*p),x+w-38,y+h+20);
    }
}
