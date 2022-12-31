package com.mymod.moc.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {

    private static final String KEY_TRANSLATION = "key.toggleMoc";
    private static final String KEY_CATEGORY = "key.categories.misc";

    public static final KeyMapping KEY_TOGGLE_MOC = new KeyMapping(KEY_TRANSLATION, KeyConflictContext.IN_GAME,
            KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, KEY_CATEGORY);
}
