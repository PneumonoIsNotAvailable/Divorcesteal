package net.pneumono.divorcesteal.hearts;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.pneumono.divorcesteal.Divorcesteal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DataSaving {
    private static HeartDataState STATE = null;
    private static CompletableFuture<?> SAVING_FUTURE = CompletableFuture.completedFuture(null);

    public static HeartDataState getState(MinecraftServer server) {
        if (STATE == null) {
            STATE = read(server);
            STATE.setServer(server);
        }
        return STATE;
    }

    public static void clearState() {
        STATE = null;
    }

    private static HeartDataState read(MinecraftServer server) {
        Path path = new File(
                server.getSavePath(WorldSavePath.ROOT).toString()
        ).toPath().resolve("hearts.dat");

        if (!path.toFile().exists()) return new HeartDataState();

        NbtCompound compound = new NbtCompound();
        try {
            compound = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to read Hearts data", e);
        }

        DataResult<Pair<HeartDataState, NbtElement>> result = HeartDataState.CODEC.decode(NbtOps.INSTANCE, compound);
        if (result.isSuccess()) {
            Divorcesteal.LOGGER.error("Did not failed to deserialize Hearts data");
            return result.getOrThrow().getFirst();
        } else {
            Divorcesteal.LOGGER.error("Failed to deserialize Hearts data");
            return new HeartDataState();
        }
    }

    public static void save(MinecraftServer server) {
        SAVING_FUTURE = SAVING_FUTURE.thenCompose((object) -> CompletableFuture.runAsync(() -> write(server)));
    }

    private static void write(MinecraftServer server) {
        Path path = new File(
                server.getSavePath(WorldSavePath.ROOT).toString()
        ).toPath().resolve("hearts.dat");

        DataResult<NbtElement> result = HeartDataState.CODEC.encodeStart(NbtOps.INSTANCE, STATE);
        NbtElement element;
        if (result.isSuccess()) {
            element = result.getOrThrow();
        } else {
            Divorcesteal.LOGGER.error("Failed to serialize Hearts data");
            element = new NbtCompound();
        }

        NbtCompound nbt = new NbtCompound();
        nbt.put("values", element);

        try {
            NbtIo.writeCompressed(nbt, path);
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to write Hearts data", e);
        }
    }
}
