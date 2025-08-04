package net.pneumono.divorcesteal.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import net.pneumono.pneumonocore.datagen.PneumonoCoreTranslationBuilder;

import java.util.concurrent.CompletableFuture;

public class DivorcestealEnUdProvider extends FabricLanguageProvider {
    public DivorcestealEnUdProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_ud", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        PneumonoCoreTranslationBuilder builder = new PneumonoCoreTranslationBuilder(translationBuilder, Divorcesteal.MOD_ID);

        builder.add(DivorcestealRegistry.HEART_ITEM, "ʇɹɐǝH");
        builder.add("item.divorcesteal.heart.crafted", "pǝʇɟɐɹƆ");
        builder.add(DivorcestealRegistry.REVIVE_BEACON_ITEM, "uoɔɐǝᗺ ǝʌᴉʌǝᴚ");
        builder.add("item.divorcesteal.revive_beacon.target", "%s :ʇǝᵷɹɐ⟘");
        builder.add("item.divorcesteal.player_head.killer", "%s ʎq pǝꞁꞁᴉꞰ");

        builder.add("divorcesteal.gui.deathban.title", "¡sʇɹɐǝɥ ɟo ʇno uɐɹ no⅄");
        builder.add("divorcesteal.gui.revive_beacon.title", "uoɔɐǝᗺ ǝʌᴉʌǝᴚ");
        builder.add("divorcesteal.gui.revive_beacon.wanted", "ᗡƎ⟘NⱯM");
        builder.add("divorcesteal.gui.revive_beacon.add_heart", "ʇɹɐǝH ppⱯ");
        builder.add("divorcesteal.gui.revive_beacon.add_head", "pɐǝH ʇǝᵷɹɐ⟘ ppⱯ");
        builder.add("divorcesteal.gui.revive_beacon.revive", "ǝʌᴉʌǝᴚ");

        builder.add(DivorcestealRegistry.USE_HEART_SOUND, "sǝᴉꞁddɐ ʇɹɐǝH");
        builder.add(DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, "sǝʇɐʌᴉʇɔɐ uoɔɐǝᗺ ǝʌᴉʌǝᴚ");
        builder.add(DivorcestealRegistry.DEATHBAN_SOUND, "sǝoɥɔǝ uɐqɥʇɐǝᗡ");
        builder.add(DivorcestealRegistry.REVIVE_SOUND, "sǝoɥɔǝ ꞁɐʌᴉʌǝᴚ");

        builder.add("divorcesteal.unknown", "¿¿¿");
        builder.add("divorcesteal.deathban", "¡sʇɹɐǝɥ ɟo ʇno uɐɹ no⅄");
        builder.add("divorcesteal.deathban_global", "¡sʇɹɐǝɥ ɟo ʇno uɐɹ %s");

        builder.add(DivorcestealRegistry.STEAL_LIFE_STAT, "uǝꞁoʇS sǝʌᴉꞀ", "stat");
        builder.add(DivorcestealRegistry.WITHDRAW_HEART_STAT, "uʍɐɹpɥʇᴉM sʇɹɐǝH", "stat");
        builder.add(DivorcestealRegistry.REVIVE_PLAYER_STAT, "pǝʌᴉʌǝᴚ sɹǝʎɐꞁԀ", "stat");
        builder.add(DivorcestealRegistry.DEATHBAN_PLAYER_STAT, "pǝuuɐqɥʇɐǝᗡ sɹǝʎɐꞁԀ", "stat");
        builder.add(DivorcestealRegistry.DEATHBAN_SELF_STAT, "pǝuuɐqɥʇɐǝᗡ sǝɯᴉ⟘", "stat");

        builder.add("commands.divorcesteal.get", "sʇɹɐǝɥ %2$s sɐɥ %1$s");
        builder.add("commands.divorcesteal.set.single", "sʇɹɐǝɥ %2$s oʇ %1$s ʇǝS");
        builder.add("commands.divorcesteal.set.multiple", "sʇɹɐǝɥ %2$s oʇ sɹǝʎɐꞁd %1$s ʇǝS");
        builder.add("commands.divorcesteal.add.single", "%2$s oʇ sʇɹɐǝɥ %1$s ǝʌɐ⅁");
        builder.add("commands.divorcesteal.add.multiple", "sɹǝʎɐꞁd %2$s oʇ sʇɹɐǝɥ %1$s ǝʌɐ⅁");
        builder.add("commands.divorcesteal.remove.single", "%2$s ɯoɹɟ sʇɹɐǝɥ %1$s ʞoo⟘");
        builder.add("commands.divorcesteal.remove.multiple", "sɹǝʎɐꞁd %2$s ɯoɹɟ sʇɹɐǝɥ %1$s ʞoo⟘");
        builder.add("commands.divorcesteal.revive.single", "%s pǝʌᴉʌǝᴚ");
        builder.add("commands.divorcesteal.revive.multiple", "sɹǝʎɐꞁd %s pǝʌᴉʌǝᴚ");
        builder.add("commands.divorcesteal.delete.single", "%s ɹoɟ ɐʇɐp pǝʇǝꞁǝᗡ");
        builder.add("commands.divorcesteal.delete.multiple", "sɹǝʎɐꞁd %s ɹoɟ ɐʇɐp pǝʇǝꞁǝᗡ");
        builder.add("commands.divorcesteal.withdraw.single", "ʇɹɐǝɥ ⥝ ʍǝɹpɥʇᴉM");
        builder.add("commands.divorcesteal.withdraw.multiple", "sʇɹɐǝɥ %s ʍǝɹpɥʇᴉM");
        builder.add("commands.divorcesteal.withdraw.fail", "¡sʇɹɐǝɥ ǝɹoɯ ʎuɐ ʍɐɹpɥʇᴉʍ ʇou pꞁnoƆ");

        builder.add("arguments.divorcesteal.error.no_data", "¡uoᴉʇɔǝꞁǝs ɹǝʎɐꞁd ʇɐɥʇ ɹoɟ sʇsᴉxǝ ɐʇɐp oN");
        builder.add("arguments.divorcesteal.error.not_deathbanned", "¡pǝuuɐqɥʇɐǝp ʇ,usᴉ ʇɐɥʇ ɹǝʎɐꞁd ɐ ǝʌᴉʌǝɹ ʇouuɐƆ");

        builder.add("divorcesteal.resource_pack.retextured_hearts", "sʇɹɐǝH pǝɹnʇxǝʇǝᴚ");
        builder.add("divorcesteal.resource_pack.retextured_hearts.description", "ǝɹnʇxǝʇ ɯoʇsnɔ ɐ sʇɹɐǝH sǝʌᴉ⅁");

        builder.addConfigScreenTitle("sᵷᴉɟuoƆ ꞁɐǝʇsǝɔɹoʌᴉᗡ");
        builder.addConfig(DivorcestealConfig.MAX_HEARTS,
                "sʇɹɐǝH xɐW",
                "ǝʌɐɥ uɐɔ ɹǝʎɐꞁd ɐ sʇɹɐǝɥ ɟo ɹǝqɯnu ɯnɯᴉxɐɯ ǝɥ⟘"
        );
        builder.addConfig(DivorcestealConfig.DEFAULT_HEARTS,
                "sʇɹɐǝH ʇꞁnɐɟǝᗡ",
                "uᴉoɾ ʇsɹᴉɟ ʎǝɥʇ uǝɥʍ ɥʇᴉʍ ʇɹɐʇs sɹǝʎɐꞁd sʇɹɐǝɥ ɟo ɹǝqɯnu ǝɥ⟘"
        );
        builder.addConfig(DivorcestealConfig.REVIVE_HEARTS,
                "sʇɹɐǝH ǝʌᴉʌǝᴚ",
                "pǝʌᴉʌǝɹ ᵷuᴉǝq ɹǝʇɟɐ ɥʇᴉʍ ʇɹɐʇs sɹǝʎɐꞁd sʇɹɐǝɥ ɟo ɹǝqɯnu ǝɥ⟘"
        );
        builder.addConfig(DivorcestealConfig.REVIVE_DAYS,
                "ǝʌᴉʌǝᴚ oʇ sʎɐᗡ",
                "pǝꞁqɐsᴉp ᵷuᴉǝq ꞁɐʌᴉʌǝɹ-oʇnɐ sʇuǝsǝɹdǝɹ ⥝- ˙pǝʌᴉʌǝɹ ʎꞁꞁɐɔᴉʇɐɯoʇnɐ ǝq oʇ sǝʞɐʇ ʇᴉ sʎɐp ǝɟᴉꞁ-ꞁɐǝɹ ʎuɐɯ ʍoH"
        );
        builder.addConfig(DivorcestealConfig.CRAFTED_HEART_LIMIT,
                "ʇᴉɯᴉꞀ ʇɹɐǝH pǝʇɟɐɹƆ",
                "pǝsn ǝq ɹǝᵷuoꞁ ou uɐɔ sʇɹɐǝɥ pǝʇɟɐɹɔ ɥɔᴉɥʍ ʇɐ sʇɹɐǝɥ ɟo ɹǝqɯnu ǝɥ⟘"
        );
        builder.addConfig(DivorcestealConfig.DISABLE_ELYTRA,
                "ɐɹʇʎꞁƎ ǝꞁqɐsᴉᗡ",
                "pǝꞁqɐsᴉp ǝɹɐ ɐɹʇʎꞁƎ ɹǝɥʇǝɥM"
        );
        builder.addConfig(DivorcestealConfig.DISABLE_TOTEMS,
                "sɯǝʇo⟘ ǝꞁqɐsᴉᗡ",
                "pǝꞁqɐsᴉp ǝɹɐ ᵷuᴉʎpu∩ ɟo sɯǝʇo⟘ ɹǝɥʇǝɥM"
        );
        builder.addConfigCategory("hearts", "sʇɹɐǝH");
        builder.addConfigCategory("rebalances", "sǝɔuɐꞁɐqǝᴚ");

        builder.addModMenuTexts(
                "ꞁɐǝʇsǝɔɹoʌᴉᗡ",
                "ꞁɐǝʇsǝɔɹoʌᴉᗡ ɹoɟ poɯ ꞁɐᴉɔᴉɟɟo ǝɥ⟘",
                "ԀWS ꞁɐǝʇsǝɟᴉꞀ ǝɥʇ ɥʇᴉʍ pǝʇɐᴉꞁᴉɟɟɐ ʇou 'ꞁɐǝʇsǝɔɹoʌᴉᗡ ɹoɟ poɯ ꞁɐᴉɔᴉɟɟo ǝɥ⟘"
        );
    }
}
