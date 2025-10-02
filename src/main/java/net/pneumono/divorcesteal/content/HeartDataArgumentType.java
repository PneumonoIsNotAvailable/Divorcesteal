package net.pneumono.divorcesteal.content;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class HeartDataArgumentType implements ArgumentType<HeartDataArgumentType.HeartDataArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "*");
    public static final SimpleCommandExceptionType NO_DATA_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.no_data")
    );
    public static final SimpleCommandExceptionType NOT_DEATHBANNED_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.not_deathbanned")
    );
    public static final SimpleCommandExceptionType NO_PLAYER_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.player.unknown")
    );

    private final boolean singleTarget;
    private final Filter filter;

    private HeartDataArgumentType(boolean singleTarget, Filter filter) {
        this.singleTarget = singleTarget;
        this.filter = filter;
    }

    public static HeartDataArgumentType player() {
        return new HeartDataArgumentType(true, Filter.ALL);
    }

    public static HeartDataArgumentType players() {
        return new HeartDataArgumentType(false, Filter.ALL);
    }

    public static HeartDataArgumentType bannedPlayer() {
        return new HeartDataArgumentType(true, Filter.BANNED_ONLY);
    }

    public static HeartDataArgumentType bannedPlayers() {
        return new HeartDataArgumentType(false, Filter.BANNED_ONLY);
    }

    public static HeartDataArgumentType unbannedPlayer() {
        return new HeartDataArgumentType(true, Filter.UNBANNED_ONLY);
    }

    public static HeartDataArgumentType unbannedPlayers() {
        return new HeartDataArgumentType(false, Filter.UNBANNED_ONLY);
    }

    public static PlayerHeartData getPlayer(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, HeartDataArgument.class).getData(context.getSource());
    }

    public static List<PlayerHeartData> getPlayers(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, HeartDataArgument.class).getDataList(context.getSource());
    }

    @Override
    public HeartDataArgument parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && (isAllowedInString(reader.peek()))) reader.skip();
        String string = reader.getString().substring(start, reader.getCursor());

        if (!this.singleTarget && string.equals("*")) {
            return source -> {
                HeartDataState state = Hearts.getHeartDataState();
                return state.getHeartDataList().stream().filter(this.filter::test).toList();
            };
        } else if (string.length() > 16) {
            throw NO_PLAYER_EXCEPTION.create();
        }
        return source -> {
            HeartDataState state = Hearts.getHeartDataState();
            GameProfile profile = Objects.requireNonNull(source.getServer().getUserCache()).findByName(string).orElseThrow(NO_PLAYER_EXCEPTION::create);
            PlayerHeartData data = state.getHeartData(profile.getId());

            if (data != null && this.filter.test(data)) {
                return List.of(data);
            } else {
                throw NO_DATA_EXCEPTION.create();
            }
        };
    }

    public static boolean isAllowedInString(char c) {
        return StringReader.isAllowedInUnquotedString(c) || c == '*';
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSource source)) return Suggestions.empty();
        if (!(context.getSource() instanceof ServerCommandSource)) return source.getCompletions(context);

        HeartDataState state = Hearts.getHeartDataState();
        List<String> strings = new ArrayList<>();
        if (!singleTarget) strings.add("*");
        strings.addAll(state.getHeartDataList()
                .stream()
                .filter(this.filter::test)
                .map(PlayerHeartData::getName)
                .toList()
        );

        return CommandSource.suggestMatching(
                strings,
                builder
        );
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface HeartDataArgument {
        List<PlayerHeartData> getDataList(ServerCommandSource source) throws CommandSyntaxException;

        default PlayerHeartData getData(ServerCommandSource source) throws CommandSyntaxException {
            return getDataList(source).getFirst();
        }
    }

    public enum Filter {
        ALL(0, "all", data -> true),
        BANNED_ONLY(1, "banned_only", PlayerHeartData::isBanned),
        UNBANNED_ONLY(2, "unbanned_only", data -> !data.isBanned());

        private final int id;
        private final String name;
        private final Predicate<PlayerHeartData> predicate;

        Filter(int id, String name, Predicate<PlayerHeartData> predicate) {
            this.id = id;
            this.name = name;
            this.predicate = predicate;
        }

        public boolean test(PlayerHeartData data) {
            return this.predicate.test(data);
        }

        public static Filter fromId(int id) {
            return values()[id % values().length];
        }
    }

    public static class Serializer implements ArgumentSerializer<HeartDataArgumentType, Serializer.Properties> {
        @Override
        public void writePacket(Properties properties, PacketByteBuf buf) {
            int i = properties.single ? 0 : 3;

            i += properties.filter.id;

            buf.writeByte(i);
        }

        @Override
        public Properties fromPacket(PacketByteBuf buf) {
            byte b = buf.readByte();
            return new Properties(b < 3, Filter.fromId(b));
        }

        @Override
        public void writeJson(Properties properties, JsonObject json) {
            json.addProperty("amount", properties.single ? "single" : "multiple");
            json.addProperty("type", properties.filter.name);
        }

        @Override
        public Properties getArgumentTypeProperties(HeartDataArgumentType argumentType) {
            return new Properties(argumentType.singleTarget, argumentType.filter);
        }

        public final class Properties implements ArgumentTypeProperties<HeartDataArgumentType> {
            private final boolean single;
            private final Filter filter;

            public Properties(boolean single, Filter filter) {
                this.single = single;
                this.filter = filter;
            }

            @Override
            public HeartDataArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return new HeartDataArgumentType(single, filter);
            }

            @Override
            public ArgumentSerializer<HeartDataArgumentType, ?> getSerializer() {
                return Serializer.this;
            }
        }
    }
}
