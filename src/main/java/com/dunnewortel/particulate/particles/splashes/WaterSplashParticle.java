package com.dunnewortel.particulate.particles.splashes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;

public class WaterSplashParticle extends BillboardParticle
{
	protected final SpriteProvider provider;
	private final float width;
	private final float height;
	private final Color color;
	private final float unit;
	protected boolean colored = true;

	// ThreadLocal to communicate rectangular dimensions to the mixin
	private static final ThreadLocal<RectangularDimensions> RECTANGULAR_DIMS = new ThreadLocal<>();

	public record RectangularDimensions(float width, float height) {}

	public static RectangularDimensions getRectangularDimensions()
	{
		return RECTANGULAR_DIMS.get();
	}

	WaterSplashParticle(ClientWorld clientWorld, double x, double y, double z, float width, float height, SpriteProvider provider)
	{
		super(clientWorld, x, y, z, provider.getSprite(clientWorld.getRandom()));
		gravityStrength = 0;
		maxAge = 18;
		this.width = width;
		this.height = height;
		this.provider = provider;
		updateSprite(provider);

		color = new Color(BiomeColors.getWaterColor(clientWorld, BlockPos.ofFloored(x, y, z)));
		unit = 0.002f; // UV offset to prevent texture bleeding (equivalent to 2 pixels on a 1024-wide texture) // TODO: FIX HARD CODED VALUE
	}

	@Override
	public ParticleTextureSheet textureSheet()
	{
		return ParticleTextureSheet.SINGLE_QUADS;
	}

	@Override
	public RenderType getRenderType()
	{
		return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
	}

	@Override
	public void tick()
	{
		super.tick();

		updateSprite(provider);

		if (!world.getFluidState(BlockPos.ofFloored(x, y, z)).isIn(FluidTags.WATER))
		{
			this.markDead();
		}
	}

	@Override
	public void render(BillboardParticleSubmittable submittable, Camera camera, float tickDelta)
	{
		Vec3d vec3d = camera.getPos();
		float f = (float)(MathHelper.lerp(tickDelta, lastX, x) - vec3d.getX());
		float g = (float)(MathHelper.lerp(tickDelta, lastY, y) - vec3d.getY());
		float h = (float)(MathHelper.lerp(tickDelta, lastZ, z) - vec3d.getZ());

		Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, 0.0F, -1.0f), new Vector3f(-1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, -1.0F)};
		float ageDelta = MathHelper.lerp(tickDelta, age - 1, (float)age);
		float progress = ageDelta / (float)maxAge;
		float scale = width * (0.8f + 0.2f * progress);

		for (int i = 0; i < 4; ++i)
		{
			Vector3f vector3f2 = vector3fs[i];
			vector3f2.mul(scale);
			vector3f2.add(f, g, h);
		}

		float l = getMinU() + unit;
		float m = getMaxU() - unit;
		float n = getMinV();
		float o = getMaxV();
		int light = getBrightness(tickDelta);
		int colorValue = colored ? this.color.getRGB() : Color.white.getRGB();

		// Render 4 vertical sides to create 3D cylinder effect
		// Each side is a rectangular quad with independent width and height
		renderSide(submittable, vector3fs, 0, 1, height, l, m, n, o, light, colorValue);
		renderSide(submittable, vector3fs, 1, 2, height, l, m, n, o, light, colorValue);
		renderSide(submittable, vector3fs, 2, 3, height, l, m, n, o, light, colorValue);
		renderSide(submittable, vector3fs, 3, 0, height, l, m, n, o, light, colorValue);
	}

	private void renderSide(BillboardParticleSubmittable submittable, Vector3f[] vector3fs, int a, int b, float height, float minU, float maxU, float minV, float maxV, int light, int color)
	{
		// Calculate the center position between the two corner vertices
		float centerX = (vector3fs[a].x() + vector3fs[b].x()) / 2.0f;
		float centerY = (vector3fs[a].y() + vector3fs[b].y()) / 2.0f + height / 2.0f;
		float centerZ = (vector3fs[a].z() + vector3fs[b].z()) / 2.0f;

		// Calculate width of this side (distance between corners)
		float dx = vector3fs[b].x() - vector3fs[a].x();
		float dz = vector3fs[b].z() - vector3fs[a].z();
		float sideWidth = (float)Math.sqrt(dx * dx + dz * dz);

		// Calculate rotation to align quad with edge direction
		float edgeAngle = (float)Math.atan2(dz, dx);

		// Set rectangular dimensions in ThreadLocal so the mixin can use them
		RECTANGULAR_DIMS.set(new RectangularDimensions(sideWidth / 2.0f, height / 2.0f));

		try
		{
			// Front-facing rectangular quad
			org.joml.Quaternionf frontRotation = new org.joml.Quaternionf().rotateY(edgeAngle);
			submittable.render(getRenderType(), centerX, centerY, centerZ,
				frontRotation.x, frontRotation.y, frontRotation.z, frontRotation.w,
				1.0f, // Size doesn't matter, mixin will use custom dimensions
				minU, maxU, minV, maxV,
				color, light);

			// Back-facing rectangular quad (180 degrees opposite)
			org.joml.Quaternionf backRotation = new org.joml.Quaternionf().rotateY(edgeAngle + (float)Math.PI);
			submittable.render(getRenderType(), centerX, centerY, centerZ,
				backRotation.x, backRotation.y, backRotation.z, backRotation.w,
				1.0f, // Size doesn't matter, mixin will use custom dimensions
				minU, maxU, minV, maxV,
				color, light);
		}
		finally
		{
			// Clear ThreadLocal to avoid memory leaks
			RECTANGULAR_DIMS.remove();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType>
	{
		private final SpriteProvider provider;

		public Factory(SpriteProvider provider)
		{
			this.provider = provider;
		}

		@Nullable
		@Override
		public Particle createParticle(SimpleParticleType SimpleParticleType, ClientWorld clientWorld, double x, double y, double z, double g, double h, double i, net.minecraft.util.math.random.Random random)
		{
			return new WaterSplashParticle(clientWorld, x, y, z, (float) g, (float) h, provider);
		}
	}
}