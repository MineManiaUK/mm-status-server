package co.minemania.status.statusPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerResponse {
    public String name = "";
    public String motd = "";
    public long uptime = 1;
    public List<ServerInfo> servers = new ArrayList<>();

    public static class ServerInfo {
        public String name = "";
        public List<PlayerInfo> players = new ArrayList<>();
    }

    public static class PlayerInfo {
        public String name = "";
        public String uuid = new UUID(0, 0).toString();
        public long ping = 0;
        public String client = "";
    }
}
