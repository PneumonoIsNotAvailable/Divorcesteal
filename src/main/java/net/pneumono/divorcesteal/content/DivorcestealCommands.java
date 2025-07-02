package net.pneumono.divorcesteal.content;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.hearts.Hearts;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {
    private static final ArgumentBuilder<ServerCommandSource, ?> ADMIN = literal("admin")
            .requires(source -> source.hasPermissionLevel(3))
            .then(literal("get")
                    .executes(context -> getHearts(context.getSource(), context.getSource().getPlayerOrThrow()))
                    .then(argument("player", EntityArgumentType.player())
                            .executes(context -> getHearts(context.getSource(),
                                    EntityArgumentType.getPlayer(context, "player")
                            ))
                    )
            )
            .then(literal("set")
                    .then(argument("players", EntityArgumentType.players())
                            .then(argument("amount", IntegerArgumentType.integer(0))
                                    .executes(context -> setHearts(context.getSource(),
                                            EntityArgumentType.getPlayers(context, "players"),
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            false
                                    ))
                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                            .executes(context -> setHearts(context.getSource(),
                                                    EntityArgumentType.getPlayers(context, "players"),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    BoolArgumentType.getBool(context, "bypassMax")
                                            ))
                                    )
                            )
                    )
            )
            .then(literal("reset")
                    .then(argument("players", EntityArgumentType.players())
                            .executes(context -> setHearts(context.getSource(),
                                    EntityArgumentType.getPlayers(context, "players"),
                                    Divorcesteal.DEFAULT_HEARTS.get(),
                                    false
                            ))
                    )
            )
            .then(literal("add")
                    .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> addHearts(context.getSource(),
                                    List.of(context.getSource().getPlayerOrThrow()),
                                    IntegerArgumentType.getInteger(context, "amount"),
                                    false
                            ))
                            .then(argument("players", EntityArgumentType.players())
                                    .executes(context -> addHearts(context.getSource(),
                                            EntityArgumentType.getPlayers(context, "players"),
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            false
                                    ))
                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                            .executes(context -> addHearts(context.getSource(),
                                                    EntityArgumentType.getPlayers(context, "players"),
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    BoolArgumentType.getBool(context, "bypassMax")
                                            ))
                                    )
                            )
                    )
            )
            .then(literal("remove")
                    .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> addHearts(context.getSource(),
                                    List.of(context.getSource().getPlayerOrThrow()),
                                    -IntegerArgumentType.getInteger(context, "amount"),
                                    false
                            ))
                            .then(argument("players", EntityArgumentType.players())
                                    .executes(context -> addHearts(context.getSource(),
                                            EntityArgumentType.getPlayers(context, "players"),
                                            -IntegerArgumentType.getInteger(context, "amount"),
                                            false
                                    ))
                                    .then(argument("bypassMax", BoolArgumentType.bool())
                                            .executes(context -> addHearts(context.getSource(),
                                                    EntityArgumentType.getPlayers(context, "players"),
                                                    -IntegerArgumentType.getInteger(context, "amount"),
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
                                .executes(context -> withdrawHearts(context.getSource(), context.getSource().getPlayerOrThrow(), 1))
                                .then(argument("amount", IntegerArgumentType.integer(0, Divorcesteal.MAX_HEARTS.get()))
                                        .executes(context -> withdrawHearts(context.getSource(), context.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(context, "amount")))
                                )
                        )
        ));
    }

    private static int getHearts(ServerCommandSource source, ServerPlayerEntity player) {
        int hearts = Hearts.getHearts(player);
        source.sendFeedback(() -> player.getName().copy().append(" has " + hearts + " hearts"), true);

        return 1;
    }

    private static int setHearts(ServerCommandSource source, Collection<ServerPlayerEntity> players, int amount, boolean bypassMax) {
        int finalAmount = bypassMax ? amount : MathHelper.clamp(amount, 0, Divorcesteal.MAX_HEARTS.get());

        for (ServerPlayerEntity player : players) {
            Hearts.setHearts(player, finalAmount);
        }
        if (players.size() == 1) {
            source.sendFeedback(() -> Text.literal("Set ").append(players.toArray(ServerPlayerEntity[]::new)[0].getName()).append(Text.literal(" to " + finalAmount + " hearts")), true);
        } else {
            source.sendFeedback(() -> Text.literal("Set " + players.size() + " players to " + finalAmount + " hearts"), true);
        }
        return players.size();
    }

    private static int addHearts(ServerCommandSource source, Collection<ServerPlayerEntity> players, int amount, boolean bypassMax) {
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
            source.sendFeedback(() -> Text.literal("Gave ").append(players.toArray(ServerPlayerEntity[]::new)[0].getName()).append(Text.literal(" " + amount + " hearts")), true);
        } else {
            source.sendFeedback(() -> Text.literal("Gave " + players.size() + " players " + amount + " hearts"), true);
        }
        return players.size();
    }

    private static int withdrawHearts(ServerCommandSource source, ServerPlayerEntity player, int amount) {
        int heartsWithdrawn = -Hearts.addHeartsValidated(player, -amount, false);
        if (heartsWithdrawn == 0) {
            source.sendFeedback(() -> Text.literal("Could not withdraw any more hearts!").formatted(Formatting.RED), false);
        } else if (heartsWithdrawn == 1) {
            source.sendFeedback(() -> Text.literal("Withdrew 1 heart"), false);
        } else {
            source.sendFeedback(() -> Text.literal("Withdrew " + heartsWithdrawn + " hearts"), false);
        }

        return amount;
    }
}
