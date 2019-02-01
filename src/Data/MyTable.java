package Data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class MyTable implements Serializable {

    /*
     * Custom Hash Table Class
     * Contains Entry Class to handle entries into the table
     * Populates Table using an array of entries
     */

    private int SIZE = 64;
    private Entry [] bucket = new Entry[SIZE];
    private int count = 0;

    class Entry implements Serializable{

        private String key;
        private HashSet<String> values;
        Entry next; // pointer to next thing in linked list

        public Entry(final String key, final HashSet<String> values){
            this.key = key;
            this.values = values;
        }

    }

    public void put(final String key, final String value){

        float maxLoad = 0.75f; // amount of slots in table allowed to be filled before resizing
        int myHash = (int) myHashCode(key);

        Entry currentElement = bucket[myHash];
        count++;
        int newSize = bucket.length * 2;
        int j = 0;
        for(; currentElement != null; currentElement = currentElement.next) {
            if(j == 0) {
                count--;
                j++;
            }
            if(currentElement.key.equals(key)){
                currentElement.values.add(value);
                return;
            }

        }

        if(count >= (bucket.length * maxLoad)){

            Entry [] newTable = new Entry [newSize];
            SIZE = newSize;
            for (int i = 0; i < bucket.length; i++){
                Entry element;

                while ((element = bucket[i]) != null){
                    bucket[i] = element.next;
                    int newBucketNumber = (int) myHashCode(element.key);
                    element.next = newTable[newBucketNumber];
                    newTable[newBucketNumber] = element;
                }
            }

            bucket = newTable;
        }


        HashSet<String> vals = new HashSet<>();
        vals.add(value);
        Entry entry = new Entry(key, vals);
        entry.next = bucket[myHash];
        bucket[myHash] = entry;


    }

    public HashSet<String> get(final String key){
        int index = (int)myHashCode(key);

        Entry element = bucket[index];

        while (element != null){

            if(element.key.equals(key)) {
                return element.values;
            }
            element = element.next;
        }

        return null;
    }

    public long myHashCode(final String key){

        long h = Long.parseLong(key);

        h %= 1007933;

        h = (h>>>5)^h;

        h &= SIZE - 1; // gets index to go into

        return h;
    }

    public ArrayList<String> getKeys(final ArrayList<String> keys){
        for (Entry e: bucket) {
            if(e != null) {
                keys.add(e.key);
                if(e.next != null)
                    getRecursiveKeys(e.next, keys);
            }
        }
        return keys;
    }

    private ArrayList<String> getRecursiveKeys(final Entry e, final ArrayList<String> keys){
        if(e != null){
            keys.add(e.key);
            if(e.next != null)
                getRecursiveKeys(e.next, keys);
        }
        return keys;
    }


}
