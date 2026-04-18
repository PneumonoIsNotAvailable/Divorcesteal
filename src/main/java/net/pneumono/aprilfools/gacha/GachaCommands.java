package net.pneumono.aprilfools.gacha;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.pneumono.divorcesteal.command.DivorcestealExceptions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class GachaCommands {
    public static int executeAdd(CommandSourceStack source, String target) throws CommandSyntaxException {
        NameAndId nameAndId = Objects.requireNonNull(source.getServer().services().nameToIdCache()).get(target)
                .orElseThrow(DivorcestealExceptions.NO_PLAYER_EXCEPTION::create);

        List<NameAndId> gachaParticipants = GachaDataSaving.getGachaParticipantList();
        gachaParticipants.add(nameAndId);
        source.sendSuccess(() -> Component.translatable("commands.divorcesteal.gachaparticipant.add", target), true);
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestAdd(CommandContext<?> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider source)) return Suggestions.empty();

        List<String> invalidNames = GachaDataSaving.getGachaParticipantList().stream().map(NameAndId::name).toList();

        return SharedSuggestionProvider.suggest(
                source.getOnlinePlayerNames().stream()
                        .filter(string -> !invalidNames.contains(string))
                        .toList(),
                builder
        );
    }

    public static int executeRemove(CommandSourceStack source, String target) throws CommandSyntaxException {
        NameAndId nameAndId = Objects.requireNonNull(source.getServer().services().nameToIdCache()).get(target)
                .orElseThrow(DivorcestealExceptions.NO_PLAYER_EXCEPTION::create);

        List<NameAndId> gachaParticipants = GachaDataSaving.getGachaParticipantList();
        if (gachaParticipants.remove(nameAndId)) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.gachaparticipant.remove", target), true);
            return 1;
        } else {
            throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();
        }
    }

    public static CompletableFuture<Suggestions> suggestRemove(CommandContext<?> context, SuggestionsBuilder builder) {
        List<String> names = GachaDataSaving.getGachaParticipantList().stream().map(NameAndId::name).toList();

        return SharedSuggestionProvider.suggest(
                names,
                builder
        );
    }

    public static int executeList(CommandSourceStack source) {
        List<String> names = GachaDataSaving.getGachaParticipantList().stream().map(NameAndId::name).toList();

        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.gachaparticipant.list.empty"), true);
            return 0;
        }

        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (String name : names) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(name);
            first = false;
        }

        source.sendSuccess(() -> Component.translatable("commands.divorcesteal.gachaparticipant.list", builder.toString()), true);

        return names.size();
    }
}
