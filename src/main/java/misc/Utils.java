package misc;

import hive.packets.*;
import hive.packets.child.*;

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
        if(parts.length == 0) return null;
        PacketType type;

        try {
            type = PacketType.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }

        return switch (type) {
            case MESSAGE -> new Message(parts[1]);
            case SQL -> new SQLacket();
            case AUTH -> Auth.of(parts[1], parts[2]);
            case POST -> new Post(parts[1]);
            case RESPONSE -> new Response();
            case FILE -> {
                byte[] fileBytes = new byte[data.length - parts[0].length() - 1];
                System.arraycopy(data, parts[0].length() + 1, fileBytes, 0, fileBytes.length);
                yield new hive.packets.child.File(fileBytes);
            }
        };
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

    public static void mustMatch(String[] arr1, int length) {
        if(arr1.length != length) {
            throw new IllegalArgumentException("Arrays must be of length: " + length + ", but was " + arr1.length);
        }
    }

    public static void mustMatch(String[] arr1, String[] arr2) {
        if(arr1.length != arr2.length) {
            throw new IllegalArgumentException("Arrays must be of the same length");
        }
    }

    public static String hashPassword(String password) throws IllegalArgumentException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Source: <a href="https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java">...</a>
     * Input: Byte array
     * Output: Hexadecimal string corresponding to input (with leading zeroes)
     *
     * @author acc
     */
    private static String bytesToHex(byte[] bytes) {
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

    /**
     * Generates a random string of length {@code i}.
     * @param i the length of the string to generate.
     * @return a random string of length {@code i}.
     */
    public static String randomString(int i) {
        String strSet = "_+!@#$%^&*()ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        while (i-- != 0) {
            int character = (int)(Math.random()*strSet.length());
            char c = strSet.charAt(character);

            if(Character.isAlphabetic(c) && Math.random() > 0.49f)
                builder.append(Character.toLowerCase(c));

            builder.append(strSet.charAt(character));
        }
        return builder.toString();
    }
}
