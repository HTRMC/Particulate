package com.dunnewortel.particulate.particles;

import com.dunnewortel.particulate.rendering.RectangularParticle;
import com.dunnewortel.particulate.rendering.RectangularParticleSubmittable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class WaterRippleParticle extends RectangularParticle {
	protected final SpriteProvider provider;

	protected WaterRippleParticle(ClientWorld world, double x, double y, double z, SpriteProvider provider) {
		super(world, x, y, z, provider.getSprite(world.getRandom()));
		maxAge = 7;
		alpha = 0.2f;
		this.provider = provider;
		updateSprite(provider);
	}

	@Override
	public void tick() {
		lastX = x;
		lastY = y;
		lastZ = z;
		if (age++ >= maxAge) {
			markDead();
		} else {
			updateSprite(provider);
		}
	}

	@Override
	public BillboardParticle.RenderType getRenderType() {
		return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}

	@Override
	public float getWidth(float tickProgress) {
		return 0.25f;
	}

	@Override
	public float getHeight(float tickProgress) {
		return 0.25f;
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

		// Single flat quad - render both sides for visibility from above and below
		submittable.render(getRenderType(),
			corners[0].x(), corners[0].y(), corners[0].z(),
			corners[1].x(), corners[1].y(), corners[1].z(),
			corners[2].x(), corners[2].y(), corners[2].z(),
			corners[3].x(), corners[3].y(), corners[3].z(),
			minU, maxU, minV, maxV,
			color, light);

		// Back face (visible from below)
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

		@Override
		public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ, net.minecraft.util.math.random.Random random) {
			return new WaterRippleParticle(world, x, y, z, provider);
		}
	}
}