package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartDataReference;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {
    private static final ArgumentBuilder<ServerCommandSource, ?> ADMIN = literal("admin")
            .requires(source -> source.hasPermissionLevel(3))
            .then(literal("get")
                    .executes(context -> executeGet(context.getSource(), List.of(context.getSource().getPlayerOrThrow().getGameProfile())))
                    .then(argument("target", GameProfileArgumentType.gameProfile())
                            .executes(context -> executeGet(context.getSource(),
                                    GameProfileArgumentType.getProfileArgument(context, "target")
                            ))
                    )
            )
            .then(literal("set")
                    .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeSet(context.getSource(),
                                    IntegerArgumentType.getInteger(context, "amount"),
                                    List.of(context.getSource().getPlayerOrThrow().getGameProfile()),
                                    false
                            ))
                            .then(argument("targets", GameProfileArgumentType.gameProfile())
                                    .executes(context -> executeSet(context.getSource(),
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            GameProfileArgumentType.getProfileArgument(context, "targets"),
                                            false
                                    ))
                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                            .executes(context -> executeSet(context.getSource(),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    GameProfileArgumentType.getProfileArgument(context, "targets"),
                                                    BoolArgumentType.getBool(context, "bypassMax")
                                            ))
                                    )
                            )
                    )
            )
            .then(literal("reset")
                    .executes(context -> executeSet(context.getSource(),
                            Hearts.DEFAULT_HEARTS.get(),
                            List.of(context.getSource().getPlayerOrThrow().getGameProfile()),
                            false
                    ))
                    .then(argument("targets", GameProfileArgumentType.gameProfile())
                            .executes(context -> executeSet(context.getSource(),
                                    Hearts.DEFAULT_HEARTS.get(),
                                    GameProfileArgumentType.getProfileArgument(context, "targets"),
                                    false
                            ))
                    )
            )
            .then(literal("add")
                    .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeAdd(context.getSource(),
                                    IntegerArgumentType.getInteger(context, "amount"),
                                    List.of(context.getSource().getPlayerOrThrow().getGameProfile()),
                                    false
                            ))
                            .then(argument("targets", GameProfileArgumentType.gameProfile())
                                    .executes(context -> executeAdd(context.getSource(),
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            GameProfileArgumentType.getProfileArgument(context, "targets"),
                                            false
                                    ))
                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                            .executes(context -> executeAdd(context.getSource(),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    GameProfileArgumentType.getProfileArgument(context, "targets"),
                                                    BoolArgumentType.getBool(context, "bypassMax")
                                            ))
                                    )
                            )
                    )
            )
            .then(literal("remove")
                    .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeAdd(context.getSource(),
                                    -IntegerArgumentType.getInteger(context, "amount"),
                                    List.of(context.getSource().getPlayerOrThrow().getGameProfile()),
                                    false
                            ))
                            .then(argument("targets", GameProfileArgumentType.gameProfile())
                                    .executes(context -> executeAdd(context.getSource(),
                                            -IntegerArgumentType.getInteger(context, "amount"),
                                            GameProfileArgumentType.getProfileArgument(context, "targets"),
                                            false
                                    ))
                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                            .executes(context -> executeAdd(context.getSource(),
                                                    -IntegerArgumentType.getInteger(context, "amount"),
                                                    GameProfileArgumentType.getProfileArgument(context, "targets"),
                                                    BoolArgumentType.getBool(context, "bypassMax")
                                            ))
                                    )
                            )
                    )
            );

    public static void registerDivorcestealCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(
                literal("hearts")
                        .then(ADMIN)
                        .then(literal("withdraw")
                                .executes(context -> executeWithdraw(context.getSource(), context.getSource().getPlayerOrThrow(), 1))
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> executeWithdraw(context.getSource(),
                                                context.getSource().getPlayerOrThrow(),
                                                IntegerArgumentType.getInteger(context, "amount")
                                        ))
                                )
                        )
                        .then(literal("refresh")
                                .executes(context -> executeRefresh(context.getSource(),
                                        context.getSource().getPlayerOrThrow()
                                ))
                        )
        ));
    }

    private static int executeGet(ServerCommandSource source, Collection<GameProfile> profiles) throws CommandSyntaxException {
        if (profiles.size() > 1) throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        if (profiles.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        PlayerHeartDataReference player = new PlayerHeartDataReference(getHeartDataState(source), getFirst(profiles));
        source.sendFeedback(() -> Text.translatable("commands.hearts.get", player.getName(), player.getHearts()), true);

        return player.getHearts();
    }

    private static int executeSet(ServerCommandSource source, int amount, Collection<GameProfile> profiles, boolean bypassMax) throws CommandSyntaxException {
        if (profiles.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        int finalAmount = bypassMax ? amount : MathHelper.clamp(amount, 0, Hearts.MAX_HEARTS.get());

        for (ServerPlayerEntity player : playersFromProfiles(source, profiles)) {
            Hearts.setHearts(player, finalAmount);
        }
        if (profiles.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.hearts.set.single", getFirst(profiles).getName(), finalAmount), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.hearts.set.multiple", profiles.size(), finalAmount), true);
        }
        return profiles.size();
    }

    private static int executeAdd(ServerCommandSource source, int amount, Collection<GameProfile> profiles, boolean bypassMax) throws CommandSyntaxException {
        if (profiles.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        List<ServerPlayerEntity> players = playersFromProfiles(source, profiles);

        if (bypassMax) {
            for (ServerPlayerEntity player : players) {
                Hearts.addHearts(player, amount);
            }
        } else {
            for (ServerPlayerEntity player : players) {
                Hearts.addHeartsValidated(player, amount, true);
            }
        }

        if (players.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.hearts.add.single", amount, players.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.hearts.add.multiple", amount, players.size()), true);
        }
        return players.size();
    }

    private static int executeWithdraw(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        int heartsWithdrawn = -Hearts.addHeartsValidated(player, -amount, false);
        if (heartsWithdrawn == 0) {
            source.sendFeedback(() -> Text.translatable("commands.hearts.withdraw.fail").formatted(Formatting.RED), false);
        } else if (heartsWithdrawn == 1) {
            source.sendFeedback(() -> Text.translatable("commands.hearts.withdraw.single"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.hearts.withdraw.multiple", heartsWithdrawn), false);
        }

        ItemStack stack = DivorcestealRegistry.HEART_ITEM.getDefaultStack().copyWithCount(heartsWithdrawn);
        if (!stack.isEmpty() && !player.getInventory().insertStack(stack)) {
            ItemEntity itemEntity = player.dropItem(stack, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }

        return heartsWithdrawn;
    }

    private static int executeRefresh(ServerCommandSource source, ServerPlayerEntity player) {
        Hearts.updateHearts(player);
        source.sendFeedback(() -> Text.translatable("commands.hearts.refresh"), false);
        return 1;
    }

    private static HeartDataState getHeartDataState(ServerCommandSource source) {
        return Hearts.getHeartDataState(source.getWorld());
    }

    private static GameProfile getFirst(Collection<GameProfile> profiles) {
        return profiles.toArray(GameProfile[]::new)[0];
    }

    private static List<ServerPlayerEntity> playersFromProfiles(ServerCommandSource source, Collection<GameProfile> profiles) {
        return profiles.stream().map(profile -> (ServerPlayerEntity) source.getWorld().getPlayerByUuid(profile.getId())).filter(Objects::nonNull).toList();
    }
}
