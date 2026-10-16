package game.logic.collision;

import game.level.BaseLevel;
import game.model.obstacle.Obstacle;
import java.util.*;

public final class CollisionManager {
    private final Map<Obstacle,CollisionEffect> contacts=new IdentityHashMap<>();
    public void update(BaseLevel world) {
        List<Obstacle> touching=world.obstacles().stream().filter(o->!o.removed() && o.activeAt(0)
                && o.collisionShape().intersects(world.player().hitbox())).toList();
        if(world.player().shielded()) {
            if(touching.stream().anyMatch(o->o.harmfulAt(0))) world.player().phaseThrough();
            touching=touching.stream().filter(o->!o.harmfulAt(0)).toList();
        }
        // Giữ loại va chạm từ lần chạm đầu tới khi tách ra, tránh quệt cạnh rồi bị tính thành đâm đầu ở khung sau.
        Map<Obstacle,CollisionEffect> effects=new IdentityHashMap<>();
        for(Obstacle obstacle:touching) {
            CollisionEffect effect=contacts.get(obstacle);
            effects.put(obstacle,effect==null?obstacle.contactEffect(world):effect);
        }
        // Va chạm chí mạng thực sự vẫn được ưu tiên hơn miễn sát thương thường và hoàn thành màn.
        for (Obstacle o:touching) if (effects.get(o).kind()==CollisionKind.LETHAL) { effects.get(o).apply(world); return; }
        // Máy bay được xét trước sát thương nhẹ cùng khung hình để vẫn trừ đủ máu.
        touching=touching.stream().sorted(Comparator.comparingInt(o->effects.get(o).kind()==CollisionKind.PLANE_DAMAGE?0:1)).toList();
        for (Obstacle o:touching) if (!contacts.containsKey(o)) {
            effects.get(o).apply(world);
            if (o.spec().consumed()) o.remove();
        } else if(effects.get(o).kind()==CollisionKind.SLIDE)effects.get(o).apply(world);
        contacts.clear(); contacts.putAll(effects);
    }
    public void clear() { contacts.clear(); }
}
