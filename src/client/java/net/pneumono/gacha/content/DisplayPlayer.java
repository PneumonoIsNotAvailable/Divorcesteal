package net.pneumono.gacha.content;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.pneumono.gacha.GachaRarity;

public record DisplayPlayer(GachaRarity rarity, PlayerModelType modelType, RenderType renderType) {

}
