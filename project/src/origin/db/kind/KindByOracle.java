package origin.db.kind;

/**
 * Oracle用DBMSサポート定義.
 */
public class KindByOracle extends DbKind {

    /**
     * 名前を取得.
     * 
     * @return String 名前が返却されます.
     */
    public static final String getName() {
        return "oracle";
    }

    /**
     * アダプタ名を取得.
     * 
     * @return String アダプタ名が返されます.
     */
    public String getAdapter() {
        return getName();
    }

    /**
     * ドライバー名を取得.
     * 
     * @return String ドライバー名が返却されます.
     */
    public String getDriver() {
        return "oracle.jdbc.driver.OracleDriver";
    }

    /**
     * URLヘッダを取得.
     * 
     * @return String URLヘッダが返却されます.
     */
    public String getUrlHead() {
        return "jdbc:oracle:";
    }

}
