package origin.script;

import javax.script.CompiledScript;

import origin.util.atomic.AtomicNumber;

/**
 * コンパイル要素.
 */
public class CompileElement {
    private CompiledScript compile;
    private String fileName;
    private long fileTime;
    private CompileManager manager = null;
    private final AtomicNumber updateTime = new AtomicNumber(-1);

    public CompileElement() {

    }

    public CompileElement(CompiledScript c, CompileManager m, String f, long t) {
        create(c, m, f, t);
    }

    public void create(CompiledScript c, CompileManager m, String f, long t) {
        compile = c;
        manager = m;
        fileName = f;
        fileTime = t;
    }

    public CompileManager getManager() {
        return manager;
    }

    public CompiledScript getCompile() {
        return compile;
    }

    public void setCompile(CompiledScript compile) {
        this.compile = compile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileTime() {
        return fileTime;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    public void update() {
        updateTime.set(System.currentTimeMillis());
    }

    public long updateTime() {
        return updateTime.get();
    }
}
