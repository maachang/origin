package origin.script;

import origin.net.NioElement;
import origin.net.http.HttpRequest;

/**
 * Http要素.
 */
public final class HttpElement extends NioElement {
    protected HttpRequest request = null;
    protected int workerNo = -1;
    protected boolean endReceive = false;
    protected boolean endSend = false;

    public void clear() {
        super.clear();
        request = null;
    }

    /**
     * 受信バッファを破棄.
     */
    public void destroyBuffer() {
        super.buffer = null;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setWorkerNo(int n) {
        workerNo = n;
    }

    public int getWorkerNo() {
        return workerNo;
    }

    public void setEndReceive(boolean f) {
        endReceive = f;
    }

    public boolean isEndReceive() {
        return endReceive;
    }

    public void setEndSend(boolean f) {
        endSend = f;
    }

    public boolean isEndSend() {
        return endSend;
    }
}
