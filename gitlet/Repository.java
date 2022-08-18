package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import static gitlet.Utils.*;

/**  @author Xinrui Liu */
public class Repository implements Serializable {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** the file for all commits */
    private static final File ALL = join(GITLET_DIR, "allCommits");
    /** the pointer of Commit */
    protected static File head = Utils.join(GITLET_DIR, "head");
    /** The staging area for add command. */
    private static final File STAGING = join(GITLET_DIR, "staging_Area");
    /** The add area for adding files. */
    private static final File TO_ADD = join(STAGING, "add");
    /** The remove for removable files. */
    private static final File TO_REMOVE = join(STAGING, "toRemove");
    /** The TreeMap of addition of staging area. */
    private static TreeMap<String, Blob> addition = new TreeMap<>();
    /** The removal part of staging area. */
    private static TreeMap<String, Blob> removal = new TreeMap<>();
    /** treemap of all commits */
    private static TreeMap<String, Commit> allCommits = new TreeMap<>();

    /** Required filesystem operations to allow for persistence.
     * - Gitlet: storing all data
     *     - STAGING: staging files for commits.
     *         - TO_ADD: hashmap for additional files.
     *         - TO_REMOVE: hashmap for removal files.
     *     - BRANCHES: storing all about branches.
     *         - branch: the file for staging branches tree
     *         - currB: the active branch head's commit id.
     *     - ALL: storing all Commits.
     *     - head: storing the head of all commits.*/
    public static void setupPersistence() throws IOException {
        GITLET_DIR.mkdir();
        STAGING.mkdir();
        TO_ADD.createNewFile();
        TO_REMOVE.createNewFile();
        Branches.BRANCHES.mkdir();
        Branches.currB.createNewFile();
        head.createNewFile();
        ALL.createNewFile();
    }

    /** Creates and persistently saves an initial commit
     * two non-command arguments of args (message, timestamp).
     */
    public static void initSetup() throws IOException {
        Repository.setupPersistence();
        Commit initC = new Commit("initial commit", new Date(0), null, null);
        allCommits.put(initC.getCommitId(), initC);
        Branches.saveBranch("master", initC.getCommitId());
        writeObject(Branches.currB, "master");
        savingStaging();
        writeObject(ALL, allCommits);
    }

    /** the helper to get current branch's head. */
    private static Commit getBHead() {
        allCommits = readObject(ALL, TreeMap.class);
        return allCommits.get(Branches.getCurrBHead());
    }

    /** read TO_ADD, TO_REMOVE, ALL into TreeMap */
    private static void readStaging() {
        allCommits = readObject(ALL, TreeMap.class);
        addition = readObject(TO_ADD, TreeMap.class);
        removal = readObject(TO_REMOVE, TreeMap.class);
    }

    /** store addition, removal and allCommits into file */
    private static void savingStaging() {
        writeObject(ALL, allCommits);
        writeObject(TO_ADD, addition);
        writeObject(TO_REMOVE, removal);
    }
    /** Adds a copy of the file as it currently exists to the staging area.
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents. */
    public static void addFile(String f) {
        File found = new File(f);
        if (!found.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        readStaging();
        Commit branchHead = getBHead();
        if (removal.containsKey(f)) {
            removal.remove(f);
            Utils.writeObject(TO_REMOVE, removal);
            return;
        }
        createBlob(branchHead, f, found);
        savingStaging();
    }

    /** Make new blob with given filename */
    private static void createBlob(Commit branchHead, String name, File file) {
        Blob newBlob = new Blob(file, name);
        if (branchHead.blobs.containsKey(name)) {
            if (!branchHead.findBSha(name).equals(newBlob.getSha1())) {
                addition.put(name, newBlob);
            }
        } else {
            addition.put(name, newBlob);
        }
    }

    /** create a new commit with new timestamp
     * add blobs in addition into the new commit
     * add the commit to the master branch
     * clean the Add & removal */
    public static void createCommit(String m) throws IOException {
        readStaging();
        if (addition.size() == 0 & removal.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit c = new Commit(m, new Date(), getBHead(), null);
        commitHelper(c);
    }
    /** put all parent's blobs into new commit
     * put blobs in addition into new commit
     * put new commit into commit Treemap
     * clean addition and removal part of staging */
    private static void commitHelper(Commit c) throws IOException {
        c.putParentB();
        c.blobs.putAll(addition);
        if (removal.size() != 0) {
            for (String f: removal.keySet()) {
                c.blobs.remove(f);
            }
        }
        allCommits.put(c.getCommitId(), c);
        Branches.add(c);
        addition.clear();
        removal.clear();
        savingStaging();
    }

    /** Creates a new branch with the given name.
     *  points it at the current head commit.
     * */
    public static void createBranch(String name) {
        if (Branches.contains(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Branches.saveBranch(name, readObject(head, String.class));
    }

    public static void remove(String f) {
        readStaging();
        Commit h = getBHead();
        if (addition.size() == 0 && !h.blobs.containsKey(f)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        addition.remove(f);
        if (h.blobs.containsKey(f)) {
            removal.put(f, h.blobs.get(f));
            if (plainFilenamesIn(CWD).contains(f)) {
                restrictedDelete(f);
            }
        }
        savingStaging();
    }

    /** Display information of commit from head to initial commit.
     * - The commit's Sha1
     * - Date
     * - message */
    public static void printLog() {
        allCommits = readObject(ALL, TreeMap.class);
        Commit h = getBHead();
        while (h != null) {
            h.logHelper();
            h = h.getParent1();
        }
    }

    /** Displays information about all commits ever made.*/
    public static void globalLog() {
        allCommits = Utils.readObject(ALL, TreeMap.class);
        for (String k : allCommits.keySet()) {
            Commit c = allCommits.get(k);
            c.logHelper();
        }
    }

    /** Prints out the ids of all commits that have the given commit message.
     * If no such commit exists, prints the error message */
    public static void find(String m) {
        allCommits = Utils.readObject(ALL, TreeMap.class);
        int n = 0;
        for (String k : allCommits.keySet()) {
            Commit c = allCommits.get(k);
            if (c.getMessage().equals(m)) {
                System.out.println(c.getSHA());
                n++;
            }
        }
        if (n == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** checkoutHelper by filename & checkout by commit and filename.
     * If no commit with the given id exists, print "No commit with that id exists."*/
    private static void checkoutHelper(Commit c, String f) {
        Blob b = c.blobs.get(f);
        if (b == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File targetFile = new File(f);
        Utils.writeContents(targetFile, b.getContent());
        targetFile.renameTo(CWD);
    }

    /** Takes the version of the file as it exists in the head commit and puts it in the CWD,
     * overwriting the version of the file that’s already there if there is one.*/
    public static void checkoutFile(String f) {
        Commit h = getBHead();
        checkoutHelper(h, f);
    }

    /** Takes the version of the file as it exists in the commit with the given id
     *  Puts it in the working directory.
     *  Overwriting the version of the file that’s already there if there is one.
     *  If the file does not exist in the previous commit, abort.
     *  Printing the error message "File does not exist in that commit." */
    public static void checkoutCommit(String commitId, String f) {
        allCommits = readObject(ALL, TreeMap.class);
        Commit c  = allCommits.get(commitId);
        ifCommitExists(c);
        checkoutHelper(c, f);
    }

    /** Takes all files in the commit at the head of the given branch
     *  Puts them in the working directory
     *  Overwriting the versions of the files that are already there if they exist. */
    public static void checkoutBranch(String branch) {
        allCommits = readObject(ALL, TreeMap.class);
        branchPreCheck(branch, "No need to checkout");
        String nextHead = Branches.getBranchHead(branch);
        checkUntracked(nextHead);
        Commit c = allCommits.get(nextHead);
        overwritten(nextHead);
        for (String f : c.blobs.keySet()) {
            checkoutHelper(c, f);
        }
        Branches.changeCurrB(branch);
    }

    private static void branchPreCheck(String branch, String s) {
        if (!Branches.contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String current = Branches.getCurrB();
        if (branch.equals(current)) {
            System.out.println(s + " the current branch.");
            System.exit(0);
        }
    }

    /** Check if a working file is untracked in the current branch
     * and would be overwritten. */
    private static void checkUntracked(String nextHead) {
        Commit h = getBHead();
        Commit c = allCommits.get(nextHead);
        for (String file : plainFilenamesIn(CWD)) {
            if (!h.blobs.containsKey(file) && c.blobs.containsKey(file)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /** check if all files in CWD are the files in commit with the commit ID
     * If not, delete from CWD. */
    private static void overwritten(String commitId) {
        Commit c = allCommits.get(commitId);
        for (File f : CWD.listFiles()) {
            if (!c.blobs.containsKey(f.getName())) {
                restrictedDelete(f);
            }
        }
    }

    /** Displays what branches currently exist, and marks the current branch with a *.
     * Displays what files have been staged for addition or removal.*/
    public static void status() {
        System.out.println("=== Branches ===");
        for (String name : Branches.getAllBranches().keySet()) {
            if (name.equals(Branches.getCurrB())) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        readStaging();
        for (String f : addition.keySet()) {
            System.out.println(f);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String f: removal.keySet()) {
            System.out.println(f);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Check if a branch with the given name does not exist, aborts
     *  Check if user tries to remove the branch you’re currently on, aborts,*/
    public static void rmPreCheck(String b) {
        branchPreCheck(b, "Cannot remove ");
        Branches.removeBranch(b);
    }

    /** Check if the commit exists with given ID */
    private static void ifCommitExists(Commit c) {
        if (c == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    /** Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Moves the current branch’s head to that commit node
     * */
    public static void reset(String iD) {
        allCommits = readObject(ALL, TreeMap.class);
        checkUntracked(iD);
        Commit resetH = allCommits.get(iD);
        ifCommitExists(resetH);
        String current = Branches.getCurrB();
        for (String f : resetH.blobs.keySet()) {
            checkoutHelper(resetH, f);
        }
        addition.clear();
        removal.clear();
        savingStaging();
        Branches.changeHead(current, iD);
    }
    /** A helper to find the split point of current branch head and given branch head */
    private static Commit findSp(Commit c) {
        ArrayList<String> currentH = new ArrayList<>();
        TreeMap<String, Commit> givenH = new TreeMap<>();
        Commit h = getBHead();
        while (h != null) {
            currentH.add(h.getCommitId());
            h = h.getParent1();
        }
        while (c != null) {
            givenH.put(c.getCommitId(), c);
            c = c.getParent1();
        }
        for (String s : currentH) {
            if (givenH.containsKey(s)) {
                return givenH.get(s);
            }
        }
        return null;
    }

    /** Merges files from the given branch into the current branch. */
    public static void merge(String b) throws IOException {
        readStaging();
        checkError(b);
        Commit currentH = getBHead();
        Commit givenH = allCommits.get(Branches.getBranchHead(b));
        checkUntracked(Branches.getBranchHead(b));
        Commit split = findSp(givenH);
        checkOrder(currentH, givenH, split, b);
        if (currentH.hasTwoParents()) {
            currentH = currentH.getParent1();
        }
        mergeHelper(currentH, givenH, split);
        afterMerge(currentH, givenH, b);
    }

    /** check if current branch head or given branch head is the split point of two head.
     *  if is, print error message and quit.*/
    private static void checkOrder(Commit c, Commit g, Commit s, String b) {
        if (c == s) {
            checkoutBranch(b);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else if (g == s) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
    }

    /** Create a new merged commit after merge */
    private static void afterMerge(Commit c, Commit g, String b) throws IOException {
        Commit newC = new Commit("Merged " + b + " into " +
                Branches.getCurrB() + ".", new Date(), c, g);
        commitHelper(newC);
        overwritten(newC.getCommitId());
    }

    /** Check out possible errors conditions during merge */
    private static void checkError(String b) {
        if (b.equals(Branches.getCurrB())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (!Branches.contains(b)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (addition.size() != 0 || removal.size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    /** check every file in current, given and split blobs, with nine conditions.*/
    private static void mergeHelper(Commit c, Commit g, Commit s) {
        for (String name: s.blobs.keySet()) {
            if (c.blobs.containsKey(name) && g.blobs.containsKey(name)) {
                sameVersion(c, g, s, name);
            } else if (c.blobs.containsKey(name) && !g.blobs.containsKey(name)) {
                if (c.findBSha(name).equals(s.findBSha(name))) {
                    c.blobs.remove(name);
                    remove(name);
                } else {
                    conflictedF( c.blobs.get(name), g.blobs.get(name));
                }
            }
        }
        for (String name: g.blobs.keySet()) {
            if (!c.blobs.containsKey(name) && !s.blobs.containsKey(name)) {
                checkoutCommit(g.getCommitId(), name);
                addition.put(name, g.blobs.get(name));
            } else if (g.blobs.containsKey(name) && !s.blobs.containsKey(name)) {
                if (!c.findBSha( name).equals(g.findBSha(name))) {
                    conflictedF(c.blobs.get(name), g.blobs.get(name));
                }
            }
        }
    }

    /** Check if the files in current, given, and split commit are the same version */
    private static void sameVersion(Commit c, Commit g, Commit s, String f) {
        String cBlob = c.findBSha(f);
        String gBlob = g.findBSha(f);
        String sBlob = s.findBSha(f);
        if (!cBlob.equals(gBlob)) {
            if (cBlob.equals(sBlob)) {
                checkoutCommit(g.getCommitId(), f);
                addition.put(f, g.blobs.get(f));
            } else if (gBlob.equals(sBlob)) {
                addition.put(f, c.blobs.get(f));
                checkoutCommit(c.getCommitId(), f);
            } else {
                conflictedF(c.blobs.get(f), g.blobs.get(f));
            }
        }
    }

    /** Write contents into conflicted(current) file
     *  Add the file into addition area
     *  print a merge conflict message */
    private static void conflictedF(Blob c, Blob g) {
        Utils.writeContents(c.getF(), conflictedContent(c, g));
        addition.put(c.getName(), c);
        System.out.println("Encountered a merge conflict.");
    }

    /** Change the content of conflicted file. There are three conditions:
     *      - file of current head not exists
     *      - file of given head not exists
     *      - both of them exist */
    private static String conflictedContent(Blob c, Blob g) {
        String content;
        if (c == null) {
            content = "<<<<<<< HEAD\n=======\n" + g.getContent() + "\n>>>>>>>\n";
        } else if (g == null) {
            content = "<<<<<<< HEAD\n" + c.getContent()
                    + "\n=======\n>>>>>>>\n";
        } else {
            content = "<<<<<<< HEAD\n" + c.getContent()
                    + "\n=======\n" + g.getContent() + ">>>>>>>\n";
        }
        return content;
    }
}

