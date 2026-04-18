package net.pneumono.aprilfools.gacha.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.pneumono.aprilfools.gacha.GachaRegistry;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class GachaBeaconSpecialRenderer implements SpecialModelRenderer<GachaResult> {
    private final GachaBeaconRenderer renderer;

    public GachaBeaconSpecialRenderer(GachaBeaconRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void submit(
            @Nullable GachaResult data,
            @NonNull ItemDisplayContext context,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            int lightCoords, int j, boolean bl, int k
    ) {
        if (data != null) {
            this.renderer.submitWithPlayer(
                    0, this.renderer.createDisplayPlayer(data),
                    poseStack, collector, lightCoords, null
            );
        } else {
            this.renderer.submitWithoutPlayer(
                    poseStack, collector, lightCoords, null
            );
        }
    }

    @Override
    public void getExtents(@NonNull Consumer<Vector3fc> consumer) {

    }

    @Override
    public @Nullable GachaResult extractArgument(@NonNull ItemStack stack) {
        return stack.get(GachaRegistry.GACHA_DATA_COMPONENT);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<GachaBeaconSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @NonNull MapCodec<GachaBeaconSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.@NonNull BakingContext bakingContext) {
            return new GachaBeaconSpecialRenderer(new GachaBeaconRenderer(bakingContext));
        }
    }
}
