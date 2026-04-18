package net.pneumono.gacha;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.pneumono.divorcesteal.command.DivorcestealExceptions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GachaCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("gachaparticipant")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(literal("add")
                            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                            .then(argument("target", StringArgumentType.word())
                                    .executes(context -> executeAdd(context.getSource(),
                                            StringArgumentType.getString(context, "target")
                                    ))
                                    .suggests(GachaCommands::suggestAdd)
                            )
                    )
                    .then(literal("remove")
                            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                            .then(argument("target", StringArgumentType.word())
                                    .executes(context -> executeRemove(context.getSource(),
                                            StringArgumentType.getString(context, "target")
                                    ))
                                    .suggests(GachaCommands::suggestRemove)
                            )
                    )
                    .then(literal("list")
                            .executes(context -> executeList(context.getSource()))
                    )
            );
        });
    }

    private static int executeAdd(CommandSourceStack source, String target) throws CommandSyntaxException {
        NameAndId nameAndId = Objects.requireNonNull(source.getServer().services().nameToIdCache()).get(target)
                .orElseThrow(DivorcestealExceptions.NO_PLAYER_EXCEPTION::create);

        List<NameAndId> gachaParticipants = GachaDataSaving.getGachaParticipantList();
        gachaParticipants.add(nameAndId);
        source.sendSuccess(() -> Component.translatable("commands.divorcesteal.gachaparticipant.add", target), true);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestAdd(CommandContext<?> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider source)) return Suggestions.empty();

        List<String> invalidNames = GachaDataSaving.getGachaParticipantList().stream().map(NameAndId::name).toList();

        return SharedSuggestionProvider.suggest(
                source.getOnlinePlayerNames().stream()
                        .filter(string -> !invalidNames.contains(string))
                        .toList(),
                builder
        );
    }

    private static int executeRemove(CommandSourceStack source, String target) throws CommandSyntaxException {
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

    private static CompletableFuture<Suggestions> suggestRemove(CommandContext<?> context, SuggestionsBuilder builder) {
        List<String> names = GachaDataSaving.getGachaParticipantList().stream().map(NameAndId::name).toList();

        return SharedSuggestionProvider.suggest(
                names,
                builder
        );
    }

    private static int executeList(CommandSourceStack source) {
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
