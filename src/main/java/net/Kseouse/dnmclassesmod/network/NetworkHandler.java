package net.Kseouse.dnmclassesmod.network;
import com.mojang.logging.LogUtils;
import net.Kseouse.dnmclassesmod.DnmclassesMod;
import net.Kseouse.dnmclassesmod.PlayerClassCapability;
import net.Kseouse.dnmclassesmod.pClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(DnmclassesMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        LOGGER.info("Registering network channel for mod: " + DnmclassesMod.MOD_ID);
        CHANNEL.registerMessage(
                packetId++,
                SelectClassPacket.class,
                SelectClassPacket::encode,
                SelectClassPacket::decode,
                SelectClassPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                SyncClassPacket.class,
                SyncClassPacket::encode,
                SyncClassPacket::decode,
                SyncClassPacket::handle
        );
        LOGGER.info("Network channel registered, packet IDs assigned: " + packetId);
    }

    public static class SelectClassPacket {
        private final pClass selectedClass;

        public SelectClassPacket(pClass selectedClass) {
            this.selectedClass = selectedClass;
        }

        public static void encode(SelectClassPacket packet, FriendlyByteBuf buffer) {
            LOGGER.info("Encoding SelectClassPacket for class: " + packet.selectedClass.name());
            buffer.writeUtf(packet.selectedClass.name());
        }

        public static SelectClassPacket decode(FriendlyByteBuf buffer) {
            String className = buffer.readUtf();
            LOGGER.info("Decoding SelectClassPacket for class: " + className);
            return new SelectClassPacket(pClass.valueOf(className));
        }

        public static void handle(SelectClassPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();

                if (player != null) {
                    LOGGER.info("Received SelectClassPacket for class: " + packet.selectedClass.name());
                    player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY).ifPresent(cap -> {
                        // Добавлено Логирование текущего класса!
                        LOGGER.info("Current server Capability class: " + cap.getSelectedClass().name());
                        if (cap.getSelectedClass() == pClass.NONE) {
                            cap.setSelectedClass(packet.selectedClass);
                            LOGGER.info("Set server Capability to class: " + packet.selectedClass.name());
                            player.sendSystemMessage(Component.literal("Выбран класс - " + packet.selectedClass.getName() + "!"));
                            LOGGER.info("Sending SyncClassPacket to client for class: " + packet.selectedClass.name());
                            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncClassPacket(packet.selectedClass));
                        } else {
                            LOGGER.info("Class already selected: " + cap.getSelectedClass().name());
                        }
                    });
                    // Добавлено Логирование, если Capability отсутствует!
                    if (!player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY).isPresent()) {
                        LOGGER.error("SelectClassPacket: Server Capability not found!");
                    }
                } else {
                    LOGGER.error("SelectClassPacket: Player is null!");
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class SyncClassPacket {
        private final pClass selectedClass;

        public SyncClassPacket(pClass selectedClass) {
            this.selectedClass = selectedClass;
        }

        public static void encode(SyncClassPacket packet, FriendlyByteBuf buffer) {
            LOGGER.info("Encoding SyncClassPacket for class: " + packet.selectedClass.name());
            buffer.writeUtf(packet.selectedClass.name());
        }

        public static SyncClassPacket decode(FriendlyByteBuf buffer) {
            String className = buffer.readUtf();
            LOGGER.info("Decoding SyncClassPacket for class: " + className);
            return new SyncClassPacket(pClass.valueOf(className));
        }

        public static void handle(SyncClassPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                LOGGER.info("Received SyncClassPacket on client for class: " + packet.selectedClass.name());
                Player player = net.minecraft.client.Minecraft.getInstance().player;
                if (player != null) {
                    player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY).ifPresent(cap -> {
                        cap.setSelectedClass(packet.selectedClass);
                        // Добавлено Логирование для подтверждения обновления!
                        LOGGER.info("Updated client Capability to class: " + packet.selectedClass.name());
                    });
                    // Добавлено Логирование, если Capability отсутствует!
                    if (!player.getCapability(PlayerClassCapability.PLAYER_CLASS_CAPABILITY).isPresent()) {
                        LOGGER.error("SyncClassPacket: Client Capability not found!");
                    }
                } else {
                    LOGGER.error("SyncClassPacket: Client player is null!");
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}