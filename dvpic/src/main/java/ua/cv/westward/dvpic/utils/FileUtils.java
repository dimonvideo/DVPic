package ua.cv.westward.dvpic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import ua.cv.westward.dvpic.PrefKeys;

import android.content.Context;
import android.os.Environment;

import static android.os.Environment.getExternalStorageState;

/**
 * Статические методы работы с файлами.
 * @author Vladimir Kuts
 */
public class FileUtils {
    /**
     * Проверить, если отсутствует - создать базовый каталог приложения на sdcard.
     * Если невозможно создать подкаталог, выбросить исключение.
     * @throws IOException 
     */
    public static void checkBaseAppPath() throws IOException {



        // проверить возможность записи на sdcard
        String state = getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            throw new IOException( state );
        }

        StringBuilder sb = new StringBuilder( Environment.getExternalStorageDirectory().getPath() );
        sb.append( File.separatorChar );
        sb.append( PrefKeys.SD_BASE_PATH );
        // проверить наличие базового подкаталога приложения
        File fpath = new File( sb.toString() );
        if(!fpath.isDirectory()) {
            if(!fpath.mkdirs()) {
                throw new IOException( "SD card write error" );
            }
        }
        
        // создать .nomedia файл в базовом подкаталоге приложения
        sb.append( File.separatorChar );
        sb.append( ".nomedia" );
        File f = new File( sb.toString() );
        if(!f.isFile()) {
            f.createNewFile();
        }
    }
    
    /**
     * Проверить и, если отсутствует, создать папку на sdcard.
     * Если невозможно создать папку, выбросить исключение.
     * @param basePath Основной путь, который строится от корня sdcard
     * @param folderPath Дополнительный путь, который "приклеивается" к основному.
     * Если null, дополнительный путь не используется.
     * @return Полный путь к указанной папке.
     * @throws IOException 
     */
    public static String getPathSD( String basePath, String folderPath )
            throws IOException {
        StringBuilder sb = new StringBuilder( Environment.getExternalStorageDirectory().getPath() );
        sb.append( File.separatorChar );        
        sb.append( basePath );
        if( folderPath != null ) {
            sb.append( File.separatorChar );        
            sb.append( folderPath );
        }
        String fp = sb.toString();
        
        // проверить наличие папки
        File fpath = new File( fp );
        if(!fpath.isDirectory()) {
            if(!fpath.mkdirs()) {
                throw new IOException( "SD card write error" );
            }
        }
        return fp;
    }
    
    /**
     * Вернуть имя файла из полного пути.
     * @param fpath Полное имя файла.
     * @return
     */
    public static String splitFileName( String fpath ) {
        int index = fpath.lastIndexOf( File.separatorChar );
        return fpath.substring( index + 1 );
    }
    
    /**
     * Проверить доступность sdcard
     * @return true - карта памяти доступна, false- недоступна
     */
    public static boolean isStorageMounted() {
        // проверить возможность записи на sdcard
        String state = getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }
    
    /**
     * Собрать полное имя файла.
     * @param path Путь к каталогу, где находится файл.
     * @param name Имя файла.
     * @param ext Расширение файла.
     * @return Полное имя файла.
     */
    public static String makeFileName( String path, String name, String ext ) {
        return path + File.separatorChar +
                name +
                '.' +
                ext;
    }

    /**
     * Удалить файл
     * @param fname Полное имя файла
     */
    public static void deleteFile( String fname ) {        
        File file = new File( fname );
        file.delete();
    }

    /**
     * Удалить все файлы для указанного сайта
     * @param path Полный путь к папке сайта.
     */
    public static void deleteFiles( String path ) {
        // получить список файлов внутри папки
        File siteFolder = new File( path );
        String[] files = siteFolder.list();
        
        if( files != null ) {
            for( String fname : files ) {
                File file = new File( siteFolder.getPath(), fname );
                file.delete();
            }            
        }
    }

    /**
     * Скопировать src файл в dst файл.
     * Если результирующий файл не существует, он будет создан.
     * @param src Имя исходного файла.
     * @param dst Имя результирующего файла.
     * @throws IOException 
     */
    public static void copyFile( String src, String dst ) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {

            // transfer bytes from in to out
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    private File getAbsoluteFile(String relativePath, Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), relativePath);
        } else {
            return new File(context.getFilesDir(), relativePath);
        }
    }
}
