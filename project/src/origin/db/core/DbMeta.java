package origin.db.core;

import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import origin.util.Utils;

/**
 * DBメタ情報.
 */
public final class DbMeta {
    /**
     * カラムメタデータ子要素.
     */
    class JDBCMetaChild {
        public String name;
        public int type;
    }

    /**
     * カラム群.
     */
    protected JDBCMetaChild[] columns = null;

    /**
     * カラム名管理.
     */
    protected final Map<String, Integer> map = new HashMap<String, Integer>();

    /**
     * コンストラクタ.
     */
    protected DbMeta() {
    }

    /**
     * コンストラクタ.
     * 
     * @param meta
     *            対象のメタデータを設定します.
     * @exception Exception
     *                例外.
     */
    public DbMeta(ResultSetMetaData meta) throws Exception {
        int len = meta.getColumnCount();
        this.columns = new JDBCMetaChild[len];
        for (int i = 0; i < len; i++) {
            int n = i + 1;
            this.columns[i] = new JDBCMetaChild();
            this.columns[i].name = Utils.toLowerCase(meta.getColumnName(n));
            this.columns[i].type = meta.getColumnType(n);
            this.map.put(this.columns[i].name, i);
        }
    }

    /**
     * カラム名を取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return String カラム名が返されます.
     */
    public String getName(int no) {
        return getName(true, no);
    }

    /**
     * カラム名を取得.
     * 
     * @param mode
     *            [true]の場合、Java名に変換して処理します.
     * @param no
     *            対象の項番を設定します.
     * @return String カラム名が返されます.
     */
    public String getName(boolean mode, int no) {
        if (columns == null || no < 0 || no >= columns.length) {
            return null;
        }
        if (mode) {
            return DbUtils.convertDBNameByJavaName(false, columns[no].name);
        }
        return columns[no].name;
    }

    /**
     * カラムタイプを取得.
     * 
     * @param no
     *            対象の項番を設定します.
     * @return int カラムタイプが返されます.
     */
    public int getType(int no) {
        if (columns == null || no < 0 || no >= columns.length) {
            return -1;
        }
        return columns[no].type;
    }

    /**
     * 一致するカラム名に対する項番を取得.
     * 
     * @param column
     *            対象のカラム名を設定します.
     * @return int 対象の項番が返されます.
     */
    public int getNumber(String column) {
        return getNumber(true, column);
    }

    /**
     * 一致するカラム名に対する項番を取得.
     * 
     * @param mode
     *            [true]の場合、DB名に変換して処理します.
     * @param column
     *            対象のカラム名を設定します.
     * @return int 対象の項番が返されます.
     */
    public int getNumber(boolean mode, String column) {
        if (columns == null || column == null || column.length() <= 0) {
            return -1;
        }
        if (mode) {
            column = DbUtils.convertJavaNameByDBName(column);
        }
        Integer ret = map.get(column);
        if (ret == null) {
            return -1;
        }
        return ret;
    }

    /**
     * カラム数を取得. <BR>
     * <BR>
     * カラム数が返されます. <BR>
     * 
     * @return String カラム数が返されます.
     */
    public int size() {
        if (columns == null) {
            return 0;
        }
        return columns.length;
    }

    /**
     * 文字列に変換.
     */
    public String toString() {
        if (columns == null) {
            return "null";
        }
        StringBuilder buf = new StringBuilder();
        int len = columns.length;
        for (int i = 0; i < len; i++) {
            buf.append(" [").append((i + 1)).append("]");
            buf.append(" name:").append(columns[i].name);
            buf.append(" type:").append(columns[i].type);
        }
        return buf.toString();
    }
}
