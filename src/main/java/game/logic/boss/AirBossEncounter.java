package game.logic.boss;

import game.config.ConfigValues;
import game.config.GameConfig;
import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.logic.collision.CollisionKind;
import game.logic.spawn.ObstacleFactory;
import game.model.boss.AirBoss;
import game.model.obstacle.*;
import game.model.projectile.PlayerRocket;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.*;

/** Một lượt boss hoàn chỉnh; mọi đồng hồ chỉ chạy khi BaseLevel được cập nhật. */
public final class AirBossEncounter {
    public enum State { NORMAL, DRAINING, WARNING, ENTERING, LOADING, ACTIVE, EXITING, COMPLETE }
    public enum Action { PLANE_WAVES, LANE_ATTACK, REVERSE_PLANES }
    public record Explosion(double x,double y,double remaining) {}
    private final BaseLevel world;
    private final ConfigValues c;
    private final Random random;
    private final AirBoss boss;
    private final ObstacleFactory factory=new ObstacleFactory();
    private final List<PlayerRocket> rockets=new ArrayList<>();
    private final List<Explosion> explosions=new ArrayList<>();
    private State state=State.NORMAL;
    private Action action;
    private double stateTime,activeSeconds,actionTime,attackCooldown,laneCooldown,recoveryTime;
    private double healthCooldown,nextShield,rocketCooldown,waveSpeed,throwCooldown,lastRowStrikeLifetime,reverseShotCooldown,recoveryShotCooldown;
    private int lane=1,shieldSpawnCount,pendingWaveCount,lastWaveCount,pendingThrowCount,reverseWavesSpawned,openingLane;
    private int pendingReversePlaneCount,consecutiveWaveSequences,recoveryShotsFired;
    private boolean recovering,transitionPrepared,hasFired,shieldPending,throwPending,reverseShotPending;

    public AirBossEncounter(BaseLevel world,Random random) {
        this.world=world;this.c=world.config().values();this.random=random;boss=new AirBoss(c);
        healthCooldown=c.number("boss.healthPickup.firstDelaySeconds");
        nextShield=c.number("boss.shield.firstSpawnSeconds");
    }
    public void update(double dt) {
        boss.update(dt);stateTime+=dt;
        switch(state) {
            case NORMAL -> { if(world.progress().complete()) enter(State.DRAINING); }
            case DRAINING -> {
                if(hasHazards())stateTime=0;
                else if(stateTime>=c.number("boss.clearDelaySeconds"))enter(State.WARNING);
            }
            case WARNING -> {
                if(stateTime>=c.number("boss.warningSeconds")) {
                    boss.moveTo(GameConfig.laneX(1,boss.width()),c.number("boss.y"),c.number("boss.entranceSeconds"));
                    enter(State.ENTERING);
                }
            }
            case ENTERING -> { if(!boss.moving())enter(State.LOADING); }
            case LOADING -> {
                if(stateTime>=c.number("boss.healthLoadSeconds")) {enter(State.ACTIVE);chooseAction();}
            }
            case ACTIVE -> {
                if(boss.defeated()) {
                    boss.moveTo(boss.x(),-boss.height(),c.number("boss.exitSeconds"));enter(State.EXITING);
                } else {
                    activeSeconds+=dt;updateSupplies(dt);updateAction(dt);
                }
            }
            case EXITING -> {
                if(hasHazards())stateTime=0;
                else if(!boss.moving() && stateTime>=c.number("boss.finishDelaySeconds"))enter(State.COMPLETE);
            }
            case COMPLETE -> { }
        }
    }
    private void enter(State next) {state=next;stateTime=0;}
    private boolean hasHazards() {
        return world.obstacles().stream().anyMatch(o->!o.removed() && o.effect().kind()!=CollisionKind.HEAL
                && o.effect().kind()!=CollisionKind.SHIELD && o.effect().kind()!=CollisionKind.ENERGY);
    }
    private void chooseAction() {
        boolean afterLaneAttack=action==Action.LANE_ATTACK;
        if(afterLaneAttack)boss.healHits((int)c.number("boss.lane.healHitsAfterAction",1));
        if(action==null)action=c.integer("boss.startAction")==2?Action.LANE_ATTACK:Action.PLANE_WAVES;
        else action=afterLaneAttack || (consecutiveWaveSequences<c.integer("boss.maxConsecutiveWaveSequences")
                && random.nextDouble()*100<c.number("boss.action1Chance"))?Action.PLANE_WAVES:Action.LANE_ATTACK;
        if(action==Action.PLANE_WAVES)consecutiveWaveSequences++;else consecutiveWaveSequences=0;
        actionTime=0;recoveryTime=0;recovering=false;
        attackCooldown=c.number("boss.openingDelaySeconds");laneCooldown=c.number("boss.lane.changeIntervalSeconds");
        throwCooldown=c.number("boss.openingDelaySeconds");
        pendingWaveCount=0;lastWaveCount=0;pendingThrowCount=0;throwPending=false;
        openingLane=c.bool("boss.waves.openingVolleyEnabled")?0:GameConfig.LANE_COUNT+1;
        double moveSeconds=c.number("boss.moveSeconds");
        if(action==Action.PLANE_WAVES) {
            var plane=spec(ObstacleType.ENEMY_PLANE);
            waveSpeed=(plane.minSpeed()+random.nextDouble()*(plane.maxSpeed()-plane.minSpeed()))*c.number("boss.waves.speedMultiplier");
            double x=random.nextBoolean()?GameConfig.ROAD_LEFT-boss.width()-c.number("boss.sideMargin"):
                    GameConfig.ROAD_RIGHT+c.number("boss.sideMargin");
            boss.moveTo(x,c.number("boss.y"),moveSeconds);
        } else {
            lane=random.nextInt(GameConfig.LANE_COUNT);
            boss.moveTo(GameConfig.laneX(lane,boss.width()),c.number("boss.y"),moveSeconds);
        }
    }
    private void moveToLane(int target) {
        lane=target;
        boss.moveTo(GameConfig.laneX(lane,boss.width()),c.number("boss.y"),c.number("boss.moveSeconds"));
    }
    private void beginReverseAction() {
        action=Action.REVERSE_PLANES;actionTime=0;attackCooldown=0;reverseWavesSpawned=0;pendingReversePlaneCount=0;recovering=false;
        reverseShotPending=false;reverseShotCooldown=0;
        double x=boss.x()<GameConfig.WIDTH/2?GameConfig.ROAD_LEFT-boss.width()-c.number("boss.sideMargin"):
                GameConfig.ROAD_RIGHT+c.number("boss.sideMargin");
        boss.moveTo(x,c.number("boss.y"),c.number("boss.moveSeconds"));
    }
    private void updateAction(double dt) {
        if(recovering) {
            if(hasHazards()) {
                recoveryTime=0;
                // Hết đòn 2 vẫn né liên tục, tránh đứng làm bia khi chờ vật cản cũ rời màn.
                if(action==Action.LANE_ATTACK && !boss.moving())
                    moveToLane((lane+1+random.nextInt(GameConfig.LANE_COUNT-1))%GameConfig.LANE_COUNT);
                if(action==Action.LANE_ATTACK)updateRecoveryShots(dt);
                return;
            }
            if(!transitionPrepared) {
                // Đòn 1 nối sang 3; sau đòn 2 bắt buộc 1, sau đòn 3 mới chọn lại.
                if(action==Action.PLANE_WAVES)beginReverseAction();else chooseAction();
                recovering=true;transitionPrepared=true;
                // Boss.update đã chạy đầu khung hình: bù phần chuyển động của khung này.
                boss.update(dt);
            }
            recoveryTime+=dt;
            if(recoveryTime+1e-9<c.number("boss.actionGapSeconds"))return;
            dt=Math.max(0,recoveryTime-c.number("boss.actionGapSeconds"));
            recovering=false;transitionPrepared=false;attackCooldown=0;throwCooldown=0;
        }
        if(boss.moving())return;
        actionTime+=dt;
        double duration=c.number(switch(action) {
            case PLANE_WAVES -> "boss.waves.durationSeconds";
            case LANE_ATTACK -> "boss.lane.durationSeconds";
            case REVERSE_PLANES -> "boss.reverse.durationSeconds";
        });
        if(actionTime>=duration && !(action==Action.PLANE_WAVES && openingVolleyActive())) {
            if(action!=Action.REVERSE_PLANES || (reverseWavesSpawned>=c.integer("boss.reverse.waveCount") && !reverseShotPending)) {
                recovering=true;transitionPrepared=false;recoveryTime=0;
                recoveryShotsFired=0;recoveryShotCooldown=0;
                updateAction(0);return;
            }
        }
        attackCooldown-=dt;
        if(action==Action.PLANE_WAVES) {updateWaveAction(dt);return;}
        if(action==Action.REVERSE_PLANES) {updateReverseAction(dt);return;}
        if(action==Action.LANE_ATTACK) {
            laneCooldown-=dt;
            if(laneCooldown<=0) {
                moveToLane((lane+1+random.nextInt(GameConfig.LANE_COUNT-1))%GameConfig.LANE_COUNT);
                laneCooldown=c.number("boss.lane.changeIntervalSeconds");return;
            }
        }
        if(attackCooldown>1e-9)return;
        boolean accepted=spawnLaneAttack();
        attackCooldown=accepted?c.number("boss.lane.attackIntervalSeconds"):
                c.number("boss.retrySeconds");
    }
    private void updateReverseAction(double dt) {
        if(reverseShotPending) {
            reverseShotCooldown-=dt;
            if(reverseShotCooldown<=1e-9)tryReverseShot();
            if(reverseShotPending)return;
        }
        if(reverseWavesSpawned>=c.integer("boss.reverse.waveCount") || attackCooldown>1e-9)return;
        var spec=spec(ObstacleType.ENEMY_PLANE);List<Integer> lanes=shuffledLanes();
        if(pendingReversePlaneCount==0) {
            double single=c.number("boss.reverse.singleWeight"),pair=c.number("boss.reverse.pairWeight");
            double roll=random.nextDouble()*(single+pair+c.number("boss.reverse.sequenceWeight"));
            pendingReversePlaneCount=roll<single?1:roll<single+pair?2:GameConfig.LANE_COUNT;
        }
        boolean sequence=pendingReversePlaneCount==GameConfig.LANE_COUNT;
        for(int start=0;start<(sequence?1:lanes.size());start++) {
            Object formation=new Object();List<Obstacle> wave=new ArrayList<>();
            for(int i=0;i<pendingReversePlaneCount;i++) {
                int target=sequence?i:lanes.get((start+i)%lanes.size());
                double delay=sequence?i*c.number("boss.reverse.sequenceDelaySeconds"):0;
                wave.add(EnemyPlane.reverse(spec,GameConfig.laneX(target,spec.width()),c,formation,delay));
            }
            if(world.trySpawnGroup(wave)) {
                reverseWavesSpawned++;pendingReversePlaneCount=0;attackCooldown=c.number("boss.reverse.intervalSeconds");
                // Mỗi đợt chỉ xét xác suất bắn một lần; đạn chưa an toàn thì giữ để thử lại.
                reverseShotPending=random.nextDouble()*100<c.number("boss.reverse.shotChance");
                if(reverseShotPending)tryReverseShot();
                return;
            }
        }
        attackCooldown=c.number("boss.retrySeconds");
    }
    private void updateRecoveryShots(double dt) {
        if(recoveryShotsFired>=c.integer("boss.lane.recoveryShotLimit"))return;
        recoveryShotCooldown-=dt;if(recoveryShotCooldown>1e-9)return;
        for(int target:shuffledLanes()) {
            if(spawnAimedStrike(List.of(target),c.number("boss.projectile.warningSeconds"),
                    c.number("boss.lane.throwSpeed"),c.number("boss.waves.throwActiveSeconds"))) {
                recoveryShotsFired++;recoveryShotCooldown=c.number("boss.lane.recoveryShotIntervalSeconds");return;
            }
        }
        recoveryShotCooldown=c.number("boss.lane.recoveryShotRetrySeconds");
    }
    private void tryReverseShot() {
        for(int target:shuffledLanes()) {
            if(spawnAimedStrike(List.of(target),c.number("boss.reverse.shotWarningSeconds"),
                    c.number("boss.reverse.shotSpeed"),c.number("boss.reverse.shotAfterTargetSeconds"))) {
                reverseShotPending=false;return;
            }
        }
        reverseShotCooldown=c.number("boss.reverse.shotRetrySeconds");
    }
    private void updateWaveAction(double dt) {
        if(openingVolleyActive()) {
            throwCooldown-=dt;
            if(throwCooldown>1e-9)return;
            if(openingLane<GameConfig.LANE_COUNT) {
                if(spawnRowStrike(List.of(openingLane),c.number("boss.waves.openingVolleyWarningSeconds"))) {
                    openingLane++;
                    throwCooldown=openingLane<GameConfig.LANE_COUNT?c.number("boss.waves.openingVolleyIntervalSeconds"):lastRowStrikeLifetime;
                } else throwCooldown=c.number("boss.waves.throwRetrySeconds");
                return;
            }
            openingLane++;attackCooldown=0;throwCooldown=0;
        }
        updateWaveThrows(dt);
        if(attackCooldown<=1e-9)
            attackCooldown=spawnWave()?waveIntervalSeconds():c.number("boss.retrySeconds");
    }
    private void updateWaveThrows(double dt) {
        // Bắn đạn có đồng hồ riêng: vẫn bắn khi máy bay đang chờ khoảng cách hoặc chưa đặt được.
        throwCooldown-=dt;
        if(throwCooldown>1e-9)return;
        if(!throwPending) {
            if(random.nextDouble()*100>=c.number("boss.waves.throwChance")) {
                throwCooldown=c.number("boss.waves.throwIntervalSeconds");return;
            }
            throwPending=true;
        }
        if(spawnRowStrike()) {
            throwPending=false;throwCooldown=c.number("boss.waves.throwIntervalSeconds");
        } else throwCooldown=c.number("boss.waves.throwRetrySeconds");
    }
    private boolean openingVolleyActive() {return openingLane<=GameConfig.LANE_COUNT;}
    private double waveIntervalSeconds() {
        // Tính theo đợt vừa sinh: một chiếc không có phần lệch Y của chiếc thứ hai.
        double formationHeight=spec(ObstacleType.ENEMY_PLANE).height()+(lastWaveCount-1)*c.number("boss.waves.staggerY");
        return Math.max(c.number("boss.waves.intervalSeconds"),(formationHeight+c.number("boss.waves.gapPixels"))/waveSpeed);
    }
    private ObstacleSpec spec(ObstacleType type) {
        return world.config().obstacles().stream().filter(s->s.type()==type).findFirst().orElseThrow();
    }
    private List<Integer> shuffledLanes() {
        List<Integer> lanes=new ArrayList<>(List.of(0,1,2));Collections.shuffle(lanes,random);return lanes;
    }
    private boolean spawnWave() {
        ObstacleSpec spec=spec(ObstacleType.ENEMY_PLANE);
        if(pendingWaveCount==0)pendingWaveCount=random.nextDouble()*100<c.number("boss.waves.twoPlaneChance")?2:1;
        List<Integer> lanes=shuffledLanes();
        for(int start=0;start<lanes.size();start++) {
            Object formation=new Object();List<Obstacle> pair=new ArrayList<>();
            for(int i=0;i<pendingWaveCount;i++) {
                int lane=lanes.get((start+i)%lanes.size());
                double y=-spec.height()-c.number("spawn.margin")-pair.size()*c.number("boss.waves.staggerY");
                pair.add(new EnemyPlane(spec,GameConfig.laneX(lane,spec.width()),y,waveSpeed,c,false,formation));
            }
            if(world.trySpawnGroup(pair)) {
                lastWaveCount=pendingWaveCount;pendingWaveCount=0;
                return true;
            }
        }
        return false;
    }
    private ObstacleSpec projectileSpec() {
        return new ObstacleSpec(ObstacleType.BOSS_PROJECTILE,0,0,c.number("boss.projectile.width"),c.number("boss.projectile.height"),
                0,0,CollisionKind.BOSS_DAMAGE,true,c.text("sprite.bossProjectile",""));
    }
    private boolean spawnRowStrike() {
        if(pendingThrowCount==0)pendingThrowCount=random.nextDouble()*100<c.number("boss.waves.doubleThrowChance")?2:1;
        List<Integer> targets=shuffledLanes();
        for(int i=0;i<targets.size();i++) {
            List<Integer> lanes=new ArrayList<>();
            for(int j=0;j<pendingThrowCount;j++) {
                lanes.add(targets.get((i+j)%targets.size()));
            }
            if(spawnRowStrike(lanes,c.number("boss.projectile.warningSeconds"))) {pendingThrowCount=0;return true;}
        }
        return false;
    }
    private boolean spawnRowStrike(List<Integer> lanes,double warningSeconds) {
        return spawnAimedStrike(lanes,warningSeconds,c.number("boss.waves.throwSpeed"),c.number("boss.waves.throwActiveSeconds"));
    }
    private boolean spawnAimedStrike(List<Integer> lanes,double warningSeconds,double speed,double afterTargetSeconds) {
        ObstacleSpec spec=projectileSpec();double targetY=world.player().hitbox().getCenterY();
        boolean left=boss.x()<GameConfig.WIDTH/2;
        double handX=left?boss.x()+boss.width()-c.number("boss.waves.handInsetX"):boss.x()+c.number("boss.waves.handInsetX");
        double handY=boss.y()+boss.height()*c.number("boss.waves.handHeightRatio");
        List<Obstacle> projectiles=new ArrayList<>();Object volley=new Object();
        for(int target:lanes)projectiles.add(BossProjectile.aimed(spec,handX,handY,GameConfig.laneX(target,0),targetY,
                speed,warningSeconds,afterTargetSeconds,volley));
        if(!world.trySpawnGroup(projectiles))return false;
        lastRowStrikeLifetime=projectiles.stream().mapToDouble(o->((BossProjectile)o).lifetimeSeconds()).max().orElseThrow();
        return true;
    }
    private boolean spawnLaneAttack() {
        boolean accepted;
        if(random.nextDouble()*100<c.number("boss.lane.planeChance")) {
            accepted=world.trySpawnGroup(List.of(factory.create(spec(ObstacleType.ENEMY_PLANE),world.config(),random)));
        } else {
            List<Integer> targets=new ArrayList<>(List.of(lane));
            if(random.nextDouble()*100<c.number("boss.lane.doubleThrowChance"))
                targets.add((lane+1+random.nextInt(GameConfig.LANE_COUNT-1))%GameConfig.LANE_COUNT);
            ObstacleSpec spec=projectileSpec();List<Obstacle> projectiles=new ArrayList<>();
            for(int target:targets) projectiles.add(new BossProjectile(spec,GameConfig.laneX(target,spec.width()),boss.y()+boss.height(),
                    world.player().hitbox().getCenterY(),c.number("boss.lane.throwSpeed"),c.number("boss.projectile.warningSeconds"),0,false));
            accepted=world.trySpawnGroup(projectiles);
        }
        if(accepted && random.nextDouble()*100<c.number("boss.lane.birdChance"))
            world.trySpawnGroup(List.of(factory.create(spec(ObstacleType.BIRD),world.config(),random)));
        return accepted;
    }
    private void updateSupplies(double dt) {
        healthCooldown-=dt;
        if(healthCooldown<=0) {
            healthCooldown=world.spawnHealthPickup()?c.number("boss.healthPickup.minIntervalSeconds")+random.nextDouble()
                    *(c.number("boss.healthPickup.maxIntervalSeconds")-c.number("boss.healthPickup.minIntervalSeconds")):
                    c.number("healthPickup.retrySeconds");
        }
        if(shieldSpawnCount<c.integer("boss.shield.count") && activeSeconds+1e-9>=nextShield) {
            if(!shieldPending) {
                if(random.nextDouble()*100>=c.number("boss.shield.spawnChance")) {
                    nextShield=activeSeconds+c.number("boss.shield.intervalSeconds");return;
                }
                shieldPending=true;
            }
            if(spawnShield()) {
                shieldSpawnCount++;shieldPending=false;
                nextShield=activeSeconds+c.number("boss.shield.intervalSeconds");
            } else nextShield=activeSeconds+c.number("boss.retrySeconds");
        }
    }
    private boolean spawnShield() {
        if(world.obstacles().stream().anyMatch(o->!o.removed() && o instanceof ShieldPickup))return false;
        double w=c.number("boss.shield.width"),h=c.number("boss.shield.height"),speed=c.number("boss.shield.speed");
        ObstacleSpec spec=new ObstacleSpec(ObstacleType.SHIELD_PICKUP,0,0,w,h,speed,speed,CollisionKind.SHIELD,true,c.text("sprite.shieldPickup",""));
        double y=-h-c.number("spawn.margin"),travel=(world.player().hitbox().getCenterY()-h/2-y)/speed;
        for(int lane:shuffledLanes()) {
            ShieldPickup item=new ShieldPickup(spec,GameConfig.laneX(lane,w),y,speed);
            if(world.safety().canCollect(world,item,travel) && world.trySpawnGroup(List.of(item)))return true;
        }
        return false;
    }
    public boolean tryFire() {
        if(!active() || boss.defeated() || world.player().dead() || rocketCooldown>1e-9)return false;
        double w=c.number("boss.playerRocket.width"),h=c.number("boss.playerRocket.height");
        rockets.add(new PlayerRocket(world.player().hitbox().getCenterX()-w/2,world.player().hitbox().getMinY()-h,
                w,h,c.number("boss.playerRocket.speed")));
        rocketCooldown=c.number("boss.playerRocket.cooldownSeconds");hasFired=true;return true;
    }
    public void updateProjectiles(double dt) {
        rocketCooldown=Math.max(0,rocketCooldown-dt);
        explosions.replaceAll(e->new Explosion(e.x(),e.y(),e.remaining()-dt));
        explosions.removeIf(e->e.remaining()<=0);
        for(Iterator<PlayerRocket> iterator=rockets.iterator();iterator.hasNext();) {
            PlayerRocket rocket=iterator.next();Rectangle2D sweep=rocket.sweptBounds(dt);
            Obstacle nearest=null;double impactY=Double.NEGATIVE_INFINITY;boolean hitBoss=false;
            for(Obstacle obstacle:world.obstacles()) if(!obstacle.removed() && obstacle.blocksPlayerRocket()) {
                double hitY=intersectionBottom(obstacle.collisionShape(),sweep);
                if(hitY>impactY) {impactY=hitY;nearest=obstacle;}
            }
            if(active() && !boss.defeated()) {
                double hitY=intersectionBottom(boss.hitbox(),sweep);
                if(hitY>impactY) {impactY=hitY;hitBoss=true;}
            }
            if(impactY!=Double.NEGATIVE_INFINITY) {
                if(hitBoss)boss.takeHit();else nearest.hitByPlayerRocket();
                if(hitBoss || nearest.removed())
                    explosions.add(new Explosion(rocket.hitbox().getCenterX(),impactY,c.number("boss.explosion.seconds")));
                iterator.remove();
            } else {
                rocket.update(dt);if(rocket.y()+rocket.height()<0)iterator.remove();
            }
        }
    }
    private double intersectionBottom(Shape target,Rectangle2D swept) {
        if(!target.intersects(swept))return Double.NEGATIVE_INFINITY;
        Area intersection=new Area(target);intersection.intersect(new Area(swept));
        return intersection.isEmpty()?Double.NEGATIVE_INFINITY:intersection.getBounds2D().getMaxY();
    }
    public State state() {return state;}
    public Action action() {return action;}
    public AirBoss boss() {return boss;}
    public boolean normalPhase() {return state==State.NORMAL;}
    public boolean active() {return state==State.ACTIVE;}
    public boolean complete() {return state==State.COMPLETE;}
    public boolean bossVisible() {return state==State.ENTERING || state==State.LOADING || state==State.ACTIVE || state==State.EXITING;}
    public double stateTime() {return stateTime;}
    public double activeSeconds() {return activeSeconds;}
    public int shieldSpawnCount() {return shieldSpawnCount;}
    public int reverseWavesSpawned() {return reverseWavesSpawned;}
    public boolean hasFired() {return hasFired;}
    public double cooldownFraction() {return 1-rocketCooldown/c.number("boss.playerRocket.cooldownSeconds");}
    public double loadingFraction() {return state==State.LOADING?Math.min(1,stateTime/c.number("boss.healthLoadSeconds")):state==State.ENTERING?0:1;}
    public List<PlayerRocket> rockets() {return List.copyOf(rockets);}
    public List<Explosion> explosions() {return List.copyOf(explosions);}
}
