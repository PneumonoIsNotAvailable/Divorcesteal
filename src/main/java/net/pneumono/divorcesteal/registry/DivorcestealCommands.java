package net.pneumono.divorcesteal.registry;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.content.HeartDataArgumentType;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {
    public static void registerDivorcestealCommands() {
        ArgumentTypeRegistry.registerArgumentType(Divorcesteal.id("heart_data"), HeartDataArgumentType.class, new HeartDataArgumentType.Serializer());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("divorcesteal")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("players")
                            .then(literal("add")
                                    .then(argument("target", StringArgumentType.word())
                                            .executes(context -> executePlayerAdd(context.getSource(),
                                                    StringArgumentType.getString(context, "target")
                                            ))
                                    )
                            )
                            .then(literal("remove")
                                    .then(argument("target", HeartDataArgumentType.player())
                                            .executes(context -> executePlayerRemove(context.getSource(),
                                                    HeartDataArgumentType.getPlayer(context, "target")
                                            ))
                                    )
                            )
                            .then(literal("list")
                                    .executes(context -> executePlayerList(context.getSource()))
                            )
                    )
                    .then(literal("get")
                            .executes(context -> executeGet(context.getSource(),
                                    Hearts.getPlayerHeartData(context.getSource().getPlayerOrThrow())
                            ))
                            .then(argument("target", HeartDataArgumentType.player())
                                    .executes(context -> executeGet(context.getSource(),
                                            HeartDataArgumentType.getPlayer(context, "target")
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
                                    .then(argument("targets", HeartDataArgumentType.players())
                                            .executes(context -> executeSet(context.getSource(),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    HeartDataArgumentType.getPlayers(context, "targets"),
                                                    false
                                            ))
                                            .then(argument("bypassMax", BoolArgumentType.bool())
                                                    .executes(context -> executeSet(context.getSource(),
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            HeartDataArgumentType.getPlayers(context, "targets"),
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
                                    .then(argument("targets", HeartDataArgumentType.unbannedPlayers())
                                            .executes(context -> executeAdd(context.getSource(), true,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    HeartDataArgumentType.getPlayers(context, "targets"),
                                                    false
                                            ))
                                            .then(argument("bypassMax", BoolArgumentType.bool())
                                                    .executes(context -> executeAdd(context.getSource(), true,
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            HeartDataArgumentType.getPlayers(context, "targets"),
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
                                    .then(argument("targets", HeartDataArgumentType.unbannedPlayers())
                                            .executes(context -> executeAdd(context.getSource(), false,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    HeartDataArgumentType.getPlayers(context, "targets"),
                                                    false
                                            ))
                                    )
                            )
                    )
                    .then(literal("revive")
                            .then(argument("targets", HeartDataArgumentType.bannedPlayers())
                                    .executes(context -> executeRevive(context.getSource(),
                                            HeartDataArgumentType.getPlayers(context, "targets")
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

    private static int executePlayerAdd(ServerCommandSource source, String target) throws CommandSyntaxException {
        GameProfile profile = Objects.requireNonNull(source.getServer().getUserCache()).findByName(target)
                .orElseThrow(HeartDataArgumentType.NO_PLAYER_EXCEPTION::create);

        HeartDataState state = Hearts.getHeartDataState();
        if (state.getHeartData(profile.getId()) == null) {
            state.addPlayer(profile);

            updateData(source, state.getHeartData(profile.getId()));

            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.player.add.single", profile.getName()), true);
        } else {
            throw HeartDataArgumentType.PLAYER_EXISTS_EXCEPTION.create();
        }

        return 1;
    }

    private static int executePlayerRemove(ServerCommandSource source, PlayerHeartData data) {
        data.setHearts(10);
        updateData(source, data);
        Hearts.getHeartDataState().removePlayer(data.getUuid());

        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.player.remove.single", data.getName()), true);

        return 1;
    }

    private static int executePlayerList(ServerCommandSource source) {
        List<String> names = Hearts.getHeartDataState().getHeartDataList().stream().map(PlayerHeartData::getName).toList();

        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (String name : names) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(name);
            first = false;
        }

        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.player.list", builder.toString()), true);

        return names.size();
    }

    private static int executeGet(ServerCommandSource source, PlayerHeartData data) {
        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.get", data.getName(), data.getHearts()), true);
        return data.getHearts();
    }

    private static int executeSet(ServerCommandSource source, int amount, List<PlayerHeartData> dataList, boolean bypassMax) throws CommandSyntaxException {
        if (dataList.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        int finalAmount = bypassMax ? amount : MathHelper.clamp(amount, 0, DivorcestealConfig.MAX_HEARTS.getValue());

        for (PlayerHeartData data : dataList) {
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

    private static int executeAdd(ServerCommandSource source, boolean add, int amount, List<PlayerHeartData> dataList, boolean bypassMax) throws CommandSyntaxException {
        if (dataList.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        for (PlayerHeartData data : dataList) {
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

    private static int executeRevive(ServerCommandSource source, List<PlayerHeartData> dataList) throws CommandSyntaxException {
        if (dataList.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        boolean single = dataList.size() == 1;
        if (single) {
            if (!Hearts.revive(source.getWorld(), dataList.getFirst().getGameProfile())) throw HeartDataArgumentType.NOT_DEATHBANNED_EXCEPTION.create();

        } else {
            for (PlayerHeartData data : dataList) {
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

    private static void updateData(ServerCommandSource source, PlayerHeartData data) {
        Hearts.updateData(playerFromData(source, data));
    }

    private static @Nullable PlayerHeartData dataFromSource(ServerCommandSource source) throws CommandSyntaxException {
        return Hearts.getPlayerHeartData(source.getPlayerOrThrow());
    }

    private static @Nullable ServerPlayerEntity playerFromData(ServerCommandSource source, PlayerHeartData data) {
        return data == null ? null : (ServerPlayerEntity) source.getWorld().getPlayerByUuid(data.getUuid());
    }
}
