package com.pizza573.cornucopia;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config
{
    // ModConfigSpec
    public static final ForgeConfigSpec CONFIG_SPEC;// config_specialization
    // config values
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public static class Common
    {
        public final ForgeConfigSpec.BooleanValue enableClearFoods;
        public final ForgeConfigSpec.IntValue lifeThresholdValue;

        public Common(ForgeConfigSpec.Builder builder)
        {
            enableClearFoods = builder
//                    .comment("转换为丰饶角后清空食物")
                    .translation("cornucopia.config.enable_clear_foods")
                    .define("enableClearFoods", false);
            lifeThresholdValue=builder
//                    .comment("生命值低于多少时自动转换")
                    .translation("cornucopia.config.life_threshold_value")
                    .defineInRange("lifeThresholdValue", 10, 1, 19);
        }
    }
}
