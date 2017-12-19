package origin.util;

import origin.script.JsNewInstance;
import origin.script.OriginComponent;
import origin.util.Utils;
import origin.util.Xor128;

/**
 * Xor128ランダム発生コンポーネント.
 */
public class Xor128Component extends JsNewInstance implements OriginComponent {
    public Xor128Component() {
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
        return "Xor128";
    }

    @Override
    public String toString() {
        return getComponentName();
    }

    @Override
    public Object newObject(Object... params) {
        Xor128Object ret = new Xor128Object();
        if (params != null && params.length > 0) {
            ret.setSeet(Utils.convertLong(params[0]));
        }
        return ret;
    }

    /** インスタンス対象オブジェクト. **/
    public static final class Xor128Object {
        private Xor128 xor128 = new Xor128();

        /**
         * ランダム係数を設定.
         * 
         * @param seet
         *            ランダム係数を設定します.
         */
        public final void setSeet(long seet) {
            xor128.setSeet(seet);
        }

        /**
         * 32ビット乱数を取得.
         * 
         * @return int 32ビット乱数が返されます.
         */
        public final int nextInt() {
            return xor128.nextInt();
        }

        /**
         * 32ビット乱数を取得.
         * 
         * @param n
         *            最大値を設定します.
         * @return int 32ビット乱数が返されます.
         */
        public final int nextInt(int n) {
            return (xor128.nextInt() & 0x7fffffff) % n;
        }

        @Override
        public String toString() {
            return "[object Xor128]";
        }
    }
}
