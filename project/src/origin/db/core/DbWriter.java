package origin.db.core;

/**
 * DB書き込み専用オブジェクト. ※このオブジェクトは主にInsert,update,deleteなどで利用.
 */
public interface DbWriter extends DatabaseOperation {

    /** 結果なしの情報. **/
    public static final int[] NON_INT_ARRAY = new int[0];

    /**
     * バッチ出力. [execution]メソッドで実行した内容をDBに出力します.
     * 
     * @return 実行結果の戻り値が返却されます.
     */
    public int[] flush();

    /**
     * バッチキャンセル.
     */
    public void clearFlush();

    /**
     * バッチ件数を取得.
     * 
     * @return int バッチ件数が返却されます.
     */
    public int getBatchSize();

    /**
     * 実行処理. ※この処理は、flushメソッドを実行しないと、条件は反映されません.
     * ただし、規定数の条件が登録された場合は、内部でflush処理が呼び出されます.
     * また、コネクションオブジェクトのclose処理やcommit処理を呼び出した場合も 合わせて呼び出されます.
     * また、都度実行内容を反映させたい場合は、eachメソッドを利用します.
     * 
     * @param args
     *            対象のパラメータを設定します.
     */
    public void batch(Object... args);

    /**
     * 実行処理. ※この処理は、batch処理と違い、都度実行されます. 毎度データベースと通信が発生するため、速度が低下します.
     * 通常は、batchを利用してください.
     * 
     * @param args
     *            対象のパラメータを設定します.
     */
    public int each(Object... args);

}
