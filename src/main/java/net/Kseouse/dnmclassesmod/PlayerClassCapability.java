package net.Kseouse.dnmclassesmod;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.slf4j.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerClassCapability implements ICapabilitySerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Capability<IPlayerClass> PLAYER_CLASS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private pClass selectedClass = pClass.NONE;

    private final LazyOptional<IPlayerClass> instance = LazyOptional.of(() -> new IPlayerClass() {
        @Override
        public pClass getSelectedClass() {
            LOGGER.info("Getting selected class: " + selectedClass.name());
            return selectedClass;
        }

        @Override
        public void setSelectedClass(pClass newClass) {
            LOGGER.info("Setting selected class to: " + newClass.name());
            selectedClass = newClass;
        }
    });

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        LOGGER.info("Requested Capability: " + cap);
        return PLAYER_CLASS_CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("SelectedClass", selectedClass.name());
        LOGGER.info("Serializing Capability, class: " + selectedClass.name());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        String className = tag.getString("SelectedClass");
        // Добавлено Проверка на пустое имя класса!
        if (!className.isEmpty()) {
            try {
                selectedClass = pClass.valueOf(className);
                LOGGER.info("Deserializing Capability, class: " + selectedClass.name());
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to deserialize class: " + className, e);
                selectedClass = pClass.NONE;
            }
        } else {
            LOGGER.warn("Deserializing Capability, empty class name, defaulting to NONE");
            selectedClass = pClass.NONE;
        }
    }

    public interface IPlayerClass {
        pClass getSelectedClass();
        void setSelectedClass(pClass newClass);
    }
}