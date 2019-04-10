package base.storage;

public class RestorerData {
    private final byte[] data;
    private final int chunk_no;

    public RestorerData(byte[] data, int chunk_no) {
        this.data = data;
        this.chunk_no = chunk_no;
    }

    public byte[] getData() {
        return data;
    }

    public int getChunkNo() {
        return chunk_no;
    }
}
