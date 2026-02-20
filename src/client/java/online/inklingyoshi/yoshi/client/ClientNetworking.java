package online.inklingyoshi.yoshi.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled;
import online.inklingyoshi.yoshi.network.ModPackets;

public class ClientNetworking {
    public static void sendFlutterJumpPacket(boolean isHoldingJump) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(isHoldingJump);
        ClientPlayNetworking.send(ModPackets.FLUTTER_JUMP_PACKET, buf);
    }
    
    public static void sendReelInPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        // No additional data needed
        ClientPlayNetworking.send(ModPackets.REEL_IN_PACKET, buf);
    }
}