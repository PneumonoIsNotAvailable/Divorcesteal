package net.pneumono.divorcesteal.command;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;

public final class DivorcestealExceptions {
    public static final SimpleCommandExceptionType NO_PARTICIPANT_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.no_participant")
    );
    public static final SimpleCommandExceptionType PARTICIPANT_LISTED_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.participant_already_listed")
    );
    public static final SimpleCommandExceptionType NOT_DEATHBANNED_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("arguments.divorcesteal.error.not_deathbanned")
    );
    public static final SimpleCommandExceptionType NO_PLAYER_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("argument.player.unknown")
    );
}
