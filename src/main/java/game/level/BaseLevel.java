package game.level;

import game.config.GameConfig;
import game.logic.collision.*;
import game.logic.effect.*;
import game.logic.progress.ProgressManager;
import game.logic.spawn.*;
import game.logic.boss.AirBossEncounter;
import game.logic.event.LevelEvent;
import game.logic.event.LevelEvents;
import game.model.*;
import game.model.obstacle.*;
import game.level.DayConfig.ObstacleSpec;
import java.util.*;

/** Owns a single session of a level. Restart/transition construct a fresh session. */
public abstract class BaseLevel implements Level {
    private final DayConfig config;
    private final Player player;
    private final List<Obstacle> obstacles=new ArrayList<>();
    private final EffectManager effects=new EffectManager();
    private final CollisionManager collisions=new CollisionManager();
    private final ProgressManager progress;
    private final EnergySystem energy;
    private final SafeSpawnPolicy safety=new SafeSpawnPolicy();
    private final ObstacleSpawner spawner;
    private final HealthPickupSpawner healthPickups;
    private final AirBossEncounter bossEncounter;
    private final LevelEvent event;
    private final Random random;
    private double scroll, pickupTime;
    private int currentDirection;
    private final Map<Double,Double> travelForecast=new HashMap<>();
    private long forecastRevision=-1;
    protected BaseLevel(DayConfig config) {
        this.config=config;
        player=config.helicopter()?new Helicopter(config):new Player(config);
        progress=new ProgressManager(config.duration()); energy=new EnergySystem(config.values());
        effects.setEnergyFraction(energy.fraction());
        String seed=config.values().text("spawn.randomSeed");
        random=seed.equals("random")?new Random():new Random(Long.parseLong(seed));
        spawner=new ObstacleSpawner(this,random);
        healthPickups=new HealthPickupSpawner(this,random,safety);
        bossEncounter=Boolean.parseBoolean(config.values().text("boss.enabled","false"))?new AirBossEncounter(this,random):null;
        event=LevelEvents.create(this,random);
    }
    @Override public final void update(double dt,int direction) {
        if (player.dead() || complete()) return;
        currentDirection=direction;
        effects.setEnergyFraction(energy.fraction());
        effects.setIceRoad(event.iceLane(),event.iceRoadRemainingSeconds());
        effects.update(dt);
        player.update(dt,direction,effects);
        for (Obstacle obstacle:List.copyOf(obstacles)) if (!obstacle.removed()) obstacle.update(dt,this);
        if(bossEncounter!=null) bossEncounter.updateProjectiles(dt);
        collisions.update(this);
        if (effects.active(EffectManager.Type.FREEZE)) currentDirection=0;
        obstacles.removeIf(Obstacle::removed);
        energy.update(dt);
        effects.setEnergyFraction(energy.fraction());
        if (energy.empty()) player.kill();
        if (player.dead()) return;
        var c=config.values();
        progress.update(dt,c.number("progress.normalMultiplier"),c.number("progress.hitMultiplier"),effects.progressMultiplier(c));
        scroll+=c.number("level.scrollSpeed")*travelSecondsAhead(dt);
        if(bossEncounter!=null) bossEncounter.update(dt);
        if (complete()) return;
        event.update(dt);
        if(bossEncounter==null || bossEncounter.normalPhase()) {
            spawner.update(dt,this);
            updatePickups(dt);
            healthPickups.update(dt,this);
        }
    }
    private void updatePickups(double dt) {
        if (!energy.enabled()) return;
        pickupTime+=dt;
        var c=config.values();
        boolean urgent=energy.value()<=c.number("energy.minimumReserve")+c.number("energy.drainPerSecond")*c.number("energy.pickupTravelSeconds");
        if (!urgent && (pickupTime<c.number("energy.pickupIntervalSeconds") || !event.energyWindow())) return;
        if (obstacles.stream().anyMatch(o->o.spec().type()==ObstacleType.ENERGY_PICKUP)) return;
        double w=c.number("energy.pickupWidth"),h=c.number("energy.pickupHeight");
        ObstacleSpec spec=new ObstacleSpec(ObstacleType.ENERGY_PICKUP,0,0,w,h,0,0,CollisionKind.ENERGY,true,c.text("sprite.energyPickup",""));
        double travel=urgent?c.number("energy.urgentTravelSeconds"):c.number("energy.pickupTravelSeconds");
        double speed=c.number("level.scrollSpeed");
        for (int attempt=0;attempt<c.integer("spawn.attempts");attempt++) {
            double x=attempt==0?player.hitbox().getCenterX()-w/2:GameConfig.laneX(random.nextInt(3),w);
            double y=player.hitbox().getY()-h/2-speed*travelSecondsAhead(travel);
            EnergyPickup pickup=new EnergyPickup(spec,x,y,speed);
            pickup.attachTo(this);
            if (safety.canCollect(this,pickup,travel)) { addObstacle(pickup); pickupTime=0; return; }
        }
        // Retry next tick instead of letting a missed RNG roll decide energy starvation.
    }
    public final void chooseSharkAttack(Shark shark) {
        double oldX=shark.x(),oldY=shark.y();
        List<Integer> lanes=new ArrayList<>(List.of(0,1,2)); Collections.shuffle(lanes,random);
        for (int lane:lanes) {
            double x=GameConfig.laneX(lane,shark.width());
            double y=player.hitbox().getCenterY()-shark.height()/2;
            shark.proposeTarget(x,y); shark.beginWarning();
            if (safety.accepts(this,shark)) return;
            shark.cancelTarget(oldX,oldY);
        }
    }
    public final boolean fireMissile(EnemyPlane plane) {
        var c=config.values();
        if (obstacles.size()>=c.integer("spawn.maxActiveObstacles")) return false;
        List<Integer> sides=new ArrayList<>(List.of(-1,1));Collections.shuffle(sides,random);
        ObstacleSpec spec=new ObstacleSpec(ObstacleType.MISSILE,0,0,c.number("missile.width"),c.number("missile.height"),
                0,0,CollisionKind.valueOf(c.text("missile.collisionType")),true,c.text("sprite.missile",""));
        for (int side:sides) {
            double center=Math.max(config.minX()+spec.width()/2,Math.min(config.maxX()+player.width()-spec.width()/2,
                    plane.hitbox().getCenterX()+side*c.number("missile.sideOffset")));
            Missile missile=new Missile(spec,plane,center-spec.width()/2,plane.y()+plane.height(),
                    player.hitbox().getCenterY(),c.number("player.maxSpeed")*c.number("missile.speedMultiplier"),
                    c.number("missile.warningSeconds"));
            if (safety.accepts(this,missile)) {addObstacle(missile);return true;}
        }
        return false;
    }
    public final void addObstacle(Obstacle obstacle) { Objects.requireNonNull(obstacle).attachTo(this);obstacles.add(obstacle); }
    /** Kiểm tra cả đội hình trước khi giữ lại; thất bại thì hoàn tác toàn bộ nhóm. */
    public final boolean trySpawnGroup(List<? extends Obstacle> group) {
        if(obstacles.size()+group.size()>config.values().integer("spawn.maxActiveObstacles"))return false;
        var placement=PlaneSpawnPlacement.place(this,group);
        if(placement.isEmpty())return false;
        List<Obstacle> added=new ArrayList<>();
        for(Obstacle obstacle:placement.get()) {
            if(!safety.accepts(this,obstacle)) {obstacles.removeAll(added);return false;}
            addObstacle(obstacle);added.add(obstacle);
        }
        return true;
    }
    public final boolean spawnHealthPickup() { return healthPickups.trySpawn(this); }
    public final boolean allowsPlaneFire() { return bossEncounter==null || bossEncounter.normalPhase() || bossEncounter.active(); }
    public final void fire() { if(bossEncounter!=null)bossEncounter.tryFire(); }
    public final AirBossEncounter bossEncounter() { return bossEncounter; }
    public final LevelEvent event() { return event; }
    /** Integrate travel modifiers for world movement and the same trajectory forecast used by safety. */
    public final double travelSecondsAhead(double seconds) {
        if (!config.values().bool("energy.enabled") && !effects.active(EffectManager.Type.DRAIN)
                && !effects.active(EffectManager.Type.SHARK_SLOW)
                && !effects.active(EffectManager.Type.SLIDE)
                && !effects.active(EffectManager.Type.FREEZE) && !effects.active(EffectManager.Type.SNOWBALL_SLOW)) return seconds;
        if (forecastRevision!=effects.revision()) {travelForecast.clear();forecastRevision=effects.revision();}
        Double cached=travelForecast.get(seconds);
        if (cached!=null) return cached;
        double total=0;
        for (double t=0;t<seconds-1e-10;t+=GameConfig.STEP) {
            double dt=Math.min(GameConfig.STEP,seconds-t);
            total+=dt*effects.progressMultiplier(config.values(),t);
        }
        travelForecast.put(seconds,total);
        return total;
    }
    @Override public final DayConfig config() { return config; }
    @Override public final Player player() { return player; }
    @Override public final List<Obstacle> obstacles() { return Collections.unmodifiableList(obstacles); }
    @Override public final boolean complete() { return bossEncounter==null?progress.complete():bossEncounter.complete(); }
    public final EffectManager effects() { return effects; }
    public final ProgressManager progress() { return progress; }
    public final EnergySystem energy() { return energy; }
    public final SafeSpawnPolicy safety() { return safety; }
    public final ObstacleSpawner spawner() { return spawner; }
    public final double scroll() { return scroll; }
    public final int currentDirection() { return currentDirection; }
}
