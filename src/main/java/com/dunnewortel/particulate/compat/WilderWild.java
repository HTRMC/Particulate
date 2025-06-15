package com.dunnewortel.particulate.compat;

import com.dunnewortel.particulate.Main;
import com.dunnewortel.particulate.Particles;
import net.minecraft.util.Identifier;

public class WilderWild
{
	private static String MOD_ID = "wilderwild";

	private static Identifier id(String path)
	{
		return Identifier.of(MOD_ID, path);
	}

	public static void addLeaves()
	{
		Main.registerLeafData(id("baobab_leaves"), new Main.LeafData(Particles.WW_BAOBAB_LEAF));
		Main.registerLeafData(id("cypress_leaves"), new Main.LeafData(Particles.WW_CYPRESS_LEAF));
		Main.registerLeafData(id("palm_fronds"), new Main.LeafData(Particles.WW_PALM_LEAF));
	}
}