package com.pizza573.cornucopia.handler;

import com.pizza573.cornucopia.Cornucopia;
import com.pizza573.cornucopia.init.ModItems;
import com.pizza573.cornucopia.item.CornucopiaItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Cornucopia.MOD_ID,value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEventHandler
{
    @SubscribeEvent
    public static void propertyOverride(FMLClientSetupEvent event)
    {
        // 添加自定义物品渲染，通过 weight 切换丰饶角贴图
        ItemProperties.register(
                ModItems.CORNUCOPIA.get(),
                new ResourceLocation(Cornucopia.MOD_ID, "weight"),
                (stack, level, entity, seed) -> CornucopiaItem.getWeightDisplay(stack)
        );
    }
}
