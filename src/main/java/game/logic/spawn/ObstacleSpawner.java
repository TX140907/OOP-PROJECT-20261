package game.logic.spawn;

import game.level.BaseLevel;
import game.level.DayConfig.ObstacleSpec;
import game.model.obstacle.Obstacle;
import java.util.*;

public final class ObstacleSpawner {
    private final Random random;
    private final ObstacleFactory factory=new ObstacleFactory();
    private double cooldown;
    public ObstacleSpawner(BaseLevel world,Random random) {
        this.random=random; cooldown=world.config().values().number("spawn.initialDelaySeconds");
    }
    public double density(BaseLevel world) {
        var c=world.config().values(); double p=world.progress().fraction();
        double base=c.text("difficulty.mode","phased").equals("fixed")?c.number("difficulty.fixedSpawnMultiplier"):
                c.number(p<c.number("difficulty.phase1End")?"difficulty.phase1SpawnMultiplier":
                p<c.number("difficulty.phase2End")?"difficulty.phase2SpawnMultiplier":"difficulty.phase3SpawnMultiplier");
        return base*world.event().spawnMultiplier();
    }
    public void update(double dt,BaseLevel world) {
        if(!world.event().allowsNormalSpawns())return;
        cooldown-=dt;
        if (cooldown>0) return;
        var c=world.config().values();
        cooldown=(c.number("spawn.minIntervalSeconds")+random.nextDouble()*(c.number("spawn.maxIntervalSeconds")-c.number("spawn.minIntervalSeconds")))/density(world);
        List<ObstacleSpec> selected=new ArrayList<>();
        if (c.text("spawn.mode").equals("independent")) {
            for (ObstacleSpec spec:world.config().obstacles()) if (random.nextDouble()*100<spec.chance()) selected.add(spec);
            Collections.shuffle(selected,random);
        } else {
            double total=world.config().obstacles().stream().mapToDouble(ObstacleSpec::weight).sum(), roll=random.nextDouble()*total;
            for (ObstacleSpec spec:world.config().obstacles()) { roll-=spec.weight(); if (roll<0) { selected.add(spec); break; } }
        }
        for (ObstacleSpec spec:selected) {
            if (world.obstacles().size()>=c.integer("spawn.maxActiveObstacles")) break;
            for (int attempt=0;attempt<c.integer("spawn.attempts");attempt++) {
                Obstacle candidate=factory.create(spec,world.config(),random);
                if (world.trySpawnGroup(List.of(candidate))) break;
            }
        }
    }
}
