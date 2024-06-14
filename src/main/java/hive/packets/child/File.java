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

    public File(byte[] fileBytes) {
        super(PacketType.FILE);
        this.fileBytes = fileBytes;
    }

    @Override
    public byte[] serialize() {
        byte[] data = String.format("%s|", this.getType()).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length).put(data);
        return buffer.array();
    }
}
