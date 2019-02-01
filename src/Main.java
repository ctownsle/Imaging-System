import Data.MyTable;
import DataGeneration.DataParser;
import DataGeneration.FileGenerator;
import Tree.BTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Main {

    // We assume the data is checked before saving, so there is no immediate need for delete

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        // Create two trees
        // One for main directory containing patient IDs
        // Second tree for subdirectories containing datetimes where patients received scans
        BTree tree, tree2;
        FileGenerator gen;
        ArrayList<String> ids = new ArrayList<>();
        gen = new FileGenerator();

        DataParser dp = new DataParser();
        MyTable table = dp.getTable();
        MyTable table2 = dp.getLookup();
        ArrayList<String> keys = new ArrayList<>();
        keys = table.getKeys(keys);
        ids = table2.getKeys(ids);
        File file = new File("BTree.raf");
        File file2 = new File("BTree2.raf");
        if(!file.exists()) {
            tree = new BTree(0);
            for (String id: ids) {
                tree.insert(tree, id);
            }
        } else
            tree = new BTree(0);
        Scanner kb = new Scanner(System.in);

        if(!file2.exists()){
            tree2 = new BTree(1);
            for(String key: keys){
                tree2.insert(tree2, key);
            }
        } else
            tree2 = new BTree(1);

        while (true) {

            System.out.println("Scan for or search by patient's ID, or q to exit:");
            System.out.println();
            String input = kb.nextLine();

            switch (input){
                case "scan":
                    for(String id: ids){
                        System.out.println(id);
                    }
                    break;
                case "q":
                    return;
                case "search":
                        System.out.println("Enter a patient ID: ");
                        // gets all keys that match prefix
                        System.out.println();
                        String input2 = kb.nextLine();
                        if (input2.length() == 7) {
                            boolean t = tree.search(tree.root, input2);
                            if(t == false){
                                System.out.println("Patient does not exist");
                                break;
                            } else {
                                HashSet<String> vals = table2.get(input2);
                                for (String val:vals) {
                                    System.out.println(val);
                                }
                                System.out.println("Enter date patient received scans: ");
                                System.out.println();
                                String input3 = kb.nextLine();
                                if(input3.length() == 12){
                                    boolean t2 = tree2.search(tree2.root, input2 + input3);
                                    if(t2 == false){
                                        System.out.println("Invalid date");
                                        break;
                                    } else {
                                        HashSet<String> vals2 = table.get(input2 + input3);
                                        for(String val: vals2){
                                            System.out.println(input2 + input3 + "." + val);
                                        }
                                        break;
                                    }
                                } else
                                    System.out.println("Invalid date");
                            }
                        } else if(input2.equals("q")) {
                            return;
                        } else {
                            System.out.println("invalid id");
                            break;
                        }
            }
        }
    }
}
