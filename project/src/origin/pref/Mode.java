package origin.pref;

/**
 * モード.
 */
public class Mode {
    protected Mode() {
    }

    /**
     * サーバ起動モード.
     */
    public static boolean SERVER = false;

    /**
     * SQLデバッグ出力モード.
     */
    public static boolean DEBUG_SQL = false;
    
    /**
     * クラスキャッシュサイズ.
     */
    public static int CLASS_CACHE_SIZE = 100;

}
