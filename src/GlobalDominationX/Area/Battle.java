package GlobalDominationX.Area;

import GlobalDominationX.Farm.Logic;
import GlobalDominationX.Logic.UnitCapacityGroup;
import GlobalDominationX.Main;
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
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;

public class Battle {
    public static Team winner = Team.derelict;
    public static UnitCapacityGroup blue = new UnitCapacityGroup(),
            sharded = new UnitCapacityGroup();
    public static Rules rules = new Rules() {
        {
            for (Block block : Vars.content.blocks()) {
                if (block instanceof Turret) {
                    bannedBlocks.add(block);
                }
            }
            canGameOver = false;
        }
    };
    public static boolean started = false;
    public static Interval interval = new Interval(3);

    public static void update() {
        if (started) {
            if (Team.sharded.core() == null) winner = Team.blue;
            if (Team.blue.core() == null) winner = Team.sharded;
            if (winner != Team.derelict) {
                end(true);
            }
            int uu = 0;
            for (Unit unit : Groups.unit) {
                if (!unit.isPlayer()) uu++;
            }
            if (uu <= 0) end(true);
        }
    }

    public static void end(boolean b) {
        if (b) {
            Call.infoMessage("[accent]Winner: " + winner.name);
            Timer.schedule(() -> {
                Logic.goTo();
            }, 3);
            winner = Team.derelict;
            started = false;
        }
    }

    public static void goTo() {
        Main.state = Main.State.BATTLE;
        blue = Logic.blue.copy();
        sharded = Logic.sharded.copy();
        Logic.blue = new UnitCapacityGroup();
        Logic.sharded = new UnitCapacityGroup();

        // Update Values
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        // Logic Reset Start
        Vars.logic.reset();

        // World Load Start
        Call.worldDataBegin();
        Vars.world.loadGenerator(75, 25, new Generator());
        // World Load End

        // Rules Load
        Vars.state.rules = rules.copy();
        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
            p.team(Team.derelict);
        }

        Timer.schedule(() -> {
            start();
        }, 2);
    }

    public static void start() {
        sharded.spawn(Team.sharded, 15 * Vars.tilesize, Vars.world.height() / 2 * Vars.tilesize);
        blue.spawn(Team.blue, (Vars.world.width() - 15), Vars.world.height() / 2 * Vars.tilesize);
        started = true;
    }
}
