package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.particles.splashes.WaterSplashParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterSplashParticle.class)
public abstract class InjectWaterSplashParticle
{
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void onRender(BillboardParticleSubmittable submittable, Camera camera, float tickDelta, CallbackInfo ci)
	{
		WaterSplashParticle particle = (WaterSplashParticle)(Object)this;

		// Get the VertexConsumer from the submittable
		BillboardParticleSubmittable.Vertices vertices = ((AccessorBillboardParticleSubmittable)submittable).getVertices();
		VertexConsumer vertexConsumer = ((AccessorBillboardParticleSubmittableVertices)vertices).getVertices();

		// Get particle properties using accessor
		AccessorWaterSplashParticle accessor = (AccessorWaterSplashParticle)(Object)this;
		float width = accessor.getWidth();
		float height = accessor.getHeight();
		int color = accessor.getColor().getRGB();

		// Use particle's own accessors for base properties
		AccessorParticle particleAccessor = (AccessorParticle)(Object)this;
		double x = particleAccessor.getX();
		double y = particleAccessor.getY();
		double z = particleAccessor.getZ();
		double lastX = particleAccessor.getLastX();
		double lastY = particleAccessor.getLastY();
		double lastZ = particleAccessor.getLastZ();
		int age = particleAccessor.getAge();
		int maxAge = particleAccessor.getMaxAge();

		Vec3d vec3d = camera.getPos();
		float f = (float)(MathHelper.lerp(tickDelta, lastX, x) - vec3d.getX());
		float g = (float)(MathHelper.lerp(tickDelta, lastY, y) - vec3d.getY());
		float h = (float)(MathHelper.lerp(tickDelta, lastZ, z) - vec3d.getZ());

		// Create base square for positioning
		Vector3f[] vector3fs = new Vector3f[]{
			new Vector3f(-1.0F, 0.0F, -1.0f),
			new Vector3f(-1.0F, 0.0F, 1.0F),
			new Vector3f(1.0F, 0.0F, 1.0F),
			new Vector3f(1.0F, 0.0F, -1.0F)
		};

		float ageDelta = MathHelper.lerp(tickDelta, age - 1, (float)age);
		float progress = ageDelta / (float)maxAge;
		float scale = width * (0.8f + 0.2f * progress);

		for (int i = 0; i < 4; ++i)
		{
			Vector3f vector3f2 = vector3fs[i];
			vector3f2.mul(scale);
			vector3f2.add(f, g, h);
		}

		float minU = accessor.getMinU() + accessor.getUnit();
		float maxU = accessor.getMaxU() - accessor.getUnit();
		float minV = accessor.getMinV();
		float maxV = accessor.getMaxV();
		int light = accessor.getBrightness(tickDelta);
		int colorValue = accessor.isColored() ? color : 0xFFFFFF;

		// Render 4 vertical sides
		renderSide(vertexConsumer, vector3fs, 0, 1, height, minU, maxU, minV, maxV, light, colorValue);
		renderSide(vertexConsumer, vector3fs, 1, 2, height, minU, maxU, minV, maxV, light, colorValue);
		renderSide(vertexConsumer, vector3fs, 2, 3, height, minU, maxU, minV, maxV, light, colorValue);
		renderSide(vertexConsumer, vector3fs, 3, 0, height, minU, maxU, minV, maxV, light, colorValue);

		ci.cancel();
	}

	private void renderSide(VertexConsumer vertexConsumer, Vector3f[] vector3fs, int a, int b,
	                       float height, float minU, float maxU, float minV, float maxV,
	                       int light, int color)
	{
		Vector3f pos1 = vector3fs[a];
		Vector3f pos2 = vector3fs[b];
		Vector3f pos3 = new Vector3f(pos2.x(), pos2.y() + height, pos2.z());
		Vector3f pos4 = new Vector3f(pos1.x(), pos1.y() + height, pos1.z());

		// Front face (counter-clockwise winding)
		vertexConsumer.vertex(pos1.x(), pos1.y(), pos1.z()).texture(maxU, maxV).color(color).light(light);
		vertexConsumer.vertex(pos4.x(), pos4.y(), pos4.z()).texture(maxU, minV).color(color).light(light);
		vertexConsumer.vertex(pos3.x(), pos3.y(), pos3.z()).texture(minU, minV).color(color).light(light);
		vertexConsumer.vertex(pos2.x(), pos2.y(), pos2.z()).texture(minU, maxV).color(color).light(light);

		// Back face (counter-clockwise winding from back side)
		vertexConsumer.vertex(pos2.x(), pos2.y(), pos2.z()).texture(minU, maxV).color(color).light(light);
		vertexConsumer.vertex(pos3.x(), pos3.y(), pos3.z()).texture(minU, minV).color(color).light(light);
		vertexConsumer.vertex(pos4.x(), pos4.y(), pos4.z()).texture(maxU, minV).color(color).light(light);
		vertexConsumer.vertex(pos1.x(), pos1.y(), pos1.z()).texture(maxU, maxV).color(color).light(light);
	}
}