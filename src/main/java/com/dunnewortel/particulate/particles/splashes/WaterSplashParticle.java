package com.dunnewortel.particulate.particles.splashes;

import com.dunnewortel.particulate.rendering.RectangularParticle;
import com.dunnewortel.particulate.rendering.RectangularParticleSubmittable;
import net.minecraft.client.particle.BillboardParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class WaterSplashParticle extends RectangularParticle {
	protected final SpriteProvider provider;
	private final float baseWidth;
	private final float baseHeight;
	private final Color waterColor;
	private final float unit;
	protected boolean colored = true;

	WaterSplashParticle(ClientWorld clientWorld, double x, double y, double z, float width, float height, SpriteProvider provider) {
		super(clientWorld, x, y, z, provider.getSprite(clientWorld.getRandom()));
		gravityStrength = 0;
		maxAge = 18;
		this.baseWidth = width;
		this.baseHeight = height;
		this.provider = provider;

		waterColor = new Color(BiomeColors.getWaterColor(clientWorld, BlockPos.ofFloored(x, y, z)));
		// Inset UVs to prevent texture bleeding - use larger value for particle atlas
		unit = 0.01f;

		setColor(waterColor.getRed() / 255f, waterColor.getGreen() / 255f, waterColor.getBlue() / 255f);
	}

	@Override
	public BillboardParticle.RenderType getRenderType() {
		return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}

	@Override
	public float getWidth(float tickProgress) {
		float ageDelta = MathHelper.lerp(tickProgress, age - 1, (float) age);
		float progress = ageDelta / (float) maxAge;
		return baseWidth * (0.8f + 0.2f * progress);
	}

	@Override
	public float getHeight(float tickProgress) {
		return baseHeight;
	}

	@Override
	public void tick() {
		super.tick();
		updateSprite(provider);

		if (!world.getFluidState(BlockPos.ofFloored(x, y, z)).isIn(FluidTags.WATER)) {
			this.markDead();
		}
	}

	@Override
	public void render(RectangularParticleSubmittable submittable, Camera camera, float tickDelta) {
		Vec3d vec3d = camera.getPos();
		float f = (float) (MathHelper.lerp(tickDelta, lastX, x) - vec3d.getX());
		float g = (float) (MathHelper.lerp(tickDelta, lastY, y) - vec3d.getY());
		float h = (float) (MathHelper.lerp(tickDelta, lastZ, z) - vec3d.getZ());

		Vector3f[] corners = new Vector3f[]{
			new Vector3f(-1.0F, 0.0F, -1.0f),
			new Vector3f(-1.0F, 0.0F, 1.0F),
			new Vector3f(1.0F, 0.0F, 1.0F),
			new Vector3f(1.0F, 0.0F, -1.0F)
		};

		float scale = getWidth(tickDelta);
		for (Vector3f corner : corners) {
			corner.mul(scale);
			corner.add(f, g, h);
		}

		float minU = getMinU() + unit;
		float maxU = getMaxU() - unit;
		float minV = getMinV();  // No unit adjustment on V (matches old working code)
		float maxV = getMaxV();
		int light = getBrightness(tickDelta);
		int colorInt = colored ? ColorHelper.fromFloats(alpha, red, green, blue) : 0xFFFFFFFF;

		renderSide(submittable, corners, 0, 1, tickDelta, minU, maxU, minV, maxV, light, colorInt);
		renderSide(submittable, corners, 1, 2, tickDelta, minU, maxU, minV, maxV, light, colorInt);
		renderSide(submittable, corners, 2, 3, tickDelta, minU, maxU, minV, maxV, light, colorInt);
		renderSide(submittable, corners, 3, 0, tickDelta, minU, maxU, minV, maxV, light, colorInt);
	}

	private void renderSide(RectangularParticleSubmittable submittable, Vector3f[] corners,
	                        int a, int b, float tickDelta,
	                        float minU, float maxU, float minV, float maxV,
	                        int light, int color) {
		float height = getHeight(tickDelta);

		// Front face - vertices: a, b, b+h, a+h
		submittable.render(getRenderType(),
			corners[a].x(), corners[a].y(), corners[a].z(),
			corners[b].x(), corners[b].y(), corners[b].z(),
			corners[b].x(), corners[b].y() + height, corners[b].z(),
			corners[a].x(), corners[a].y() + height, corners[a].z(),
			minU, maxU, minV, maxV,
			color, light);

		// Back face - vertices: b, a, a+h, b+h (reversed winding)
		submittable.render(getRenderType(),
			corners[b].x(), corners[b].y(), corners[b].z(),
			corners[a].x(), corners[a].y(), corners[a].z(),
			corners[a].x(), corners[a].y() + height, corners[a].z(),
			corners[b].x(), corners[b].y() + height, corners[b].z(),
			minU, maxU, minV, maxV,
			color, light);
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider provider;

		public Factory(SpriteProvider provider) {
			this.provider = provider;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType type, ClientWorld world,
		                               double x, double y, double z,
		                               double velX, double velY, double velZ,
		                               net.minecraft.util.math.random.Random random) {
			return new WaterSplashParticle(world, x, y, z, (float) velX, (float) velY, provider);
		}
	}
}
