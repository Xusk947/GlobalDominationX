package GlobalDominationX.Farm;

import arc.func.Cons;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class Generator implements Cons<Tiles> {

    public static int margin = 5;

    @Override
    public void get(Tiles t) {
        for (int x = 0; x < t.width; x++) {
            for (int y = 0; y < t.height; y++) {
                Block wall = Blocks.air;
                if (x < margin || y < margin
                        || x > t.width - margin || y > t.height - margin
                        || (x > t.width / 2 + margin * 2 && x < t.width / 2 - margin * 2)) {
                    wall = Blocks.stoneWall;
                }
                t.set(x, y, new Tile(x, y, Blocks.stone, Blocks.oreCopper, wall));
            }
        }
        
        t.getn(t.width / 4, t.height / 2).setBlock(Blocks.coreShard, Team.sharded);
        t.getn(t.width - t.width / 4, t.height / 2).setBlock(Blocks.coreShard, Team.blue);
    }

}
