package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Branches implements Serializable {
    /** the dir for staging all things about branches */
    protected static File BRANCHES = join(GITLET_DIR, "BRANCHES");
    /** the file for staging branches tree */
    protected static File branch = join(BRANCHES, "all branches");
    /** the pointer of active branch */
    protected static File currB = join(BRANCHES, "currB");
    /** the treeMap of the branches */
    protected static TreeMap<String, String> allBranches = new TreeMap<>();

    public Branches() {
    }

    public static TreeMap<String, String> getAllBranches() {
        allBranches = readObject(branch, TreeMap.class);
        return allBranches;
    }

    public static boolean contains(String name) {
        allBranches = readObject(branch, TreeMap.class);
        return allBranches.containsKey(name);
    }

    public static void saveBranch(String name, String head) {
        allBranches.put(name, head);
        writeObject(branch, allBranches);
    }
    /** get the current branch's head's SHA1 */
    public static String getCurrBHead() {
        String current = getCurrB();
        return getBranchHead(current);
    }
    /** get any branch's head with given branch name */
    public static String getBranchHead(String b) {
        allBranches = readObject(branch, TreeMap.class);
        return allBranches.get(b);
    }
    /** get current branch's name */
    public static String getCurrB() {
        return readObject(currB, String.class);
    }
    /** change the current branch by changing the name in the file */
    public static void changeCurrB(String b) {
        readObject(currB, String.class);
        writeObject(currB, b);
    }

    public static void add(Commit c) throws IOException {
        saveBranch(getCurrB(), c.getCommitId());
    }

    /** helper of removing the branch from TreeMap. */
    public static void removeBranch(String b) {
        allBranches = readObject(branch, TreeMap.class);
        allBranches.remove(b);
        writeObject(branch, allBranches);
    }
    /** change the branch's head by replacing SHA1 */
    public static void changeHead(String br, String iD) {
        allBranches = readObject(branch, TreeMap.class);
        saveBranch(br, iD);
    }
}
