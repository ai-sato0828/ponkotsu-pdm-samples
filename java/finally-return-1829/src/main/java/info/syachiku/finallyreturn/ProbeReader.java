package info.syachiku.finallyreturn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

// read/closeの失敗を別々に注入するローカルモデル。
public final class ProbeReader extends BufferedReader {
    private final IOException readFailure;
    private final IOException closeFailure;
    private int closeCalls;

    public ProbeReader(String content, IOException readFailure, IOException closeFailure) {
        super(new StringReader(content));
        this.readFailure = readFailure;
        this.closeFailure = closeFailure;
    }

    @Override
    public String readLine() throws IOException {
        if (readFailure != null) {
            throw readFailure;
        }
        return super.readLine();
    }

    @Override
    public void close() throws IOException {
        closeCalls++;
        super.close();
        if (closeFailure != null) {
            throw closeFailure;
        }
    }

    public int closeCalls() {
        return closeCalls;
    }
}
