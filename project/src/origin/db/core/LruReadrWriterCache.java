package origin.db.core;

import origin.util.LruCache;

/**
 * DbReaderDbWriter用LRUキャッシュ.
 */
public class LruReadrWriterCache extends LruCache<String, DatabaseOperation> {

    /**
     * 指定された最大数でインスタンスを生成
     * 
     * @param initSize
     *            初期サイズを設定します.
     * @param size
     *            管理最大数
     */
    public LruReadrWriterCache(int size) {
        super(size);
    }

    /** エントリの削除要否を判断 */
    @Override
    protected void maxDataByRemove(String key, DatabaseOperation value) {
        // DbWriterの場合は、Batchデータをフラッシュ.
        if (value instanceof DbWriter) {
            DbWriter w = (DbWriter) value;
            try {
                if (!w.isClose() && w.getBatchSize() > 0) {
                    w.flush();
                }
            } catch (Exception e) {
            }
        }
        try {
            value.close();
        } catch (Exception e) {
        }
    }

}