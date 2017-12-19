package origin.script.component;

import origin.script.HttpException;
import jdk.nashorn.api.scripting.JSObject;

import origin.script.JsFunction;
import origin.script.OriginComponent;

/**
 * Sleep命令.
 */
public class SynchronizedFunction extends JsFunction implements OriginComponent {
    public SynchronizedFunction() {
    }

    private static final SynchronizedFunction SNGL = new SynchronizedFunction();

    /**
     * オブジェクトを取得.
     * 
     * @return SynchronizedFunction オブジェクトが返却されます.
     */
    public static final SynchronizedFunction getInstance() {
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
        return "sync";
    }

    @Override
    public String toString() {
        return "function sync() { [native code] } ";
    }

    @Override
    public Object call(Object arg0, Object... arg1) {
        if (arg1 == null || arg1.length < 2 || !(arg1[1] instanceof JSObject)) {
            return null;
        }
        Object o = arg1[0];
        if(o instanceof String) {
            o = ((String) o).intern();
        }
        JSObject func = (JSObject)arg1[1];
        int len = arg1.length;
        Object[] params = null;
        if(len > 2) {
            params = new Object[len-2];
            System.arraycopy(arg1,2,params,0,len-2);
        }
        try {
            synchronized(o) {
                return func.call(this,params);
            }
        } catch(Exception e) {
            throw new HttpException(500, e);
        }
    }
}
