package com.lenovo.billing.common;

import java.util.*;
import java.net.*;
import java.io.*;

public class Util {

    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sendString(Socket socket, String str) throws IOException {
        sendBytes(socket, str.getBytes());
    }

    static void sendBytes(Socket socket, byte[] data) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.write(data);
        out.flush();
    }

    public static String toHexString(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] extractCommands(byte[] a) {
        int size = 0;
        for (byte anA : a) {
            if (anA == (byte) 0xfe) {
                size += 8;
            }
        }

        //System.out.format("size = %d\n", size);
        if (size == 0) {
            return null;
        }
        byte[] newA = new byte [size];
        System.arraycopy(a, 0, newA, 0, size);
        return newA;
    }

    public static String generateTidListJson(Set tagSet) {
        StringBuilder str = new StringBuilder();
        Object[] tagSetArray = tagSet.toArray();
        str.append("{\n");
        str.append("    \"tidList\": [\n");
        if (tagSetArray != null) {
            for (int i = 0; i < tagSetArray.length; i++) {
                String tid = (String) tagSetArray[i];
                if (i == tagSet.size() - 1) {
                    str.append(String.format("        \"%s\"\n", tid));
                } else {
                    str.append(String.format("        \"%s\",\n", tid));
                }
            }
        }
        str.append("    ]\n");
        str.append("}\n");
        System.out.println(str);

        return str.toString();
    }

    public static void dispatchEvent(EventHandler handler, String event) {
        handler.handleEvent(event);
    }
}