package origin.io;

import origin.script.OriginComponent;
import origin.util.Utils;

/**
 * ファイル操作用コンポーネント.
 */
public class FileComponent implements OriginComponent {
    public FileComponent() {
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
        return "File";
    }

    @Override
    public String toString() {
        return "[object " + getComponentName() + "]";
    }

    /**
     * ファイル名の存在チェック.
     * 
     * @param name
     *            対象のファイル名を設定します.
     * @return boolean [true]の場合、ファイルは存在します.
     */
    public boolean isFile(String name) {
        return Utils.isFile(name);
    }

    /**
     * ディレクトリ名の存在チェック.
     * 
     * @param name
     *            対象のディレクトリ名を設定します.
     * @return boolean [true]の場合、ディレクトリは存在します.
     */
    public boolean isDir(String name) {
        return Utils.isDir(name);
    }

    /**
     * 指定情報が読み込み可能かチェック.
     * 
     * @param name
     *            対象のファイル／ディレクトリ名を設定します.
     * @return boolean [true]の場合、読み込み可能です.
     */
    public boolean isRead(String name) {
        return Utils.isRead(name);
    }

    /**
     * 指定情報が書き込み可能かチェック.
     * 
     * @param name
     *            対象のファイル／ディレクトリ名を設定します.
     * @return boolean [true]の場合、書き込み可能です.
     */
    public boolean isWrite(String name) {
        return Utils.isWrite(name);
    }

    /**
     * 対象のディレクトリを生成.
     * 
     * @param dirName
     *            生成対象のディレクトリ名を設定します.
     * @exception Exception
     *                例外.
     */
    public void mkdirs(String dir) throws Exception {
        Utils.mkdirs(dir);
    }

    /**
     * ファイルの長さを取得.
     * 
     * @param name
     *            対象のファイル名を設定します.
     * @return long ファイルの長さが返却されます. [-1L]が返却された場合、ファイルは存在しません.
     */
    public long length(String name) {
        try {
            return Utils.getFileLength(name);
        } catch (Exception e) {
            return -1L;
        }
    }

    /**
     * ファイルタイムを取得.
     * 
     * @param name
     *            対象のファイル名を設定します.
     * @return java.util.Date ファイルタイムが返却されます. [null]が返却された場合、ファイルは存在しません.
     */
    public java.util.Date time(String name) {
        try {
            long tm = Utils.getFileTime(name);
            if (tm <= 0L) {
                return null;
            }
            return new java.util.Date(tm);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ファイル名のフルパスを取得.
     * 
     * @param name
     *            対象のファイル名を設定します.
     * @return String フルパス名が返却されます.
     */
    public String path(String name) {
        try {
            return Utils.getFullPath(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 対象パスのファイル名のみ取得.
     * 
     * @param path
     *            対象のパスを設定します.
     * @return String ファイル名が返却されます.
     */
    public String name(String path) {
        return Utils.getFileName(path);
    }

    /**
     * ファイル内容を取得.
     * 
     * @param name
     *            対象のファイル名を設定します.
     * @return String 文字列情報が返却されます.
     */
    public String read(String name) {
        return read(name, "UTF8");
    }

    /**
     * ファイル内容を取得.
     * 
     * @param name
     *            対象のファイル名を設定します.
     * @param charset
     *            対象のキャラクタセットを設定します.
     * @return String 文字列情報が返却されます.
     */
    public String read(String name, String charset) {
        try {
            return Utils.getFileString(name, charset);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文字情報をファイル出力.
     * 
     * @param name
     *            ファイル名を設定します.
     * @param value
     *            出力対象の文字列を設定します.
     * @return boolean [true]の場合、書き込みに成功しました.
     */
    public boolean write(String name, String value) {
        return write(name, value, "UTF8");
    }

    /**
     * 文字情報をファイル出力.
     * 
     * @param name
     *            ファイル名を設定します.
     * @param value
     *            出力対象の文字列を設定します.
     * @param charset
     *            対象のキャラクタセットを設定します.
     * @return boolean [true]の場合、書き込みに成功しました.
     */
    public boolean write(String name, String value, String charset) {
        try {
            Utils.setFileString(true, name, value, charset);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 指定ファイルorフォルダを削除.
     * <p>
     * 対象がフォルダの場合は、完全に空でなければ削除出来ません
     * </p>
     * 
     * @param name
     *            対象のファイルorフォルダ名を設定します.
     * @return boolean 削除結果が返されます.
     */
    public boolean remove(String name) {
        try {
            return Utils.removeFile(name);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * ファイル、フォルダの移動.
     * 
     * @param src
     *            移動元のファイル名を設定します.
     * @param dest
     *            移動先のファイル名を設定します.
     * @return boolean [true]が返却された場合、移動は成功しました.
     */
    public boolean move(String src, String dest) {
        return Utils.move(src, dest);
    }
}
