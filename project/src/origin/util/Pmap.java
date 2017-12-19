package origin.util;

import java.util.HashMap;

/**
 * 連結可能Mapオブジェクト.
 */

public class Pmap<K, V> extends HashMap<K, V> implements ConvertGet<K> {
    private static final long serialVersionUID = -7908082471261794163L;

    public Pmap() {
        super();
    }

    public Pmap(int size) {
        super(size);
    }

    /**
     * 連結可能な情報追加.
     * 
     * @param k
     *            対象のキーを設定します.
     * @param v
     *            対象の要素を設定します.
     * @return Pmap<K,V> オブジェクトが返却されます.
     */
    public Pmap<K, V> ad(K k, V v) {
        super.put(k, v);
        return this;
    }

    // original 取得.
    @Override
    public Object getOriginal(K n) {
        return get(n);
    }
}
