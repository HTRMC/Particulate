package com.dunnewortel.particulate.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ViewerCountManager.class)
public interface InvokerViewerCountManager {
    @Invoker
    public abstract void invokeOnContainerOpen(World world, BlockPos pos, BlockState state);

    @Invoker
    public abstract void invokeOnContainerClose(World world, BlockPos pos, BlockState state);

    @Invoker
    public abstract void invokeScheduleBlockTick(World world, BlockPos pos, BlockState state);

}
