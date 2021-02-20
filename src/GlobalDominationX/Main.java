package GlobalDominationX;

import GlobalDominationX.Logic.Generator;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timer;
import arc.util.serialization.Json;
import arc.util.serialization.UBJsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.mod.Plugin;
import mindustry.net.Administration;

/**
 *
 * @author Xusk
 */
public class Main extends Plugin {

    public static Rules rules = new Rules();
    public static Interval interval = new Interval(3);
    public static int pingInterval = 1, pingTime = 120,
            consoleInterval = 2, consoleTime = 240;
    public static Seq<BufferedReader> consoles = new Seq<>();
    
    @Override
    public void init() {
        Json json = new Json();
        
        class Avoid {
            int value = 0;
        }
        
        Fi data = Fi.get("globalX.bin");
        json.toUBJson(new Avoid(), null, data.write());
        UBJsonReader reader = new UBJsonReader();
        Avoid a = json.readValue(Avoid.class, null, reader.parse(data));
        
        Events.on(EventType.ServerLoadEvent.class, event -> {
            initRules();
            ServerLoad();
        });

        // Main Logic
        Events.run(EventType.Trigger.update, () -> {
            if (interval.get(pingInterval, pingTime)) {
                Vars.net.pingHost("localhost", Administration.Config.port.num(), (t) -> {
                }, (t) -> {
                    Core.settings.put("main_server", false);
                    Core.settings.put("current_server", 123);
                    System.exit(0);
                });
            }
            
            if (interval.get(consoleInterval, consoleTime)) {
                
            }
        });

        // IDK just must work lol
        Events.on(EventType.DisposeEvent.class, event -> {
            if (Core.settings.getBool("main_server")) {
                Core.settings.put("main_server", false);
            }
            Log.info("WHY");
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("run", "create game", (args) -> {
            runServer();
        });
    }

    public static void ServerLoad() {
        Core.settings.put("server_port_ip", Administration.Config.port.num());
        int ip = Core.settings.getInt("current_server");
        // Load World
        Log.info("Global Dominations Starting...");
        Vars.logic.reset();
        Call.worldDataBegin();
        Vars.world.loadGenerator(50, 50, new Generator());
        Vars.state.rules = rules.copy();
        
        try {
            int port = Administration.Config.port.num() + Core.settings.getInt("current_server");
            Vars.net.host(port);
            Log.info("Server Hosted On Port: " + port);
        } catch (IOException ex) {
            Log.err(ex);
            System.exit(0);
        }
        Vars.logic.play();
        Log.info("Process end");
    }

    public static void initRules() {
        rules.canGameOver = false;
        rules.waves = false;
    }

    public static void runServer() {
        try {
            Process proc = Runtime.getRuntime().exec("java -jar server.jar");
            Timer.schedule(() -> {

                BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                consoles.add(input);
            }, 2);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
