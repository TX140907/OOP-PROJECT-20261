package game.rendering;

import game.config.GameConfig;
import game.level.BaseLevel;
import java.awt.*;
import java.awt.geom.Line2D;

/** Nền băng khác màu, các vết nứt trôi cùng mặt đường; không tạo collider giả. */
public final class IceRoadRenderer {
    public void draw(Graphics2D graphics,BaseLevel level) {
        int lane=level.event().iceLane();
        if(lane<0)return;
        var c=level.config().values();
        double left=GameConfig.ROAD_LEFT+lane*GameConfig.LANE_WIDTH;
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.clipRect((int)left+4,0,GameConfig.LANE_WIDTH-8,GameConfig.HEIGHT);
            g.setComposite(AlphaComposite.SrcOver.derive((float)c.number("event.ice.roadAlpha",.85)));
            g.setColor(Color.decode(c.text("event.ice.roadColor","#9bd7ff")));
            g.fillRect((int)left,0,GameConfig.LANE_WIDTH,GameConfig.HEIGHT);
            g.setComposite(AlphaComposite.SrcOver);
            double spacing=c.number("event.ice.detailSpacing",72),offset=level.scroll()%spacing;
            for(int row=-1;row*spacing+offset<GameConfig.HEIGHT;row++) {
                double y=row*spacing+offset;
                double x=left+GameConfig.LANE_WIDTH*(row%2==0?.3:.65);
                g.setStroke(new BasicStroke(2));
                g.setColor(Color.decode(c.text("event.ice.crackColor","#4a91bd")));
                g.draw(new Line2D.Double(x-25,y-18,x,y));
                g.draw(new Line2D.Double(x,y,x-10,y+24));
                g.draw(new Line2D.Double(x,y,x+36,y+8));
                g.setColor(Color.decode(c.text("event.ice.edgeColor","#e8faff")));
                g.draw(new Line2D.Double(left+18,y+28,left+70,y+11));
                g.draw(new Line2D.Double(left+160,y+44,left+211,y+29));
            }
            g.setColor(Color.decode(c.text("event.ice.edgeColor","#e8faff")));
            g.drawLine((int)left+5,0,(int)left+5,GameConfig.HEIGHT);
            g.drawLine((int)left+GameConfig.LANE_WIDTH-6,0,(int)left+GameConfig.LANE_WIDTH-6,GameConfig.HEIGHT);
        } finally {g.dispose();}
    }
}
