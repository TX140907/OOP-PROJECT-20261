package game.ui;

import game.engine.Game;
import game.engine.GameState;
import game.save.SaveData;
import game.save.SaveRepository;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Giao diện menu chỉ gửi lệnh cho Game; đọc ô lưu khi mở danh sách, không đọc theo mỗi frame. */
final class MenuOverlay extends JPanel {
    private enum Screen { HOME, PAUSE, LOAD, SAVE, OVERWRITE, END }
    private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
    private final Game game;
    private final SaveRepository saves;
    private final Runnable quit;
    private GameState observedState;
    private Screen screen;
    private String title="",description="",message="";
    private boolean error;
    MenuOverlay(Game game,SaveRepository saves,Runnable quit) {
        this.game=game;this.saves=saves;this.quit=quit;
        setLayout(null);setOpaque(false);setBounds(0,0,1000,700);refresh();
    }
    void refresh() {
        if(game.state()==observedState)return;
        observedState=game.state();message="";error=false;
        switch(observedState) {
            case MAIN_MENU -> home();
            case PAUSED -> pause("");
            case GAME_OVER,LEVEL_COMPLETE,GAME_COMPLETE -> end();
            default -> {setVisible(false);removeAll();}
        }
    }
    /** Esc ở màn chọn ô/ghi đè quay về một bước, không vô tình tiếp tục gameplay. */
    boolean escape() {
        if(!isVisible())return false;
        switch(screen) {
            case LOAD -> home();
            case SAVE -> pause("");
            case OVERWRITE -> slots(true);
            case HOME,END -> { }
            default -> {return false;}
        }
        return true;
    }
    private void begin(Screen next,String heading,String subtitle) {
        screen=next;title=heading;description=subtitle;message="";error=false;
        removeAll();setVisible(true);revalidate();repaint();
    }
    private void button(String label,int y,boolean primary,boolean danger,Runnable action) {
        MenuButton button=new MenuButton(label,"",primary,danger);
        button.setBounds(324,y,352,54);
        button.addActionListener(event->{
            try {action.run();}
            catch(RuntimeException e) {fail("Không thể mở màn chơi. Kiểm tra lại cấu hình của màn.");}
            refresh();
        });
        add(button);
    }
    private void home() {
        begin(Screen.HOME,"HUST EXAM RUN","Đường đến phòng thi còn nhiều thử thách.");
        button("Vào game",286,true,false,game::startNewGame);
        button("Chơi màn đã lưu",354,false,false,()->slots(false));
        button("Thoát game",422,false,true,quit);
    }
    private void pause(String notice) {
        begin(Screen.PAUSE,"Tạm dừng",game.level().config().name());
        button("Tiếp tục chơi",234,true,false,game::togglePause);
        button("Chơi lại màn",299,false,false,game::restart);
        button("Lưu game",364,false,false,()->slots(true));
        button("Ra menu",429,false,false,game::openMainMenu);
        button("Thoát game",494,false,true,quit);
        message=notice;
    }
    private void end() {
        String heading=switch(game.state()) {
            case GAME_OVER -> "Thử lại nhé!";
            case GAME_COMPLETE -> "Hoàn thành hành trình!";
            default -> "Hoàn thành màn!";
        };
        begin(Screen.END,heading,game.level().config().name());
        button("Chơi lại màn",286,true,false,game::restart);
        button("Ra menu",354,false,false,game::openMainMenu);
        button("Thoát game",422,false,true,quit);
    }
    private void slots(boolean writing) {
        begin(writing?Screen.SAVE:Screen.LOAD,writing?"Chọn ô lưu":"Màn đã lưu",
                "Tải bản lưu sẽ bắt đầu lại từ đầu màn.");
        for(SaveRepository.Slot slot:saves.slots()) {
            SaveData data=slot.data();
            String label="Ô "+slot.number()+" · "+(slot.empty()?"Trống":data==null?"Không đọc được":data.levelName());
            String detail=slot.empty()?(writing?"Lưu màn hiện tại vào ô này":"Chưa có bản lưu")
                    :data==null?"Bản lưu lỗi; có thể ghi đè từ menu lưu game"
                    :TIME.format(data.savedAt())+" · "+(data.checkpoint()==SaveData.Checkpoint.LEVEL_START?"Đầu màn":"Đầu hội thoại");
            MenuButton card=new MenuButton(label,detail,false,false);
            card.setBounds(274,222+(slot.number()-1)*103,452,86);
            card.setEnabled(writing || (data!=null && data.checkpoint()==SaveData.Checkpoint.LEVEL_START));
            card.addActionListener(event->{
                if(writing) {
                    // Đọc lại lúc bấm để không ghi đè im lặng nếu dữ liệu vừa thay đổi.
                    if(saves.read(slot.number()).occupied())overwrite(slot.number());else save(slot.number());
                } else load(slot.number());
                refresh();
            });
            add(card);
        }
        button("Quay lại",552,false,false,()->{if(writing)pause("");else home();});
        revalidate();repaint();
    }
    private void overwrite(int slot) {
        begin(Screen.OVERWRITE,"Ghi đè ô "+slot+"?","Bản lưu cũ trong ô này sẽ được thay thế.");
        button("Xác nhận ghi đè",330,true,false,()->save(slot));
        button("Hủy",398,false,false,()->slots(true));
    }
    private void save(int slot) {
        try {
            saves.write(slot,game.createSave());
            pause("Đã lưu ô "+slot+". Khi tải sẽ bắt đầu từ đầu màn.");
        } catch(IOException | RuntimeException e) {fail("Không lưu được. Kiểm tra quyền ghi thư mục lưu game.");}
    }
    private void load(int slot) {
        SaveRepository.Slot latest=saves.read(slot);
        if(latest.data()==null) {slots(false);fail(latest.empty()?"Ô lưu này đang trống.":latest.error());return;}
        try {game.loadSave(latest.data());}
        catch(RuntimeException e) {fail("Không tải được màn đã lưu. Kiểm tra bản lưu và cấu hình màn.");}
    }
    private void fail(String text) {message=text;error=true;repaint();}
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if(game.state()==GameState.MAIN_MENU) {
                g.setPaint(new GradientPaint(0,0,new Color(12,23,43),1000,700,new Color(27,75,78)));
                g.fillRect(0,0,1000,700);
                g.setColor(new Color(104,228,203,18));
                for(int i=0;i<7;i++)g.fillRoundRect(74+i*147,-180+i*35,30,1050,25,25);
                g.setColor(new Color(57,224,184,45));g.fillOval(790,490,300,300);
                g.setColor(new Color(250,197,90,35));g.fillOval(-120,-150,340,340);
            } else {g.setColor(new Color(5,12,24,205));g.fillRect(0,0,1000,700);}
            g.setColor(new Color(0,0,0,55));g.fillRoundRect(238,81,540,570,30,30);
            g.setPaint(new GradientPaint(230,70,new Color(23,39,58),770,640,new Color(14,28,45)));
            g.fill(new RoundRectangle2D.Double(230,70,540,570,28,28));
            g.setColor(new Color(80,131,145,160));g.setStroke(new BasicStroke(1));g.drawRoundRect(230,70,540,570,28,28);
            g.setColor(new Color(70,214,181));g.fillRoundRect(472,96,56,5,5,5);
            center(g,"HUST · ĐƯỜNG ĐẾN PHÒNG THI",124,11,new Color(119,180,187),Font.BOLD);
            center(g,title,171,screen==Screen.HOME?32:30,new Color(240,248,251),Font.BOLD);
            center(g,description,201,14,new Color(161,185,201),Font.PLAIN);
            String footer=message.isEmpty()?switch(screen) {
                case HOME -> "A / D hoặc ← / → để di chuyển · Esc để tạm dừng";
                case PAUSE -> "Esc để tiếp tục · Lưu game bằng ba ô độc lập";
                case LOAD,SAVE -> "3 ô lưu · Mỗi ô giữ một mốc bắt đầu màn";
                default -> "";
            }:message;
            center(g,footer,624,12,error?new Color(255,159,159):new Color(136,208,189),Font.PLAIN);
        } finally {g.dispose();}
    }
    private void center(Graphics2D g,String text,int y,int size,Color color,int style) {
        g.setFont(new Font("SansSerif",style,size));g.setColor(color);
        while(g.getFontMetrics().stringWidth(text)>500 && size>10)g.setFont(new Font("SansSerif",style,--size));
        g.drawString(text,(1000-g.getFontMetrics().stringWidth(text))/2,y);
    }
}
