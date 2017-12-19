package origin.util;

import java.util.HashSet;
import java.util.Set;

/**
 * 連結可能Setオブジェクト.
 */
public class Pset<T> extends HashSet<T> implements Set<T> {
    private static final long serialVersionUID = -6073723798681487075L;

    /**
     * 連結可能な情報追加.
     * 
     * @param v
     *            対象の要素を設定します.
     * @return Plist<T> オブジェクトが返却されます.
     */
    public Pset<T> ad(T k) {
        super.add(k);
        return this;
    }
}
