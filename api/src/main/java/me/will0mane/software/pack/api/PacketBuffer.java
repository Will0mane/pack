package me.will0mane.software.pack.api;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.ArrayList;
import java.util.List;

public class PacketBuffer {

    public static final int MAX_PACKET_SIZE = 2048;

    private final boolean read;

    private final ByteArrayDataInput input;
    private final ByteArrayDataOutput output;

    public PacketBuffer(byte[] buffer) {
        this.read = true;
        this.input = ByteStreams.newDataInput(buffer);
        this.output = ByteStreams.newDataOutput();
    }

    public PacketBuffer() {
        this.read = false;
        this.input = null;
        this.output = ByteStreams.newDataOutput();
    }

    public void checkReadOnly() throws IllegalStateException {
        if(!this.read) return;
        throw new IllegalStateException("PacketBuffer is read-only");
    }

    // WRITE

    public PacketBuffer writeUTF(String str) {
        checkReadOnly();
        this.output.writeUTF(str);
        return this;
    }

    public PacketBuffer writeBoolean(boolean b) {
        checkReadOnly();
        this.output.writeBoolean(b);
        return this;
    }

    public PacketBuffer writeInt(int i) {
        checkReadOnly();
        this.output.writeInt(i);
        return this;
    }

    public PacketBuffer writeShort(short s) {
        checkReadOnly();
        this.output.writeShort(s);
        return this;
    }

    public PacketBuffer writeDouble(double d) {
        checkReadOnly();
        this.output.writeDouble(d);
        return this;
    }


    public PacketBuffer writeFloat(float f) {
        checkReadOnly();
        this.output.writeFloat(f);
        return this;
    }

    public PacketBuffer writeLong(long l) {
        checkReadOnly();
        this.output.writeLong(l);
        return this;
    }

    public PacketBuffer writeChar(char c) {
        checkReadOnly();
        this.output.writeChar(c);
        return this;
    }

    public PacketBuffer writeByte(byte b) {
        checkReadOnly();
        this.output.writeByte(b);
        return this;
    }

    public PacketBuffer writeEnum(Enum<?> e) {
        return writeUTF(e.name());
    }

    public PacketBuffer writeArray(byte[] bytes) {
        checkReadOnly();
        writeInt(bytes.length);
        for (byte aByte : bytes) {
            this.output.writeByte(aByte);
        }
        return this;
    }

    public PacketBuffer writeArray(int[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (int i : arr) {
            this.output.writeInt(i);
        }
        return this;
    }

    public PacketBuffer writeArray(double[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (double var : arr) {
            this.output.writeDouble(var);
        }
        return this;
    }

    public PacketBuffer writeArray(short[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (short var : arr) {
            this.output.writeShort(var);
        }
        return this;
    }

    public PacketBuffer writeArray(float[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (float var : arr) {
            this.output.writeFloat(var);
        }
        return this;
    }


    public PacketBuffer writeArray(String[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (String var : arr) {
            this.output.writeUTF(var);
        }
        return this;
    }

    public PacketBuffer writeArray(long[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (long var : arr) {
            this.output.writeLong(var);
        }
        return this;
    }

    public PacketBuffer writeArray(char[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (char var : arr) {
            this.output.writeChar(var);
        }
        return this;
    }

    public PacketBuffer writeArray(boolean[] arr) {
        checkReadOnly();
        writeInt(arr.length);
        for (boolean var : arr) {
            this.output.writeBoolean(var);
        }
        return this;
    }

    public byte[] writeFully() {
        checkReadOnly();
        return this.output.toByteArray();
    }

    // READ

    public void checkWriteOnly() {
        if(this.read) return;
        throw new IllegalStateException("PacketBuffer is write-only");
    }

    public String readUTF() {
        checkWriteOnly();
        return this.input.readUTF();
    }

    public int readInt() {
        checkWriteOnly();
        return this.input.readInt();
    }

    public short readShort() {
        checkWriteOnly();
        return this.input.readShort();
    }

    public double readDouble() {
        checkWriteOnly();
        return this.input.readDouble();
    }

    public float readFloat() {
        checkWriteOnly();
        return this.input.readFloat();
    }

    public long readLong() {
        checkWriteOnly();
        return this.input.readLong();
    }

    public char readChar() {
        checkWriteOnly();
        return this.input.readChar();
    }

    public byte readByte() {
        checkWriteOnly();
        return this.input.readByte();
    }

    public byte[] readBytes() {
        checkWriteOnly();
        int len = this.input.readInt();
        byte[] result = new byte[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readByte();
        }
        return result;
    }

    public byte[] readBytes(int length) {
        checkWriteOnly();
        byte[] result = new byte[length];
        for(int i = 0; i < length; ++i) {
            result[i] = this.input.readByte();
        }
        return result;
    }

    public int[] readInts() {
        checkWriteOnly();
        int len = this.input.readInt();
        int[] result = new int[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readInt();
        }
        return result;
    }

    public String[] readUTFs() {
        checkWriteOnly();
        int len = this.input.readInt();
        String[] result = new String[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readUTF();
        }
        return result;
    }

    public char[] readChars() {
        checkWriteOnly();
        int len = this.input.readInt();
        char[] result = new char[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readChar();
        }
        return result;
    }

    public double[] readDoubles() {
        checkWriteOnly();
        int len = this.input.readInt();
        double[] result = new double[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readDouble();
        }
        return result;
    }

    public float[] readFloats() {
        checkWriteOnly();
        int len = this.input.readInt();
        float[] result = new float[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readFloat();
        }
        return result;
    }

    public long[] readLongs() {
        checkWriteOnly();
        int len = this.input.readInt();
        long[] result = new long[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readLong();
        }
        return result;
    }

    public short[] readShorts() {
        checkWriteOnly();
        int len = this.input.readInt();
        short[] result = new short[len];
        for(int i = 0; i < len; ++i) {
            result[i] = this.input.readShort();
        }
        return result;
    }

    public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
        checkWriteOnly();
        String string = readUTF();
        return Enum.valueOf(enumClass, string);
    }

    public byte[] heavyReadFully() {
        checkWriteOnly();
        List<Byte> result = new ArrayList<>();

        while(true) {
            try {
                result.add(this.input.readByte());
            }catch (IllegalStateException ignored) {
                break;
            }
        }

        byte[] arr = new byte[result.size()];
        for(int i = 0; i < result.size(); ++i) {
            arr[i] = result.get(i);
        }

        return arr;
    }

    public Byte[] lightReadFully() {
        checkWriteOnly();
        List<Byte> result = new ArrayList<>();

        while(true) {
            try {
                result.add(this.input.readByte());
            }catch (IllegalStateException ignored) {
                break;
            }
        }

        return result.toArray(new Byte[]{});
    }

    public PacketBuffer skip(int length) {
        checkWriteOnly();
        this.input.skipBytes(length);
        return this;
    }

}
