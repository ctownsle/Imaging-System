package Tree;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BTree implements Serializable{

    private static final int MAX_NUMBER_OF_ELEMENTS_PER_NODE = 2;
    private static final int NUM_CHILDREN = 2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE;
    private static final int NUM_KEYS = 2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1;
    private static final int NODE_SIZE = 69;

    public class Node implements Serializable{

        /*
        Node class handles generation of nodes, with 2t children and 2t - 1
        elements per node. Initializes children and keys to -1, which is essentially empty.
         */
        private long [] keys = new long [2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE -1];
        private int numOfKeys;
        private long [] children = new long[2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE];
        private boolean leaf;
        private long address;

        public Node(final long[] keys, final int numOfKeys, final long[] children, final boolean leaf, final long address){

            this.keys = keys;
            this.numOfKeys = numOfKeys;
            this.children = children;
            this.leaf = leaf;
            this.address = address;

        }

        public Node(){

            for (int i = 0; i < 2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE; i++) {
                children[i] = -1;
            }

            for (int i = 0; i < 2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1 ; i++) {
                keys[i] = -1;
            }


            leaf = true;
            address = -1;
            numOfKeys = 0;

        }

        @Override
        public String toString() {
            String str = "[";
            for (int i = 0; i < keys.length; i++) {
                str = str.concat(String.format("%d, ", keys[i]));
            }
            return "str " + str + "]"  + " " + address;
        }

        public long[] getKeys() {
            return keys;
        }
    }

    private final File bTreeFile;
    public Node root;
    private int treeNum;

    public BTree(final int i) throws IOException{
        treeNum = i;
        if(treeNum == 0)
            bTreeFile = new File("BTree.raf");
        else
            bTreeFile = new File("BTree2.raf");

        if(!bTreeFile.exists()){
            bTreeFile.createNewFile();
            create();
        } else {
            long rootAddress = getRootAddress();
            root = diskRead(rootAddress);
        }
    }
    /*
    Writes node to disk
     */
    private void diskWrite(final Node x) throws IOException{
        RandomAccessFile file;
        if(treeNum == 0)
            file = new RandomAccessFile("BTree.raf", "rw");
        else
            file = new RandomAccessFile("BTree2.raf", "rw");
        file.seek(x.address);

        byte [] nodeBytes = new byte [NODE_SIZE];

        byte [] bytes = nodeToBytes(x);

        System.arraycopy(bytes, 0, nodeBytes, 0, bytes.length);
        file.write(nodeBytes);
        file.close();
    }

    private Node diskRead(final long address) throws IOException{
        RandomAccessFile file;
        if(treeNum == 0)
            file = new RandomAccessFile("BTree.raf", "rw");
        else
            file = new RandomAccessFile("BTree2.raf", "rw");

        file.seek(address);
        byte [] nodeBytes = new byte[NODE_SIZE];
        file.readFully(nodeBytes);

        Node x = bytesToNode(nodeBytes);
        file.close();

        return x;
    }

    private Node allocateNode() throws IOException{
        Node x = new Node();
        RandomAccessFile file;
        if(treeNum == 0)
            file = new RandomAccessFile("BTree.raf","rw");
        else
            file = new RandomAccessFile("BTree2.raf", "rw");
        x.address = file.length();
        file.close();
        return x;
    }

    private void insertNonFull(final Node x, final String key) throws IOException, ClassNotFoundException{
        int i = x.numOfKeys - 1;
        // For now do comparison of key values based on first x digits
        // maybe find a way to check dates afterwards
        if(x.leaf == true){
            // do comparisons on keys
            while(i >= 0 && Long.parseLong(key) < x.keys[i]){
                x.keys[i + 1] = x.keys[i];
                i -= 1;
            }

            x.keys[i + 1] = Long.parseLong(key);
            x.numOfKeys = x.numOfKeys + 1;
            diskWrite(x);
        } else {
            while (i >= 0 && Long.parseLong(key) < x.keys[i]){
                i -= 1;
            }

            i += 1;
            Node xc1 = diskRead(x.children[i]);
            // check if node is full
            if(xc1.numOfKeys == (2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE) - 1){
                splitChild(x, i);
                if(Long.parseLong(key) > x.keys[i])
                    i += 1;
            }

            insertNonFull(diskRead(x.children[i]), key);
        }



    }

    private void splitChild(final Node x, final int i) throws IOException, ClassNotFoundException{
        Node z = allocateNode();
        Node y = diskRead(x.children[i]);
        z.leaf = y.leaf;
        z.numOfKeys = MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1;
        for (int j = 0; j < MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1; j++) {
            z.keys[j] = y.keys[MAX_NUMBER_OF_ELEMENTS_PER_NODE + j];
        }

        if (y.leaf == false){
            for (int j = 0; j < MAX_NUMBER_OF_ELEMENTS_PER_NODE; j++) {
                z.children[j] = y.children[j + MAX_NUMBER_OF_ELEMENTS_PER_NODE];
            }
        }
        y.numOfKeys = MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1;
        for (int j = x.numOfKeys; j >= i + 1 ; j-- ) {
            x.children[j + 1] = x.children[j];
        }

        x.children[i + 1] = z.address;

        for (int j = x.numOfKeys - 1; j >= i ; j--) {
            x.keys[j + 1] = x.keys[j];
        }

        x.keys[i] = y.keys[MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1];

        x.numOfKeys = x.numOfKeys + 1;

        diskWrite(y);
        diskWrite(z);
        diskWrite(x);
    }

    private void create() throws IOException{

        Node x = allocateNode();
        x.leaf = true;
        x.numOfKeys = 0;
        root = x;
        writeRootAddress();
        diskWrite(x);

    }

    public void insert(final BTree T, final String key) throws IOException, ClassNotFoundException{
        Node r = T.root;
        if(r.numOfKeys == 2 * MAX_NUMBER_OF_ELEMENTS_PER_NODE - 1){
            Node x = allocateNode();
            T.root = x;
            x.leaf = false;
            x.numOfKeys = 0;
            x.children[0] = r.address;
            writeRootAddress();
            diskWrite(x);
            splitChild(x, 0);
            insertNonFull(x, key);

        } else
            insertNonFull(r, key);

    }

    private byte [] longToBytes(final long numlong){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(numlong);
        byte [] longBytes = buffer.array();
        return longBytes;
    }

    private byte [] intToBytes(final int numint){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(numint);
        byte [] intBytes = buffer.array();
        return intBytes;
    }

    private byte [] booleanToBytes(final boolean bool){
        byte [] bools = new byte[1];
        if (bool == true)
            bools[0] = 0;
        else
            bools[0] = 1;
        return bools;
    }

    private byte [] nodeToBytes(final Node x){
        byte[] childrenBytes = longArrayToBytes(x.children);
        byte[] keyBytes = longArrayToBytes(x.keys);
        byte[] leafByte = booleanToBytes(x.leaf);
        byte[] numOfKeyBytes = intToBytes(x.numOfKeys);
        byte[] addressBytes = longToBytes(x.address);

        byte[] nodeBytes = new byte[childrenBytes.length + keyBytes.length + leafByte.length + numOfKeyBytes.length + addressBytes.length];
        System.arraycopy(childrenBytes, 0, nodeBytes, 0, childrenBytes.length);
        System.arraycopy(keyBytes, 0, nodeBytes, childrenBytes.length, keyBytes.length);
        System.arraycopy(leafByte, 0, nodeBytes, childrenBytes.length + keyBytes.length, leafByte.length);
        System.arraycopy(numOfKeyBytes, 0, nodeBytes, childrenBytes.length + keyBytes.length
                + leafByte.length, numOfKeyBytes.length);
        System.arraycopy(addressBytes, 0, nodeBytes, childrenBytes.length + keyBytes.length +
                leafByte.length + numOfKeyBytes.length, addressBytes.length);

        return nodeBytes;

    }

    private byte [] longArrayToBytes(final long[] longarray){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * longarray.length);
        for (int i = 0; i < longarray.length; i++) {
            buffer.putLong(longarray[i]);
        }
        byte [] longarr = buffer.array();
        return longarr;

    }

    private int bytesToInt(final byte [] intBytes){
        ByteBuffer buffer = ByteBuffer.wrap(intBytes);
        int res = buffer.getInt();
        return res;
    }

    private long bytesToLong(final byte [] longBytes){
        ByteBuffer buffer = ByteBuffer.wrap(longBytes);
        long res = buffer.getLong();
        return res;
    }

    private long [] bytesToLongArray(final byte [] longarrBytes, final int numLongsInArray){
        ByteBuffer buffer = ByteBuffer.wrap(longarrBytes);
        long [] res = new long [numLongsInArray];
        for (int i = 0; i < numLongsInArray; i++) {
            res[i] = buffer.getLong(Long.BYTES * i);
        }

        return res;
    }

    private boolean byteToBool(final byte [] booleanByte){
        if(booleanByte[0] == 1)
            return false;
        else
            return true;
    }

    private Node bytesToNode(final byte [] nodeBytes){

        int pointer = 0;
        byte[] childrenBytes = Arrays.copyOfRange(nodeBytes,pointer,pointer + Long.BYTES * NUM_CHILDREN);
        pointer = Long.BYTES * NUM_CHILDREN;
        byte[] keyBytes = Arrays.copyOfRange(nodeBytes, pointer, pointer + Long.BYTES * NUM_KEYS);
        pointer += Long.BYTES * NUM_KEYS;
        byte[] booleanByte = Arrays.copyOfRange(nodeBytes, pointer, pointer + 1);
        pointer += 1;
        byte[] intBytes = Arrays.copyOfRange(nodeBytes, pointer, pointer + Integer.BYTES);
        pointer += Integer.BYTES;
        byte[] addressBytes = Arrays.copyOfRange(nodeBytes, pointer, pointer + Long.BYTES);
        long[] children = bytesToLongArray(childrenBytes, NUM_CHILDREN);
        long[] keys = bytesToLongArray(keyBytes, NUM_KEYS);
        boolean leaf = byteToBool(booleanByte);
        int numOfKeys = bytesToInt(intBytes);
        long address = bytesToLong(addressBytes);

        Node x = new Node(keys, numOfKeys, children, leaf, address);

        return x;
    }

    public boolean search(final Node r, final String key) throws IOException{
        int i = 0;
        Node temp;
        while (i < r.numOfKeys && Long.parseLong(key) > r.keys[i]){
            i++;
        }
        if(i < r.numOfKeys && Long.parseLong(key) == r.keys[i]){
            return true;
        } else if (r.leaf)
            return false;
        else {
            temp = diskRead(r.children[i]);
            return search(temp, key);
        }

    }



    private void writeRootAddress() throws IOException{
        RandomAccessFile file;
        if(treeNum == 0)
            file = new RandomAccessFile("BTree.raf", "rw");
        else
            file = new RandomAccessFile("BTree2.raf", "rw");
        file.seek(0);

        byte [] bytes = longToBytes(root.address);

        file.write(bytes);
        file.close();

    }

    private long getRootAddress() throws IOException{
        RandomAccessFile file;
        if(treeNum == 0)
            file = new RandomAccessFile("BTree.raf", "rw");
        else
            file = new RandomAccessFile("BTree2.raf", "rw");

        file.seek(0);
        byte [] bytes = new byte[Long.BYTES];

        file.readFully(bytes);

        long address = bytesToLong(bytes);

        return address;
    }

}
