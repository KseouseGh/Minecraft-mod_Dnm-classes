package net.Kseouse.dnmclassesmod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DnmclassesMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (DnmclassesMod.OPEN_CLASS_GUI.isDown() && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().setScreen(new ClassSelectionScreen(Minecraft.getInstance().player));
        }
    }
}