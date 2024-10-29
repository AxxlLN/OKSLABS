package com.oks.okslabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Packets {
    private static final int MAX_DATA_SIZE = 26;
    static final int GROUP_NUMBER = 26;
    private static final int FIXED_DATA_LENGTH = GROUP_NUMBER + 1;
    private byte flag;
    private byte sourceAddress;
    private byte destinationAddress;
    private byte[] data;
    private byte fcs;

    public Packets(byte flag, byte sourceAddress, byte destinationAddress, byte[] data) {
        this.flag = flag;
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.data = data;
        this.fcs = 0;
    }

    public byte[] toByteArrayWithBitStuffing() {
        byte[] frame = new byte[data.length + 4];
        frame[0] = flag;
        frame[1] = sourceAddress;
        frame[2] = destinationAddress;
        System.arraycopy(data, 0, frame, 3, data.length);
        frame[frame.length - 1] = fcs;
        return frame;
    }

    public String displayFrameStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("Структура кадра:\n");
        sb.append("Флаг: 0x").append(String.format("%02X", flag)).append("\n");
        sb.append("Исходный адрес: 0x").append(String.format("%02X", sourceAddress)).append("\n");
        sb.append("Адрес назначения: 0x").append(String.format("%02X", destinationAddress)).append("\n");
        sb.append("Данные: ");
        for (byte b : data) {
            sb.append("0x").append(String.format("%02X", b)).append(" ");
        }
        sb.append("\nFCS: 0x").append(String.format("%02X", fcs));
        return sb.toString();
    }

    public static List<Packets> fragmentData(byte flag, byte sourceAddress, byte destinationAddress, byte[] data) {
        List<Packets> packetList = new ArrayList<>();
        int offset = 0;

        while (offset < data.length) {
            int length = Math.min(MAX_DATA_SIZE, data.length - offset);
            byte[] fragment = new byte[length];
            System.arraycopy(data, offset, fragment, 0, length);
            offset += length;

            if (fragment.length < FIXED_DATA_LENGTH) {
                byte[] paddedFragment = new byte[FIXED_DATA_LENGTH];
                System.arraycopy(fragment, 0, paddedFragment, 0, fragment.length);
                Arrays.fill(paddedFragment, fragment.length, FIXED_DATA_LENGTH, (byte) 0x00);
                packetList.add(new Packets(flag, sourceAddress, destinationAddress, paddedFragment));
            } else {
                packetList.add(new Packets(flag, sourceAddress, destinationAddress, fragment));
            }
        }

        return packetList;
    }
}