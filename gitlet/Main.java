package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Xinrui Liu
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <INIT> <ADD> <COMMIT> <REMOVE> <LOG> <GLOBAL-LOG>
     *  <FIND> <STATUS> <BRANCH> <RM-BRANCH> <RESET> <MERGE> */

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        String message;
        String f;
        String commitId;
        String branch;
        switch (firstArg) {
            case "init" -> {
                if (new File(".gitlet").exists()) {
                    System.out.println("A Gitlet version-control system already " +
                            "exists in the current directory.");
                    System.exit(0);
                }
                Repository.initSetup();
            }
            case "add" -> {
                gitD();
                f = args[1];
                Repository.addFile(f);
            }
            case "commit" -> {
                gitD();
                message = args[1];
                if (args[1] == null || args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.createCommit(message);
            }
            case "rm" -> {
                gitD();
                f = args[1];
                Repository.remove(f);
            }
            case "log" -> {
                gitD();
                Repository.printLog();
            }
            case "global-log" -> {
                gitD();
                Repository.globalLog();
            }
            case "find" -> {
                gitD();
                message = args[1];
                Repository.find(message);
            }
            case "branch" -> {
                gitD();
                branch = args[1];
                Repository.createBranch(branch);
            }
            case "checkout" -> {
                gitD();
                if (args[1].equals("--")) {
                    f = args[2];
                    Repository.checkoutFile(f);
                } else if (args.length > 2 && args[2].equals("--")) {
                    commitId = args[1].substring(0, 6);
                    f = args[3];
                    Repository.checkoutCommit(commitId, f);
                } else if (args.length == 2) {
                    branch = args[1];
                    Repository.checkoutBranch(branch);
                } else {
                    System.out.println("Incorrect operands.");
                }
            }
            case "status" -> {
                gitD();
                Repository.status();
            }
            case "rm-branch" -> {
                gitD();
                branch = args[1];
                Repository.rmPreCheck(branch);
            }
            case "reset" -> {
                gitD();
                commitId = args[1].substring(0, 6);
                Repository.reset(commitId);
            }
            case "merge" -> {
                gitD();
                branch = args[1];
                Repository.merge(branch);
            }
            default -> System.out.println("No command with that name exists.");
        }
    }

    public static void gitD() {
         if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
         }
    }
}
