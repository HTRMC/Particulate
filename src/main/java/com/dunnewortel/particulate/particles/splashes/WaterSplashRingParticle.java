package com.dunnewortel.particulate.particles.splashes;

import com.dunnewortel.particulate.rendering.RectangularParticle;
import com.dunnewortel.particulate.rendering.RectangularParticleSubmittable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
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

@Environment(EnvType.CLIENT)
public class WaterSplashRingParticle extends RectangularParticle {
	protected final SpriteProvider provider;
	private final float width;

	WaterSplashRingParticle(ClientWorld clientWorld, double x, double y, double z, float width, SpriteProvider provider) {
		super(clientWorld, x, y, z, provider.getSprite(clientWorld.getRandom()));
		gravityStrength = 0;
		maxAge = 18;
		this.width = width;
		this.provider = provider;
		updateSprite(provider);
	}

	@Override
	public BillboardParticle.RenderType getRenderType() {
		return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}

	@Override
	public float getWidth(float tickProgress) {
		float ageDelta = MathHelper.lerp(tickProgress, age - 1, (float) age);
		float progress = ageDelta / (float) maxAge;
		return width * (0.8f + 0.2f * progress);
	}

	@Override
	public float getHeight(float tickProgress) {
		return getWidth(tickProgress);
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

		float size = getWidth(tickDelta);

		// Flat quad on XZ plane (laying on water surface)
		Vector3f[] corners = new Vector3f[]{
			new Vector3f(-size, 0, -size),
			new Vector3f(-size, 0, size),
			new Vector3f(size, 0, size),
			new Vector3f(size, 0, -size)
		};

		for (Vector3f corner : corners) {
			corner.add(f, g, h);
		}

		float minU = getMinU();
		float maxU = getMaxU();
		float minV = getMinV();
		float maxV = getMaxV();
		int light = getBrightness(tickDelta);
		int color = ColorHelper.fromFloats(alpha, red, green, blue);

		// Top face (visible from above)
		submittable.render(getRenderType(),
			corners[0].x(), corners[0].y(), corners[0].z(),
			corners[1].x(), corners[1].y(), corners[1].z(),
			corners[2].x(), corners[2].y(), corners[2].z(),
			corners[3].x(), corners[3].y(), corners[3].z(),
			minU, maxU, minV, maxV,
			color, light);

		// Bottom face (visible from below)
		submittable.render(getRenderType(),
			corners[3].x(), corners[3].y(), corners[3].z(),
			corners[2].x(), corners[2].y(), corners[2].z(),
			corners[1].x(), corners[1].y(), corners[1].z(),
			corners[0].x(), corners[0].y(), corners[0].z(),
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
		public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.math.random.Random random) {
			return new WaterSplashRingParticle(world, x, y, z, (float) velX, provider);
		}
	}
}