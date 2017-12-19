package origin.db.core;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import origin.db.kind.DbKind;
import origin.util.ByteArrayIO;
import origin.util.Utils;

/**
 * DBユーティリティ.
 */
public final class DbUtils {
    private DbUtils() {
    }

    /**
     * 新規コネクション実行時の初期処理.
     * 
     * @param kind
     *            対象のDBKindを設定します.
     * @param conn
     *            対象のコネクションオブジェクトを設定します.
     * @exception Exception
     *                例外.
     */
    public static final void initConnection(DbKind kind, Connection conn)
            throws Exception {
        String[] sql = kind.getInitConnectionSQL();
        if (sql != null && sql.length > 0) {
            Statement stmt = null;
            try {

                // 実行ステートメントを取得.
                stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
                kind.setBusyTimeout(stmt, DbKind.DEF_BUSY_TIMEOUT);

                int len = sql.length;
                for (int i = 0; i < len; i++) {
                    // トランザクションをOFFで実行.
                    try {
                        stmt.execute(DbUtils.sqlBySemicolonOnOff(kind, sql[i]));
                    } catch (Exception e) {
                    }
                }

                stmt.close();
                stmt = null;

            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
     * PreparedStatementパラメータをセット.
     * 
     * @param noSupportBoolean
     *            [true]を指定した場合はBooleanがサポートされていないDBです.
     * @param pre
     *            対象のステートメントを設定します.
     * @aram params 対象のパラメータを設定します.
     */
    public static final void preParams(final boolean noSupportBoolean,
            final String booleanTrue, final String booleanFalse,
            final PreparedStatement pre, ParameterMetaData meta, Object[] params)
            throws Exception {
        int len = params.length;
        for (int i = 0; i < len; i++) {
            putParam(noSupportBoolean, booleanTrue, booleanFalse, i + 1, pre,
                    meta, params[i]);
        }
    }

    /**
     * 1つのパラメータセット.
     */
    public static final void putParam(final boolean noSupportBoolean,
            final String booleanTrue, final String booleanFalse, final int no,
            final PreparedStatement pre, ParameterMetaData meta, Object v)
            throws Exception {

        // ParameterMetaDataがサポートしていない場合.
        if (meta == null) {

            // nullの場合、タイプが不明なので、無作法だがsetObjectにNULLをセット.
            if (v == null) {
                pre.setObject(no, null);
            } else if (v instanceof Boolean) {
                boolean b = ((Boolean) v).booleanValue();
                if (noSupportBoolean) {
                    pre.setString(no, (b ? booleanTrue : booleanFalse));
                } else {
                    pre.setBoolean(no, b);
                }
            } else if (v instanceof String) {
                pre.setString(no, (String) v);
            } else if (v instanceof Integer) {
                pre.setInt(no, (Integer) v);
            } else if (v instanceof Long) {
                pre.setLong(no, (Long) v);
            } else if (v instanceof Float) {
                pre.setFloat(no, (Float) v);
            } else if (v instanceof Double) {
                pre.setDouble(no, (Double) v);
            } else if (v instanceof BigDecimal) {
                pre.setBigDecimal(no, (BigDecimal) v);
            } else if (v instanceof java.util.Date) {
                if (v instanceof java.sql.Timestamp) {
                    pre.setTimestamp(no, (java.sql.Timestamp) v);
                } else if (v instanceof java.sql.Time) {
                    pre.setTime(no, (java.sql.Time) v);
                } else if (v instanceof java.sql.Date) {
                    pre.setDate(no, (java.sql.Date) v);
                } else {
                    pre.setTimestamp(no, new java.sql.Timestamp(
                            ((java.util.Date) v).getTime()));
                }
            } else if (v instanceof byte[]) {
                pre.setBytes(no, (byte[]) v);
            } else {
                pre.setObject(no, v);
            }

            return;
        }

        // ParameterMetaDataがサポートされている場合.
        int type = meta.getParameterType(no);

        // 情報がnullの場合はこちらのほうが行儀がよいのでこのように処理する.
        if (v == null) {
            pre.setNull(no, type);
            return;
        }

        // タイプ別で処理をセット.
        switch (type) {
        case Types.BOOLEAN:
            if (v instanceof Boolean) {
                pre.setBoolean(no, (Boolean) v);
            } else {
                pre.setBoolean(no, Utils.convertBool(v));
            }
            break;
        case Types.BIT:
        case Types.TINYINT:
        case Types.SMALLINT:
            if (v instanceof Boolean) {
                pre.setInt(no, (((Boolean) v).booleanValue()) ? 1 : 0);
            } else {
                pre.setInt(no, Utils.convertInt(v));
            }
            break;
        case Types.INTEGER:
        case Types.BIGINT:
            if (v instanceof Boolean) {
                pre.setLong(no, (((Boolean) v).booleanValue()) ? 1 : 0);
            } else if (v instanceof java.util.Date) {
                pre.setLong(no, ((java.util.Date) v).getTime());
            } else {
                pre.setLong(no, Utils.convertLong(v));
            }
            break;
        case Types.FLOAT:
        case Types.REAL:
            if (v instanceof Float) {
                pre.setFloat(no, (Float) v);
            } else {
                pre.setFloat(no, Utils.convertFloat(v));
            }
            break;
        case Types.DOUBLE:
            if (v instanceof Double) {
                pre.setDouble(no, (Double) v);
            } else {
                pre.setDouble(no, Utils.convertDouble(v));
            }
            break;
        case Types.NUMERIC:
        case Types.DECIMAL:
            if (v instanceof BigDecimal) {
                pre.setBigDecimal(no, (BigDecimal) v);
            } else {
                pre.setBigDecimal(no, new BigDecimal(Utils.convertDouble(v)
                        .toString()));
            }
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.DATALINK:
            if (v instanceof String) {
                pre.setString(no, (String) v);
            } else {
                pre.setString(no, Utils.convertString(v));
            }
            break;
        case Types.DATE:
            if (v instanceof java.sql.Date) {
                pre.setDate(no, (java.sql.Date) v);
            } else {
                pre.setDate(no, Utils.convertSqlDate(v));
            }
            break;
        case Types.TIME:
            if (v instanceof java.sql.Time) {
                pre.setTime(no, (java.sql.Time) v);
            } else {
                pre.setTime(no, Utils.convertSqlTime(v));
            }
            break;
        case Types.TIMESTAMP:
            if (v instanceof java.sql.Timestamp) {
                pre.setTimestamp(no, (java.sql.Timestamp) v);
            } else {
                pre.setTimestamp(no, Utils.convertSqlTimestamp(v));
            }
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.BLOB:
            if (v instanceof byte[]) {
                pre.setBytes(no, (byte[]) v);
                break;
            } else if (v instanceof String) {
                pre.setBytes(no, ((String) v).getBytes("UTF8"));
                break;
            }
            break;
        case Types.JAVA_OBJECT:
            pre.setObject(no, v);
            break;
        default:
            pre.setObject(no, v);
            break;
        }

    }

    /**
     * 結果のカラム情報を取得.
     * 
     * @param result
     *            対象の結果オブジェクトを設定します.
     * @param type
     *            対象のSQLタイプを設定します.
     * @param no
     *            対象の項番を設定します. この番号は１から開始されます.
     */
    public static final Object getResultColumn(ResultSet result, int type,
            int no) throws Exception {
        if (result.getObject(no) == null) {
            return null;
        }
        Object data = null;
        switch (type) {
        case Types.BOOLEAN:
            data = result.getBoolean(no);
            break;
        case Types.BIT:
        case Types.TINYINT:
            data = result.getByte(no);
            byte b = ((Byte) data).byteValue();
            if (b == 1) {
                data = Boolean.TRUE;
            } else {
                data = Boolean.FALSE;
            }
            break;
        case Types.SMALLINT:
            data = result.getInt(no);
            break;
        case Types.INTEGER:
            // Sqliteでは、数値系はすべてIntegerになるので、
            // Integer定義であったとしても、とりあえずLongで受ける.
            data = result.getLong(no);
            break;
        case Types.BIGINT:
            data = result.getLong(no);
            break;
        case Types.FLOAT:
        case Types.REAL:
            data = result.getFloat(no);
            break;
        case Types.DOUBLE:
            data = result.getDouble(no);
            break;
        case Types.NUMERIC:
        case Types.DECIMAL:
            data = result.getBigDecimal(no);
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            data = result.getString(no);
            break;
        case Types.DATE:
            data = result.getDate(no);
            break;
        case Types.TIME:
            data = result.getTime(no);
            break;
        case Types.TIMESTAMP:
            data = result.getTimestamp(no);
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            data = result.getBytes(no);
            break;
        case Types.BLOB:
            data = result.getBlob(no);
            break;
        case Types.DATALINK:
            data = result.getString(no);
            break;
        case Types.STRUCT:// 未サポート.
        case Types.CLOB:// 未サポート.
        case Types.NCLOB:// 未サポート.
        case Types.REF:// 未サポート.
            break;
        }
        // blob.
        if (data instanceof Blob) {
            InputStream b = new BufferedInputStream(
                    ((Blob) data).getBinaryStream());
            ByteArrayIO bo = new ByteArrayIO();
            byte[] bin = new byte[4096];
            int len;
            while (true) {
                if ((len = b.read(bin)) <= -1) {
                    break;
                }
                if (len > 0) {
                    bo.write(bin, 0, len);
                }
            }
            b.close();
            b = null;
            data = bo.toByteArray();
            bo.close();
            bo = null;
        }
        return data;
    }

    /** 小文字大文字差分. **/
    private static final int _Aa = (int) 'a' - (int) 'A';

    public static final String convertJavaNameByDBName(String name) {
        int cnt = 0;
        int len = name.length();
        char c = name.charAt(0);
        char[] buf = new char[len << 1];
        if (c >= 'A' && c <= 'Z') {
            buf[cnt++] = (char) (c + _Aa);
        } else {
            buf[cnt++] = c;
        }
        for (int i = 1; i < len; i++) {
            c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                buf[cnt] = '_';
                buf[cnt + 1] = (char) (c + _Aa);
                cnt += 2;
            } else {
                buf[cnt++] = c;
            }
        }
        return new String(buf, 0, cnt);
    }

    /**
     * DB用データ名をJava用データ名に変換.
     * 
     * @param table
     *            テーブル名の変換の場合は[true]を設定します.
     * @param name
     *            対象の名前を設定します.
     * @return String Java用の名前が返却されます.
     */
    public static final String convertDBNameByJavaName(final boolean table,
            final String name) {
        char cp;
        char c = name.charAt(0);
        final int len = name.length();
        StringBuilder buf = new StringBuilder(len + (len >> 1));
        if (table && c >= 'a' && c <= 'z') {
            buf.append((char) (c - _Aa));
        } else {
            buf.append(c);
        }
        for (int i = 1; i < len; i++) {
            if ((c = name.charAt(i)) == '_') {
                if (i + 1 < len
                        && ((cp = name.charAt(i + 1)) >= 'a' && cp <= 'z')) {
                    buf.append((char) ('A' + (cp - 'a')));
                    i++;
                }
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * SQL文のセミコロン許可Kindの場合は、付加、そうでない場合は除去処理.
     * 
     * @param kind
     *            対象のDbKindオブジェクトを設定します.
     * @param sql
     *            対象のSQLを設定します.
     * @return String 処理されたSQLが返却されます.
     */
    public static final String sqlBySemicolonOnOff(DbKind kind, String sql) {
        sql = sql.trim();
        String end = kind.getSemicolon();
        if (";".equals(end)) {
            if (sql.endsWith(";")) {
                return sql;
            }
            return sql + ";";
        } else if (end.length() == 0) {
            if (sql.endsWith(";")) {
                return sql.substring(0, sql.length() - 1).trim();
            }
            return sql;
        } else {
            if (sql.endsWith(end)) {
                return sql;
            }
            return sql + end;
        }
    }
}
