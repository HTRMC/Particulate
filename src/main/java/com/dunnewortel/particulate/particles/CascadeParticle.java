package com.dunnewortel.particulate.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class CascadeParticle extends BillboardParticle
{
	protected final SpriteProvider provider;

	protected CascadeParticle(ClientWorld clientWorld, double x, double y, double z, SpriteProvider provider)
	{
		super(clientWorld, x, y, z, provider.getSprite(clientWorld.getRandom()));
		this.provider = provider;
		maxAge = 9;
		scale = 1f;
		gravityStrength = 0.4f;
		setVelocity(random.nextDouble() * 0.25f - 0.125f, 0, random.nextDouble() * 0.25f - 0.125f);
		updateSprite(provider);
		removeIfInsideSolidBlock();
	}

	@Override
	public void tick()
	{
		super.tick();

		removeIfInsideSolidBlock();

		updateSprite(provider);
	}

	private void removeIfInsideSolidBlock()
	{
		BlockPos pos = BlockPos.ofFloored(new Vec3d(x, y, z));
		if (world.getBlockState(pos).isSolidBlock(world, pos))
		{
			alpha = 0;
			markDead();
		}
	}

	@Override
	protected RenderType getRenderType()
	{
		return RenderType.PARTICLE_ATLAS_OPAQUE;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType>
	{
		private final SpriteProvider provider;

		public Factory(SpriteProvider provider)
		{
			this.provider = provider;
		}

		@Override
		public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ, Random random)
		{
			return new CascadeParticle(world, x, y, z, provider);
		}
	}
}