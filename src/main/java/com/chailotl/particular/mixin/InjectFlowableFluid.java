package com.chailotl.particular.mixin;

import net.minecraft.fluid.FlowableFluid;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlowableFluid.class)
public class InjectFlowableFluid
{
	// Cascade updates moved to client-side InjectFlowableFluidClient
}
