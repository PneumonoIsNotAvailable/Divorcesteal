package net.pneumono.divorcesteal.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.ParticipantHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {
    public static void registerDivorcestealCommands() {
        ArgumentTypeRegistry.registerArgumentType(Divorcesteal.id("heart_data"), ParticipantArgumentType.class, new ParticipantArgumentType.Serializer());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("divorcesteal")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("participants")
                            .then(literal("add")
                                    .then(argument("target", StringArgumentType.word())
                                            .executes(context -> executeParticipantAdd(context.getSource(),
                                                    StringArgumentType.getString(context, "target")
                                            ))
                                            .suggests(DivorcestealCommands::suggestParticipantAdd)
                                    )
                            )
                            .then(literal("remove")
                                    .then(argument("target", ParticipantArgumentType.participant())
                                            .executes(context -> executeParticipantRemove(context.getSource(),
                                                    ParticipantArgumentType.getParticipant(context, "target")
                                            ))
                                    )
                            )
                            .then(literal("list")
                                    .executes(context -> executeParticipantList(context.getSource()))
                            )
                    )
                    .then(literal("get")
                            .executes(context -> executeGet(context.getSource(),
                                    Hearts.getParticipantHeartData(context.getSource().getPlayerOrThrow())
                            ))
                            .then(argument("target", ParticipantArgumentType.participant())
                                    .executes(context -> executeGet(context.getSource(),
                                            ParticipantArgumentType.getParticipant(context, "target")
                                    ))
                            )
                    )
                    .then(literal("set")
                            .then(argument("amount", IntegerArgumentType.integer(0))
                                    .executes(context -> executeSet(context.getSource(),
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            List.of(dataFromSource(context.getSource())),
                                            false
                                    ))
                                    .then(argument("targets", ParticipantArgumentType.participants())
                                            .executes(context -> executeSet(context.getSource(),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    ParticipantArgumentType.getParticipants(context, "targets"),
                                                    false
                                            ))
                                            .then(argument("bypassMax", BoolArgumentType.bool())
                                                    .executes(context -> executeSet(context.getSource(),
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
                                    .executes(context -> executeAdd(context.getSource(), true,
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            List.of(dataFromSource(context.getSource())),
                                            false
                                    ))
                                    .then(argument("targets", ParticipantArgumentType.unbannedParticipants())
                                            .executes(context -> executeAdd(context.getSource(), true,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    ParticipantArgumentType.getParticipants(context, "targets"),
                                                    false
                                            ))
                                            .then(argument("bypassMax", BoolArgumentType.bool())
                                                    .executes(context -> executeAdd(context.getSource(), true,
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
                                    .executes(context -> executeAdd(context.getSource(), false,
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            List.of(dataFromSource(context.getSource())),
                                            false
                                    ))
                                    .then(argument("targets", ParticipantArgumentType.unbannedParticipants())
                                            .executes(context -> executeAdd(context.getSource(), false,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    ParticipantArgumentType.getParticipants(context, "targets"),
                                                    false
                                            ))
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
                    .executes(context -> executeWithdraw(context.getSource(), context.getSource().getPlayerOrThrow(), 1))
                    .then(argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> executeWithdraw(context.getSource(),
                                    context.getSource().getPlayerOrThrow(),
                                    IntegerArgumentType.getInteger(context, "amount")
                            ))
                    )
            );
        });
    }

    private static int executeParticipantAdd(ServerCommandSource source, String target) throws CommandSyntaxException {
        GameProfile profile = Objects.requireNonNull(source.getServer().getUserCache()).findByName(target)
                .orElseThrow(DivorcestealExceptions.NO_PLAYER_EXCEPTION::create);

        HeartDataState state = Hearts.getHeartDataState();
        if (state.getHeartData(profile.getId()) == null) {
            state.addParticipant(profile);

            updateData(source, state.getHeartData(profile.getId()));

            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.participant.add", profile.getName()), true);
        } else {
            throw DivorcestealExceptions.PARTICIPANT_LISTED_EXCEPTION.create();
        }

        return 1;
    }

    private static CompletableFuture<Suggestions> suggestParticipantAdd(CommandContext<?> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSource source)) return Suggestions.empty();

        List<String> invalidNames = Hearts.getHeartDataState().getHeartDataList().stream().map(ParticipantHeartData::getName).toList();

        return CommandSource.suggestMatching(
                source.getPlayerNames().stream()
                        .filter(string -> !invalidNames.contains(string))
                        .toList(),
                builder
        );
    }

    private static int executeParticipantRemove(ServerCommandSource source, ParticipantHeartData data) {
        data.setHearts(10);
        updateData(source, data);
        Hearts.getHeartDataState().removeParticipant(data.getUuid());

        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.participant.remove", data.getName()), true);

        return 1;
    }

    private static int executeParticipantList(ServerCommandSource source) {
        List<String> names = Hearts.getHeartDataState().getHeartDataList().stream().map(ParticipantHeartData::getName).toList();

        if (names.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.participant.list.empty"), true);
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

        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.participant.list", builder.toString()), true);

        return names.size();
    }

    private static int executeGet(ServerCommandSource source, ParticipantHeartData data) {
        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.get", data.getName(), data.getHearts()), true);
        return data.getHearts();
    }

    private static int executeSet(ServerCommandSource source, int amount, List<ParticipantHeartData> dataList, boolean bypassMax) throws CommandSyntaxException {
        if (dataList.isEmpty()) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();

        int finalAmount = bypassMax ? amount : MathHelper.clamp(amount, 0, DivorcestealConfig.MAX_HEARTS.getValue());

        for (ParticipantHeartData data : dataList) {
            data.setHearts(finalAmount);
            updateData(source, data);
            if (finalAmount == 0) {
                ServerPlayerEntity bannedPlayer = playerFromData(source, data);
                if (bannedPlayer != null) {
                    bannedPlayer.networkHandler.disconnect(Text.translatable("divorcesteal.deathban"));
                }
            }
        }

        if (dataList.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.set.single", dataList.getFirst().getName(), finalAmount), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.set.multiple", dataList.size(), finalAmount), true);
        }
        return dataList.size();
    }

    private static int executeAdd(ServerCommandSource source, boolean add, int amount, List<ParticipantHeartData> dataList, boolean bypassMax) throws CommandSyntaxException {
        if (dataList.isEmpty()) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();

        for (ParticipantHeartData data : dataList) {
            int hearts = data.getHearts();
            int finalAmount = Math.max(hearts + (add ? amount : -amount), 0);
            if (!bypassMax) {
                finalAmount = Math.min(finalAmount, Math.max(hearts, DivorcestealConfig.MAX_HEARTS.getValue()));
            }
            data.setHearts(finalAmount);
            updateData(source, data);
            if (finalAmount == 0) {
                ServerPlayerEntity bannedPlayer = playerFromData(source, data);
                if (bannedPlayer != null) {
                    bannedPlayer.networkHandler.disconnect(Text.translatable("divorcesteal.deathban"));
                }
            }
        }

        String translation = "commands.divorcesteal." + (add ? "add" : "remove") + ".";
        if (dataList.size() == 1) {
            source.sendFeedback(() -> Text.translatable(translation + "single", amount, dataList.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(translation + "multiple", amount, dataList.size()), true);
        }
        return dataList.size();
    }

    private static int executeRevive(ServerCommandSource source, List<ParticipantHeartData> dataList) throws CommandSyntaxException {
        if (dataList.isEmpty()) throw DivorcestealExceptions.NO_PARTICIPANT_EXCEPTION.create();

        boolean single = dataList.size() == 1;
        if (single) {
            if (!Hearts.revive(source.getWorld(), dataList.getFirst().getGameProfile())) throw DivorcestealExceptions.NOT_DEATHBANNED_EXCEPTION.create();

        } else {
            for (ParticipantHeartData data : dataList) {
                Hearts.revive(source.getWorld(), data.getGameProfile());
            }
        }

        if (dataList.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.revive.single", dataList.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.revive.multiple", dataList.size()), true);
        }
        return dataList.size();
    }

    private static int executeWithdraw(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        int heartsWithdrawn = -Hearts.addHeartsValidated(player, -amount, false);
        if (heartsWithdrawn == 0) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.withdraw.fail").formatted(Formatting.RED), false);
        } else if (heartsWithdrawn == 1) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.withdraw.single"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.withdraw.multiple", heartsWithdrawn), false);
        }

        ItemStack stack = new ItemStack(DivorcestealRegistry.HEART_ITEM, heartsWithdrawn);
        if (!stack.isEmpty() && !player.getInventory().insertStack(stack)) {
            ItemEntity itemEntity = player.dropItem(stack, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }

        player.increaseStat(DivorcestealRegistry.WITHDRAW_HEART_STAT, heartsWithdrawn);

        return heartsWithdrawn;
    }

    private static void updateData(ServerCommandSource source, ParticipantHeartData data) {
        Hearts.updateData(playerFromData(source, data));
    }

    private static @Nullable ParticipantHeartData dataFromSource(ServerCommandSource source) throws CommandSyntaxException {
        return Hearts.getParticipantHeartData(source.getPlayerOrThrow());
    }

    private static @Nullable ServerPlayerEntity playerFromData(ServerCommandSource source, ParticipantHeartData data) {
        return data == null ? null : (ServerPlayerEntity) source.getWorld().getPlayerByUuid(data.getUuid());
    }
}
