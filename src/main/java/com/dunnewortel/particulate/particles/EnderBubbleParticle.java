package com.dunnewortel.particulate.particles;

import com.dunnewortel.particulate.Main;
import com.dunnewortel.particulate.Particles;
import com.dunnewortel.particulate.mixin.AccessorBillboardParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

public class EnderBubbleParticle extends BubbleColumnUpParticle
{
	protected EnderBubbleParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Sprite sprite)
	{
		super(clientWorld, d, e, f, g, h, i, sprite);
	}

	@Override
	public void tick()
	{
		super.tick();

		if (!Main.CONFIG.poppingBubbles()) { return; }

		if (this.dead)
		{
			Particle bubble = MinecraftClient.getInstance().particleManager.addParticle(Particles.ENDER_BUBBLE_POP, x, y, z, 0, 0, 0);
			if (bubble != null)
			{
				((AccessorBillboardParticle) bubble).setScale(this.scale * 2f);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleFactory<SimpleParticleType>
	{
		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(SimpleParticleType SimpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random)
		{
			Sprite sprite = this.spriteProvider.getSprite(random);
			EnderBubbleParticle enderBubbleParticle = new EnderBubbleParticle(clientWorld, d, e, f, g, h, i, sprite);
			enderBubbleParticle.setSprite(sprite);
			return enderBubbleParticle;
		}
	}
}