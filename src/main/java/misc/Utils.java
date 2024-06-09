package misc;

import hive.packets.*;
import hive.sql.cmdbuilder.SQLCommandType;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Utils {

    /**
     * Deserializes a byte array into an object of {@code Packet} class.
     * @param data the byte array to deserialize.
     * @return the packet object deserialized from the byte array.
     */
    public static Packet deserializePacket(byte[] data) {
        String[] parts = new String(data).split("\\|");
        PacketType type = PacketType.valueOf(parts[0]);
        switch (type) {
            case MESSAGE:
                return new MSGPacket(parts[1]);
            case SQL:
                DBPacket packet = new DBPacket(parts[2], SQLCommandType.valueOf(parts[1]));
                boolean hasColumns = parts[3].isEmpty();

                if (!hasColumns) {
                    String[] columns = parts[3].split(",");
                    for (String column : columns) {
                        String[] keyValue = column.split("=");
                        packet.addColumn(keyValue[0], keyValue[1]);
                    }
                }

                if (parts.length > 4) {
                    packet.setCondition(parts[4]);
                }

                return packet;
            default:
                throw new IllegalArgumentException("Invalid packet type: " + type);
        }
    }

    /**
     * Pretty prints a list of {@code SocketChannel} clients' remote address.
     * @param clients the list of clients to print the remote address of
     * @return a string representation of the clients' remote address as a collection.
     * @throws IOException if an I/O error occurs.
     */
    public static String prettyPrintClients(List<SocketChannel> clients) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i = 0;
        for (SocketChannel client : clients) {
            sb.append(client.getRemoteAddress());
            if(i < clients.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static void strParameterNotNull(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Parameter cannot be null");
        }
    }

    private static void strParameterNotEmpty(String str) {
        if(str.isEmpty()) {
            throw new IllegalArgumentException("Parameter cannot be empty");
        }
    }

    private static void strArrayParameterNotEmpty(String[] strArray) {
        if(strArray.length == 0) {
            throw new IllegalArgumentException("Parameter array cannot be empty");
        }
        for(String str : strArray) {
            strParameterNotNull(str);
            strParameterNotEmpty(str);
        }
    }

    private static void strArrayParameterNotNull(String[] strArray) {
        if(strArray == null) {
            throw new IllegalArgumentException("Parameter array cannot be null");
        }
    }

    public static void strNNorNE(String str) {
        strParameterNotNull(str);
        strParameterNotEmpty(str);
    }

    public static void strArrayNNorNE(String[] strArray) {
        strArrayParameterNotNull(strArray);
        strArrayParameterNotEmpty(strArray);
    }

    public static void mustMatch(String[] arr1, String[] arr2) {
        if(arr1.length != arr2.length) {
            throw new IllegalArgumentException("Arrays must be of the same length");
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Source: <a href="https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java">...</a>
     *
     * Input: Byte array
     * Output: Hexadecimal string corresponding to input (with leading zeroes)
     *
     * @author acc
     */
    public static String bytesToHex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Reads the contents of a UTF-8 `.txt` file and returns it as a string.
     * @param file the file to read.
     * @return the contents of the file as a string.
     */
    public static String readFileRaw(String file) {
        File f = new File(file);
        try {
            String s = java.nio.file.Files.readString(f.toPath());
            if(s.isEmpty())
                throw new IllegalArgumentException("File is empty");
            return s;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
