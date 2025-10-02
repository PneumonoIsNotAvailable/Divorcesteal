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

        CompletableFuture.runAsync(() -> {
            makeBackup();

            STATE = read();
        });
    }

    public static void clearState() {
        STATE = null;
    }

    private static Path getHeartsPath() {
        return ROOT_PATH.resolve("hearts.dat");
    }

    private static HeartDataState read() {
        Path path = getHeartsPath();

        if (!path.toFile().exists()) return new HeartDataState();

        NbtCompound compound = new NbtCompound();
        try {
            compound = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to read Hearts data", e);
        }

        NbtElement element = compound.get("values");

        DataResult<Pair<HeartDataState, NbtElement>> result = HeartDataState.CODEC.decode(NbtOps.INSTANCE, element);
        if (result.isSuccess()) {
            return result.getOrThrow().getFirst();
        } else {
            Divorcesteal.LOGGER.error("Failed to deserialize Hearts data");
            return new HeartDataState();
        }
    }

    public static void save() {
        CompletableFuture.runAsync(DataSaving::write);
    }

    @SuppressWarnings("LoggingSimilarMessage")
    private static void write() {
        Path path = getHeartsPath();

        DataResult<NbtElement> result = HeartDataState.CODEC.encodeStart(NbtOps.INSTANCE, STATE);
        NbtElement element;
        if (result.isSuccess()) {
            element = result.getOrThrow();
        } else {
            Divorcesteal.LOGGER.error("Failed to serialize Hearts data");
            element = new NbtCompound();
        }

        NbtCompound compound = new NbtCompound();
        compound.put("values", element);

        try {
            NbtIo.writeCompressed(compound, path);
        } catch (IOException e) {
            Divorcesteal.LOGGER.error("Failed to write Hearts data", e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void makeBackup() {
        Path source = getHeartsPath();

        if (!source.toFile().exists()) {
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
