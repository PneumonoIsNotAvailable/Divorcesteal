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
import net.pneumono.divorcesteal.hearts.ParticipantHeartData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ParticipantArgumentType implements ArgumentType<ParticipantArgumentType.ParticipantArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "*");
    public static final SimpleCommandExceptionType NO_DATA_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.no_data")
    );
    public static final SimpleCommandExceptionType NOT_DEATHBANNED_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.not_deathbanned")
    );
    public static final SimpleCommandExceptionType PARTICIPANT_LISTED_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.participant_listed")
    );
    public static final SimpleCommandExceptionType NO_PARTICIPANT_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.participant_unlisted")
    );

    private final boolean singleTarget;
    private final Filter filter;

    private ParticipantArgumentType(boolean singleTarget, Filter filter) {
        this.singleTarget = singleTarget;
        this.filter = filter;
    }

    public static ParticipantArgumentType participant() {
        return new ParticipantArgumentType(true, Filter.ALL);
    }

    public static ParticipantArgumentType participants() {
        return new ParticipantArgumentType(false, Filter.ALL);
    }

    public static ParticipantArgumentType bannedParticipant() {
        return new ParticipantArgumentType(true, Filter.BANNED_ONLY);
    }

    public static ParticipantArgumentType bannedParticipants() {
        return new ParticipantArgumentType(false, Filter.BANNED_ONLY);
    }

    public static ParticipantArgumentType unbannedParticipant() {
        return new ParticipantArgumentType(true, Filter.UNBANNED_ONLY);
    }

    public static ParticipantArgumentType unbannedParticipants() {
        return new ParticipantArgumentType(false, Filter.UNBANNED_ONLY);
    }

    public static ParticipantHeartData getParticipant(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ParticipantArgument.class).getData(context.getSource());
    }

    public static List<ParticipantHeartData> getParticipants(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ParticipantArgument.class).getDataList(context.getSource());
    }

    @Override
    public ParticipantArgument parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && (isAllowedInString(reader.peek()))) reader.skip();
        String string = reader.getString().substring(start, reader.getCursor());

        if (!this.singleTarget && string.equals("*")) {
            return source -> {
                HeartDataState state = Hearts.getHeartDataState();
                return state.getHeartDataList().stream().filter(this.filter::test).toList();
            };
        } else if (string.length() > 16) {
            throw NO_PARTICIPANT_EXCEPTION.create();
        }
        return source -> {
            HeartDataState state = Hearts.getHeartDataState();
            GameProfile profile = Objects.requireNonNull(source.getServer().getUserCache()).findByName(string).orElseThrow(NO_PARTICIPANT_EXCEPTION::create);
            ParticipantHeartData data = state.getHeartData(profile.getId());

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
                .map(ParticipantHeartData::getName)
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

    public interface ParticipantArgument {
        List<ParticipantHeartData> getDataList(ServerCommandSource source) throws CommandSyntaxException;

        default ParticipantHeartData getData(ServerCommandSource source) throws CommandSyntaxException {
            return getDataList(source).getFirst();
        }
    }

    public enum Filter {
        ALL(0, "all", data -> true),
        BANNED_ONLY(1, "banned_only", ParticipantHeartData::isBanned),
        UNBANNED_ONLY(2, "unbanned_only", data -> !data.isBanned());

        private final int id;
        private final String name;
        private final Predicate<ParticipantHeartData> predicate;

        Filter(int id, String name, Predicate<ParticipantHeartData> predicate) {
            this.id = id;
            this.name = name;
            this.predicate = predicate;
        }

        public boolean test(ParticipantHeartData data) {
            return this.predicate.test(data);
        }

        public static Filter fromId(int id) {
            return values()[id % values().length];
        }
    }

    public static class Serializer implements ArgumentSerializer<ParticipantArgumentType, Serializer.Properties> {
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
        public Properties getArgumentTypeProperties(ParticipantArgumentType argumentType) {
            return new Properties(argumentType.singleTarget, argumentType.filter);
        }

        public final class Properties implements ArgumentTypeProperties<ParticipantArgumentType> {
            private final boolean single;
            private final Filter filter;

            public Properties(boolean single, Filter filter) {
                this.single = single;
                this.filter = filter;
            }

            @Override
            public ParticipantArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return new ParticipantArgumentType(single, filter);
            }

            @Override
            public ArgumentSerializer<ParticipantArgumentType, ?> getSerializer() {
                return Serializer.this;
            }
        }
    }
}
