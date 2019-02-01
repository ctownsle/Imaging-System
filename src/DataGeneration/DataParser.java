package DataGeneration;

import Data.MyTable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DataParser {
    // need two lookup tables
    // lookup.ser references the id to dateTime
    // Cache.ser references the whole key to extension
    // Parses text file and generates tables
    private File f;
    private File f2, f3;
    private String[] keyandextension;
    private MyTable table, table2;
    public DataParser() throws IOException, ClassNotFoundException{
        f = new File("dcmfiles.txt");
        f2 = new File("Cache.ser");
        f3 = new File("Lookup.ser");
        if(!f2.exists() && !f3.exists()) {
            table = new MyTable();
            table2 = new MyTable();
            try (Stream<String> lines = Files.lines(Paths.get(f.getPath()))) {
                lines.forEachOrdered(line -> {
                    keyandextension = line.split("\\.");
                    table.put(keyandextension[0], keyandextension[1]);
                    String id = keyandextension[0].substring(0, 7);
                    String dateTime = keyandextension[0].substring(7);
                    table2.put(id, dateTime);
                });
                writeToDisk(0, table);
                writeToDisk(1, table2);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            table = readFromDisk(0);
            table2 = readFromDisk(1);
        }

    }


    private void writeToDisk(final int i, final MyTable table) throws IOException{
        FileOutputStream fos;
        if(i == 0)
            fos = new FileOutputStream("Cache.ser");
        else
            fos = new FileOutputStream("Lookup.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(table);
        fos.close();
    }

    private MyTable readFromDisk(final int i) throws IOException, ClassNotFoundException{
        MyTable t;
        FileInputStream fis;
        if(i == 0)
            fis = new FileInputStream("Cache.ser");
        else
            fis = new FileInputStream("Lookup.ser");
        ObjectInputStream ois =  new ObjectInputStream(fis);
        t = (MyTable) ois.readObject();
        ois.close();

        return t;
    }

    public MyTable getTable() {
        return table;
    }

    public MyTable getLookup() {
        return table2;
    }
}
