package origin.net.http;

import java.util.HashMap;
import java.util.Map;

import origin.conf.Config;
import origin.util.Utils;

/**
 * MimeType.
 */
public class MimeType {
    
    /** MimeTypeコンフィグファイル名. **/
    public static final String MIME_CONF = "mime.conf" ;
    
    /** 不明なMimeType. **/
    public static final String UNKNONW_MIME_TYPE = "application/octet-stream" ;
    
    /** スクリプト実行でのMimeType返却条件. **/
    public static final String SCRIPT_MIMETYPE = "text/html" ;
    
    
    
    /** 拡張子に対するデフォルトMime定義. **/
    private final Map<String,String> mimeTable = new HashMap<String,String>() ;
    
    /** charsetを付加するMimeType. **/
    private final Map<String,Boolean> charsetMimeTable = new HashMap<String,Boolean>() ;
    
    /**
     * 拡張Mimeタイプ定義を読み込む.
     * @param name 対象のコンフィグファイル名を設定します.
     * @return MimeType 拡張Mimeタイプオブジェクトが返却されます.
     * @exception Exception 例外.
     */
    public static final MimeType createMime( String name )
        throws Exception {
        return createMime( Config.read( name ) ) ;
    }
    
    /**
     * 拡張Mimeタイプ定義を読み込む.
     * @param conf 対象のコンフィグファイルを設定します.
     * @return MimeType 拡張Mimeタイプオブジェクトが返却されます.
     * @exception Exception 例外.
     */
    public static final MimeType createMime( Config conf )
        throws Exception {
        if( !conf.isSection( "mime" ) ) {
            return new MimeType() ;
        }
        
        String[] keys = conf.getKeys( "mime" ) ;
        if( keys == null || keys.length <= 0 ) {
            return new MimeType() ;
        }
        
        String value ;
        MimeType ret = new MimeType() ;
        int len = keys.length ;
        for( int i = 0 ; i < len ; i ++ ) {
            value = conf.getString( "mime",keys[ i ],0 ) ;
            if( Utils.useString( value ) ) {
                ret.mimeTable.put( keys[ i ],value ) ;
            }
        }
        
        return ret ;
    }
    
    
    /**
     * コンストラクタ.
     */
    public MimeType() {
        // デフォルトMimeType.
        mimeTable.put( "htm","text/html" ) ;
        mimeTable.put( "html","text/html" ) ;
        mimeTable.put( "htc","text/x-component" ) ;
        mimeTable.put( "pdf","application/pdf" ) ;
        mimeTable.put( "rtf","application/rtf" ) ;
        mimeTable.put( "doc","application/msword" ) ;
        mimeTable.put( "xls","application/vnd.ms-excel" ) ;
        mimeTable.put( "ppt","application/ppt" ) ;
        mimeTable.put( "tsv","text/tab-separated-values" ) ;
        mimeTable.put( "csv","application/octet-stream" ) ;
        mimeTable.put( "txt","text/plain" ) ;
        mimeTable.put( "xml","text/xml" ) ;
        mimeTable.put( "xhtml","application/xhtml+xml" ) ;
        mimeTable.put( "jar","application/java-archiver" ) ;
        mimeTable.put( "sh","application/x-sh" ) ;
        mimeTable.put( "shar","application/x-sh" ) ;
        mimeTable.put( "tar","application/x-tar" ) ;
        mimeTable.put( "z","application/x-compress" ) ;
        mimeTable.put( "zip","application/zip" ) ;
        mimeTable.put( "bmp","image/x-bmp" ) ;
        mimeTable.put( "rle","image/x-bmp" ) ;
        mimeTable.put( "dib","image/x-bmp" ) ;
        mimeTable.put( "gif","image/gif" ) ;
        mimeTable.put( "jpg","image/jpeg" ) ;
        mimeTable.put( "jpeg","image/jpeg" ) ;
        mimeTable.put( "jpe","image/jpeg" ) ;
        mimeTable.put( "jfif","image/jpeg" ) ;
        mimeTable.put( "jfi","image/jpeg" ) ;
        mimeTable.put( "png","image/x-png" ) ;
        mimeTable.put( "tiff","image/tiff" ) ;
        mimeTable.put( "tif","image/tiff" ) ;
        mimeTable.put( "aiff","audio/aiff" ) ;
        mimeTable.put( "aif","audio/aiff" ) ;
        mimeTable.put( "au","audio/basic" ) ;
        mimeTable.put( "kar","audio/midi" ) ;
        mimeTable.put( "midi","audio/midi" ) ;
        mimeTable.put( "mid","audio/midi" ) ;
        mimeTable.put( "smf","audio/midi" ) ;
        mimeTable.put( "wav","audio/wav" ) ;
        mimeTable.put( "asf","video/x-ms-asf" ) ;
        mimeTable.put( "avi","vide/x-msvideo" ) ;
        mimeTable.put( "m1s","vide/mpeg" ) ;
        mimeTable.put( "m1v","vide/mpeg" ) ;
        mimeTable.put( "m2s","vide/mpeg" ) ;
        mimeTable.put( "m2v","vide/mpeg" ) ;
        mimeTable.put( "mpeg","vide/mpeg" ) ;
        mimeTable.put( "mpg","vide/mpeg" ) ;
        mimeTable.put( "mpe","vide/mpeg" ) ;
        mimeTable.put( "mpv","vide/mpeg" ) ;
        mimeTable.put( "m1a","audio/mpeg" ) ;
        mimeTable.put( "m2a","audio/mpeg" ) ;
        mimeTable.put( "mp2","audio/mpeg" ) ;
        mimeTable.put( "mp3","audio/mpeg" ) ;
        mimeTable.put( "ogg","audio/ogg" ) ;
        mimeTable.put( "m4a","audio/aac" ) ;
        mimeTable.put( "webm","audio/webm" ) ;
        mimeTable.put( "moov","video/quicktime" ) ;
        mimeTable.put( "mov","video/quicktime" ) ;
        mimeTable.put( "qt","video/quicktime" ) ;
        mimeTable.put( "rm","audio/x-pn-realaudio" ) ;
        mimeTable.put( "swf","application/x-shockwave-flash" ) ;
        mimeTable.put( "exe","application/exe" ) ;
        mimeTable.put( "pl","application/x-perl" ) ;
        mimeTable.put( "ram","audio/x-pn-realaudio" ) ;
        mimeTable.put( "js","text/javascript" ) ;
        mimeTable.put( "css","text/css" ) ;
        mimeTable.put( "ico","image/x-icon" ) ;
        mimeTable.put( "manifest","text/cache-manifest" ) ;
        
        // charset付加をするMimeType.
        charsetMimeTable.put( "text/html",true ) ;
        charsetMimeTable.put( "text/javascript",true ) ;
        charsetMimeTable.put( "text/css",true ) ;
        charsetMimeTable.put( "text/plain",true ) ;
        charsetMimeTable.put( "text/xml",true ) ;
        charsetMimeTable.put( "application/xhtml+xml",true ) ;
        charsetMimeTable.put( "text/x-component",true ) ;
    }
    
    /**
     * 初期処理.
     * @param addMime 新たに追加するMimeType群が格納されたMapオブジェクトを設定します.
     * @param charsetMime Charsetを付加するMimeTypeを設定します.
     */
    public void init( Map<String,String> addMime,Map<String,Boolean> charsetMime ) {
        mimeTable.putAll( addMime ) ;
        charsetMimeTable.putAll( charsetMime ) ;
    }
    
    /**
     * 指定URLからMimeTypeを取得.
     * @param url 対象のURLを設定します.
     * @return String MimeTypeが返却されます.
     *                [null]が返却された場合は、スクリプト実行条件が考慮されます.
     */
    public String get( String url ) {
        int p = url.lastIndexOf( "." ) ;
        if( p == -1 || url.lastIndexOf( "/" ) > p ) {
            return null ;
        }
        String ret = mimeTable.get( url.substring( p+1 ).trim() ) ;
        return (ret==null)? UNKNONW_MIME_TYPE : ret ;
    }
    
    /**
     * 指定MimeTypeがCharsetを付加する必要があるかチェック.
     * @param mime 対象のMimeTypeを設定します.
     * @return boolean [true]の場合、charsetの設定は必要です.
     */
    public boolean isCharset( String mime ) {
        Boolean ret = charsetMimeTable.get( mime ) ;
        return (ret==null)? false : ret ;
    }
    
}
