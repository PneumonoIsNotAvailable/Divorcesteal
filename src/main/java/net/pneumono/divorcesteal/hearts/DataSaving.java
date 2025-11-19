package net.pneumono.divorcesteal.hearts;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.pneumono.divorcesteal.Divorcesteal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DataSaving {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
    private static volatile CompletableFuture<ParticipantMap> STATE = null;
    private static Path ROOT_PATH = null;

    public static ParticipantMap getState() {
        try {
            return STATE.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to get hearts data", e);
        }
    }

    public static void backupAndLoadHeartDataState(MinecraftServer server) {
        ROOT_PATH = server.getWorldPath(LevelResource.ROOT);

        CompletableFuture.runAsync(DataSaving::makeBackup);
        STATE = CompletableFuture.supplyAsync(DataSaving::read);
    }

    private static Path getHeartsPath() {
        return ROOT_PATH.resolve("hearts.dat");
    }

    private static ParticipantMap read() {
        Divorcesteal.LOGGER.info("Reading hearts data...");

        Path path = getHeartsPath();

        if (!path.toFile().exists()) {
            Divorcesteal.LOGGER.info("No hearts data exists, creating empty data");
            return new ParticipantMap();
        }

        CompoundTag compound;
        try {
            compound = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to read hearts data", e);
            return new ParticipantMap();
        }

        Tag tag = compound.get("values");

        DataResult<Pair<ParticipantMap, Tag>> result = ParticipantMap.CODEC.decode(NbtOps.INSTANCE, tag);
        if (result.isSuccess()) {
            Divorcesteal.LOGGER.info("Successfully read hearts data");
            return result.getOrThrow().getFirst();
        } else {
            String message = null;
            if (result.error().isPresent()) {
                message = result.error().get().message();
            }
            Divorcesteal.LOGGER.error("Failed to deserialize hearts data: {}", message);
            return new ParticipantMap();
        }
    }

    // Saving is NOT done asynchronously, or else the server closes before it can finish saving heart data
    public static void save() {
        Divorcesteal.LOGGER.info("Saving hearts data...");

        Path path = getHeartsPath();

        ParticipantMap state;
        try {
            state = STATE.get();
        } catch (ExecutionException | InterruptedException e) {
            Divorcesteal.LOGGER.error("Failed to get hearts data", e);
            return;
        }

        DataResult<Tag> result = ParticipantMap.CODEC.encodeStart(NbtOps.INSTANCE, state);
        Tag tag;
        if (result.isSuccess()) {
            tag = result.getOrThrow();
        } else {
            String message = null;
            if (result.error().isPresent()) {
                message = result.error().get().message();
            }
            Divorcesteal.LOGGER.error("Failed to serialize Hearts data: {}", message);
            return;
        }

        CompoundTag compound = new CompoundTag();
        compound.put("values", tag);

        try {
            NbtIo.writeCompressed(compound, path);
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to write Hearts data", e);
            return;
        }

        Divorcesteal.LOGGER.info("Successfully saved hearts data");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void makeBackup() {
        Divorcesteal.LOGGER.info("Backing up hearts data...");

        Path source = getHeartsPath();

        if (!source.toFile().exists()) {
            Divorcesteal.LOGGER.info("No hearts data exists to back up");
            return;
        }

        Path backups = ROOT_PATH.resolve("hearts_backups");
        backups.toFile().mkdirs();

        Date date = new Date();
        Path destination = backups.resolve(DATE_FORMAT.format(date) + ".dat");
        int count = 1;
        while (destination.toFile().exists()) {
            count++;
            destination = backups.resolve(DATE_FORMAT.format(date) + "_" + count + ".dat");
        }


        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            Divorcesteal.LOGGER.info("Successfully backed up Hearts data to {}", destination);
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to backup Hearts data", e);
        }
    }
}
