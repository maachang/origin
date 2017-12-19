package origin.script.component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import origin.script.JsNewInstance;
import origin.script.OriginComponent;

/**
 * Lockコンポーネント.
 */
public class LockComponent extends JsNewInstance implements OriginComponent {
    public LockComponent() {
    }

    private static final LockComponent SNGL = new LockComponent();

    /**
     * オブジェクトを取得.
     * 
     * @return LockComponent オブジェクトが返却されます.
     */
    public static final LockComponent getInstance() {
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
        return "Lock";
    }

    @Override
    public String toString() {
        return getComponentName();
    }

    @Override
    public Object newObject(Object... params) {
        return new LockObject();
    }

    /** インスタンス対象オブジェクト. **/
    public static final class LockObject {
        private Lock lock = new ReentrantLock();
        
        /**
         * ロック処理.
         */
        public void lock() {
            lock.lock();
        }

        /**
         * アンロック.
         */
        public void unlock() {
            lock.unlock();
        }

        @Override
        public String toString() {
            return "[object Lock]";
        }
    }
}
