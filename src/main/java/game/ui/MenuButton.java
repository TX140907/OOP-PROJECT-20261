package game.ui;

import javax.swing.JButton;
import java.awt.*;

/** Nút dùng chung cho menu và ô lưu: hover, nhấn, focus bàn phím và trạng thái vô hiệu. */
final class MenuButton extends JButton {
    private final String subtitle;
    private final boolean primary,danger;
    MenuButton(String text,String subtitle,boolean primary,boolean danger) {
        super(text);this.subtitle=subtitle;this.primary=primary;this.danger=danger;
        setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setOpaque(false);
        setRolloverEnabled(true);setFont(new Font("SansSerif",Font.BOLD,17));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleDescription(subtitle);
    }
    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);setCursor(Cursor.getPredefinedCursor(enabled?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover=isEnabled() && getModel().isRollover(),pressed=getModel().isPressed();
            Color fill=!isEnabled()?new Color(31,42,56):danger?(hover?new Color(120,54,65):new Color(70,38,51))
                    :primary?(hover?new Color(69,224,194):new Color(44,192,163))
                    :hover?new Color(51,78,95):new Color(32,51,69);
            if(pressed && isEnabled())fill=fill.darker();
            int y=pressed?3:0,height=getHeight()-4;
            g.setColor(new Color(0,0,0,60));g.fillRoundRect(0,4,getWidth(),height,16,16);
            g.setColor(fill);g.fillRoundRect(0,y,getWidth()-1,height,16,16);
            g.setColor(isFocusOwner()?new Color(157,255,233):hover?new Color(111,199,198):new Color(78,99,118));
            g.setStroke(new BasicStroke(isFocusOwner()?2:1));g.drawRoundRect(1,y+1,getWidth()-3,height-2,16,16);
            g.setFont(getFont());
            g.setColor(!isEnabled()?new Color(112,128,144):primary?new Color(12,41,41):new Color(237,246,250));
            if(subtitle.isEmpty()) {
                FontMetrics metrics=g.getFontMetrics();
                g.drawString(getText(),(getWidth()-metrics.stringWidth(getText()))/2,y+(height+metrics.getAscent()-metrics.getDescent())/2);
            } else {
                g.drawString(getText(),22,y+30);
                g.setFont(new Font("SansSerif",Font.PLAIN,13));
                g.setColor(isEnabled()?new Color(182,204,216):new Color(112,128,144));
                g.drawString(fit(subtitle,g.getFontMetrics(),getWidth()-44),22,y+54);
            }
        } finally {g.dispose();}
    }
    private static String fit(String text,FontMetrics metrics,int width) {
        if(metrics.stringWidth(text)<=width)return text;
        int end=text.length();
        while(end>0 && metrics.stringWidth(text.substring(0,end)+"…")>width)end--;
        return text.substring(0,end)+"…";
    }
}
