package origin.util;

import java.util.ArrayList;

/**
 * 連結可能Listオブジェクト.
 */
public class Plist<T> extends ArrayList<T> implements ConvertGet<Integer> {
    private static final long serialVersionUID = -1786235200364053147L;

    /**
     * コンストラクタ.
     */
    public Plist() {
        super();
    }

    /**
     * コンストラクタ.
     * 
     * @param args
     *            パラメータ群を設定します.
     */
    @SuppressWarnings("unchecked")
    public Plist(T... args) {
        super(args.length);
        int len = args.length;
        for (int i = 0; i < len; i++) {
            ad(args[i]);
        }
    }

    /**
     * 連結可能な情報追加.
     * 
     * @param v
     *            対象の要素を設定します.
     * @return Plist<T> オブジェクトが返却されます.
     */
    public Plist<T> ad(T v) {
        super.add(v);
        return this;
    }

    // original 取得.
    @Override
    public Object getOriginal(Integer n) {
        return get(n);
    }

}
