package net.creeperhost.creeperhost.serverstuffs.pregen;

import net.creeperhost.creeperhost.common.Pair;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;

public class PregenTask
{
    public int dimension;
    public transient ArrayList<Pair<Integer, Integer>> chunksToGen;
    public int chunksPerTick;
    public int storedCurX;
    public int storedCurZ;
    public int minX;
    public int maxX;
    public int minZ;
    public int maxZ;
    public int radiusX = 0;
    public int radiusZ = 0;
    public long startTime = 0;
    public transient long lastCheckedTime = -9001;
    public int chunksDone = 0;
    public int totalChunks = 0;
    public transient int lastChunksDone = 0;
    public transient String lastPregenString = "No status yet!";

    public PregenTask(int dimension, int minX, int maxX, int minZ, int maxZ, int chunksPerTick)
    {
        this.dimension = dimension;
        this.chunksPerTick = chunksPerTick;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.storedCurX = minX;
        this.storedCurZ = minZ;

        init();
    }

    public void init()
    {
        if (chunksToGen != null) return;

        if (radiusX > 0 && totalChunks == 0) // only the first time
        {
            World world = DimensionManager.getWorld(dimension);
            ChunkCoordinates pos = world.getSpawnPoint();
            minX = (pos.posX << 4) - (radiusX / 2);
            maxX = (pos.posX << 4) + (radiusX / 2);

            minZ = (pos.posZ << 4) - (radiusZ / 2);
            maxZ = (pos.posZ << 4) + (radiusZ / 2);
            storedCurX = minX;
            storedCurZ = minZ;
        }

        chunksDone = 0;
        totalChunks = 0;

        ArrayList<Pair<Integer, Integer>> chunks = new ArrayList<Pair<Integer, Integer>>();

        for (int curX = minX; curX <= maxX; curX++) {
            if (curX < storedCurX)
                continue;;
            for(int curZ = minZ; curZ <= maxZ; curZ++) {
                if (curX == storedCurX && curZ <= storedCurZ)
                    continue;

                chunks.add(new Pair<Integer, Integer>(curX, curZ));
                totalChunks++;
            }
        }

        chunksToGen = chunks;

    }
}
