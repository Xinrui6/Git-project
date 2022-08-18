package gitlet;

import java.util.*;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Xinrui
 */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private String message;
    /**Date*/
    /** timestamp of this Commit. */
    private String timestamp;
    /** blobs of this Commit. */
    protected TreeMap<String, Blob> blobs;
    /** first parent commit */
    private Commit parent1;
    /** second parent commit */
    private Commit parent2;
    /** SHA1*/
    private String SHA;
    /** Commit id*/
    private String commitId;


    public Commit(String m, Date t, Commit parent1, Commit parent2) {
        this.message = m;
        timestamp = String.format(Locale.US, "%ta %tb %te %tH:%tM:%tS %tY %tz",
                t, t, t, t, t, t, t, t);
        this.parent1 = parent1;
        if (parent2 != null) {
            this.parent2 = parent2;
        }
        blobs = new TreeMap<>();
        SHA = Utils.sha1(Utils.serialize(this));
        commitId = SHA.substring(0, 6);
        Utils.writeObject(Repository.head, commitId);
    }

    public String getSHA() {
        return SHA;
    }

    public String getCommitId() {
        return commitId;
    }

    public String findBSha(String filename) {
        if (blobs.get(filename) == null) {
            return null;
        }
        Blob target = blobs.get(filename);
        return target.getSha1();
    }

    public Commit getParent1() {
        return this.parent1;
    }

    public String getMessage() {
        return message;
    }

    /** helper for log displaying*/
    public void logHelper() {
        System.out.println("===");
        System.out.println("commit " + SHA);
        if (this.parent2 != null) {
            System.out.println("Merge: " + this.parent1.SHA.substring(0, 7)
                    + " " + this.parent2.SHA.substring(0, 7));
        }
        System.out.println("Date: " + timestamp);
        System.out.println(this.message);
        System.out.println();
    }

    /** put all blobs from commit's parent */
    public void putParentB() {
        blobs.putAll(parent1.blobs);
    }

    public boolean hasTwoParents() {
        return parent2 != null;
    }

}
