package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String sha1;
    private String content;
    private byte[] bContent;
    private String name;
    private File f;

    public Blob(File f, String filename) {
        content = Utils.readContentsAsString(f);
        bContent = Utils.readContents(f);
        sha1 = Utils.sha1(bContent);
        name = filename;
        this.f = f;
    }

    public String getSha1() {
        return this.sha1;
    }

    public String getContent() {
        return this.content;
    }

    public String getName() {
        return this.name;
    }

    public File getF() {
        return this.f;
    }
}
