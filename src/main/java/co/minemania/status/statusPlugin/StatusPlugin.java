package co.minemania.status.statusPlugin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Plugin(id = "mmstatus", name = "StatusPlugin", version = "1.0.0")
public class StatusPlugin {
    public static StatusPlugin Instance;

    @Inject @DataDirectory public Path dataDir;
    @Inject public ProxyServer proxy;
    @Inject public Logger logger;
    public StatusServer server;
    public ConfigData config;
    public Instant startedAt;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Instance = this;

        if(!Files.exists(dataDir))
        {
            try {
                Files.createDirectories(dataDir);
            } catch (IOException e) {
                logger.error("Failed to create config dir", e);
                return;
            }
        }

        Path configPath = dataDir.resolve("conf.json");
        Gson gson = new Gson();

        try {
            if(Files.exists(configPath)) {
                config = gson.fromJson(Files.readString(configPath), ConfigData.class);
            } else {
                InputStream resource = getClass().getResourceAsStream("/conf.json");
                assert resource != null;
                byte[] defaultConfig = resource.readAllBytes();
                resource.close();

                Files.write(configPath, defaultConfig);
                config = gson.fromJson(new String(defaultConfig), ConfigData.class);
            }
        } catch (IOException e) {
            logger.error("Failed to load config", e);
            return;
        }

        try {
            server = new StatusServer(config.port);
        } catch (IOException e) {
            logger.error("Failed to create server! " , e);
            return;
        }

        startedAt = Instant.now();
        logger.info("Set up status plugin!");
    }

    public ServerResponse createResponse() {
        ServerResponse r = new ServerResponse();
        r.name = config.serverName;
        r.uptime = Instant.now().getEpochSecond() - startedAt.getEpochSecond();

        Component rawMotd = proxy.getConfiguration().getMotd();
        r.motd = PlainTextComponentSerializer.plainText().serialize(rawMotd);

        for(RegisteredServer server : proxy.getAllServers()) {
            ServerResponse.ServerInfo si = new ServerResponse.ServerInfo();
            si.name = server.getServerInfo().getName();

            for(Player player : server.getPlayersConnected()) {
                ServerResponse.PlayerInfo pi = new ServerResponse.PlayerInfo();
                pi.name = player.getUsername();
                pi.uuid = player.getUniqueId().toString();
                pi.ping = player.getPing();
                pi.client = player.getClientBrand();
                si.players.add(pi);
            }

            r.servers.add(si);
        }

        return r;
    }
}
