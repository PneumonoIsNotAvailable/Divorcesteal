package net.pneumono.aprilfools;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.pneumono.aprilfools.gacha.GachaCommands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AprilFoolsCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
            dispatcher.register(literal("divorcesteal").then(literal("aprilfools")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(literal("gacha")
                            .then(literal("add")
                                    .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                    .then(argument("target", StringArgumentType.word())
                                            .executes(context -> GachaCommands.executeAdd(context.getSource(),
                                                    StringArgumentType.getString(context, "target")
                                            ))
                                            .suggests(GachaCommands::suggestAdd)
                                    )
                            )
                            .then(literal("remove")
                                    .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                    .then(argument("target", StringArgumentType.word())
                                            .executes(context -> GachaCommands.executeRemove(context.getSource(),
                                                    StringArgumentType.getString(context, "target")
                                            ))
                                            .suggests(GachaCommands::suggestRemove)
                                    )
                            )
                            .then(literal("list")
                                    .executes(context -> GachaCommands.executeList(context.getSource()))
                            )
                    )
            ));
        });
    }
}
