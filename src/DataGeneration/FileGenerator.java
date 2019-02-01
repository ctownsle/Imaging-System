package DataGeneration;

import java.io.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Random;

public class FileGenerator {


    private static final int lengthOfCode = 6;

    private long rangeBegin = Timestamp.valueOf("2015-02-08 00:00:00").getTime();
    private long rangeEnd = Timestamp.valueOf("2018-02-08 00:00:00").getTime();
    private long diff = rangeEnd - rangeBegin + 1;
    Timestamp rand;
    private String patiendId;
    private String fileExtension;
    private Random r;
    private File f;

    public FileGenerator() throws IOException{
        f = new File("dcmfiles.txt");
        r = new Random();
            if (!f.exists())
                writeDataToFile();
    }

    private String generateId(){
        int idDigit;
        String id = "";

        for (int i = 0; i <= lengthOfCode; i++) {
            idDigit = r.nextInt(8) + 1;

            id = id.concat(String.format("%d", idDigit));
        }

        return id;
    }

    private String parseDateTime(){
        String dateTimeString = rand.toString();
        dateTimeString = dateTimeString.split("\\.")[0];
        dateTimeString = dateTimeString.replace("-", "");
        dateTimeString = dateTimeString.replace(" ", "");
        dateTimeString = dateTimeString.replace(":", "");
        dateTimeString = dateTimeString.replace(dateTimeString.subSequence(0, 2), "");

        while (dateTimeString.length() != 12){
            dateTimeString = dateTimeString.concat(String.format("%d", r.nextInt(4) + 1));
        }

        return dateTimeString; // yymmddhhmmss
    }




    private void writeDataToFile() throws FileNotFoundException{
        PrintWriter printWriter = new PrintWriter("dcmfiles.txt");
        String key = "";
        int rn = 0;
        for (int i = 0; i < r.nextInt(100) + 50; i++) {
            patiendId = generateId();
            for (int j = 0; j < r.nextInt(40) + 1; j++) {
                fileExtension = generateId();
                rand = new Timestamp(rangeBegin + (long)(Math.random() * diff));

                if(rn % 2 == 0 || rn == 0){
                    key = patiendId + parseDateTime() + ".";
                }
                printWriter.write(key + fileExtension);
                printWriter.write("\n");
                rn = r.nextInt();
            }
        }
        printWriter.close();
    }

}
