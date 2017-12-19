package origin.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;

import origin.db.DbCreateBaseDao;
import origin.db.DbManager;
import origin.net.http.HttpStatus;
import origin.pref.Def;
import origin.script.ExecuteScript;
import origin.script.HttpException;
import origin.script.HttpRedirectException;
import origin.script.HttpResponse;
import origin.script.Json;
import origin.script.OriginBindings;
import origin.script.OriginComponent;
import origin.util.Utils;

/**
 * アプリケーションテストモジュール.
 */
public class TestApplication implements OriginComponent {

    /**
     * このオブジェクトの生成に対して、Bindingsを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にBindingsが必要です.
     */
    @Override
    public boolean useBindings() {
        return false;
    }

    /**
     * このオブジェクトの生成に対して、ScriptContextを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にScriptContextが必要です.
     */
    @Override
    public boolean useScriptContext() {
        return false;
    }

    /**
     * シングルトンオブジェクトの場合は[true]を返却.
     * 
     * @return boolean [true]の場合は、シングルトンオブジェクトです.
     */
    @Override
    public boolean singleton() {
        return true;
    }

    /**
     * サーバモードで起動する場合は[true]を返却.
     * 
     * @return boolean [true]の場合、サーバモードで起動します.
     */
    @Override
    public boolean useServer() {
        return false;
    }

    /**
     * javascript登録オブジェクト名を取得.
     * 
     * @return String オブジェクト名が返却されます.
     */
    @Override
    public String getComponentName() {
        return "Apps";
    }

    public TestApplication() {
    }

    /** エラーレスポンスを送信. **/
    private String errorResponse(int status) throws IOException {
        return errorResponse(status, null);
    }

    /** エラーレスポンスを送信. **/
    private String errorResponse(int status, String message) throws IOException {
        StringBuilder buf = new StringBuilder(
                "{\"result\": false, \"status\": ").append(status);
        if (message == null) {
            message = HttpStatus.getMessage(status);
        }
        String res = buf.append(", \"message\": \"").append(message)
                .append("\"").append("}").toString();
        buf = null;
        return res;
    }

    /** リダイレクト送信. **/
    private static final String redirectResponse(HttpRedirectException redirect)
            throws IOException {
        return "";
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
        return url + ".js";
    }

    /**
     * [GET]jsonアプリケーションアクセス処理.
     * 
     * @param url
     *            接続先URLを設定します.
     * @return Object JSON情報が返却されます.
     * @throws IOException
     */
    public Object get(String url) throws IOException {
        return json("GET", url, null, null);
    }

    /**
     * [GET]jsonアプリケーションアクセス処理.
     * 
     * @param url
     *            接続先URLを設定します.
     * @param params
     *            パラメータを設定します.
     * @return Object JSON情報が返却されます.
     * @throws IOException
     */
    public Object get(String url, Map<String, Object> params)
            throws IOException {
        return json("GET", url, params, null);
    }

    /**
     * [GET]jsonアプリケーションアクセス処理.
     * 
     * @param url
     *            接続先URLを設定します.
     * @param params
     *            パラメータを設定します.
     * @param header
     *            ヘッダ情報を設定します.
     * @return Object JSON情報が返却されます.
     * @throws IOException
     */
    public Object get(String url, Map<String, Object> params,
            Map<String, String> header) throws IOException {
        return json("GET", url, params, header);
    }

    /**
     * [POST]jsonアプリケーションアクセス処理.
     * 
     * @param url
     *            接続先URLを設定します.
     * @param params
     *            パラメータを設定します.
     * @return Object JSON情報が返却されます.
     * @throws IOException
     */
    public Object post(String url, Map<String, Object> params)
            throws IOException {
        return json("POST", url, params, null);
    }

    /**
     * [POST]jsonアプリケーションアクセス処理.
     * 
     * @param url
     *            接続先URLを設定します.
     * @param params
     *            パラメータを設定します.
     * @param header
     *            ヘッダ情報を設定します.
     * @return Object JSON情報が返却されます.
     * @throws IOException
     */
    public Object post(String url, Map<String, Object> params,
            Map<String, String> header) throws IOException {
        return json("POST", url, params, header);
    }

    /**
     * jsonアプリケーションアクセス処理.
     * 
     * @param method
     *            POST or GETを設定します.
     * @param url
     *            接続先URLを設定します.
     * @param params
     *            パラメータを設定します.
     * @param header
     *            ヘッダ情報を設定します.
     * @return Object JSON情報が返却されます.
     * @throws IOException
     */
    public Object json(String method, String url, Map<String, Object> params,
            Map<String, String> header) throws IOException {
        String ret = null;
        try {
            if (!("GET".equals(method) || "POST".equals(method))) {
                return errorResponse(405);
            }
            if (params == null) {
                params = new HashMap<String, Object>();
            }
            if (header == null) {
                header = new HashMap<String, String>();
            }
            TestHttpRequest req = new TestHttpRequest(method, url, header);
            String path = Def.SCRPIT_DIR + getPath(req.getUrl());
            if (!Utils.isFile(path)) {
                return Json.decode(errorResponse(404));
            }
            Bindings b = new OriginBindings(DbManager.getInstance()
                    .getDbFactory(),new DbCreateBaseDao());
            HttpResponse res = new HttpResponse();
            b.put(Def.SCRIPT_PARAMS, params);
            b.put(Def.SCRIPT_REQUEST, req);
            b.put(Def.SCRIPT_RESPONSE, res);
            try {
                ret = (String) ExecuteScript.execute(path, b);
            } catch (HttpRedirectException redirect) {
                ret = redirectResponse(redirect);
            } catch (HttpException httpException) {
                ret = errorResponse(httpException.getStatus(),
                        httpException.getMessage());
            }
        } catch (Exception e) {
            ret = errorResponse(500, e.getMessage());
        }
        return Json.decode(ret);
    }
}
