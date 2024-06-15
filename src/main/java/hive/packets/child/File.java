package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * A packet that represents a file.
 * Created by SixEyes on 06/13/24.
 */
public class File extends Packet {

    byte[] fileBytes;
    private final String fileName;

    public File(String fileName, byte[] fileBytes) {
        super(PacketType.FILE);
        this.fileName = fileName;
        this.fileBytes = fileBytes;
    }

    public String getFileName() {
        return this.fileName;
    }

    public byte[] getFileBytes() {
        return this.fileBytes;
    }

    public void writeToFile() throws IOException {
        Path downloads = Paths.get("").toAbsolutePath().resolve("downloads").toAbsolutePath();
        if(!downloads.toFile().exists()) {
            if(!downloads.toFile().mkdir()) {
                throw new IOException("Failed to create downloads directory.");
            }
        }
        Path filePath = downloads.resolve(getFileName()).toAbsolutePath();
        if(!filePath.toFile().exists()) {
            Files.write(filePath, getFileBytes(), StandardOpenOption.CREATE_NEW);
        } else {
            Files.write(filePath, getFileBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @Override
    public byte[] serialize() {
        byte[] packetData = String.format("%s|%s", this.getType(), getFileName()).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(
                (2 * Integer.BYTES) + (packetData.length + fileBytes.length)
        );
        buffer.putInt(packetData.length).put(packetData).putInt(getFileBytes().length).put(getFileBytes());
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("{File: %s}", this.getFileName());
    }
}
