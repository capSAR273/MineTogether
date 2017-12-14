package net.creeperhost.minetogether;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.creeperhost.minetogether.api.CreeperHostAPI;
import net.creeperhost.minetogether.api.ICreeperHostMod;
import net.creeperhost.minetogether.api.IServerHost;
import net.creeperhost.minetogether.common.Config;
import net.creeperhost.minetogether.paul.Callbacks;
import net.creeperhost.minetogether.paul.CreeperHostServerHost;
import net.creeperhost.minetogether.siv.QueryGetter;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Random;

@Mod(
        modid = CreeperHost.MOD_ID,
        name = CreeperHost.NAME,
        version = CreeperHost.VERSION,
        acceptableRemoteVersions="*",
        guiFactory = "net.creeperhost.minetogether.gui.config.GuiCreeperConfigFactory"
)
public class CreeperHost implements ICreeperHostMod
{

    public static final String MOD_ID = "creeperhost";
    public static final String NAME = "CreeperHost";
    public static final String VERSION = "@VERSION@";
    public static final Logger logger = LogManager.getLogger("creeperhost");

    @Mod.Instance(value="creeperhost")
    public static CreeperHost instance;

    public ArrayList<IServerHost> implementations = new ArrayList<IServerHost>();
    public IServerHost currentImplementation;
    public File configFile;

    private QueryGetter queryGetter;
    private String lastCurse = "";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        if (event.getSide() != Side.SERVER) {
            EventHandler eventHandler = new EventHandler();
            FMLCommonHandler.instance().bus().register(eventHandler);
            MinecraftForge.EVENT_BUS.register(eventHandler);
        }

        configFile = event.getSuggestedConfigurationFile();

        InputStream configStream = null;
        try
        {
            String configString;
            if (configFile.exists()) {
                configStream = new FileInputStream(configFile);
                configString = IOUtils.toString(configStream);
            } else {
                configString = "{}";
            }

            Config.loadConfig(configString);
        } catch (Throwable t)
        {
            logger.error("Unable to read config", t);
            throw new RuntimeException("Fatal error, unable to read config");
        } finally {
            try {
                if (configStream != null) {
                    configStream.close();
                }
            } catch (Throwable t) {
            }

        }

        saveConfig();
    }

    private CreeperHostServerHost implement;

    public void saveConfig(){
        FileOutputStream configOut = null;
        try
        {
            configOut = new FileOutputStream(configFile);
            IOUtils.write(Config.saveConfig(), configOut);
            configOut.close();
        } catch (Throwable t)
        {
        } finally {
            try
            {
                if (configOut != null) {
                    configOut.close();
                }
            } catch (Throwable t) {
            }
        }

        if (Config.getInstance().isCreeperhostEnabled() && implement == null) {
            CreeperHost.instance.implementations.remove(implement);
            CreeperHostAPI.registerImplementation(implement = new CreeperHostServerHost());
        }


        if (!Config.getInstance().isCreeperhostEnabled()) {
            CreeperHost.instance.implementations.remove(implement);
            implement = null;
        }
    }

    public void updateCurse()
    {
        if(!Config.getInstance().curseProjectID.equals(lastCurse) && Config.getInstance().isCreeperhostEnabled())
        {
            Config.getInstance().setVersion(Callbacks.getVersionFromCurse(Config.getInstance().curseProjectID));
        }

        lastCurse = Config.getInstance().curseProjectID;
    }

    private Random randomGenerator;

    public void setRandomImplementation() {
        if (randomGenerator == null)
            randomGenerator = new Random();
        if (implementations.size() == 0)
            return;
        int random = randomGenerator.nextInt(implementations.size());
        currentImplementation = implementations.get(random);
    }

    public IServerHost getImplementation()
    {
        return currentImplementation;
    }

    @Override
    public void registerImplementation(IServerHost serverHost)
    {
        implementations.add(serverHost);
    }

    public void makeQueryGetter() {
        try {
            if (FMLClientHandler.instance().getClientToServerNetworkManager() != null) {
                SocketAddress socketAddress = FMLClientHandler.instance().getClientToServerNetworkManager().getSocketAddress();

                String host = "127.0.0.1";
                int port = 25565;

                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress add = (InetSocketAddress) socketAddress;
                    host = add.getHostName();
                    port = add.getPort();
                }

                queryGetter = new QueryGetter(host, port);
            }
        } catch (Throwable t) {
            // Catch _ALL_ errors. We should _NEVER_ crash.
        }

    }

    public QueryGetter getQueryGetter(){
        if(queryGetter == null) {
            makeQueryGetter();
        }
        return queryGetter;
    }
}