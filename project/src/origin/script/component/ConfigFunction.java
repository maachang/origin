package origin.script.component;

import origin.conf.ConfigCache;
import origin.script.HttpException;
import origin.util.Utils;

import origin.script.JsFunction;
import origin.script.OriginComponent;

/**
 * Configデータ読み込み命令..
 */
public class ConfigFunction extends JsFunction implements OriginComponent {
    public ConfigFunction() {
    }

    private static final ConfigFunction SNGL = new ConfigFunction();

    /**
     * オブジェクトを取得.
     * 
     * @return ConfigFunction オブジェクトが返却されます.
     */
    public static final ConfigFunction getInstance() {
        return SNGL;
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
        return "config";
    }

    @Override
    public String toString() {
        return "function config() { [native code] } ";
    }

    @Override
    public Object call(Object arg0, Object... arg1) {
        if (arg1 == null || arg1.length == 0) {
            return null;
        }
        try {
            //return Config.read(Utils.convertString(arg1[0]));
            return ConfigCache.getInstance().get(Utils.convertString(arg1[0]));
        } catch (Exception e) {
            throw new HttpException(500, e);
        }
    }
}
