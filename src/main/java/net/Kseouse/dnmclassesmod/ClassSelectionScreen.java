package net.Kseouse.dnmclassesmod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.Kseouse.dnmclassesmod.network.NetworkHandler;
import org.slf4j.Logger;

public class ClassSelectionScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Player player;

    public ClassSelectionScreen(Player player) {
        super(Component.literal("Выбор класса"));
        this.player = player;
    }

    @Override
    protected void init() {

        super.init();
        LOGGER.info("Initializing ClassSelectionScreen for player: " + player.getName().getString());
        int buttonWidth = 80;
        int buttonHeight = 20;
        int startX = (this.width - 4 * buttonWidth - 3 * 10) / 2; // Сетка по центру 4x3!
        int startY = (this.height - 3 * buttonHeight - 2 * 10) / 2;
        int index = 0;
        boolean isLocked = player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY)
                .map(cap -> {
                    LOGGER.info("Checking isLocked, current class: " + cap.getSelectedClass().name());
                    return cap.getSelectedClass() != pClass.NONE;
                })
                .orElseGet(() -> {
                    LOGGER.error("Init: Client Capability not found!");
                    return false;
                });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (index >= pClass.values().length - 1) break; // Скипинг pClass.NONE!
                pClass classType = pClass.values()[index];
                int x = startX + col * (buttonWidth + 10);
                int y = startY + row * (buttonHeight + 10);

                this.addRenderableWidget(Button.builder(
                                Component.literal(classType.getName()),
                                button -> {
                                    LOGGER.info("Button clicked for class: " + classType.name() + ", isLocked: " + isLocked);
                                    if (!isLocked) {
                                        // Добавлено Логирование отправки пакета!
                                        LOGGER.info("Sending SelectClassPacket for class: " + classType.name());
                                        NetworkHandler.CHANNEL.sendToServer(new NetworkHandler.SelectClassPacket(classType));
                                        this.onClose();
                                    } else {// Добавлено Логирование, если класс уже выбран!
                                        LOGGER.info("Cannot select class, already locked: " + player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY)
                                                .map(cap -> cap.getSelectedClass().name()).orElse("!*"));
                                    }
                                })
                        .pos(x, y)
                        .size(buttonWidth, buttonHeight)
                        .tooltip(isLocked ? Tooltip.create(Component.literal("Класс уже выбран!")) : Tooltip.create(Component.literal(classType.getDescription())))
                        .build());
                index++;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks); // Отрисовка заголовка "Выбор класса"!
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF); // Класс view!
        String currentClass = player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY)
                .map(cap -> {
                    String className = cap.getSelectedClass() == pClass.NONE ? "!*!" : cap.getSelectedClass().getName();
                    LOGGER.info("Rendering GUI, current class: " + className);
                    return className;
                })
                .orElseGet(() -> {
                    LOGGER.error("Render: Client Capability not found!");
                    return "!*";
                });
        guiGraphics.drawString(this.font, " " + currentClass, 10, 10, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}