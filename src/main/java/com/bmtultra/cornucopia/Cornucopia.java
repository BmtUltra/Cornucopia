package com.pizza573.cornucopia;

import com.pizza573.cornucopia.init.ModCreativeTabs;
import com.pizza573.cornucopia.init.ModItems;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.concurrent.atomic.AtomicInteger;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Cornucopia.MOD_ID)
public class Cornucopia
{
    public static final String MOD_ID = "cornucopia";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Cornucopia()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.REGISTER.register(modEventBus);
        ModCreativeTabs.REGISTER.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);

        // 配置界面
//        ModLoadingContext.get().registerExtensionPoint(
//                ConfigScreenHandler.ConfigScreenFactory.class,
//                () -> new ConfigScreenHandler.ConfigScreenFactory(
//                        (mc, screen) -> {
//                            ModListScreen
//                        }
//                )
//        );
        // modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
