package game.rendering;

import game.level.BaseLevel;
import game.model.Player;
import game.model.obstacle.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/** Presentation switches on visual identity, never controls gameplay or collision effects. */
public final class EntityRenderer {
    private final AssetLoader assets;
    public EntityRenderer(AssetLoader assets) { this.assets=assets; }
    public void player(Graphics2D g,BaseLevel level) {
        Composite previous=g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(level.player().opacity()));
        try { drawPlayer(g,level); } finally {g.setComposite(previous);}
        if(level.player().shielded()) {
            var p=level.player();g.setColor(level.config().values().color("boss.shield.color"));
            g.drawOval((int)p.x()-4,(int)p.y()-4,(int)p.width()+8,(int)p.height()+8);
        }
    }
    private void drawPlayer(Graphics2D g,BaseLevel level) {
        Player p=level.player();
        if (!p.visible()) return;
        int x=(int)p.x(),y=(int)p.y(),w=(int)p.width(),h=(int)p.height();
        BufferedImage image=assets.image(level.config().values().text("sprite.player"));
        if (image!=null) {g.drawImage(image,x,y,w,h,null);return;}
        g.setColor(new Color(250,110,100));
        if (level.config().helicopter()) {
            g.fillRoundRect(x,y+h/4,w,h/2,24,24);g.setColor(Color.BLACK);
            g.drawLine(x-8,y+h/5,x+w+8,y+h/5);g.drawLine(x+w/2,y,x+w/2,y+h/3);
            g.drawLine(x+w/6,y+h*4/5,x+w*5/6,y+h*4/5);
        } else {
            g.fillRoundRect(x,y,w,h,16,16);g.setColor(new Color(255,220,165));g.fillOval(x+w/4,y+8,w/2,h/3);
            g.setColor(new Color(60,65,90));g.fillRect(x+w/4,y+h*2/3,w/2,h/4);
        }
    }
    public void obstacle(Graphics2D g,Obstacle o,BaseLevel level) {
        if (o.removed()) return;
        if(o instanceof EnemyPlane plane && plane.awaitingWarning())return;
        if(o instanceof EnemyPlane plane && plane.warning()) {reversePlaneWarning(g,plane,level);return;}
        if (o instanceof Shark shark) { shark(g,shark,level);return; }
        if (o instanceof Cactus cactus) {cactus(g,cactus);return;}
        if (o instanceof Missile missile && missile.warning()) { missileWarning(g,missile,level);return; }
        if (o instanceof BossProjectile projectile && projectile.warning())return;
        if (o instanceof Dog dog && dog.state()==Dog.State.BARKING) dogWarning(g,dog,level);
        int x=(int)o.x(),y=(int)o.y(),w=(int)o.width(),h=(int)o.height();
        BufferedImage image=assets.image(o.spec().sprite());
        if (image!=null) {
            if(o instanceof EnemyPlane plane && plane.reverseFlight()) {
                Graphics2D rotated=(Graphics2D)g.create();rotated.rotate(Math.PI,x+w/2.0,y+h/2.0);
                rotated.drawImage(image,x,y,w,h,null);rotated.dispose();
            } else if(o instanceof Tumbleweed weed) {
                Graphics2D rotated=(Graphics2D)g.create();rotated.rotate(weed.rollingRadians(),x+w/2.0,y+h/2.0);
                rotated.drawImage(image,x,y,w,h,null);rotated.dispose();
            } else g.drawImage(image,x,y,w,h,null);
            planeHitFlash(g,o);return;
        }
        Color color=switch (o.spec().type()) {
            case VEHICLE -> new Color(60,180,120); case BICYCLE -> new Color(100,215,180);
            case BUS -> new Color(230,150,50);case BARRIER -> new Color(245,145,70);
            case DRAIN -> new Color(20,40,70);case FLOATING_CRATE -> new Color(150,100,65);
            case ICE_PATCH -> new Color(100,220,250);case ICE_HOLE -> new Color(70,140,180);case SNOWBALL, SNOWMAN -> Color.WHITE;
            case TUMBLEWEED -> new Color(185,140,65);
            case CACTUS -> new Color(50,150,75);case TRAFFIC_JAM_VEHICLE -> new Color(145,145,155);
            case DOG -> new Color(160,95,55);case BIRD -> new Color(240,215,130);
            case ENERGY_PICKUP -> new Color(80,240,120);case HEALTH_PICKUP -> new Color(225,65,80);default -> Color.GRAY;
        };
        g.setColor(color);
        switch(o.spec().type()) {
            case HEALTH_PICKUP -> {
                g.fillRoundRect(x,y,w,h,10,10);g.setColor(Color.WHITE);
                g.fillRect(x+w*2/5,y+h/5,Math.max(1,w/5),h*3/5);
                g.fillRect(x+w/5,y+h*2/5,w*3/5,Math.max(1,h/5));
            }
            case SHIELD_PICKUP -> {
                g.setColor(level.config().values().color("boss.shield.color"));g.fillRoundRect(x,y,w,h,10,10);
                g.setColor(Color.WHITE);g.drawPolygon(new int[]{x+w/4,x+w*3/4,x+w*3/4,x+w/2,x+w/4},
                        new int[]{y+h/5,y+h/5,y+h*3/5,y+h*4/5,y+h*3/5},5);
            }
            case BOSS_PROJECTILE -> {g.setColor(level.config().values().color("boss.projectile.color"));g.fillOval(x,y,w,h);}
            case ICE_PATCH,DRAIN -> g.fillOval(x,y,w,h);
            case ICE_HOLE -> {
                g.fillOval(x,y,w,h);
                g.setColor(o instanceof IceHole hole && hole.collapsed()?new Color(15,35,60):new Color(35,70,105));
                g.fillOval(x+w/6,y+h/5,w*2/3,h*3/5);
                g.setColor(new Color(170,230,250));
                g.drawLine(x+w/2,y,x+w/3,y+h/3);g.drawLine(x+w/3,y+h/3,x,y+h/2);
                g.drawLine(x+w*2/3,y+h*2/3,x+w,y+h);g.drawLine(x+w*2/3,y+h/3,x+w,y+h/5);
            }
            case TUMBLEWEED -> {
                Graphics2D rolling=(Graphics2D)g.create();
                rolling.rotate(o instanceof Tumbleweed weed?weed.rollingRadians():0,x+w/2.0,y+h/2.0);
                rolling.fillOval(x,y,w,h);rolling.setColor(new Color(110,75,30));
                rolling.drawOval(x+w/8,y+h/8,w*3/4,h*3/4);
                rolling.drawOval(x+w/3,y,w/3,h);
                rolling.drawLine(x,y+h/2,x+w,y+h/2);rolling.drawLine(x+w/2,y,x+w/2,y+h);
                rolling.dispose();
            }
            case SNOWBALL -> { g.fillOval(x,y,w,h);g.setColor(Color.CYAN);g.drawArc(x+8,y+8,w-16,h-16,10,190); }
            case SNOWMAN -> { g.fillOval(x+w/4,y,w/2,h/3);g.fillOval(x,y+h/4,w,h*3/4); }
            case BIRD -> { g.fillPolygon(new int[]{x,x+w/2,x+w,x+w/2},new int[]{y+h/2,y+h/4,y+h/2,y+h},4); }
            case ENEMY_PLANE -> {
                g.setColor(new Color(175,70,90));
                g.fill(o.collisionShape());
            }
            case MISSILE -> {
                g.setColor(new Color(245,100,40));g.fillOval(x,y,w,h);
                g.setColor(Color.YELLOW);g.fillOval(x+w/4,y-h/4,w/2,h/3);
            }
            default -> g.fillRoundRect(x,y,w,h,10,10);
        }
        planeHitFlash(g,o);
        g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,11));
        String label=switch(o.spec().type()) {
            case VEHICLE -> "CAR";case BICYCLE -> "BIKE";case BUS -> "BUS";case BARRIER -> "BARRIER";
            case TRAFFIC_JAM_VEHICLE -> "PARKED";case ENERGY_PICKUP -> "+";default -> "";
        };
        g.drawString(label,x+6,y+h/2);
    }
    private void planeHitFlash(Graphics2D g,Obstacle obstacle) {
        if(obstacle instanceof EnemyPlane plane && plane.hitFlashing()) {
            g.setColor(new Color(255,255,255,150));g.fill(plane.collisionShape());
        }
    }
    private void cactus(Graphics2D graphics,Cactus cactus) {
        double x=cactus.x(),y=cactus.y(),w=cactus.width(),h=cactus.height();
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.rotate((cactus.fallsRight()?1:-1)*Math.PI/2*cactus.fallProgress(),x+w/2,y+h);
            var image=assets.image(cactus.spec().sprite());
            if(image!=null)g.drawImage(image,(int)x,(int)y,(int)w,(int)h,null);
            else {
                g.setColor(new Color(55,140,65));g.fill(cactus.standingShape());
                g.setColor(new Color(25,75,35));g.setStroke(new BasicStroke(2));g.draw(cactus.standingShape());
            }
        } finally {g.dispose();}
    }
    private void cactusFallWarning(Graphics2D g,Cactus cactus,BaseLevel level) {
        var c=level.config().values();var area=cactus.fallTarget();Color color=c.color("cactus.warningColor");
        int alpha=(int)(255*c.number("cactus.warningFillAlpha"));
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha));g.fill(area);
        g.setColor(color);g.setStroke(new BasicStroke(3));g.draw(area);
        if((int)(cactus.age()/c.number("cactus.blinkSeconds"))%2!=0)return;
        int size=Math.max(12,(int)(area.getHeight()*.7));
        g.setFont(new Font("SansSerif",Font.BOLD,size));
        int x=(int)(area.getCenterX()-g.getFontMetrics().stringWidth("!")/2.0);
        int baseline=(int)(area.getCenterY()+(g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent())/2.0);
        g.setColor(Color.BLACK);g.drawString("!",x+1,baseline+1);
        g.setColor(color);g.drawString("!",x,baseline);
    }
    private void reversePlaneWarning(Graphics2D g,EnemyPlane plane,BaseLevel level) {
        var c=level.config().values();int x=(int)plane.x(),w=(int)plane.width();
        g.setColor(new Color(255,190,40,35));g.fillRect(x,76,w,700-76);
        if((int)(plane.warningElapsedSeconds()/c.number("boss.reverse.blinkSeconds"))%2!=0)return;
        int size=c.integer("boss.reverse.warningSize"),y=c.integer("boss.reverse.warningY"),left=x+(w-size)/2;
        var image=assets.image(c.text("sprite.reversePlaneWarning",""));
        if(image!=null)g.drawImage(image,left,y,size,size,null);
        else {
            g.setColor(Color.ORANGE);g.fillRoundRect(left,y,size,size,8,8);
            g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,size/2));
            String label="↑ !";g.drawString(label,left+(size-g.getFontMetrics().stringWidth(label))/2,y+size*3/4);
        }
    }
    private void bossProjectileWarning(Graphics2D g,BossProjectile projectile,BaseLevel level) {
        var c=level.config().values();
        var area=projectile.targetArea(c.number("boss.projectile.warningSize"));
        if(projectile.aimed()) {
            Graphics2D beam=(Graphics2D)g.create();Color color=c.color("boss.waves.aimLineColor");
            beam.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)Math.round(255*c.number("boss.waves.aimLineAlpha"))));
            beam.setStroke(new BasicStroke((float)c.number("boss.waves.aimLineWidth")));
            beam.draw(new java.awt.geom.Line2D.Double(projectile.sourceX(),projectile.sourceY(),area.getCenterX(),area.getCenterY()));
            beam.dispose();
        }
        if((int)(projectile.age()/c.number("boss.projectile.blinkSeconds"))%2!=0)return;
        var image=assets.image(c.text("sprite.bossProjectileWarning",""));
        if(image!=null)g.drawImage(image,(int)area.getX(),(int)area.getY(),(int)area.getWidth(),(int)area.getHeight(),null);
        else {
            g.setColor(new Color(255,130,30,190));g.fill(area);
            g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,(int)(area.getHeight()*.75)));
            g.drawString("!",(int)area.getCenterX()-g.getFontMetrics().stringWidth("!")/2,(int)(area.getY()+area.getHeight()*.8));
        }
    }
    /** Cảnh báo dọc làn được vẽ sau nhân vật để không bị thân hoặc sprite che mất. */
    public void foregroundWarnings(Graphics2D graphics,BaseLevel level) {
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            for(Obstacle obstacle:level.obstacles())if(!obstacle.removed()) {
                if(obstacle instanceof Cactus cactus && cactus.telegraphing())cactusFallWarning(g,cactus,level);
                else if(obstacle instanceof BossProjectile projectile && projectile.warning()) {
                    if(projectile.rowStrike())bossProjectileWarning(g,projectile,level);
                    else laneWarning(g,projectile.targetArea(level.config().values().number("boss.projectile.warningSize")),projectile.age(),level);
                }
                else if(obstacle instanceof Missile missile && missile.warning()
                        && level.bossEncounter()!=null && level.bossEncounter().active())
                    laneWarning(g,missile.targetArea(level.config().values().number("missile.warningSize")),missile.age(),level);
            }
        } finally {g.dispose();}
    }
    private void laneWarning(Graphics2D g,java.awt.geom.Rectangle2D target,double age,BaseLevel level) {
        var c=level.config().values();Color color=c.color("boss.lane.warningColor");
        // Dấu đích luôn hiện; tam giác nhấp nháy nằm phía trên hàng nhân vật, đúng X của đích.
        g.setColor(color);g.setStroke(new BasicStroke(3));g.draw(target);
        if((int)(age/c.number("boss.projectile.blinkSeconds"))%2!=0)return;
        int size=c.integer("boss.lane.warningSize"),x=(int)Math.round(target.getCenterX()-size/2.0);
        int y=(int)Math.round(target.getCenterY()-level.player().height()/2-size-c.integer("boss.lane.warningOffsetY"));
        var image=assets.image(c.text("sprite.bossProjectileWarning",""));
        if(image!=null) {g.drawImage(image,x,y,size,size,null);return;}
        Polygon triangle=new Polygon(new int[]{x+size/2,x+size,x},new int[]{y,y+size,y+size},3);
        g.setColor(color);g.fill(triangle);g.setColor(Color.BLACK);g.draw(triangle);
        g.setFont(new Font("SansSerif",Font.BOLD,(int)(size*.65)));
        g.drawString("!",x+(size-g.getFontMetrics().stringWidth("!"))/2,y+(int)(size*.85));
    }
    private void dogWarning(Graphics2D g,Dog dog,BaseLevel level) {
        var c=level.config().values();var target=dog.chargeTarget();
        g.setColor(new Color(255,100,35,65));g.fill(target);
        g.setColor(Color.ORANGE);g.setStroke(new BasicStroke(2));g.draw(target);
        if ((int)(Math.max(0,dog.stateTime())/c.number("dog.warningBlinkSeconds"))%2!=0) return;
        int size=c.integer("dog.warningSize");
        int x=(int)dog.hitbox().getCenterX()-size/2,y=(int)dog.y()-size-c.integer("dog.warningOffsetY");
        var icon=assets.image(c.text("sprite.dog.warning",""));
        if (icon!=null) {g.drawImage(icon,x,y,size,size,null);return;}
        g.setColor(Color.ORANGE);g.fillRoundRect(x,y,size,size,8,8);
        g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,(int)(size*.8)));
        g.drawString("!",x+(size-g.getFontMetrics().stringWidth("!"))/2,y+(size+g.getFontMetrics().getAscent())/2-4);
    }
    private void missileWarning(Graphics2D g,Missile missile,BaseLevel level) {
        var c=level.config().values();
        if ((int)(missile.age()/c.number("missile.blinkSeconds"))%2!=0) return;
        var area=missile.targetArea(c.number("missile.warningSize"));
        var image=assets.image(c.text("sprite.missile.warning",""));
        if (image!=null) {g.drawImage(image,(int)area.getX(),(int)area.getY(),(int)area.getWidth(),(int)area.getHeight(),null);return;}
        g.setColor(new Color(255,80,45,180));g.fillOval((int)area.getX(),(int)area.getY(),(int)area.getWidth(),(int)area.getHeight());
        g.setColor(Color.YELLOW);g.setStroke(new BasicStroke(2));
        g.drawLine((int)missile.hitbox().getCenterX(),(int)missile.hitbox().getCenterY(),(int)missile.targetX(),(int)missile.targetY());
        g.setFont(new Font("SansSerif",Font.BOLD,30));g.drawString("!",(int)area.getCenterX()-5,(int)area.getCenterY()+10);
    }
    private void shark(Graphics2D g,Shark s,BaseLevel level) {
        var c=level.config().values();int x=(int)s.x(),y=(int)s.y(),w=(int)s.width(),h=(int)s.height();
        String suffix=switch(s.state()) {case FIN_VISIBLE->"fin";case DIVING->"diving";case WARNING->"warning";case ATTACK->"attack";default->"";};
        BufferedImage image=assets.image(c.text("sprite.shark."+suffix,""));
        if (image!=null) { g.drawImage(image,x,y,w,h,null);return; }
        switch (s.state()) {
            case FIN_VISIBLE -> {
                int fw=c.integer("shark.finWidth"),fh=c.integer("shark.finHeight"),cx=x+w/2,cy=y+h/2;
                g.setColor(Color.LIGHT_GRAY);g.fillPolygon(new int[]{cx-fw/2,cx+fw/2,cx+fw/4},new int[]{cy+fh/2,cy+fh/2,cy-fh/2},3);
            }
            case DIVING -> {int size=c.integer("shark.rippleSize");g.setColor(Color.WHITE);g.drawOval(x+(w-size)/2,y+(h-size)/2,size,size);}
            case WARNING -> {
                g.setColor(new Color(10,20,35,210));g.fillOval(x,y,w,h);
                if ((int)(s.stateTime()/c.number("shark.blinkSeconds"))%2==0) {
                    g.setColor(Color.ORANGE);g.setStroke(new BasicStroke(3));g.drawRect(x,y,w,h);
                    g.setFont(new Font("SansSerif",Font.BOLD,52));g.drawString("!",x+w/2-10,y+h/2+18);
                }
            }
            case ATTACK -> {g.setColor(new Color(70,85,105));g.fillOval(x,y,w,h);g.setColor(new Color(190,40,45));g.fillOval(x+w/4,y+h/3,w/2,h/2);}
            default -> { }
        }
    }
}
