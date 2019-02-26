package com.github.revadyndavion.enums;

import com.pixelmonmod.pixelmon.config.PixelmonItemsBadges;

import net.minecraft.item.Item;

public enum EnumBadgeItem {
	BALANCE(PixelmonItemsBadges.balanceBadge),
	BASIC(PixelmonItemsBadges.basicBadge),
	BEACON(PixelmonItemsBadges.beaconBadge),
	BOLT(PixelmonItemsBadges.boltBadge),
	BOULDER(PixelmonItemsBadges.boulderBadge),
	BUG(PixelmonItemsBadges.bugBadge),
	CASCADE(PixelmonItemsBadges.cascadeBadge),
	CLIFF(PixelmonItemsBadges.cliffBadge),
	COAL(PixelmonItemsBadges.coalBadge),
	COBBLE(PixelmonItemsBadges.cobbleBadge),
	DYNAMO(PixelmonItemsBadges.dynamoBadge),
	EARTH(PixelmonItemsBadges.earthBadge),
	FAIRY(PixelmonItemsBadges.fairyBadge),
	FEATHER(PixelmonItemsBadges.featherBadge),
	FEN(PixelmonItemsBadges.fenBadge),
	FOG(PixelmonItemsBadges.fogBadge),
	FOREST(PixelmonItemsBadges.forestBadge),
	FREEZE(PixelmonItemsBadges.freezeBadge),
	GLACIER(PixelmonItemsBadges.glacierBadge),
	HEAT(PixelmonItemsBadges.heatBadge),
	HIVE(PixelmonItemsBadges.hiveBadge),
	ICEBERG(PixelmonItemsBadges.icebergBadge),
	ICICLE(PixelmonItemsBadges.icicleBadge),
	INSECT(PixelmonItemsBadges.insectBadge),
	JET(PixelmonItemsBadges.jetBadge),
	KNUCKLE(PixelmonItemsBadges.knuckleBadge),
	LEGEND(PixelmonItemsBadges.legendBadge),
	MARSH(PixelmonItemsBadges.marshBadge),
	MIND(PixelmonItemsBadges.mindBadge),
	MINE(PixelmonItemsBadges.mineBadge),
	MINERAL(PixelmonItemsBadges.mineralBadge),
	PLAIN(PixelmonItemsBadges.plainBadge),
	PLANT(PixelmonItemsBadges.plantBadge),
	PSYCHIC(PixelmonItemsBadges.psychicBadge),
	QUAKE(PixelmonItemsBadges.quakeBadge),
	RAIN(PixelmonItemsBadges.rainBadge),
	RAINBOW(PixelmonItemsBadges.rainbowBadge),
	RELIC(PixelmonItemsBadges.relicBadge),
	RISING(PixelmonItemsBadges.risingBadge),
	RUMBLE(PixelmonItemsBadges.rumbleBadge),
	SOUL(PixelmonItemsBadges.soulBadge),
	STONE(PixelmonItemsBadges.stoneBadge),
	STORM(PixelmonItemsBadges.stormBadge),
	THUNDER(PixelmonItemsBadges.thunderBadge),
	TOXIC(PixelmonItemsBadges.toxicBadge),
	TRIO(PixelmonItemsBadges.trioBadge),
	VOLCANO(PixelmonItemsBadges.volcanoBadge),
	VOLTAGE(PixelmonItemsBadges.voltageBadge),
	WAVE(PixelmonItemsBadges.waveBadge),
	ZEPHYR(PixelmonItemsBadges.zephyrBadge);
	
	
	private final Item item;
	
	private EnumBadgeItem(Item pixelmonItem) {
		this.item = pixelmonItem;
	}
	
	public Item get() {
		return this.item;
	}
}