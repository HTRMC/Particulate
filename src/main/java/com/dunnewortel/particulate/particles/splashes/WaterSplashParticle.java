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
		// Each side needs 2 quads (front and back faces)
		renderSide(submittable, vector3fs, 0, 1, height, l, m, n, o, light, colorValue);
		renderSide(submittable, vector3fs, 1, 2, height, l, m, n, o, light, colorValue);
		renderSide(submittable, vector3fs, 2, 3, height, l, m, n, o, light, colorValue);
		renderSide(submittable, vector3fs, 3, 0, height, l, m, n, o, light, colorValue);
	}

	private void renderSide(BillboardParticleSubmittable submittable, Vector3f[] vector3fs, int a, int b, float height, float minU, float maxU, float minV, float maxV, int light, int color)
	{
		// Calculate direction vector for this side
		float dx = vector3fs[b].x() - vector3fs[a].x();
		float dz = vector3fs[b].z() - vector3fs[a].z();
		float sideWidth = (float)Math.sqrt(dx * dx + dz * dz);

		// Calculate angle - the quad should be aligned with the edge direction to form box walls
		float angle = (float)Math.atan2(dz, dx);

		// Create quaternion for a vertical quad aligned with the edge
		// Rotate around Y axis to align with the edge direction (not perpendicular)
		org.joml.Quaternionf rotation = new org.joml.Quaternionf()
			.rotateY(angle);  // Align with the edge direction

		org.joml.Quaternionf backRotation = new org.joml.Quaternionf()
			.rotateY(angle + (float)Math.PI);  // Opposite side (180 degrees)

		// Since BillboardParticleSubmittable only supports square quads (single size parameter),
		// we need to render multiple quads to create the rectangular vertical side.
		// Render quads with width matching sideWidth, stacked to reach full height
		int numQuads = Math.max(1, (int)Math.ceil(height / sideWidth));
		float quadHeight = height / numQuads;

		for (int i = 0; i < numQuads; i++)
		{
			float yOffset = (i + 0.5f) * quadHeight;
			float centerX = (vector3fs[a].x() + vector3fs[b].x()) / 2.0f;
			float centerY = (vector3fs[a].y() + vector3fs[b].y()) / 2.0f + yOffset;
			float centerZ = (vector3fs[a].z() + vector3fs[b].z()) / 2.0f;

			// Calculate UV coordinates for this segment
			float vRange = maxV - minV;
			float segmentMinV = minV + (vRange * i / numQuads);
			float segmentMaxV = minV + (vRange * (i + 1) / numQuads);

			// Render front face
			submittable.render(getRenderType(), centerX, centerY, centerZ,
				rotation.x, rotation.y, rotation.z, rotation.w,
				sideWidth / 2.0f,  // Use sideWidth for consistent quad sizing
				minU, maxU, segmentMinV, segmentMaxV,
				color, light);

			// Render back face
			submittable.render(getRenderType(), centerX, centerY, centerZ,
				backRotation.x, backRotation.y, backRotation.z, backRotation.w,
				sideWidth / 2.0f,
				minU, maxU, segmentMinV, segmentMaxV,
				color, light);
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