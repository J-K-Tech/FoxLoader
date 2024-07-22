package com.fox2code.foxloader.loader.packet;

import com.fox2code.foxloader.loader.ModLoader;
import com.fox2code.foxloader.registry.EntityTypeRegistryEntry;
import com.fox2code.foxloader.registry.RegistryEntry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class ServerHello extends FoxPacket {
    /**
	 * <p>The current version of the Server Hello packet.</p>
     * <b>V0</b> - <i>Block and Item registry entries and metadata</i>.<br/>
     * <b>V1</b> - <i>Entity type registry metadata appended to the end</i>.
     */
    public static final short SERVER_HELLO_VERSION = 1;
    /**
     * <p>The minimum version of ServerHello on older clients for the server to be compatible with.</p>
     * V0 clients will always expect exactly {@literal 0}.
     */
    private static final short CLIENT_BACKWARD_COMPATIBILITY_VERSION = 0;

    public HashMap<String, RegistryEntry> registryEntries;
    public HashMap<String, String> metadata;
    public HashMap<String, EntityTypeRegistryEntry> entityTypeRegistryEntries;

    public ServerHello() {
        super(0, false);
    }

    public ServerHello(HashMap<String, RegistryEntry> registryEntries,
                       HashMap<String, String> metadata,
                       HashMap<String, EntityTypeRegistryEntry> entityTypeRegistryEntries) {
        super(0, false);
        this.registryEntries = registryEntries;
        this.metadata = metadata;
        this.entityTypeRegistryEntries = entityTypeRegistryEntries;
    }

	/**
	 * @throws IOException if the data cannot be read as instructed.
	 */
    @Override
    public void readData(DataInputStream inStream) throws IOException {
        int serverHelloVersion = inStream.readUnsignedShort();
        int clientBackwardCompatibilityVersion = inStream.readUnsignedShort();
        if (SERVER_HELLO_VERSION < clientBackwardCompatibilityVersion) {
            throw new IOException("Client is critically out of date, please update FoxLoader.");
        }

        int entries = inStream.readUnsignedShort();
        this.registryEntries = new HashMap<>(entries);
        while (entries-- > 0) {
            RegistryEntry entry = new RegistryEntry(
                    inStream.readShort(), inStream.readShort(),
                    inStream.readUTF(), inStream.readUTF());
            this.registryEntries.put(entry.name, entry);
        }

        if (inStream.available() < 2) {
            // Too few bytes to read the next short.
            this.metadata = new HashMap<>();
            ModLoader.getModLoaderLogger().log(Level.WARNING, "Server Hello: Too few bytes to read metadata.");
        } else {
            entries = inStream.readUnsignedShort();
            metadata = new HashMap<>();
            while (entries-- > 0) {
                metadata.put(inStream.readUTF(), inStream.readUTF());
            }
        }

        if (inStream.available() < 4) {
            this.entityTypeRegistryEntries = new HashMap<>();
            ModLoader.getModLoaderLogger().log(Level.WARNING, "Server Hello: Too few bytes to read entity data.");
        } else {
            int entityEntries = inStream.readInt();
            entityTypeRegistryEntries = new HashMap<>(entityEntries);
            while (entityEntries-- > 0) {
                EntityTypeRegistryEntry entry = new EntityTypeRegistryEntry(
                        inStream.readInt(), inStream.readInt(), inStream.readUTF());
                this.entityTypeRegistryEntries.put(entry.name, entry);
            }
        }
    }

    @Override
    public void writeData(DataOutputStream outStream) throws IOException {
        outStream.writeShort(SERVER_HELLO_VERSION);
        outStream.writeShort(CLIENT_BACKWARD_COMPATIBILITY_VERSION);

        outStream.writeShort(this.registryEntries.size());
        for (RegistryEntry registryEntry : registryEntries.values()) {
            outStream.writeShort(registryEntry.realId);
            outStream.writeByte(registryEntry.fallbackId);
            outStream.writeUTF(registryEntry.name);
            outStream.writeUTF(registryEntry.fallbackDisplayName);
        }

        outStream.writeShort(this.metadata.size());
        for (Map.Entry<String, String> metadata : this.metadata.entrySet()) {
            outStream.writeUTF(metadata.getKey());
            outStream.writeUTF(metadata.getValue());
        }

        outStream.writeInt(this.entityTypeRegistryEntries.size());
        for (EntityTypeRegistryEntry entry : entityTypeRegistryEntries.values()) {
            outStream.writeInt(entry.realId);
            outStream.writeInt(entry.fallbackId);
            outStream.writeUTF(entry.name);
        }
    }
}
