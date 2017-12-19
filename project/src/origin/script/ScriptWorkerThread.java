package origin.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import origin.db.CreateBaseDao;
import origin.db.DbCreateBaseDao;
import origin.db.core.DbFactory;
import origin.net.PermissionAccessHeader;
import origin.net.http.HttpAnalysis;
import origin.net.http.HttpRequest;
import origin.net.http.HttpStatus;
import origin.pref.Def;
import origin.util.ByteArrayIO;
import origin.util.Utils;
import origin.util.atomic.Wait;

/**
 * スクリプト実行用ワーカースレッド.
 */
public class ScriptWorkerThread extends Thread {
    private static final Log LOG = LogFactory.getLog(ScriptWorkerThread.class);
    private static final int TIMEOUT = 1000;
    private static final String JS_PLUS = ".js";
    private static final String FILTER_NAME = "@filter.js";

    private static final CreateBaseDao CREATE_BASE_DAO = new DbCreateBaseDao();

    private int no;
    private Queue<HttpElement> queue = null;
    private Wait wait = null;
    private DbFactory dbFactory;
    private CompileManager compileManager;
    private Map<String,Object> shareMemory = null;

    private volatile boolean stopFlag = true;
    private volatile boolean endThreadFlag = false;

    public ScriptWorkerThread(DbFactory f, CompileManager c, Map<String,Object> memory, int n) {
        dbFactory = f;
        compileManager = c;
        no = n;
        shareMemory = memory;
        queue = new ConcurrentLinkedQueue<HttpElement>();
        wait = new Wait();
    }

    public void register(HttpElement em) throws IOException {
        em.setWorkerNo(no);
        queue.offer(em);
        wait.signal();
    }

    public void startThread() {
        stopFlag = false;
        setDaemon(true);
        start();
    }

    public void stopThread() {
        stopFlag = true;
    }

    public boolean isStopThread() {
        return stopFlag;
    }

    public boolean isEndThread() {
        return endThreadFlag;
    }

    public void run() {
        LOG.info("*** start origin workerThread(" + no + ").");

        ThreadDeath td = execute();

        LOG.info("*** stop origin workerThread(" + no + ").");
        endThreadFlag = true;
        if (td != null) {
            throw td;
        }
    }

    private final ThreadDeath execute() {
        HttpElement em = null;
        ThreadDeath ret = null;
        boolean endFlag = false;
        while (!endFlag && !stopFlag) {
            try {
                while (!endFlag && !stopFlag) {
                    if ((em = queue.poll()) == null) {
                        wait.await(TIMEOUT);
                        continue;
                    }
                    if (executionRequest(em)) {
                        executeScript(dbFactory, em, compileManager, shareMemory);
                    }
                    em = null;
                }
            } catch (Throwable to) {
                if (em != null) {
                    em.clear();
                }
                LOG.debug("error", to);
                if (to instanceof InterruptedException) {
                    endFlag = true;
                } else if (to instanceof ThreadDeath) {
                    endFlag = true;
                    ret = (ThreadDeath) to;
                }
            }
        }
        return ret;
    }

    /** Request処理. **/
    private static final boolean executionRequest(HttpElement em)
            throws IOException {

        // 既に受信処理が終わっている場合.
        if (em.isEndReceive()) {
            return true;
        }

        // 受信バッファに今回分の情報をセット.
        ByteArrayIO buffer = em.getBuffer();

        // Httpリクエストを取得.
        HttpRequest request = em.getRequest();
        if (request == null) {

            // HTTPリクエストが存在しない場合は、新規作成.
            int endPoint = HttpAnalysis.endPoint(buffer);
            if (endPoint == -1) {

                // 受信途中の場合.
                return false;
            }
            request = HttpAnalysis.getRequest(buffer, endPoint);
            em.setRequest(request);
        }

        String method = request.getMethod();

        // OPTIONの場合は、Optionヘッダを返却.
        if ("OPTIONS".equals(method)) {

            // Optionsレスポンス.
            sendOptions(em);
            return false;
        }
        // POSTの場合は、ContentLength分の情報を取得.
        else if ("POST".equals(method)) {

            // ContentLengthを取得.
            int contentLength = request.getContentLength();
            if (contentLength <= -1) {

                // 存在しない場合はコネクション強制クローズ.
                // chunkedの受信は対応しない.
                // 411エラー.
                errorResponse(em, 411);
                return false;
            }

            // 指定サイズを超えるBody長.
            if (contentLength > Def.MAX_CONTENT_LENGTH) {

                // 413エラー.
                errorResponse(em, 413);
                return false;
            }

            // Body情報が受信完了かチェック.
            if (buffer.size() >= contentLength) {
                byte[] body = new byte[contentLength];
                buffer.read(body);
                request.setBody(body);
            } else {

                // PostのBody受信中.
                return false;
            }
        }
        // POST,GET以外の場合は処理しない.
        else if (!"GET".equals(method)) {

            // 405エラー.
            errorResponse(em, 405);
            return false;
        }

        // 受信完了.
        em.setEndReceive(true);
        em.destroyBuffer();
        return true;
    }

    /** Response処理. **/
    private static final void executeScript(DbFactory dbFactory,
            HttpElement em, CompileManager man, Map<String,Object> shareMemory) {

        // 既に送信処理が終わっている場合.
        if (em.isEndSend()) {
            return;
        }
        OriginBindings b = null;
        try {
            HttpRequest req = em.getRequest();
            em.setRequest(null);
            
            // アクセス対象のパスを取得.
            String path = Def.SCRPIT_DIR + getPath(req.getUrl());
            
            // 実行ファイルのパスが存在しない場合.
            if (!Utils.isFile(path + JS_PLUS)) {
                
                // 存在しない場合.
                errorResponse(em, 404);
                return;
            }
            
            // パスを、[スクリプトファイル]と[パス]に分解.
            String scriptFile = null;
            int p = path.lastIndexOf("/");
            if(p == -1) {
                scriptFile = path;
                path = "";
            } else {
                scriptFile = path.substring(p+1);
                path = path.substring(0,p+1);
            }
            
            // スクリプト名の頭に@マークがある
            // ものは、処理できない.
            if(scriptFile.startsWith("@")) {
                
                // 存在しない場合.
                errorResponse(em, 404);
                return;
            }
            
            scriptFile += JS_PLUS;
            String method = req.getMethod();
            Object params = null;
            if ("GET".equals(method)) {
                params = getParams(req.getUrl());
            } else if ("POST".equals(method)) {
                params = postParams(req);
            }
            boolean gzip = isGzip(req);
            
            b = new OriginBindings(dbFactory, CREATE_BASE_DAO);
            HttpResponse res = new HttpResponse();
            b.put(Def.SCRIPT_PARAMS, params);
            b.put(Def.SCRIPT_REQUEST, req);
            b.put(Def.SCRIPT_RESPONSE, res);
            b.put(Def.SHARE_MEMORY, shareMemory);
            String ret = "";
            try {
                
                // フィルタ処理が存在する場合は、最初にフィルタを実行.
                if (Utils.isFile(path + FILTER_NAME)) {
                    CompileElement ce = man.get(path + FILTER_NAME);
                    ce.update();
                    Object filterResult = ExecuteScript.execute(ce, b);
                    
                    // フィルタ実行結果、OKの場合.
                    if("true".equals(""+filterResult)) {
                        
                        // スクリプトの実行.
                        ce = man.get(path + scriptFile);
                        ce.update();
                        ret = (String) ExecuteScript.execute(ce, b);
                        
                    // フィルター内でエラーメッセージを送付していない場合.
                    } else if(!em.isEndSend()) {
                        
                        if(!"false".equals(""+filterResult)) {
                            errorResponse(em, 500, ""+filterResult);
                        } else {
                            errorResponse(em, 500, "処理に失敗しました");
                        }
                        return;
                    }
                } else {
                    
                    // スクリプトの実行.
                    CompileElement ce = man.get(path + scriptFile);
                    ce.update();
                    ret = (String) ExecuteScript.execute(ce, b);
                }
            } catch (HttpRedirectException redirect) {
                redirectResponse(em, redirect);
                return;
            } catch (HttpException httpException) {
                errorResponse(em, httpException.getStatus(),
                        httpException.getMessage());
                return;
            }
            if (ret == null) {
                ret = "";
                gzip = false;
            }
            sendResponse(gzip, em, res.getStatus(), res, ret);
        } catch (Exception e) {
            LOG.info("error", e);
            try {
                errorResponse(em, 500, e.getMessage());
            } catch (Exception ee) {
            }
        } finally {
            if (b != null) {
                b.close();
            }
        }
    }
    
    /** 要求パス取得. **/
    private static final String getPath(String url) {
        int p = url.indexOf("?");
        if (p != -1) {
            url = url.substring(0, p);
        }
        if (url.endsWith("/")) {
            url += "index";
        }
        return url;
    }

    /** GETパラメータを取得. **/
    private static final Object getParams(String url) throws IOException {
        int p = url.indexOf("?");
        if (p != -1) {
            return HttpAnalysis.paramsAnalysis(url, p + 1);
        }
        return new HashMap<String, String>();
    }

    /** POSTパラメータを取得. **/
    private static final Object postParams(HttpRequest req) throws IOException {
        String v = new String(req.getBody(), "UTF8");
        req.setBody(null);

        // Body内容がJSON形式の場合.
        String contentType = req.getHeader("Content-Type");
        if (contentType.indexOf("application/json") == 0) {
            return Json.decode(v);
        } else if ("application/x-www-form-urlencoded".equals(contentType)) {
            return HttpAnalysis.paramsAnalysis(v, 0);
        } else {
            return HttpAnalysis.paramsAnalysis(v, 0);
        }
    }

    /** GZIP返却許可チェック. **/
    private static final boolean isGzip(HttpRequest req) throws IOException {
        String n = req.getHeader("Accept-Encoding");
        if (n == null || n.indexOf("gzip") == -1) {
            return false;
        }
        return true;
    }

    /** Options送信. **/
    private static final void sendOptions(HttpElement em) throws IOException {
        em.setRequest(null);
        em.destroyBuffer();
        em.setEndReceive(true);
        em.setEndSend(true);
        em.getSendData().set(OPSIONS_RESPONSE);
    }

    /** レスポンス送信. **/
    private static final void sendResponse(boolean gzip, HttpElement em,
            int status, HttpResponse header, String body) throws IOException {
        em.setRequest(null);
        em.destroyBuffer();
        em.setEndReceive(true);
        em.setEndSend(true);
        if (gzip && body.length() > Def.NOT_GZIP_BODY_LENGTH) {
            header.setHeader("Content-Encoding", "gzip");
            em.getSendData()
                    .set(stateResponse(status, header, pressGzip(body)));
        } else {
            em.getSendData().set(stateResponse(status, header, body));
        }
    }

    /** リダイレクト送信. **/
    private static final void redirectResponse(HttpElement em,
            HttpRedirectException redirect) throws IOException {
        em.setRequest(null);
        em.destroyBuffer();
        em.setEndReceive(true);
        em.setEndSend(true);
        HttpResponse res = new HttpResponse();
        res.setHeader("Location", redirect.getUrl());
        em.getSendData().set(stateResponse(redirect.getStatus(), res, ""));
    }

    /** GZIP圧縮. **/
    private static final byte[] pressGzip(String body) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        GZIPOutputStream go = new GZIPOutputStream(bo);
        go.write(body.getBytes("UTF8"));
        go.flush();
        go.finish();
        go.close();
        return bo.toByteArray();
    }

    /** エラーレスポンスを送信. **/
    private static final void errorResponse(HttpElement em, int status)
            throws IOException {
        errorResponse(em, status, null);
    }

    /** エラーレスポンスを送信. **/
    private static final void errorResponse(HttpElement em, int status,
            String message) throws IOException {
        StringBuilder buf = new StringBuilder(
                "{\"result\": false, \"status\": ").append(status);
        if (message == null) {
            message = HttpStatus.getMessage(status);
        }
        // コーテーション系の情報は大文字に置き換える.
        message = Utils.changeString(message,"\"","”");
        message = Utils.changeString(message,"\'","’");
        
        // カッコ系の情報も大文字に置き換える.
        message = Utils.changeString(message,"[","［");
        message = Utils.changeString(message,"]","］");
        message = Utils.changeString(message,"{","｛");
        message = Utils.changeString(message,"}","｝");
        
        String res = buf.append(", \"message\": \"").append(message)
                .append("\"").append("}").toString();
        buf = null;
        
        HttpResponse header = new HttpResponse();
        header.setHeader("Content-Type", "application/json; charset=UTF-8");

        // 処理結果を返却.
        em.setRequest(null);
        em.destroyBuffer();
        em.setEndReceive(true);
        em.setEndSend(true);
        em.getSendData().set(stateResponse(status, header, res));
    }

    /** ステータス指定Response返却用バイナリの生成. **/
    private static final byte[] stateResponse(int state, HttpResponse header,
            String b) throws IOException {
        return stateResponse(state, header, b.getBytes("UTF8"));
    }

    /** ステータス指定Response返却用バイナリの生成. **/
    private static final byte[] stateResponse(int state, HttpResponse header,
            byte[] b) throws IOException {
        byte[] stateBinary = new StringBuilder(String.valueOf(state))
                .append(" ").append(HttpStatus.getMessage(state)).toString()
                .getBytes("UTF8");

        byte[] foot = (new StringBuilder(String.valueOf(b.length))
                .append("\r\n").append(HttpResponse.headers(header))
                .append("\r\n").toString()).getBytes("UTF8");
        int all = STATE_RESPONSE_1.length + stateBinary.length
                + STATE_RESPONSE_2.length + foot.length + b.length;
        byte[] ret = new byte[all];

        int pos = 0;
        System.arraycopy(STATE_RESPONSE_1, 0, ret, pos, STATE_RESPONSE_1.length);
        pos += STATE_RESPONSE_1.length;
        System.arraycopy(stateBinary, 0, ret, pos, stateBinary.length);
        pos += stateBinary.length;
        System.arraycopy(STATE_RESPONSE_2, 0, ret, pos, STATE_RESPONSE_2.length);
        pos += STATE_RESPONSE_2.length;
        System.arraycopy(foot, 0, ret, pos, foot.length);
        pos += foot.length;
        System.arraycopy(b, 0, ret, pos, b.length);
        
        return ret;
    }

    /** Optionsレスポンス. **/
    private static final byte[] OPSIONS_RESPONSE;

    /** ステータス指定レスポンス. **/
    private static final byte[] STATE_RESPONSE_1;
    private static final byte[] STATE_RESPONSE_2;

    static {
        String headers = PermissionAccessHeader.get();
        if(headers.length() > 0) {
            headers += ",";
        }
        byte[] b;
        byte[] s1;
        byte[] s2;
        try {
            b = ("HTTP/1.1 200 OK\r\n" + "Allow: GET, POST, HEAD, OPTIONS\r\n"
                    + "Cache-Control: no-cache\r\n"
                    + "X-Accel-Buffering: no\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "Access-Control-Allow-Headers: content-type, "+headers+" *\r\n"
                    + "Access-Control-Allow-Methods: GET, POST, HEAD, OPTIONS\r\n"
                    + "Server: "
                    + Def.SERVER_NAME + "\r\n" + "Connection: close\r\n" + "Content-Length: 0\r\n\r\n")
                    .getBytes("UTF8");

            s1 = ("HTTP/1.1 ").getBytes("UTF8");
            s2 = ("\r\n" + "Cache-Control: no-cache\r\n"
                    + "X-Accel-Buffering: no\r\n"
                    + "Access-Control-Allow-Origin: *\r\n"
                    + "Access-Control-Allow-Headers: content-type, "+headers+" *\r\n"
                    + "Access-Control-Allow-Methods: GET, POST, HEAD, OPTIONS\r\n"
                    + "Server: "
                    + Def.SERVER_NAME + "\r\n" + "Connection: close\r\n"
                    + "Content-Length: ")
                    .getBytes("UTF8");

        } catch (Exception e) {
            b = null;
            s1 = null;
            s2 = null;
        }
        OPSIONS_RESPONSE = b;
        STATE_RESPONSE_1 = s1;
        STATE_RESPONSE_2 = s2;
    }
}