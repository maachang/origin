package origin.test;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * テスト結果をレポートとして、ファイル出力する.
 */
public class ReportOutput implements Closeable,AutoCloseable {
    private BufferedWriter writer = null;
    
    public ReportOutput(String name) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name + ".log"),"UTF8"));
    }
    
    protected void finalize() throws Exception {
        close();
    }
    
    public void close() throws IOException {
        if(writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }
    }
    
    public void print(Object value) throws IOException {
        writer.write((value == null) ? "null" : value.toString());
    }
    
    public void println(Object value) throws IOException {
        writer.write((value == null) ? "null" : value.toString());
        writer.write("\n");
    }
}

