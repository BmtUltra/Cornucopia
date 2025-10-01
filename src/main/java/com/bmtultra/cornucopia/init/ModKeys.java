package com.pizza573.cornucopia.init;

import com.mojang.blaze3d.platform.InputConstants;
import com.pizza573.cornucopia.Cornucopia;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class ModKeys
{
    // 自定义按键多了试试枚举Enum
    public static final KeyMapping DROP_CORNUCOPIA_CONTENTS = new KeyMapping("key." + Cornucopia.MOD_ID + ".drop_cornucopia_contents",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            "key.categories.cornucopia"
    );
}
