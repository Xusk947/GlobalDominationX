package GlobalDominationX.Farm;

import GlobalDominationX.Area.Battle;
import GlobalDominationX.Logic.UnitCapacityGroup;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Nulls;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.type.ItemStack;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;

public class Logic {

    public static Rules rules = new Rules() {
        {
            infiniteResources = true;
        }
    };
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
                Building b = build.nearby(2,0) != null ? build.nearby(2, 0) 
                        : build.nearby(-2, 0) != null ? build.nearby(-2, 0) 
                        : build.nearby(0, 2) != null ? build.nearby(0, 2) 
                        : build.nearby(0, -2) != null ? build.nearby(0, -2) : null;
                if (b != null && b.block == Blocks.payloadConveyor && b.getPayload() != null) {
                    Unit u = (Unit) b.getPayload();
                    if (u.team == Team.sharded) {
                        sharded.units.put(u.type, sharded.units.get(u.type));
                    } else if (u.team == Team.blue) {
                        blue.units.put(u.type, blue.units.get(u.type));
                    }
                }
            }

            if (build.block instanceof UnitFactory) {
                UnitFactory uf = (UnitFactory) build.block;
                for (UnitFactory.UnitPlan plan : uf.plans) {
                    for (int i = 0; i < plan.requirements.length; i++) {
                        if (build.items.get(plan.requirements[i].item) < build.block.itemCapacity) {
                            Call.transferItemTo(Nulls.unit, plan.requirements[i].item, plan.requirements[i].amount, build.x, build.y, build);
                        }
                    }
                }
            }

            if (build.block instanceof Reconstructor) {
                Reconstructor r = (Reconstructor) build.block;
                for (ItemStack requirement : r.requirements) {
                    if (build.items.get(requirement.item) < build.block.itemCapacity) {
                        Call.transferItemTo(Nulls.unit, requirement.item, requirement.amount, build.x, build.y, build);
                    }
                }
            }
        });

        Groups.player.each(p -> {
            if (p.team() == Team.blue) {
                Call.setHudText(p.con, "units: " + blue.units.size);
            } else if (p.team() == Team.sharded) {
                Call.setHudText(p.con, "units: " + sharded.units.size);
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
        Vars.world.loadGenerator(320, 200, new Generator());
        // World Load End

        // Rules Load
        Tier++;
        if (Tier < 2) {
            rules.bannedBlocks.add(Blocks.additiveReconstructor);
            rules.bannedBlocks.add(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.add(Blocks.exponentialReconstructor);
            rules.bannedBlocks.add(Blocks.tetrativeReconstructor);
        } else if (Tier < 3) {
            rules.bannedBlocks.remove(Blocks.additiveReconstructor);
            rules.bannedBlocks.add(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.add(Blocks.tetrativeReconstructor);
        } else if (Tier < 4) {
            rules.bannedBlocks.add(Blocks.exponentialReconstructor);
            rules.bannedBlocks.remove(Blocks.additiveReconstructor);
            rules.bannedBlocks.remove(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.add(Blocks.exponentialReconstructor);
            rules.bannedBlocks.add(Blocks.tetrativeReconstructor);

        } else {
            rules.bannedBlocks.remove(Blocks.additiveReconstructor);
            rules.bannedBlocks.remove(Blocks.multiplicativeReconstructor);
            rules.bannedBlocks.remove(Blocks.exponentialReconstructor);
            rules.bannedBlocks.remove(Blocks.tetrativeReconstructor);
            Tier = 0;
        }

        Vars.state.rules = rules.copy();
        Call.setRules(rules);

        // Logic Reset End
        Vars.logic.play();

        // Send World Data To All Players
        for (Player p : players) {
            Vars.netServer.sendWorldData(p);
        }
    }
}
