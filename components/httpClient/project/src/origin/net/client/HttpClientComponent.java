package origin.net.client;

import java.io.IOException;
import java.util.Map;

import origin.net.client.HttpClient;
import origin.net.client.HttpResult;
import origin.script.OriginComponent;

/**
 * HttpClient.
 */
@SuppressWarnings("rawtypes")
public class HttpClientComponent implements OriginComponent {
    public HttpClientComponent() {
    }

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
        return true;
    }

    /**
     * javascript登録オブジェクト名を取得.
     * 
     * @return String オブジェクト名が返却されます.
     */
    @Override
    public String getComponentName() {
        return "HttpClient";
    }

    @Override
    public String toString() {
        return "[object " + getComponentName() + "]";
    }

    /**
     * [GET]HttpClient接続.
     * 
     * @param url
     *            対象のURLを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult get(String url) throws IOException {
        return HttpClient.get(url);
    }

    /**
     * [GET]HttpClient接続.
     * 
     * @param url
     *            対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult get(String url, Object params) throws IOException {
        return HttpClient.get(url, params);
    }

    /**
     * [GET]HttpClient接続.
     * 
     * @param url
     *            対象のURLを設定します.
     * @param header
     *            対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult get(String url, Map header) throws IOException {
        return HttpClient.get(url, header);
    }

    /**
     * [GET]HttpClient接続.
     * 
     * @param url
     *            対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header
     *            対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult get(String url, Object params, Map header)
            throws IOException {
        return HttpClient.get(url, params, header);
    }

    /**
     * [POST]HttpClient接続.
     * 
     * @param url
     *            対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult post(String url, Object params) throws IOException {
        return HttpClient.post(url, params);
    }

    /**
     * [POST]HttpClient接続.
     * 
     * @param url
     *            対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header
     *            対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult post(String url, Object params, Map header)
            throws IOException {
        return HttpClient.post(url, params, header);
    }

    /**
     * HttpClient接続.
     * 
     * @param method
     *            対象のMethodを設定します.
     * @param url
     *            対象のURLを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult connect(String method, String url)
            throws IOException {
        return HttpClient.connect(method, url);
    }

    /**
     * HttpClient接続.
     * 
     * @param method
     *            対象のMethodを設定します.
     * @param url
     *            対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult connect(String method, String url, Object params)
            throws IOException {
        return HttpClient.connect(method, url, params);
    }

    /**
     * HttpClient接続.
     * 
     * @param method
     *            対象のMethodを設定します.
     * @param url
     *            対象のURLを設定します.
     * @param header
     *            対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult connect(String method, String url, Map header)
            throws IOException {
        return HttpClient.connect(method, url, header);
    }

    /**
     * HttpClient接続.
     * 
     * @param method
     *            対象のMethodを設定します.
     * @param url
     *            対象のURLを設定します.
     * @oaram params 対象のパラメータを設定します.
     * @param header
     *            対象のヘッダを設定します.
     * @return HttpResult 返却データが返されます.
     */
    public final HttpResult connect(String method, String url, Object params,
            Map header) throws IOException {
        return HttpClient.connect(method, url, params, header);
    }
}
