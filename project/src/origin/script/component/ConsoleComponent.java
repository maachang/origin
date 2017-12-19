package origin.script.component;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import origin.Origin;
import origin.pref.Mode;

import origin.script.OriginComponent;

/**
 * コンソール出力.
 */
public class ConsoleComponent implements OriginComponent {
    private static final Log LOG = LogFactory.getLog(Origin.class);
    private Bindings bindings = null;

    public ConsoleComponent() {
    }

    public ConsoleComponent(Bindings b) {
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
        return "console";
    }

    @Override
    public String toString() {
        return "[object " + getComponentName() + "]";
    }

    // ログ出力.
    private static final void logout(int m, Object n, Object... params) {
        String out = "";
        if (n == null) {
            out = "null";
        } else {
            out = String.format(n.toString(), params);
        }
        switch (m) {
        case 0: // log.
            System.out.println(out);
            if (Mode.SERVER)
                LOG.debug(out);
            break;
        case 1: // info.
            System.out.println(out);
            if (Mode.SERVER)
                LOG.info(out);
            break;
        case 2: // warm.
            System.err.println(out);
            if (Mode.SERVER)
                LOG.warn(out);
            break;
        case 3: // error.
            System.err.println(out);
            if (Mode.SERVER)
                LOG.error(out);
            break;
        }
    }

    /**
     * ログ出力.
     * 
     * @parma n 出力内容を設定します.
     * @param params
     *            パラメータを設定します.
     */
    public void log(Object n, Object... params) {
        logout(0, n, params);
    }

    /**
     * ログ出力.
     * 
     * @parma n 出力内容を設定します.
     * @param params
     *            パラメータを設定します.
     */
    public void info(Object n, Object... params) {
        logout(1, n, params);
    }

    /**
     * ログ出力.
     * 
     * @parma n 出力内容を設定します.
     * @param params
     *            パラメータを設定します.
     */
    public void warn(Object n, Object... params) {
        logout(2, n, params);
    }

    /**
     * ログ出力.
     * 
     * @parma n 出力内容を設定します.
     * @param params
     *            パラメータを設定します.
     */
    public void error(Object n, Object... params) {
        logout(3, n, params);
    }

    private static final String TIME_MAP_KEY = "@console_timeMap";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, Long> getTimeMap(boolean mode) {
        Map<String, Long> ret = (Map) bindings.get(TIME_MAP_KEY);
        if (ret == null) {
            if (mode) {
                ret = new HashMap<String, Long>();
                bindings.put(TIME_MAP_KEY, ret);
            }
        }
        return ret;
    }

    /**
     * 開始時間をセット.
     * 
     * @param label
     *            対象のlabelを設定します.
     * @return long 現在の時間が返却されます.
     */
    public long time() {
        return System.currentTimeMillis();
    }

    /**
     * 開始時間をセット.
     * 
     * @param label
     *            対象のlabelを設定します.
     * @return long 現在の時間が返却されます.
     */
    public long time(String label) {
        long ret = System.currentTimeMillis();
        if (label != null && label.length() != 0) {
            Map<String, Long> map = getTimeMap(true);
            map.put(label, ret);
        }
        return ret;
    }

    /**
     * time開始の終了をセット.
     * 
     * @param label
     *            対象のlabelを設定します.
     * @return long timeからのミリ秒が返却されます.
     */
    public long timeEnd(String label) {
        Long n = null;
        Map<String, Long> map = getTimeMap(false);
        if (map == null || (n = map.remove(label)) == null) {
            return -1L;
        }
        return System.currentTimeMillis() - n;
    }
}
