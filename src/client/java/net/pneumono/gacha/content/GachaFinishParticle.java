package net.pneumono.gacha.content;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import net.pneumono.gacha.GachaRarity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GachaFinishParticle extends SimpleAnimatedParticle {
    protected GachaFinishParticle(ClientLevel clientLevel, double x, double y, double z, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, x, y, z, spriteSet, 0.5f);
        this.xd = g;
        this.yd = h;
        this.zd = i;
    }

    @Override
    public void tick() {
        super.tick();
    }

    public static class Provider implements ParticleProvider<GachaRarityParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(
                GachaRarityParticleOption particleOption, @NonNull ClientLevel clientLevel,
                double d, double e, double f,
                double g, double h, double i,
                @NonNull RandomSource randomSource
        ) {
            GachaFinishParticle particle = new GachaFinishParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            GachaRarity rarity = particleOption.getRarity();
            particle.setColor(red(rarity), green(rarity), blue(rarity));
            return particle;
        }

        public static float red(GachaRarity rarity) {
            return switch (rarity) {
                case COMMON -> 0xC6;
                case UNCOMMON -> 0x8B;
                case RARE -> 0x89;
                case EPIC -> 0xBD;
                case LEGENDARY -> 0xE0;
                case MYTHIC -> 0xD1;
            } / (float)(0xFF);
        }

        public static float green(GachaRarity rarity) {
            return switch (rarity) {
                case COMMON -> 0xC6;
                case UNCOMMON -> 0xCE;
                case RARE -> 0xA4;
                case EPIC -> 0x93;
                case LEGENDARY -> 0xB3;
                case MYTHIC -> 0x53;
            } / (float)(0xFF);
        }

        public static float blue(GachaRarity rarity) {
            return switch (rarity) {
                case COMMON -> 0xC6;
                case UNCOMMON -> 0x73;
                case RARE -> 0xD6;
                case EPIC -> 0xD8;
                case LEGENDARY -> 0x70;
                case MYTHIC -> 0x5C;
            } / (float)(0xFF);
        }
    }
}
