package net.pneumono.divorcesteal.hearts;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.pneumono.divorcesteal.Divorcesteal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class DataSaving {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
    private static volatile HeartDataState STATE = null;
    private static Path ROOT_PATH = null;

    public static HeartDataState getState() {
        while (STATE == null) {
            Thread.onSpinWait();
        }
        return STATE;
    }

    public static void backupAndLoadHeartDataState(MinecraftServer server) {
        ROOT_PATH = server.getSavePath(WorldSavePath.ROOT);
        STATE = null;

        CompletableFuture.runAsync(() -> {
            makeBackup();

            STATE = read();
        });
    }

    private static Path getHeartsPath() {
        return ROOT_PATH.resolve("hearts.dat");
    }

    private static HeartDataState read() {
        Divorcesteal.LOGGER.info("Reading hearts data...");

        Path path = getHeartsPath();

        if (!path.toFile().exists()) {
            Divorcesteal.LOGGER.info("No hearts data exists, creating empty data");
            return new HeartDataState();
        }

        NbtCompound compound;
        try {
            compound = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to read hearts data", e);
            return new HeartDataState();
        }

        NbtElement element = compound.get("values");

        DataResult<Pair<HeartDataState, NbtElement>> result = HeartDataState.CODEC.decode(NbtOps.INSTANCE, element);
        if (result.isSuccess()) {
            Divorcesteal.LOGGER.info("Successfully read hearts data");
            return result.getOrThrow().getFirst();
        } else {
            String message = null;
            if (result.error().isPresent()) {
                message = result.error().get().message();
            }
            Divorcesteal.LOGGER.error("Failed to deserialize hearts data: {}", message);
            return new HeartDataState();
        }
    }

    // Saving is NOT done asynchronously, or else the server closes before it can finish saving heart data
    @SuppressWarnings("LoggingSimilarMessage")
    public static void save() {
        Divorcesteal.LOGGER.info("Saving hearts data...");

        Path path = getHeartsPath();

        DataResult<NbtElement> result = HeartDataState.CODEC.encodeStart(NbtOps.INSTANCE, STATE);
        NbtElement element;
        if (result.isSuccess()) {
            element = result.getOrThrow();
        } else {
            String message = null;
            if (result.error().isPresent()) {
                message = result.error().get().message();
            }
            Divorcesteal.LOGGER.error("Failed to serialize Hearts data: {}", message);
            return;
        }

        NbtCompound compound = new NbtCompound();
        compound.put("values", element);

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
