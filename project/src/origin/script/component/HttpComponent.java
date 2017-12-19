package origin.script.component;

import javax.script.Bindings;

import origin.net.http.HttpRequest;
import origin.net.http.HttpStatus;
import origin.script.HttpException;
import origin.script.HttpRedirectException;
import origin.util.Utils;

import origin.script.OriginComponent;

/**
 * Httpコンポーネント.
 */
public class HttpComponent implements OriginComponent {
    private Bindings bindings;

    public HttpComponent() {
    }

    public HttpComponent(Bindings b) {
        bindings = b;
    }

    /**
     * このオブジェクトの生成に対して、Bindingsを設定する場合は[true].
     * 
     * @return boolean [true]の場合、オブジェクト生成時にBindingsが必要です.
     */
    @Override
    public boolean useBindings() {
        return true;
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
        return false;
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
        return "Http";
    }

    @Override
    public String toString() {
        return "[object " + getComponentName() + "]";
    }

    /**
     * リダイレクト処理.
     * 
     * @param url
     *            対象のURLを設定します.
     */
    public void redirect(Object url) {
        if (url == null) {
            throw new IllegalArgumentException("リダイレクト先のURLが設定されていません");
        }
        final String n = url.toString();
        if (!(n.startsWith("http://") || n.startsWith("https://"))) {
            throw new IllegalArgumentException("リダイレクト先のURLは不正です:" + n);
        }
        // GETならstatus301 POSTならstatus307を利用.
        int status = 301;
        HttpRequest req = (HttpRequest) bindings.get("request");
        if (req != null && "POST".equals(req.getMethod())) {
            status = 307;
        }
        throw new HttpRedirectException(status, n);
    }

    /**
     * エラー処理.
     * 
     * @param status
     *            HTTPステータスを設定します.
     * @param message
     *            エラーメッセージを設定します.
     */
    public void error(Object status) {
        error(status, null);
    }

    /**
     * エラー処理.
     * 
     * @param status
     *            HTTPステータスを設定します.
     * @param message
     *            エラーメッセージを設定します.
     */
    public void error(Object status, Object message) {
        if (!Utils.isNumeric(status)) {
            status = 500;
        }
        int state = Utils.convertInt(status);
        String msg = null;
        if (message != null) {
            msg = message.toString();
        } else {
            msg = HttpStatus.getMessage(state);
        }
        throw new HttpException(state, msg);
    }
}
