package online.inklingyoshi.yoshi.client.renderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import online.inklingyoshi.yoshi.entity.YoshiEggEntity;
import online.inklingyoshi.yoshi.item.YoshiItems;

public class YoshiEggEntityRenderer extends EntityRenderer<YoshiEggEntity> {
    private final ItemRenderer itemRenderer;
    
    public YoshiEggEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15f;
        this.shadowOpacity = 0.75f;
    }
    
    @Override
    public void render(YoshiEggEntity entity, float yaw, float tickDelta, MatrixStack matrices, 
                      VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        
        // Scale the egg to match the larger hitbox
        matrices.scale(1f, 1f, 1f);
        
        // Rotate the egg based on its motion
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw(tickDelta)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch(tickDelta)));
        
        // Render the Yoshi egg item
        ItemStack itemStack = new ItemStack(YoshiItems.YOSHI_EGG);
        this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, light, 
                                   OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 
                                   entity.getWorld(), entity.getId());
        
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
    
    @Override
    public Identifier getTexture(YoshiEggEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}