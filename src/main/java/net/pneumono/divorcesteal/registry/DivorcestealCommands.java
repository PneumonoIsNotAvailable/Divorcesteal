package net.pneumono.divorcesteal.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.pneumono.divorcesteal.hearts.PlayerHeartDataReference;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {
    public static void registerDivorcestealCommands() {
        ArgumentTypeRegistry.registerArgumentType(Divorcesteal.id("heart_data"), HeartDataArgumentType.class, new HeartDataArgumentType.Serializer());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("divorcesteal")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("get")
                            .executes(context -> executeGet(context.getSource(), PlayerHeartDataReference.create(context.getSource().getPlayerOrThrow())))
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
                                            List.of(referenceFromSource(context.getSource())),
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
                                            List.of(referenceFromSource(context.getSource())),
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
                                            List.of(referenceFromSource(context.getSource())),
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
                    .then(literal("delete")
                            .then(argument("targets", HeartDataArgumentType.players())
                                    .executes(context -> executeDelete(context.getSource(),
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

    private static int executeGet(ServerCommandSource source, PlayerHeartDataReference reference) {
        source.sendFeedback(() -> Text.translatable("commands.divorcesteal.get", reference.getName(), reference.getHearts()), true);
        return reference.getHearts();
    }

    private static int executeSet(ServerCommandSource source, int amount, List<PlayerHeartDataReference> references, boolean bypassMax) throws CommandSyntaxException {
        if (references.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        int finalAmount = bypassMax ? amount : MathHelper.clamp(amount, 0, DivorcestealConfig.MAX_HEARTS.getValue());

        for (PlayerHeartDataReference reference : references) {
            reference.setHearts(finalAmount);
            updateData(source, reference);
        }

        if (references.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.set.single", references.getFirst().getName(), finalAmount), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.set.multiple", references.size(), finalAmount), true);
        }
        return references.size();
    }

    private static int executeAdd(ServerCommandSource source, boolean add, int amount, List<PlayerHeartDataReference> references, boolean bypassMax) throws CommandSyntaxException {
        if (references.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        for (PlayerHeartDataReference reference : references) {
            int hearts = reference.getHearts();
            int finalAmount = Math.max(hearts + (add ? amount : -amount), 0);
            if (!bypassMax) {
                finalAmount = Math.min(finalAmount, Math.max(hearts, DivorcestealConfig.MAX_HEARTS.getValue()));
            }
            reference.setHearts(finalAmount);
            updateData(source, reference);
        }

        String translation = "commands.divorcesteal." + (add ? "add" : "remove") + ".";
        if (references.size() == 1) {
            source.sendFeedback(() -> Text.translatable(translation + "single", amount, references.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(translation + "multiple", amount, references.size()), true);
        }
        return references.size();
    }

    private static int executeRevive(ServerCommandSource source, List<PlayerHeartDataReference> references) throws CommandSyntaxException {
        if (references.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        boolean single = references.size() == 1;
        if (single) {
            if (!Hearts.revive(source.getWorld(), references.getFirst().getGameProfile())) throw HeartDataArgumentType.NOT_DEATHBANNED_EXCEPTION.create();

        } else {
            for (PlayerHeartDataReference reference : references) {
                Hearts.revive(source.getWorld(), reference.getGameProfile());
            }
        }

        if (references.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.revive.single", references.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.revive.multiple", references.size()), true);
        }
        return references.size();
    }

    private static int executeDelete(ServerCommandSource source, List<PlayerHeartDataReference> references) throws CommandSyntaxException {
        if (references.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        HeartDataState state = getHeartDataState(source);
        for (PlayerHeartDataReference reference : references) {
            reference.delete();
            PlayerEntity player = playerFromReference(source, reference);
            if (player != null) {
                state.getOrCreateHeartData(player.getGameProfile().getId(), player.getGameProfile().getName());
                Hearts.updateData(player);
            } else {
                Hearts.updateBan(source.getServer(), reference, false);
            }
        }

        if (references.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.delete.single", references.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.divorcesteal.delete.multiple", references.size()), true);
        }

        return references.size();
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

    public static HeartDataState getHeartDataState(ServerCommandSource source) {
        return Hearts.getHeartDataState(source.getWorld());
    }

    private static void updateData(ServerCommandSource source, PlayerHeartDataReference reference) {
        Hearts.updateData(playerFromReference(source, reference), source.getServer(), reference);
    }

    private static PlayerHeartDataReference referenceFromSource(ServerCommandSource source) throws CommandSyntaxException {
        return PlayerHeartDataReference.create(source.getPlayerOrThrow());
    }

    private static ServerPlayerEntity playerFromReference(ServerCommandSource source, PlayerHeartDataReference reference) {
        return (ServerPlayerEntity) source.getWorld().getPlayerByUuid(reference.getUUID());
    }
}
