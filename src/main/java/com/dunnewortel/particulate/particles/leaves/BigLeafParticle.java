package com.dunnewortel.particulate.particles.leaves;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BigLeafParticle extends LeafParticle
{
	protected BigLeafParticle(ClientWorld world, double x, double y, double z, double r, double g, double b, SpriteProvider provider)
	{
		super(world, x, y, z, r, g, b, provider);

		rotateFactor = 9f + ((float) Math.random() * 3f);
		scale = 9f / 32f;
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
		public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velX, double velY, double velZ)
		{
			return new BigLeafParticle(world, x, y, z, velX, velY, velZ, provider);
		}
	}
}
