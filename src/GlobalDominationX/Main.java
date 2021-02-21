package GlobalDominationX;

import GlobalDominationX.Logic.Generator;
import GlobalDominationX.Logic.ServerData;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Timer;
import arc.util.serialization.Jval;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
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
    public static int id;
    public static int nowServers = 0, maxServers = 2;
    public static Fi file = Fi.get("GlobalX.bin");
    public static Fi config = Fi.get("config.bin");
    public static String serverIp = "192.168.0.2";

    @Override
    public void init() {
        if (!config.exists()) {
            config.write();
            Jval jval = Jval.read(config.readString());
            jval.add("ip", serverIp);
            config.writeString(jval.toString());
        } else {
            Jval jval = Jval.read(config.readString());
            serverIp = jval.get("ip").toString();
        }

        if (Core.settings.getBool("setted")) {
            id = getID();
            writeID(id + 1);
        } else {
            file.write();
            Jval jval = Jval.read(file.readString());
            jval.add("id", "1");
            file.writeString(jval.toString());
            id = 0;

            Core.settings.put("setted", true);
            Core.settings.saveValues();
        }

        if (id == maxServers) {

        }

        Events.on(EventType.ServerLoadEvent.class, event -> {
            initRules();
            ServerLoad();
        });

        // Main Logic
        Events.run(EventType.Trigger.update, () -> {
            if (interval.get(pingInterval, pingTime)) {
                Vars.net.pingHost("localhost", Administration.Config.port.num(), (t) -> {
                    Core.settings.put("setted", true);
                    Core.settings.saveValues();
                }, (t) -> {
                    Core.settings.put("setted", false);
                    Core.settings.saveValues();
                    System.exit(0);
                });

                if (id == 0) {

                }
            }

            if (interval.get(consoleInterval, consoleTime)) {
                if (id == 0) {
                    Log.info(consoles.size + " : " + maxServers);
                    while (nowServers < maxServers) {
                        runServer();
                        nowServers++;
                    }
                }
            }

            int height = Vars.world.height(), width = Vars.world.width();

            for (Player player : Groups.player) {
                if (id == 0) {
                    if (player.tileY() > height - 2) {
                        Call.connect(player.con, serverIp, Administration.Config.port.num() + id  + 1);
                    }
                } else if (id == maxServers) {
                    if (player.tileY() < -2) {
                        Call.connect(player.con, serverIp, Administration.Config.port.num() + id  - 1);
                    }
                } else {
                    if (player.tileY() > height - 2) {
                        Call.connect(player.con, serverIp, Administration.Config.port.num() + id + 1);
                    }
                    if (player.tileY() < -2) {
                        Call.connect(player.con, serverIp, Administration.Config.port.num() + id  - 1);
                    }
                }
            }
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("setip", "<ip>", "set ip to work", args -> {
            if (args.length > 0) {
                serverIp = args[0];
                Log.info("server IP now: " + serverIp);
                Jval j = Jval.read(config.readString());
                j.add("ip", serverIp);
                config.writeString(j.toString());
            }
        });
    }

    public static int getID() {
        Jval j = Jval.read(file.readString());
        return Integer.valueOf(j.get("id").toString());
    }

    public static void writeID(int id) {
        
        Jval j = Jval.read(file.readString());
        j.add("id", "2");
        file.writeString(j.toString());
    }

    public static void ServerLoad() {
        // Load World
        Log.info("Global Dominations Starting...");
        Vars.logic.reset();
        Call.worldDataBegin();

        Generator gen = new Generator();

        if (id == 0) {
            gen.side = Generator.Side.down;
        } else if (id == maxServers) {
            gen.side = Generator.Side.up;
        } else {
            gen.side = Generator.Side.centre;
        }

        Vars.world.loadGenerator(50, 500, gen);
        Vars.state.rules = rules.copy();

        try {
            int port = Administration.Config.port.num() + id;
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
