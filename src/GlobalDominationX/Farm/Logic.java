package GlobalDominationX.Farm;

import GlobalDominationX.Area.Battle;
import static GlobalDominationX.Area.Battle.rules;
import GlobalDominationX.Logic.UnitCapacityGroup;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;

public class Logic {

    public static int startFarmTimer = 60 * 60 * 5,
            farmTimer = startFarmTimer;
    public static int Tier = 1;
    public static UnitCapacityGroup blue = new UnitCapacityGroup(),
            sharded = new UnitCapacityGroup();
    public static void update() {
        
        if (Groups.player.size() < 0) {
            return;
        }
        
        farmTimer--;
        if (farmTimer < 0) {
            Battle.goTo();
            farmTimer = startFarmTimer;
        }
        
        Groups.build.each(build -> {
            if (build.block == Blocks.launchPad) {
                Building b = build.nearby(2, 0);
                if (b.block != null && b.block == Blocks.payloadConveyor && b.getPayload().size() > 0) {
                    Unit u = (Unit) b.getPayload();
                }
            }
        });
    }

    public static void goTo() {
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        // Logic Reset Start
        Vars.logic.reset();

        // World Load Start
        Call.worldDataBegin();
        Vars.world.loadGenerator(150, 50, new Generator());
        // World Load End

        // Rules Load
        Tier++;
        if (Tier < 2) {
            rules.bannedBlocks.add(Blocks.additiveReconstructor);
            rules.bannedBlocks.add(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.add(Blocks.exponentialReconstructor);
        } else if (Tier < 3) {
            rules.bannedBlocks.remove(Blocks.additiveReconstructor);
            rules.bannedBlocks.add(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.add(Blocks.exponentialReconstructor);
        } else if (Tier < 4) {
            rules.bannedBlocks.remove(Blocks.additiveReconstructor);
            rules.bannedBlocks.remove(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.add(Blocks.exponentialReconstructor);
        } else {
            rules.bannedBlocks.remove(Blocks.additiveReconstructor);
            rules.bannedBlocks.remove(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.remove(Blocks.exponentialReconstructor);
            Tier = 0;
        }

        Vars.state.rules = rules.copy();

        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
        }
    }
}
