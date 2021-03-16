package GlobalDominationX.Logic;

import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.type.UnitType;

public class UnitCapacityGroup{
    
    public ObjectMap<UnitType, Integer> units = new ObjectMap<>();
    
    public UnitCapacityGroup() {
        Vars.content.units().each(unit -> {
            units.put(unit, 0);
        });
    }
    
    public void spawn(Team team, int x, int y) {
        units.keys().forEach(key -> {
            for (int i = 0; i < units.get(key); i++) {
                Unit unit = key.create(Team.crux);
                unit.set(x, y);
                unit.add();
                unit.team(team);
            }
        });
        // null all value when unit spawned
        units.keys().forEach(key -> units.put(key, 0));
    }
    
    public void set(UnitType type, int count) {
        units.put(type, count);
    }
    
    public int get(UnitType type) {
        return units.get(type);
    }
    
    public UnitCapacityGroup copy() {
        UnitCapacityGroup g = new UnitCapacityGroup();
        g.units = units;
        return g;
    }
}
