package com.chailotl.particular.mixin;

import com.chailotl.particular.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Mixin(LeavesBlock.class)
public class InjectLeavesBlock
{
	// Set of vanilla leaf blocks to skip
	private static final Set<Block> VANILLA_LEAVES = new HashSet<Block>() {{
		add(Blocks.OAK_LEAVES);
		add(Blocks.BIRCH_LEAVES);
		add(Blocks.SPRUCE_LEAVES);
		add(Blocks.JUNGLE_LEAVES);
		add(Blocks.ACACIA_LEAVES);
		add(Blocks.DARK_OAK_LEAVES);
		add(Blocks.AZALEA_LEAVES);
		add(Blocks.FLOWERING_AZALEA_LEAVES);
		add(Blocks.MANGROVE_LEAVES);
		add(Blocks.CHERRY_LEAVES);
		add(Blocks.PALE_OAK_LEAVES);
	}};

	@Inject(
		method = "randomDisplayTick",
		at = @At("HEAD"))
	private void dropLeaves(BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci)
	{
		if (!Main.CONFIG.fallingLeaves()) { return; }
		
		// Skip vanilla leaf blocks as they have their own particles in 1.21.5+
		Block block = state.getBlock();
		if (VANILLA_LEAVES.contains(block)) { return; }

		if (random.nextInt(Main.CONFIG.fallingLeavesSettings.spawnChance()) == 0)
		{
			BlockPos blockPos = pos.down();
			BlockState blockState = world.getBlockState(blockPos);
			if (!Block.isFaceFullSquare(blockState.getCollisionShape(world, blockPos), Direction.UP))
			{
				double x = pos.getX() + 0.02d + random.nextDouble() * 0.96d;
				double y = pos.getY() - 0.05d;
				double z = pos.getZ() + 0.02d + random.nextDouble() * 0.96d;

				Main.LeafData leafData = Main.getLeafData(state.getBlock());

				ParticleEffect particle = leafData.getParticle();
				if (particle == null) { return; }
				Color color = leafData.getColor(world, pos);

				Particle leaf = MinecraftClient.getInstance().particleManager.addParticle(particle, x, y, z, 0, 0, 0);
				if (leaf != null)
				{
					leaf.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
				}
			}
		}
	}
}