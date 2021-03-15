package GlobalDominationX;

import GlobalDominationX.Farm.Logic;
import arc.Events;
import arc.util.Interval;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.mod.Plugin;

/**
 *
 * @author Xusk
 */
public class Main extends Plugin {

    public static Rules rules = new Rules();
    public static Interval interval = new Interval(3);

    @Override
    public void init() {
        Events.on(EventType.ServerLoadEvent.class, event -> {
            serverStart();
        });
    }

    public void serverStart() {
        Logic.goTo();
        Blocks.coreFoundation.unitCapModifier = 99999;
        Blocks.coreNucleus.unitCapModifier = 99999;
        Blocks.coreShard.unitCapModifier = 99999;
    }
}
