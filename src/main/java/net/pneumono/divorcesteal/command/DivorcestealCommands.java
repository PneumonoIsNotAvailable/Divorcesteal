package net.pneumono.divorcesteal.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.hearts.HeartsUtil;
import net.pneumono.divorcesteal.hearts.ParticipantMap;
import net.pneumono.divorcesteal.hearts.Participant;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DivorcestealCommands {
    public static void registerDivorcestealCommands() {
        ArgumentTypeRegistry.registerArgumentType(Divorcesteal.id("heart_data"), ParticipantArgumentType.class, new ParticipantArgumentType.Serializer());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("divorcesteal")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(literal("participants")
                            .then(literal("add")
                                    .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                    .then(argument("target", StringArgumentType.word())
                                            .executes(context -> executeParticipantsAdd(context.getSource(),
                                                    StringArgumentType.getString(context, "target")
                                            ))
                                            .suggests(DivorcestealCommands::suggestParticipantsAdd)
                                    )
                            )
                            .then(literal("remove")
                                    .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                    .then(argument("target", ParticipantArgumentType.participant())
                                            .executes(context -> executeParticipantsRemove(context.getSource(),
                                                    ParticipantArgumentType.getParticipant(context, "target")
                                            ))
                                    )
                            )
                            .then(literal("list")
                                    .executes(context -> executeParticipantsList(context.getSource()))
                            )
                    )
                    .then(literal("hearts")
                            .then(literal("get")
                                    .executes(context -> executeHeartsGet(context.getSource(),
                                            HeartsUtil.getParticipant(context.getSource().getPlayerOrException())
                                    ))
                                    .then(argument("target", ParticipantArgumentType.participant())
                                            .executes(context -> executeHeartsGet(context.getSource(),
                                                    ParticipantArgumentType.getParticipant(context, "target")
                                            ))
                                    )
                            )
                            .then(literal("set")
                                    .then(argument("amount", IntegerArgumentType.integer(0))
                                            .executes(context -> executeHeartsSet(context.getSource(),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    List.of(participantFromSource(context.getSource())),
                                                    false
                                            ))
                                            .then(argument("targets", ParticipantArgumentType.participants())
                                                    .executes(context -> executeHeartsSet(context.getSource(),
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            ParticipantArgumentType.getParticipants(context, "targets"),
                                                            false
                                                    ))
                                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                                            .executes(context -> executeHeartsSet(context.getSource(),
                                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                                    ParticipantArgumentType.getParticipants(context, "targets"),
                                                                    BoolArgumentType.getBool(context, "bypassMax")
                                                            ))
                                                    )
                                            )
                                    )
                            )
                            .then(literal("add")
                                    .then(argument("amount", IntegerArgumentType.integer(0))
                                            .executes(context -> executeHeartsAdd(context.getSource(), true,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    List.of(participantFromSource(context.getSource())),
                                                    false
                                            ))
                                            .then(argument("targets", ParticipantArgumentType.unbannedParticipants())
                                                    .executes(context -> executeHeartsAdd(context.getSource(), true,
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            ParticipantArgumentType.getParticipants(context, "targets"),
                                                            false
                                                    ))
                                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                                            .executes(context -> executeHeartsAdd(context.getSource(), true,
                                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                                    ParticipantArgumentType.getParticipants(context, "targets"),
                                                                    BoolArgumentType.getBool(context, "bypassMax")
                                                            ))
                                                    )
                                            )
                                    )
                            )
                            .then(literal("remove")
                                    .then(argument("amount", IntegerArgumentType.integer(0))
                                            .executes(context -> executeHeartsAdd(context.getSource(), false,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    List.of(participantFromSource(context.getSource())),
                                                    false
                                            ))
                                            .then(argument("targets", ParticipantArgumentType.unbannedParticipants())
                                                    .executes(context -> executeHeartsAdd(context.getSource(), false,
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            ParticipantArgumentType.getParticipants(context, "targets"),
                                                            false
                                                    ))
                                            )
                                    )
                            )
                    )
                    .then(literal("revive")
                            .then(argument("targets", ParticipantArgumentType.bannedParticipants())
                                    .executes(context -> executeRevive(context.getSource(),
                                            ParticipantArgumentType.getParticipants(context, "targets")
                                    ))
                            )
                    )
            );
            dispatcher.register(literal("withdraw")
                    .executes(context -> executeWithdraw(context.getSource(), context.getSource().getPlayerOrException(), 1))
                    .then(argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> executeWithdraw(context.getSource(),
                                    context.getSource().getPlayerOrException(),
                                    IntegerArgumentType.getInteger(context, "amount")
                            ))
                    )
            );
        });
    }

    private static int executeParticipantsAdd(CommandSourceStack source, String target) throws CommandSyntaxException {
        NameAndId nameAndId = Objects.requireNonNull(source.getServer().services().nameToIdCache()).get(target)
                .orElseThrow(DivorcestealExceptions.NO_PLAYER_EXCEPTION::create);

        ParticipantMap map = HeartsUtil.getParticipantMap();
        if (map.getParticipant(nameAndId.id()) == null) {
            map.addParticipant(nameAndId);

            updateParticipant(source, map.getParticipant(nameAndId.id()), false);

            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.participant.add", nameAndId.name()), true);
        } else {
            throw DivorcestealExceptions.PARTICIPANT_LISTED_EXCEPTION.create();
        }

        return 1;
    }

    private static CompletableFuture<Suggestions> suggestParticipantsAdd(CommandContext<?> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof SharedSuggestionProvider source)) return Suggestions.empty();

        List<String> invalidNames = HeartsUtil.getParticipantMap().getParticipants().stream().map(Participant::getName).toList();

        return SharedSuggestionProvider.suggest(
                source.getOnlinePlayerNames().stream()
                        .filter(string -> !invalidNames.contains(string))
                        .toList(),
                builder
        );
    }

    private static int executeParticipantsRemove(CommandSourceStack source, Participant participant) {
        participant.setHearts(10);
        updateParticipant(source, participant, false);
        HeartsUtil.getParticipantMap().removeParticipant(participant.getUuid());

        source.sendSuccess(() -> Component.translatable("commands.divorcesteal.participant.remove", participant.getName()), true);

        return 1;
    }

    private static int executeParticipantsList(CommandSourceStack source) {
        List<String> names = HeartsUtil.getParticipantMap().getParticipants().stream().map(Participant::getName).toList();

        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.participant.list.empty"), true);
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

        source.sendSuccess(() -> Component.translatable("commands.divorcesteal.participant.list", builder.toString()), true);

        return names.size();
    }

    private static int executeHeartsGet(CommandSourceStack source, Participant participant) throws CommandSyntaxException {
        if (participant == null) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();
        source.sendSuccess(() -> Component.translatable("commands.divorcesteal.get", participant.getName(), participant.getHearts()), true);
        return participant.getHearts();
    }

    private static int executeHeartsSet(CommandSourceStack source, int amount, List<Participant> participants, boolean bypassMax) throws CommandSyntaxException {
        if (participants.isEmpty()) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();

        int finalAmount = bypassMax ? amount : Mth.clamp(amount, 0, DivorcestealConfig.MAX_HEARTS.getValue());

        int successes = 0;
        for (Participant participant : participants) {
            if (participant == null) continue;
            participant.setHearts(finalAmount);
            updateParticipant(source, participant);
            if (finalAmount == 0) {
                ServerPlayer bannedPlayer = playerFromParticipant(source, participant);
                if (bannedPlayer != null) {
                    bannedPlayer.connection.disconnect(Component.translatable("divorcesteal.deathban"));
                }
            }
            successes++;
        }

        if (participants.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.set.single", participants.getFirst().getName(), finalAmount), true);
        } else {
            int finalSuccesses = successes;
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.set.multiple", finalSuccesses, finalAmount), true);
        }
        return successes;
    }

    private static int executeHeartsAdd(CommandSourceStack source, boolean add, int amount, List<Participant> participants, boolean bypassMax) throws CommandSyntaxException {
        if (participants.isEmpty()) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();

        int successes = 0;
        for (Participant participant : participants) {
            if (participant == null) continue;
            int hearts = participant.getHearts();
            int finalAmount = Math.max(hearts + (add ? amount : -amount), 0);
            if (!bypassMax) {
                finalAmount = Math.min(finalAmount, Math.max(hearts, DivorcestealConfig.MAX_HEARTS.getValue()));
            }
            participant.setHearts(finalAmount);
            updateParticipant(source, participant);
            if (finalAmount == 0) {
                ServerPlayer bannedPlayer = playerFromParticipant(source, participant);
                if (bannedPlayer != null) {
                    bannedPlayer.connection.disconnect(Component.translatable("divorcesteal.deathban"));
                }
            }
            successes++;
        }

        String translation = "commands.divorcesteal." + (add ? "add" : "remove") + ".";
        if (participants.size() == 1) {
            source.sendSuccess(() -> Component.translatable(translation + "single", amount, participants.getFirst().getName()), true);
        } else {
            int finalSuccesses = successes;
            source.sendSuccess(() -> Component.translatable(translation + "multiple", amount, finalSuccesses), true);
        }
        return successes;
    }

    private static int executeRevive(CommandSourceStack source, List<Participant> participants) throws CommandSyntaxException {
        if (participants.isEmpty()) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();

        int successes = 0;
        boolean single = participants.size() == 1;
        if (single) {
            if (!HeartsUtil.revive(source.getLevel(), participants.getFirst().getUuid())) throw DivorcestealExceptions.NOT_DEATHBANNED_EXCEPTION.create();
            successes = 1;

        } else {
            for (Participant participant : participants) {
                if (participant == null) continue;
                HeartsUtil.revive(source.getLevel(), participant.getUuid());
                successes++;
            }
        }

        if (participants.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.revive.single", participants.getFirst().getName()), true);
        } else {
            int finalSuccesses = successes;
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.revive.multiple", finalSuccesses), true);
        }
        return successes;
    }

    private static int executeWithdraw(CommandSourceStack source, ServerPlayer player, int amount) {
        if (!HeartsUtil.isParticipant(player)) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.withdraw.non_participant").withStyle(ChatFormatting.RED), false);
            return 0;
        }

        int heartsWithdrawn = -HeartsUtil.addHeartsValidated(player, -amount, false);
        if (heartsWithdrawn == 0) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.withdraw.fail").withStyle(ChatFormatting.RED), false);
            return 0;
        } else if (heartsWithdrawn == 1) {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.withdraw.single"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.divorcesteal.withdraw.multiple", heartsWithdrawn), false);
        }

        ItemStack stack = new ItemStack(DivorcestealRegistry.HEART_ITEM, heartsWithdrawn);
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            ItemEntity itemEntity = player.drop(stack, false);
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay();
            }
        }

        player.awardStat(DivorcestealRegistry.WITHDRAW_HEART_STAT, heartsWithdrawn);

        return heartsWithdrawn;
    }

    private static void updateParticipant(CommandSourceStack source, Participant participant) {
        updateParticipant(source, participant, true);
    }

    private static void updateParticipant(CommandSourceStack source, Participant participant, boolean effects) {
        HeartsUtil.updateParticipant(playerFromParticipant(source, participant), source.getServer(), participant, effects);
    }

    private static @Nullable Participant participantFromSource(CommandSourceStack source) throws CommandSyntaxException {
        return HeartsUtil.getParticipant(source.getPlayerOrException());
    }

    private static @Nullable ServerPlayer playerFromParticipant(CommandSourceStack source, @Nullable Participant participant) {
        return participant == null ? null : (ServerPlayer) source.getLevel().getPlayerByUUID(participant.getUuid());
    }
}
