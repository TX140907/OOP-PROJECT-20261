public class Obstacle {
    private int posX;
    private int posY;
    private int width;
    private int height;
    public Obstacle(int initX, int initY, int width, int height) {
        this.posX = initX;
        this.posY = initY;
        this.width = width;
        this.height = height;
    }
    public void move(int dir, int val) {
        if(dir == 0) this.posX += val;
        if(dir == 1) this.posY -= val;
        if(dir == 2) this.posX -= val;
        if(dir == 3) this.posY += val;
    }
    public int getposX() {
        return this.posX;
    }
    public int getPosY() {
        return this.posY;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
}
