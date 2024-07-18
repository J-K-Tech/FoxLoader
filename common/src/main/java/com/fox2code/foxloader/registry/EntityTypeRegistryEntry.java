package com.fox2code.foxloader.registry;

public class EntityTypeRegistryEntry {
	public final int realId, fallbackId;
	public final String name;

	EntityTypeRegistryEntry(int realId, int fallbackId, String name) {
		this.realId = realId;
		this.fallbackId = fallbackId;
		this.name = name;
	}
}
