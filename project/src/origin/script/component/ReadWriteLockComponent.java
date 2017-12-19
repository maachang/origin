package origin.script.component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import origin.script.JsNewInstance;
import origin.script.OriginComponent;

/**
 * ReadWriteLockコンポーネント.
 */
public class ReadWriteLockComponent extends JsNewInstance implements OriginComponent {
    public ReadWriteLockComponent() {
    }

    private static final ReadWriteLockComponent SNGL = new ReadWriteLockComponent();

    /**
     * オブジェクトを取得.
     * 
     * @return ReadWriteLockComponent オブジェクトが返却されます.
     */
    public static final ReadWriteLockComponent getInstance() {
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
        return "ReadWriteLock";
    }

    @Override
    public String toString() {
        return getComponentName();
    }

    @Override
    public Object newObject(Object... params) {
        return new ReadWriteLockObject();
    }

    /** インスタンス対象オブジェクト. **/
    public static final class ReadWriteLockObject {
        private ReadWriteLock rw = new ReentrantReadWriteLock();
        
        /**
         * 読み込みロック処理.
         */
        public void readLock() {
            rw.readLock().lock();
        }

        /**
         * 読み込みアンロック.
         */
        public void readUnlock() {
            rw.readLock().unlock();
        }

        /**
         * 書き込みロック処理.
         */
        public void writeLock() {
            rw.writeLock().lock();
        }

        /**
         * 書き込みアンロック.
         */
        public void writeUnlock() {
            rw.writeLock().unlock();
        }

        @Override
        public String toString() {
            return "[object ReadWriteLock]";
        }
    }
}
