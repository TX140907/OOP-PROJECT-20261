package game.logic.event;

import game.config.ConfigValues;
import game.level.BaseLevel;

/** Nắng gắt xen khoảng dịu theo thời gian chơi; giữ tiếp tế khẩn cấp ngay cả khi đang nắng. */
final class HeatCycleEvent implements LevelEvent {
    private final BaseLevel world;
    private final ConfigValues config;
    private boolean started;
    private double elapsed;
    HeatCycleEvent(BaseLevel world) {this.world=world;config=world.config().values();}
    @Override public void update(double dt) {
        if(!started) {
            if(world.progress().fraction()*world.config().duration()+1e-9<config.number("event.startSeconds",6))return;
            started=true;elapsed=0;
        } else elapsed+=dt;
    }
    private boolean warning() {return !started || elapsed<config.number("event.warningSeconds",1.2);}
    private double hotSeconds() {return config.number("event.heat.hotSeconds",5);}
    private double cycleTime() {
        return Math.max(0,elapsed-config.number("event.warningSeconds",1.2))
                %(hotSeconds()+config.number("event.heat.clearSeconds",3));
    }
    @Override public double glareStrength() {
        if(warning())return 0;
        double phase=cycleTime(),hot=hotSeconds();
        if(phase>=hot)return 0;
        double fade=Math.min(config.number("event.heat.fadeSeconds",.6),hot/2);
        return fade<=0?1:Math.max(0,Math.min(1,Math.min(phase/fade,(hot-phase)/fade)));
    }
    @Override public double spawnMultiplier() {
        return 1+(config.number("event.heat.hotSpawnMultiplier",.75)-1)*glareStrength();
    }
    @Override public boolean energyWindow() {return warning() || cycleTime()>=hotSeconds();}
    @Override public String message() {
        if(!started)return "";
        if(warning())return "Nắng sắp gắt – chú ý đường và năng lượng";
        return cycleTime()<hotSeconds()?"Nắng gắt – vật cản khó thấy từ xa":"Dịu nắng – tìm thanh năng lượng";
    }
}
