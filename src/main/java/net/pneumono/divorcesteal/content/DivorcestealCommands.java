package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
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
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import net.pneumono.divorcesteal.hearts.PlayerHeartDataReference;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {
    public static final SimpleCommandExceptionType NO_DATA_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("commands.hearts.error.no_data")
    );

    public static void registerDivorcestealCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("divorcesteal-admin")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(literal("get")
                            .executes(context -> executeGet(context.getSource(), List.of(context.getSource().getPlayerOrThrow().getGameProfile())))
                            .then(argument("target", GameProfileArgumentType.gameProfile())
                                    .suggests(DivorcestealCommands::suggestions)
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
                                            .suggests(DivorcestealCommands::suggestions)
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
                                    .suggests(DivorcestealCommands::suggestions)
                                    .executes(context -> executeSet(context.getSource(),
                                            Hearts.DEFAULT_HEARTS.get(),
                                            GameProfileArgumentType.getProfileArgument(context, "targets"),
                                            false
                                    ))
                            )
                    )
                    .then(literal("add")
                            .then(argument("amount", IntegerArgumentType.integer(0))
                                    .executes(context -> executeAdd(context.getSource(), true,
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            List.of(context.getSource().getPlayerOrThrow().getGameProfile()),
                                            false
                                    ))
                                    .then(argument("targets", GameProfileArgumentType.gameProfile())
                                            .suggests(DivorcestealCommands::suggestions)
                                            .executes(context -> executeAdd(context.getSource(), true,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    GameProfileArgumentType.getProfileArgument(context, "targets"),
                                                    false
                                            ))
                                            .then(argument("bypassMax", BoolArgumentType.bool())
                                                    .executes(context -> executeAdd(context.getSource(), true,
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
                                    .executes(context -> executeAdd(context.getSource(), false,
                                            IntegerArgumentType.getInteger(context, "amount"),
                                            List.of(context.getSource().getPlayerOrThrow().getGameProfile()),
                                            false
                                    ))
                                    .then(argument("targets", GameProfileArgumentType.gameProfile())
                                            .suggests(DivorcestealCommands::suggestions)
                                            .executes(context -> executeAdd(context.getSource(), false,
                                                    IntegerArgumentType.getInteger(context, "amount"),
                                                    GameProfileArgumentType.getProfileArgument(context, "targets"),
                                                    false
                                            ))
                                            .then(argument("bypassMax", BoolArgumentType.bool())
                                                    .executes(context -> executeAdd(context.getSource(), false,
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            GameProfileArgumentType.getProfileArgument(context, "targets"),
                                                            BoolArgumentType.getBool(context, "bypassMax")
                                                    ))
                                            )
                                    )
                            )
                    )
                    .then(literal("refresh")
                            .then(argument("targets", GameProfileArgumentType.gameProfile())
                                    .suggests(DivorcestealCommands::suggestions)
                                    .executes(context -> executeRefresh(context.getSource(),
                                            GameProfileArgumentType.getProfileArgument(context, "targets")
                                    ))
                            )
                    )
            );
            dispatcher.register(literal("withdraw-hearts")
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

    private static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        HeartDataState state = Hearts.getHeartDataState(context.getSource().getWorld());
        return CommandSource.suggestMatching(
                state.getHeartDataList()
                        .stream()
                        .map(PlayerHeartData::name),
                builder
        );
    }

    private static int executeGet(ServerCommandSource source, Collection<GameProfile> profiles) throws CommandSyntaxException {
        if (profiles.size() > 1) throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        if (profiles.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        GameProfile profile = getFirst(profiles);
        HeartDataState heartDataState = getHeartDataState(source);

        if (!heartDataState.hasData(profile.getId())) throw NO_DATA_EXCEPTION.create();

        PlayerHeartDataReference player = new PlayerHeartDataReference(heartDataState, profile);
        source.sendFeedback(() -> Text.translatable("commands.hearts.get", player.getName(), player.getHearts()), true);

        return player.getHearts();
    }

    private static int executeSet(ServerCommandSource source, int amount, Collection<GameProfile> profiles, boolean bypassMax) throws CommandSyntaxException {
        if (profiles.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        int finalAmount = bypassMax ? amount : MathHelper.clamp(amount, 0, Hearts.MAX_HEARTS.get());

        for (PlayerHeartDataReference reference : referencesFromProfiles(source, profiles)) {
            reference.setHearts(finalAmount);
        }
        for (ServerPlayerEntity player : playersFromProfiles(source, profiles)) {
            Hearts.updateHearts(player);
        }
        if (profiles.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.hearts.set.single", getFirst(profiles).getName(), finalAmount), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.hearts.set.multiple", profiles.size(), finalAmount), true);
        }
        return profiles.size();
    }

    private static int executeAdd(ServerCommandSource source, boolean add, int amount, Collection<GameProfile> profiles, boolean bypassMax) throws CommandSyntaxException {
        if (profiles.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();

        List<PlayerHeartDataReference> references = referencesFromProfiles(source, profiles);

        for (PlayerHeartDataReference reference : referencesFromProfiles(source, profiles)) {
            int hearts = reference.getHearts();
            int finalAmount = Math.max(hearts + (add ? amount : -amount), 0);
            if (!bypassMax) {
                finalAmount = Math.min(finalAmount, Math.max(hearts, Hearts.MAX_HEARTS.get()));
            }
            reference.setHearts(finalAmount);
        }
        for (ServerPlayerEntity player : playersFromProfiles(source, profiles)) {
            Hearts.updateHearts(player);
        }

        String translation = "commands.hearts." + (add ? "add" : "remove") + ".";
        if (references.size() == 1) {
            source.sendFeedback(() -> Text.translatable(translation + "single", amount, references.getFirst().getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(translation + "multiple", amount, references.size()), true);
        }
        return references.size();
    }

    private static int executeRefresh(ServerCommandSource source, Collection<GameProfile> profiles) {
        for (GameProfile profile : profiles) {
            PlayerHeartDataReference reference = new PlayerHeartDataReference(Hearts.getHeartDataState(source.getWorld()), profile);
            reference.setName(profile.getName());
        }
        for (ServerPlayerEntity player : playersFromProfiles(source, profiles)) {
            Hearts.updateHearts(player);
        }

        if (profiles.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.hearts.refresh.single", getFirst(profiles).getName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.hearts.refresh.multiple", profiles.size()), true);
        }

        return profiles.size();
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

    private static HeartDataState getHeartDataState(ServerCommandSource source) {
        return Hearts.getHeartDataState(source.getWorld());
    }

    private static GameProfile getFirst(Collection<GameProfile> profiles) {
        return profiles.toArray(GameProfile[]::new)[0];
    }

    private static List<PlayerHeartDataReference> referencesFromProfiles(ServerCommandSource source, Collection<GameProfile> profiles) {
        return profiles.stream().map(profile -> new PlayerHeartDataReference(Hearts.getHeartDataState(source.getWorld()), profile)).toList();
    }

    private static List<ServerPlayerEntity> playersFromProfiles(ServerCommandSource source, Collection<GameProfile> profiles) {
        return profiles.stream().map(profile -> (ServerPlayerEntity) source.getWorld().getPlayerByUuid(profile.getId())).filter(Objects::nonNull).toList();
    }
}
