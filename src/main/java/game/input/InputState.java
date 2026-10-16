package game.input;
import java.util.HashSet;
import java.util.Set;
import java.awt.event.KeyEvent;
public final class InputState {
    private final Set<Integer> held=new HashSet<>();
    public boolean press(int key) { return held.add(key); }
    public void release(int key) { held.remove(key); }
    public boolean firing() { return held.contains(KeyEvent.VK_E); }
    public int direction() {
        boolean left=held.contains(KeyEvent.VK_A)||held.contains(KeyEvent.VK_LEFT);
        boolean right=held.contains(KeyEvent.VK_D)||held.contains(KeyEvent.VK_RIGHT);
        return (right?1:0)-(left?1:0);
    }
    public void clear() { held.clear(); }
}
