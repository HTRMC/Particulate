package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.particles.splashes.WaterSplashParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BillboardParticleSubmittable.class)
public class InjectBillboardParticleSubmittable
{
	@Inject(method = "drawFace", at = @At("HEAD"), cancellable = true)
	private void onDrawFace(VertexConsumer vertexConsumer, float x, float y, float z,
	                        float rotationX, float rotationY, float rotationZ, float rotationW,
	                        float size, float minU, float maxU, float minV, float maxV,
	                        int color, int brightness, CallbackInfo ci)
	{
		// Check if we have custom rectangular dimensions stored
		WaterSplashParticle.RectangularDimensions dims = WaterSplashParticle.getRectangularDimensions();
		if (dims != null)
		{
			// Use custom rectangular rendering instead of square
			Quaternionf quaternionf = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);

			// Create 4 vertices with independent width and height
			renderVertex(vertexConsumer, quaternionf, x, y, z, dims.width(), -dims.height(), maxU, maxV, color, brightness);
			renderVertex(vertexConsumer, quaternionf, x, y, z, dims.width(), dims.height(), maxU, minV, color, brightness);
			renderVertex(vertexConsumer, quaternionf, x, y, z, -dims.width(), dims.height(), minU, minV, color, brightness);
			renderVertex(vertexConsumer, quaternionf, x, y, z, -dims.width(), -dims.height(), minU, maxV, color, brightness);

			ci.cancel(); // Don't run the original method
		}
	}

	private void renderVertex(VertexConsumer vertexConsumer, Quaternionf rotation,
	                         float x, float y, float z,
	                         float localX, float localY,
	                         float u, float v, int color, int brightness)
	{
		Vector3f vector3f = new Vector3f(localX, localY, 0.0F).rotate(rotation).add(x, y, z);
		vertexConsumer.vertex(vector3f.x(), vector3f.y(), vector3f.z())
		              .texture(u, v)
		              .color(color)
		              .light(brightness);
	}
}
