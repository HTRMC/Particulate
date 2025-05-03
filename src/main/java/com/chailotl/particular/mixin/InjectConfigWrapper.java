package com.chailotl.particular.mixin;

import com.chailotl.particular.sushi_bar.owo.config.SushiConfigScreen;
import com.chailotl.particular.sushi_bar.owo.config.SushiModmenu;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.ui.ConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ConfigWrapper.class)
public class InjectConfigWrapper
{
	@Inject(
		method = "<init>(Ljava/lang/Class;Lio/wispforest/owo/config/ConfigWrapper$BuilderConsumer;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/loader/api/FabricLoader;getEnvironmentType()Lnet/fabricmc/api/EnvType;"
		)
	)
	private <C> void injectSushiModmenu(Class<C> clazz, ConfigWrapper.BuilderConsumer consumer, CallbackInfo ci)
	{
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && clazz.isAnnotationPresent(SushiModmenu.class))
		{
			SushiModmenu annotation = clazz.getAnnotation(SushiModmenu.class);
			ConfigScreen.registerProvider(
				annotation.modId(),
				screen -> SushiConfigScreen.createWithCustomModel(Identifier.of(annotation.uiModelId()), (ConfigWrapper<C>)(Object)this, screen)
			);
		}
	}
}