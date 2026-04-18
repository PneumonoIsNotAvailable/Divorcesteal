package net.pneumono.aprilfools.gacha.content;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.pneumono.aprilfools.gacha.GachaRarity;

public record DisplayPlayer(GachaRarity rarity, PlayerModelType modelType, RenderType renderType) {

}
