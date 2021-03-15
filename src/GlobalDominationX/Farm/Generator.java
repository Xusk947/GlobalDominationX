package GlobalDominationX.Farm;

import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.noise.Simplex;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.maps.filters.GenerateFilter;
import mindustry.maps.filters.OreFilter;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class Generator implements Cons<Tiles> {

    public static int margin = 5;

    Block[][] floors = {
        {Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.grass},
        {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.grass},
        {Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.shale},
        {Blocks.darksandTaintedWater, Blocks.darksandTaintedWater, Blocks.moss, Blocks.moss, Blocks.sporeMoss, Blocks.stone},
        {Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.dacite, Blocks.hotrock, Blocks.salt}
    };

    Block[][] blocks = {
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.sandWall, Blocks.sandWall, Blocks.pine, Blocks.pine},
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine},
        {Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine},
        {Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.stoneWall},
        {Blocks.iceWall, Blocks.snowWall, Blocks.snowWall, Blocks.snowWall, Blocks.stoneWall, Blocks.saltWall}
    };

    @Override
    public void get(Tiles t) {
        Simplex tt = new Simplex(Mathf.random(0, 10000));
        Simplex e = new Simplex(Mathf.random(0, 10000));
        Seq<GenerateFilter> ores = new Seq<>();
        Vars.maps.addDefaultOres(ores);
        ores.each(o -> ((OreFilter) o).threshold -= 0.05f);
        ores.insert(0, new OreFilter() {
            {
                ore = Blocks.oreScrap;
                scl += 2 / 2.1F;
            }
        });

        GenerateFilter.GenerateInput in = new GenerateFilter.GenerateInput();
        for (int x = 0; x < t.width; x++) {
            for (int y = 0; y < t.height; y++) {
                int temp = Mathf.clamp((int) ((tt.octaveNoise2D(12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * blocks.length), 0, blocks.length - 1);
                int elev = Mathf.clamp((int) (((e.octaveNoise2D(12, 0.6, 1.0 / 700, x, y) - 0.5) * 10 + 0.15f) * blocks[0].length), 0, blocks[0].length - 1);

                Block floor = floors[temp][elev];
                Block ore = Blocks.air;
                Block wall = Blocks.air;
                if (x < margin || y < margin
                        || x > t.width - margin || y > t.height - margin
                        || (x > t.width / 2 - margin * 2 && x < t.width / 2 + margin * 2)) {
                    wall = blocks[temp][elev];
                }

                for (GenerateFilter f : ores) {
                    in.floor = Blocks.stone;
                    in.block = wall;
                    in.overlay = ore;
                    in.x = x;
                    in.y = y;
                    f.apply(in);
                    if (in.overlay != Blocks.air) {
                        ore = in.overlay;
                    }
                }
                
                
                t.set(x, y, new Tile(x, y, floor.id, ore.id, wall.id));
            }
        }
        
        for (int x = 0; x < t.width / 2; x++) {
            for (int y = 0; y < t.height; y++) {
                if (x <= t.width && y <= t.height) {
                    t.set(t.width / 2 + x, y, new Tile(t.width / 2 +  x, y, t.getn(x, y).floor(), t.getn(x, y).overlay(), t.getn(x, y).block()));
                }
            }
        }

        t.getn(t.width / 4, t.height / 2 + 6).setBlock(Blocks.launchPad, Team.sharded);
        t.getn(t.width / 4, t.height / 2).setBlock(Blocks.coreShard, Team.sharded);
        t.getn(t.width - t.width / 4, t.height / 2 + 6).setBlock(Blocks.launchPad, Team.blue);
        t.getn(t.width - t.width / 4, t.height / 2).setBlock(Blocks.coreShard, Team.blue);
    }

}
