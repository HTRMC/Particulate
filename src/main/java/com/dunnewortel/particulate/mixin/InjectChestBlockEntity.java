package com.dunnewortel.particulate.mixin;

import com.dunnewortel.particulate.Main;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class InjectChestBlockEntity extends LootableContainerBlockEntity implements LidOpenable
{
	@Unique
	private int ticksUntilNextSwitch = 20;
	@Unique
	private boolean isOpen = false;

	@Unique
	private static final int minClosedTime = 20 * 8;
	@Unique
	private static final int maxClosedTime = 20 * 24;
	@Unique
	private static final int minOpenTime = 20 * 2;
	@Unique
	private static final int maxOpenTime = 20 * 3;

	public InjectChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Unique
	private static boolean getSoulSand(World world, BlockPos pos, BlockState state)
	{
		pos = pos.down();

		if (world.getBlockState(pos).getBlock() == Blocks.SOUL_SAND) { return true; }

		if (state.get(Properties.CHEST_TYPE) == ChestType.RIGHT)
		{
			BlockPos pos2 = pos.add(state.get(Properties.HORIZONTAL_FACING).rotateCounterclockwise(Direction.Axis.Y).getVector());

			return world.getBlockState(pos2).getBlock() == Blocks.SOUL_SAND;
		}

		return false;
	}

	@SuppressWarnings("InvalidInjectorMethodSignature")
	@Inject(
		method = "clientTick",
		at = @At("TAIL"))
	private static void randomlyOpen(World world, BlockPos pos, BlockState state, InjectChestBlockEntity blockEntity, CallbackInfo ci)
	{
		if (!Main.CONFIG.soulSandBubbles()) { return; }

		if (!state.get(Properties.WATERLOGGED) ||
			state.get(Properties.CHEST_TYPE) == ChestType.LEFT ||
			!getSoulSand(world, pos, state))
		{
			return;
		}

		if (--blockEntity.ticksUntilNextSwitch <= 0)
		{
			ViewerCountManager manager = ((AccessorChestBlockEntity) blockEntity).getStateManager();
			if (blockEntity.isOpen)
			{
				blockEntity.isOpen = false;
				blockEntity.ticksUntilNextSwitch = world.random.nextBetween(minClosedTime, maxClosedTime);
				((AccessorChestBlockEntity) blockEntity).getLidAnimator().setOpen(false);
				((InvokerViewerCountManager)manager).invokeOnContainerClose(world, pos, blockEntity.getCachedState());
			}
			else
			{
				blockEntity.isOpen = true;
				blockEntity.ticksUntilNextSwitch = world.random.nextBetween(minOpenTime, maxOpenTime);
				((AccessorChestBlockEntity) blockEntity).getLidAnimator().setOpen(true);
				((InvokerViewerCountManager)manager).invokeOnContainerOpen(world, pos, blockEntity.getCachedState());
				world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.AMBIENT, 1f, 1f, true);
			}
		}

		if (blockEntity.isOpen &&
			blockEntity.ticksUntilNextSwitch > 10 &&
			blockEntity.ticksUntilNextSwitch % 2 == 0)
		{
			if (state.get(Properties.CHEST_TYPE) == ChestType.SINGLE)
			{
				Main.spawnBubble(ParticleTypes.BUBBLE_COLUMN_UP, world, pos);
			}
			else
			{
				Main.spawnDoubleBubbles(ParticleTypes.BUBBLE_COLUMN_UP, world, pos, state);
			}
		}
	}
}