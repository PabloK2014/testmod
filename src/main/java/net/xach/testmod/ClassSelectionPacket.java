package net.xach.testmod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ClassSelectionPacket {
    private static final Logger LOGGER = Logger.getLogger(TestMod.MOD_ID);
    private final String className;

    public ClassSelectionPacket(String className) {
        this.className = className;
        LOGGER.info("Created packet for class: " + className);
    }

    public ClassSelectionPacket(FriendlyByteBuf buf) {
        this.className = buf.readUtf(32767);
        LOGGER.info("Decoded packet for class: " + className + " (bytes: " + className.getBytes(StandardCharsets.UTF_8).length + ")");
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(className, 32767);
        LOGGER.info("Encoded packet for class: " + className);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(TestMod.PlayerClassCapability.CAPABILITY).ifPresent(cap -> {
                    LOGGER.info("Setting class from packet: " + className);
                    cap.setPlayerClass(className);
                    cap.sync(player);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}