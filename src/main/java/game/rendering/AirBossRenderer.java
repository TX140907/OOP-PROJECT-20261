package game.rendering;

import game.config.ConfigValues;
import game.level.BaseLevel;
import game.logic.boss.AirBossEncounter;
import java.awt.*;

/** Hình tạm, gợi ý và HUD của boss; không điều khiển nhịp tấn công. */
public final class AirBossRenderer {
    private final AssetLoader assets;
    public AirBossRenderer(AssetLoader assets) {this.assets=assets;}
    public void world(Graphics2D g,BaseLevel level) {
        AirBossEncounter encounter=level.bossEncounter();if(encounter==null)return;
        ConfigValues c=level.config().values();var boss=encounter.boss();
        if(encounter.bossVisible()) {
            var image=assets.image(c.text("sprite.boss",""));
            if(image!=null)g.drawImage(image,(int)boss.x(),(int)boss.y(),(int)boss.width(),(int)boss.height(),null);
            else {
                g.setColor(c.color("boss.color"));g.fill(boss.spriteBounds());
                g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,16));
                g.drawString("BOSS",(int)boss.x()+((int)boss.width()-g.getFontMetrics().stringWidth("BOSS"))/2,(int)boss.y()+(int)boss.height()/2);
            }
        }
        for(var rocket:encounter.rockets()) {
            var image=assets.image(c.text("sprite.playerRocket",""));
            if(image!=null)g.drawImage(image,(int)rocket.x(),(int)rocket.y(),(int)rocket.width(),(int)rocket.height(),null);
            else {g.setColor(c.color("boss.playerRocket.color"));g.fill(rocket.spriteBounds());}
        }
        for(var explosion:encounter.explosions()) {
            double fraction=explosion.remaining()/c.number("boss.explosion.seconds");
            int size=(int)(c.number("boss.explosion.size")*(1-fraction/2));
            int x=(int)explosion.x()-size/2,y=(int)explosion.y()-size/2;
            Composite previous=g.getComposite();g.setComposite(AlphaComposite.SrcOver.derive((float)Math.max(0,Math.min(1,fraction))));
            var image=assets.image(c.text("sprite.bossExplosion",""));
            if(image!=null)g.drawImage(image,x,y,size,size,null);
            else {g.setColor(Color.ORANGE);g.fillOval(x,y,size,size);g.setColor(Color.YELLOW);g.fillOval(x+size/4,y+size/4,size/2,size/2);}
            g.setComposite(previous);
        }
        if(encounter.state()==AirBossEncounter.State.WARNING)
            hint(g,level,"!",c.integer("boss.warningSize"),c.integer("boss.warningOffsetX"),encounter.stateTime(),c.number("boss.warningBlinkSeconds"),Color.ORANGE);
        else if(encounter.active() && !encounter.hasFired())
            hint(g,level,"E",c.integer("boss.hintSize"),c.integer("boss.hintOffsetX"),encounter.stateTime(),c.number("boss.hintBlinkSeconds"),Color.YELLOW);
    }
    private void hint(Graphics2D g,BaseLevel level,String text,int size,int offset,double seconds,double blink,Color color) {
        if((int)(seconds/blink)%2!=0)return;
        var player=level.player();int x=(int)(player.x()+player.width())+offset;
        if(x+size>1000)x=(int)player.x()-offset-size;
        int y=(int)(player.y()+player.height()/2)-size/2;
        g.setColor(color);g.fillRoundRect(x,y,size,size,8,8);
        g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,(int)(size*.8)));
        g.drawString(text,x+(size-g.getFontMetrics().stringWidth(text))/2,y+(size+g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent())/2);
    }
    public void hud(Graphics2D g,BaseLevel level) {
        var encounter=level.bossEncounter();if(encounter==null)return;
        var c=level.config().values();
        if(encounter.bossVisible()) {
            int x=c.integer("boss.hud.x"),y=c.integer("boss.hud.y"),w=c.integer("boss.hud.width"),h=c.integer("boss.hud.height");
            int size=c.integer("boss.hud.portraitSize"),portraitX=x-size-c.integer("boss.hud.portraitGap"),portraitY=y+(h-size)/2;
            g.setColor(new Color(15,20,32,235));g.fillRoundRect(x-6,y-24,w+12,h+32,8,8);
            g.setColor(c.color("boss.color"));g.fillRect(portraitX,portraitY,size,size);
            var portrait=assets.image(c.text("sprite.bossPortrait",""));
            if(portrait!=null)g.drawImage(portrait,portraitX,portraitY,size,size,null);
            else {g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,size/2));g.drawString("B",portraitX+size/3,portraitY+size*3/4);}
            g.setColor(new Color(45,30,45));g.fillRect(x,y,w,h);
            double health=encounter.boss().healthFraction()*encounter.loadingFraction();
            g.setColor(c.color("boss.healthBarColor"));g.fillRect(x,y,(int)(w*health),h);
            g.setFont(new Font("SansSerif",Font.BOLD,15));g.setColor(Color.WHITE);
            g.drawString(encounter.state()==AirBossEncounter.State.LOADING?"BOSS — Đang nạp máu":"BOSS",x,y-6);
        } else if(encounter.state()==AirBossEncounter.State.DRAINING) {
            g.setFont(new Font("SansSerif",Font.BOLD,18));g.setColor(Color.WHITE);
            g.drawString("Chuẩn bị gặp boss — chờ vật cản đi hết",c.integer("boss.hud.x"),c.integer("boss.hud.y"));
        }
        if(encounter.active()) {
            int x=c.integer("boss.cooldown.x"),y=c.integer("boss.cooldown.y"),w=c.integer("boss.cooldown.width"),h=c.integer("boss.cooldown.height");
            g.setColor(new Color(40,45,60));g.fillRect(x,y,w,h);
            g.setColor(encounter.cooldownFraction()>=1?Color.GREEN:Color.ORANGE);
            g.fillRect(x,y,(int)(w*encounter.cooldownFraction()),h);
            g.setFont(new Font("SansSerif",Font.BOLD,13));g.setColor(Color.WHITE);g.drawString("E: tên lửa",x,y-5);
        }
        if(level.player().shielded()) {
            g.setColor(c.color("boss.shield.color"));g.setFont(new Font("SansSerif",Font.BOLD,16));
            g.drawString(String.format("Khiên: %.1f s",level.player().shieldRemaining()),520,55);
        }
    }
}
