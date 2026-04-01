package net.pneumono.gacha.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record GachaRoll(float randomSpinMultiplier, List<GachaResult> possibleResults) {
    public static final Codec<GachaRoll> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("random_spin_multiplier").forGetter(GachaRoll::randomSpinMultiplier),
            GachaResult.CODEC.listOf().fieldOf("possible_results").forGetter(GachaRoll::possibleResults)
    ).apply(instance, GachaRoll::new));
}
