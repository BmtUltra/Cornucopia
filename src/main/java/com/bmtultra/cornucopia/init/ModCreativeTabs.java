package com.pizza573.cornucopia.init;

import com.pizza573.cornucopia.Cornucopia;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cornucopia.MOD_ID);

    public static final RegistryObject< CreativeModeTab> TAB_HOLDER = REGISTER.register("cornucopia", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup."+Cornucopia.MOD_ID))// The language key for the title of your CreativeModeTab 可以运用于本地化
            .icon(ModCreativeTabs::createTabStack)
            .displayItems(ModCreativeTabs::displayItems)
            .build()
    );

    // 添加item至创造模式物品栏
    private static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output)
    {
        output.accept(ModItems.CORNUCOPIA.get());
    }

    // 创造模式物品栏图标
    private static ItemStack createTabStack()
    {
        return ModItems.CREATIVE_TAB_DISPLAY.get().getDefaultInstance();
    }
}
