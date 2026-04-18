package net.pneumono.aprilfools.gacha;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.LevelResource;
import net.pneumono.divorcesteal.Divorcesteal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GachaDataSaving {
    private static volatile CompletableFuture<List<NameAndId>> STATE = null;
    private static Path ROOT_PATH = null;

    public static List<NameAndId> getGachaParticipantList() {
        try {
            return STATE.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to get gacha participants", e);
        }
    }

    public static void backupAndLoadParticipantMap(MinecraftServer server) {
        ROOT_PATH = server.getWorldPath(LevelResource.ROOT);

        STATE = CompletableFuture.supplyAsync(GachaDataSaving::read);
    }

    private static Path getPath() {
        return ROOT_PATH.resolve("gacha.dat");
    }

    private static List<NameAndId> read() {
        Divorcesteal.LOGGER.info("Reading gacha data...");

        Path path = getPath();

        if (!path.toFile().exists()) {
            Divorcesteal.LOGGER.info("No gacha data exists, creating empty data");
            return new ArrayList<>();
        }

        CompoundTag compound;
        try {
            compound = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to read gacha data", e);
            return new ArrayList<>();
        }

        Tag tag = compound.get("values");

        DataResult<List<NameAndId>> result = NameAndId.CODEC.listOf().decode(NbtOps.INSTANCE, tag).map(Pair::getFirst);
        if (result.isSuccess()) {
            Divorcesteal.LOGGER.info("Successfully read gacha data");
            return new ArrayList<>(result.getOrThrow());
        } else {
            String message = null;
            if (result.error().isPresent()) {
                message = result.error().get().message();
            }
            Divorcesteal.LOGGER.error("Failed to deserialize gacha data: {}", message);
            return new ArrayList<>();
        }
    }

    // Saving is NOT done asynchronously, or else the server closes before it can finish saving gacha data
    public static void save() {
        Divorcesteal.LOGGER.info("Saving gacha data...");

        Path path = getPath();

        List<NameAndId> list;
        try {
            list = STATE.get();
        } catch (ExecutionException | InterruptedException e) {
            Divorcesteal.LOGGER.error("Failed to get gacha data", e);
            return;
        }

        DataResult<Tag> result = NameAndId.CODEC.listOf().encodeStart(NbtOps.INSTANCE, list);
        Tag tag;
        if (result.isSuccess()) {
            tag = result.getOrThrow();
        } else {
            String message = null;
            if (result.error().isPresent()) {
                message = result.error().get().message();
            }
            Divorcesteal.LOGGER.error("Failed to serialize gacha data: {}", message);
            return;
        }

        CompoundTag compound = new CompoundTag();
        compound.put("values", tag);

        try {
            NbtIo.writeCompressed(compound, path);
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to write gacha data", e);
            return;
        }

        Divorcesteal.LOGGER.info("Successfully saved gacha data");
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(GachaDataSaving::serverStarting);
        ServerLifecycleEvents.AFTER_SAVE.register(GachaDataSaving::afterSave);
    }

    private static void serverStarting(MinecraftServer server) {
        GachaDataSaving.backupAndLoadParticipantMap(server);
    }

    private static void afterSave(MinecraftServer server, boolean flush, boolean force) {
        GachaDataSaving.save();
    }
}
