package GlobalDominationX;

import GlobalDominationX.Area.Battle;
import GlobalDominationX.Farm.Logic;
import arc.Events;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.mod.Plugin;
import mindustry.world.meta.BuildVisibility;

/**
 *
 * @author Xusk
 */
public class Main extends Plugin {
    
    public enum State {
        FARM, BATTLE;
    }
    
    public static State state = State.FARM;
    public static Rules rules = new Rules();
    public static Interval interval = new Interval(3);

    @Override
    public void init() {
        Events.on(EventType.ServerLoadEvent.class, event -> {
            serverStart();
            Vars.netServer.openServer();
        });
        
        Events.run(EventType.Trigger.update, () -> {
            if (state == State.FARM) {
                Logic.update();
            } else if (state == State.BATTLE) {
                Battle.update();
            }
        });
    }

    public void serverStart() {
        Logic.goTo();
        Blocks.coreFoundation.unitCapModifier = 99999;
        Blocks.coreNucleus.unitCapModifier = 99999;
        Blocks.coreShard.unitCapModifier = 99999;
    }
}
