package GlobalDominationX.Area;

import arc.func.Cons;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class Generator implements Cons<Tiles> {

    @Override
    public void get(Tiles tiles) {
        for (int x = 0; x < tiles.width; x++) {
            for (int y = 0; y < tiles.height; y++) {
                Block wall = Blocks.air;
                if (x < 5 || y < 5 || x > tiles.width - 5 || y > tiles.height - 5) wall = Blocks.stoneWall;
                tiles.set(x, y, new Tile(x, y, Blocks.stone, Blocks.air, wall));
            }
        }
        
        tiles.getn(10, tiles.height / 2).setBlock(Blocks.coreShard, Team.sharded);
        tiles.get(tiles.width - 10, tiles.height / 2).setBlock(Blocks.coreShard, Team.blue);
    }
}
