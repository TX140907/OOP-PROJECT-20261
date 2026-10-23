public class main {
    static {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            if (System.getProperty("sun.java2d.d3d") == null) {
                System.setProperty("sun.java2d.d3d", "true");
            }
            if (System.getProperty("sun.java2d.transaccel") == null) {
                System.setProperty("sun.java2d.transaccel", "true");
            }
            if (System.getProperty("sun.java2d.ddforcevram") == null) {
                System.setProperty("sun.java2d.ddforcevram", "true");
            }
            if (System.getProperty("sun.java2d.noddraw") == null) {
                System.setProperty("sun.java2d.noddraw", "false");
            }
            if (System.getProperty("sun.java2d.opengl") == null) {
                System.setProperty("sun.java2d.opengl", "false");
            }
            if (System.getProperty("sun.awt.noerasebackground") == null) {
                System.setProperty("sun.awt.noerasebackground", "true");
            }
        }
    }

    public static void main(String[] args) {
        sanbaka.SanbakaGame.main(args);
    }
}
