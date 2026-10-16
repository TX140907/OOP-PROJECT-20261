package game.model;

import java.awt.geom.Rectangle2D;

/** Sprite geometry is independent from the collision box. Position is always top-left. */
public abstract class Entity {
    private double x, y;
    private final double width, height, hitWidth, hitHeight, offsetX, offsetY;
    protected Entity(double x, double y, double width, double height, double hitWidth, double hitHeight, double offsetX, double offsetY) {
        if (width <= 0 || height <= 0 || hitWidth <= 0 || hitHeight <= 0) throw new IllegalArgumentException("Invalid geometry");
        this.x=x; this.y=y; this.width=width; this.height=height;
        this.hitWidth=hitWidth; this.hitHeight=hitHeight; this.offsetX=offsetX; this.offsetY=offsetY;
    }
    protected final void position(double x, double y) { this.x=x; this.y=y; }
    public final double x() { return x; }
    public final double y() { return y; }
    public final double width() { return width; }
    public final double height() { return height; }
    public final Rectangle2D spriteBounds() { return new Rectangle2D.Double(x,y,width,height); }
    public final Rectangle2D hitbox() { return hitboxAt(x,y); }
    public final Rectangle2D hitboxAt(double x, double y) { return new Rectangle2D.Double(x+offsetX,y+offsetY,hitWidth,hitHeight); }
}
