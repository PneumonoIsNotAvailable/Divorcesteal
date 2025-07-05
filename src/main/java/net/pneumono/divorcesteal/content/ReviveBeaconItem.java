package net.pneumono.divorcesteal.content;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.hearts.Hearts;

public class ReviveBeaconItem extends Item {
    public ReviveBeaconItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new RevivablePlayersS2CPayload(
                    Hearts.getHeartDataState(serverWorld).getHeartDataList().stream().filter(data -> data.banDate() != null).toList()
            ));
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ActionResult.SUCCESS;
    }
}
