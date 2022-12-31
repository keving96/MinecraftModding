package com.mymod.moc.events;

import com.mojang.logging.LogUtils;
import com.mymod.moc.MineOreCluster;
import com.mymod.moc.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class KeyEvents {

    @Mod.EventBusSubscriber(modid = MineOreCluster.MOD_ID, value = Dist.CLIENT)
    public class KeyForgeEvents {
        @SubscribeEvent
        public static void onKeyInputEvent(InputEvent.Key keyEvent) {
            if (KeyBinding.KEY_TOGGLE_MOC.consumeClick()) {
                MineOreCluster.mocDisabled = !MineOreCluster.mocDisabled;
                if (MineOreCluster.mocDisabled) {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal("MOC disabled"));
                } else
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal("MOC enabled"));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MineOreCluster.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class KeyModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBinding.KEY_TOGGLE_MOC);
        }
    }
}
