package net.Kseouse.dnmclassesmod;
import com.mojang.logging.LogUtils;
import net.Kseouse.dnmclassesmod.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
// Значение MOD_ID должно совпадать с entry в META-INF/mods.toml!
@Mod(DnmclassesMod.MOD_ID)
@Mod.EventBusSubscriber(modid = DnmclassesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DnmclassesMod {
    public static final String MOD_ID = "dnmclassesmod";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final KeyMapping OPEN_CLASS_GUI = new KeyMapping(
            "key.dnmclassesmod.open_class_gui",
            GLFW.GLFW_KEY_C,
            "category.dnmclassesmod"
    );

    public DnmclassesMod() {
        MinecraftForge.EVENT_BUS.register(this); // Добавлена регистрация метода для хранилища Capability!
    }

    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) { // Флаг-лог видимости успешного результата первичного сетапа мода!
        LOGGER.info("DnmclassesMod setup complete!");
        NetworkHandler.register();
    }
    // Добавлено Метод для регистрации Capability!
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        LOGGER.info("Registering PlayerClassCapability");
        event.register(PlayerClassCapability.IPlayerClass.class);
    }
    // Добавлено Привязка Capability к игроку!
    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            // Добавлено Логирование прикрепления Capability!
            LOGGER.info("Attaching Capability to Player");
            event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_class"), new PlayerClassCapability());
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();
            original.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY).ifPresent(oldCap -> {
                newPlayer.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY).ifPresent(newCap -> {
                    newCap.setSelectedClass(oldCap.getSelectedClass());
                    // Добавлено Логирование клонирования Capability!
                    LOGGER.info("Cloned Capability, class: " + oldCap.getSelectedClass().name());
                });
            });
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) { // Some setup code
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_CLASS_GUI);
        }
    }
}