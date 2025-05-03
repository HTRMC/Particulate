package com.dunnewortel.particulate.mixin;

import net.minecraft.client.particle.BillboardParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BillboardParticle.class)
public interface AccessorBillboardParticle
{
	@Accessor
	void setScale(float scale);
}