package net.pneumono.divorcesteal.command;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.pneumono.divorcesteal.hearts.ParticipantMap;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.Participant;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ParticipantArgumentType implements ArgumentType<ParticipantArgumentType.ParticipantArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "*");

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

    public static Participant getParticipant(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ParticipantArgument.class).getData(context.getSource());
    }

    public static List<Participant> getParticipants(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, ParticipantArgument.class).getDataList(context.getSource());
    }

    @Override
    public ParticipantArgument parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && (isAllowedInString(reader.peek()))) reader.skip();
        String string = reader.getString().substring(start, reader.getCursor());

        if (!this.singleTarget && string.equals("*")) {
            return source -> {
                ParticipantMap state = Hearts.getHeartDataState();
                return state.getParticipants().stream().filter(this.filter::test).toList();
            };
        } else if (string.length() > 16) {
            throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();
        }

        return source -> {
            ParticipantMap state = Hearts.getHeartDataState();
            GameProfile profile = Objects.requireNonNull(source.getServer().getProfileCache()).get(string)
                    .orElseThrow(DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION::create);
            Participant participant = state.getParticipant(profile.getId());

            if (participant != null && this.filter.test(participant)) {
                return List.of(participant);
            } else {
                throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();
            }
        };
    }

    public static boolean isAllowedInString(char c) {
        return StringReader.isAllowedInUnquotedString(c) || c == '*';
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider source)) return Suggestions.empty();
        if (!(context.getSource() instanceof CommandSourceStack)) return source.customSuggestion(context);

        ParticipantMap state = Hearts.getHeartDataState();
        List<String> strings = new ArrayList<>();
        if (!singleTarget) strings.add("*");
        strings.addAll(state.getParticipants()
                .stream()
                .filter(this.filter::test)
                .map(Participant::getName)
                .toList()
        );

        return SharedSuggestionProvider.suggest(
                strings,
                builder
        );
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface ParticipantArgument {
        List<Participant> getDataList(CommandSourceStack source) throws CommandSyntaxException;

        default Participant getData(CommandSourceStack source) throws CommandSyntaxException {
            return getDataList(source).getFirst();
        }
    }

    public enum Filter {
        ALL(0, "all", data -> true),
        BANNED_ONLY(1, "banned_only", Participant::isBanned),
        UNBANNED_ONLY(2, "unbanned_only", data -> !data.isBanned());

        private final int id;
        private final String name;
        private final Predicate<Participant> predicate;

        Filter(int id, String name, Predicate<Participant> predicate) {
            this.id = id;
            this.name = name;
            this.predicate = predicate;
        }

        public boolean test(Participant data) {
            return this.predicate.test(data);
        }

        public static Filter fromId(int id) {
            return values()[id % values().length];
        }
    }

    public static class Serializer implements ArgumentTypeInfo<ParticipantArgumentType, Serializer.Properties> {
        @Override
        public void serializeToNetwork(Properties properties, FriendlyByteBuf buf) {
            int i = properties.single ? 0 : 3;

            i += properties.filter.id;

            buf.writeByte(i);
        }

        @Override
        public @NotNull Properties deserializeFromNetwork(FriendlyByteBuf buf) {
            byte b = buf.readByte();
            return new Properties(b < 3, Filter.fromId(b));
        }

        @Override
        public void serializeToJson(Properties properties, JsonObject json) {
            json.addProperty("amount", properties.single ? "single" : "multiple");
            json.addProperty("type", properties.filter.name);
        }

        @Override
        public @NotNull Properties unpack(ParticipantArgumentType argumentType) {
            return new Properties(argumentType.singleTarget, argumentType.filter);
        }

        public final class Properties implements ArgumentTypeInfo.Template<ParticipantArgumentType> {
            private final boolean single;
            private final Filter filter;

            public Properties(boolean single, Filter filter) {
                this.single = single;
                this.filter = filter;
            }

            @Override
            public @NotNull ParticipantArgumentType instantiate(CommandBuildContext commandRegistryAccess) {
                return new ParticipantArgumentType(single, filter);
            }

            @Override
            public @NotNull ArgumentTypeInfo<ParticipantArgumentType, ?> type() {
                return Serializer.this;
            }
        }
    }
}
