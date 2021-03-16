package GlobalDominationX.Farm;

import GlobalDominationX.Area.Battle;
import GlobalDominationX.Logic.UnitCapacityGroup;
import GlobalDominationX.Main;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Bullets;
import mindustry.core.GameState;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Nulls;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.storage.Unloader;
import mindustry.world.blocks.units.UnitFactory;

public class Logic {

    public static final Vec2[] pos = new Vec2[]{new Vec2(2, 0), new Vec2(-2, 0), new Vec2(0, 2), new Vec2(0, -2)};
    public static Rules rules = new Rules() {
        {
            for (Block block : Vars.content.blocks()) {
                if (block instanceof Turret) {
                    bannedBlocks.add(block);
                }
            }
            canGameOver = false;
            pvp = true;
            infiniteResources = true;
            unitBuildSpeedMultiplier = 999f;
        }
    };
    public static int startFarmTimer = 60 * 60 / 2,
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
            farmTimer = startFarmTimer * Tier;
        }

        Groups.unit.each(unit -> {
            if (!unit.isPlayer()) {
                unit.kill();
            }
        });

        Groups.build.each(build -> {
            if (build.block == Blocks.launchPad) {
                for (Vec2 p : pos) {
                    Building b = build.nearby((int) p.x, (int) p.y);
                    if (b != null && b.block == Blocks.payloadConveyor && b.getPayload() != null) {
                        UnitPayload up = (UnitPayload) b.takePayload();
                        Unit u = up.unit;
                        if (u.team.core() != null) {
                            Call.createBullet(Bullets.artilleryExplosive, u.team, build.x, build.y, build.angleTo(u.team.core()), 0, 1f, build.dst(u.team.core()) / Bullets.artilleryExplosive.range());
                        }
                        if (u.team == Team.sharded) {
                            sharded.units.put(u.type, sharded.units.get(u.type) + 1);
                        } else if (u.team == Team.blue) {
                            blue.units.put(u.type, blue.units.get(u.type) + 1);
                        }
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

            if (build.block instanceof Unloader) {
                Building b = build.nearby(1, 0) != null ? build.nearby(1, 0)
                        : build.nearby(-1, 0) != null ? build.nearby(-1, 0)
                        : build.nearby(0, 1) != null ? build.nearby(0, 1)
                        : build.nearby(0, -1) != null ? build.nearby(0, -1) : null;
                if (b != null && b.block instanceof UnitFactory) {
                    build.kill();
                }
            }
        });

        Groups.player.each(p -> {
            StringBuilder str = new StringBuilder();
            str.append("time: ").append((int) (farmTimer / 60f)).append("\n");
            if (p.team() == Team.blue) {
                blue.units.keys().forEach(key -> {
                    if (blue.units.get(key) > 0) {
                        str.append(key.name).append(": ").append(blue.units.get(key)).append("\n");
                    }
                });
            } else if (p.team() == Team.sharded) {
                sharded.units.keys().forEach(key -> {
                    if (sharded.units.get(key) > 0) {
                        str.append(key.name).append(": ").append(sharded.units.get(key)).append("\n");
                    }
                });
            }
            Call.setHudText(p.con, str.toString());
        });
    }

    public static void goTo() {
        Main.state = Main.State.FARM;
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
