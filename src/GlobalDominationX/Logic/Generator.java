package GlobalDominationX.Logic;

import arc.func.Cons;
import arc.struct.StringMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.maps.Map;
import mindustry.world.Tile;
import mindustry.world.Tiles;

public class Generator implements Cons<Tiles> {
    public static enum side {
        leftUp, up, rightUp,
        left, centre, right,
        leftBottom, bottom, rightCentre;
    }

    @Override
    public void get(Tiles tiles) {
        for (int tx = 0; tx < tiles.width; tx++) {
            for (int ty = 0; ty < tiles.height; ty++) {
                tiles.set(tx, ty, new Tile(tx, ty, Blocks.stone, Blocks.air, Blocks.air));
            }
        }
        Vars.state.map = new Map(StringMap.of("Domination", "oh no"));
    }
}
