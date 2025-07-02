package net.pneumono.divorcesteal.content;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.hearts.Hearts;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DivorcestealCommands {

    public static void registerDivorcestealCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> dispatcher.register(
                literal("hearts")
                        .then(literal("get")
                                .requires(source -> source.hasPermissionLevel(3))
                                .executes(context -> getHearts(context.getSource(), context.getSource().getPlayerOrThrow()))
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> getHearts(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                                )
                        )
                        .then(literal("set")
                                .requires(source -> source.hasPermissionLevel(3))
                                .then(argument("players", EntityArgumentType.players())
                                        .then(argument("amount", IntegerArgumentType.integer(0, Divorcesteal.MAX_HEARTS.get()))
                                                .executes(context -> setHearts(context.getSource(), EntityArgumentType.getPlayers(context, "players"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                                )
                        )
        ));
    }

    private static int getHearts(ServerCommandSource source, ServerPlayerEntity player) {
        int hearts = Hearts.getHearts(player);
        source.sendFeedback(() -> player.getName().copy().append(" has " + hearts + " hearts"), true);

        return 1;
    }

    private static int setHearts(ServerCommandSource source, Collection<ServerPlayerEntity> players, int amount) {
        for (ServerPlayerEntity player : players) {
            Hearts.setHearts(player, amount);
        }
        if (players.size() == 1) {
            source.sendFeedback(() -> Text.literal("Set ").append(players.toArray(ServerPlayerEntity[]::new)[0].getName()).append(Text.literal(" to " + amount + " hearts")), true);
        } else {
            source.sendFeedback(() -> Text.literal("Set " + players.size() + " players to " + amount + " hearts"), true);
        }
        return players.size();
    }
}
