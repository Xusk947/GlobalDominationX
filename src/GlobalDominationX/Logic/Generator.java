package GlobalDominationX.Logic;

import arc.func.Cons;
import arc.struct.StringMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.maps.Map;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class Generator implements Cons<Tiles> {

    public static enum Side {
        none, up, centre, down;
    }
    public static int margin = 3;
    public Side side = Side.none;

    @Override
    public void get(Tiles tiles) {
        for (int tx = 0; tx < tiles.width; tx++) {
            for (int ty = 0; ty < tiles.height; ty++) {
                if (side == Side.up && (tx <= margin || tx >= tiles.width - margin || ty >= tiles.height - margin)) {
                    tiles.set(tx, ty, new Tile(tx, ty, Blocks.stone, Blocks.air, Blocks.stoneWall));
                } else if (side == Side.down && (ty <= margin || tx <= margin || tx >=  tiles.width - margin)) {
                    tiles.set(tx, ty, new Tile(tx, ty, Blocks.stone, Blocks.air, Blocks.stoneWall));
                } else if (side == Side.centre && (tx <= margin || tx >= tiles.width - margin)) {
                    tiles.set(tx, ty, new Tile(tx, ty, Blocks.stone, Blocks.air, Blocks.stoneWall));
                } else {
                    tiles.set(tx, ty, new Tile(tx, ty, Blocks.stone, Blocks.air, Blocks.air));
                }
            }
            
        }
        tiles.getn(tiles.width / 2, tiles.height / 2).setNet(Blocks.coreShard, Team.sharded, 0);
        Vars.state.map = new Map(StringMap.of("Domination", "oh no"));
    }
}
