package com.pizza573.cornucopia.init;

import com.pizza573.cornucopia.Cornucopia;
import com.pizza573.cornucopia.item.CornucopiaItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Cornucopia.MOD_ID);
    // 物品在注册的时候添加了一个数据组件
    public static final RegistryObject<Item> CORNUCOPIA = REGISTER.register("cornucopia", () -> new CornucopiaItem(new Item
                    .Properties()
//            .food(new FoodProperties.Builder().build())
                    .stacksTo(1)
//                    .component(ModDataComponents.CORNUCOPIA_CONTENTS, CornucopiaContents.EMPTY))
            )
    );
    public static final RegistryObject<Item> CREATIVE_TAB_DISPLAY = REGISTER.register("creative_tab_display", () -> new Item(new Item.Properties()));
}
