package GlobalDominationX.Area;

import GlobalDominationX.Farm.Logic;
import GlobalDominationX.Logic.Generator;
import GlobalDominationX.Logic.UnitCapacityGroup;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

public class Battle {
    
    public static UnitCapacityGroup 
            blue = new UnitCapacityGroup(),
            sharded = new UnitCapacityGroup();
    public static Rules rules = new Rules(){{
        pvp = true;
    }};
    public static Interval interval = new Interval(3);

    public static void update() {
        if (Groups.unit.size() > 0) {
            boolean end = true;
            Unit unitt = Groups.unit.getByID(0);
            for (Unit unit : Groups.unit) {
                if (unitt == unit) continue;
                if (unit.team != unitt.team) {
                    end = false;
                }
            }
            end(end);
        }
    }

    public static void end(boolean b) {
        if (b) {
            Call.infoMessage("[accent]Winner: " + Groups.unit.getByID(0).team.name);
            Timer.schedule(() -> {Logic.goTo();}, 3);
        }
    }
    
    public static void goTo() {
        
        blue = Logic.blue.copy();
        sharded = Logic.sharded.copy();
        
        // Update Values
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        // Logic Reset Start
        Vars.logic.reset();

        // World Load Start
        Call.worldDataBegin();
        Vars.world.loadGenerator(150, 50, new Generator());
        // World Load End

        // Rules Load
        Vars.state.rules = rules.copy();
        Vars.world.tile(10, Vars.world.height() / 2).setNet(Blocks.coreShard, Team.sharded, 0);
        Vars.world.tile(Vars.world.width() - 10, Vars.world.height() / 2).setNet(Blocks.coreShard, Team.blue, 0);
        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
        }
        
        Timer.schedule(() -> start(), 2);
    }
    
    public static void start() {
        sharded.spawn(Team.sharded, 10 * Vars.tilesize, Vars.world.height() / 2 * Vars.tilesize);
        blue.spawn(Team.blue, (Vars.world.width() - 10), Vars.world.height() / 2 * Vars.tilesize);
    }
}
