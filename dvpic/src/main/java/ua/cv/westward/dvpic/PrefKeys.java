package ua.cv.westward.dvpic;

public class PrefKeys {

    /* COMMON APP PREFERENCES */

    // имя основного файла SharedPreferences
    public static final String NAME = "dvpic";

    // пути к папке приложения на sdcard (все пути без / в начале и конце)
    public static final String SD_BASE_PATH       = "Android/data/ua.cv.westward.dvpic";
    public static final String SD_FILES_PATH      = "Android/data/ua.cv.westward.dvpic/files";
    public static final String SD_THEMES_PATH     = "Android/data/ua.cv.westward.dvpic/themes";

    public static final String SD_COPY_FOLDER     = "download"; // папка для копирования картинок на sdcard

    public static final String SERVICE_LOCK_NAME  = "ua.cv.westward.dvpic.service.WakeLockService";

    public static final int    NOTIFICATION_ID    = 1;          // Идентификатор notification сообщения

    /* INTENT NAMES */

    // intent commands
    public static final String INTENT_SERVICE_CMD = "ua.cv.westward.dvpic.srvCmd";  // команда для сервиса
    public static final String INTENT_APP_CMD     = "ua.cv.westward.dvpic.appCmd";  // команда для activity
    public static final String INTENT_SERVICE_UPD = "ua.cv.westward.dvpic.srvUpd";  // broadcast от сервиса

    // activity commands
    public static final char   CMD_SHOW_NEW       = 'N';        // показать новые картинки

    // intent parameters
    public static final String INTENT_GALLERY_ID  = "IDS";      // идентификаторы сайтов
    public static final String INTENT_SRC_FILE    = "SRCFILE";  // имя исходного файла
    public static final String INTENT_DST_FILE    = "DSTFILE";  // имя результирующего файла
    public static final String INTENT_IMAGE       = "IMAGE";    // инфо-объект приложения
    public static final String INTENT_COUNT       = "CNT";      // количество новых картинок
    public static final String INTENT_ERROR_MSG   = "ERRMSG";   // сообщение об ошибке от сервиса

    /* PREFERENCES KEYS */

    public static final String ORIENTATION        = "ORIENTATION";
    public static final String SHOW_REFRESH       = "SHOW_REFRESH";
    public static final String CHANGE_THEME       = "CHANGE_THEME";
    public static final String WIDGET_ONCLICK     = "WIDGET_ONCLICK";

    public static final String FULLSCREEN         = "FULLSCREEN";
    public static final String IMAGE_TOOLBAR      = "IMAGE_TOOLBAR";
    public static final String VOLUME_BUTTONS     = "VOLUME_BUTTONS";
    public static final String NEW_EXIT           = "NEW_EXIT";
    public static final String SHOW_IMAGE_TITLE   = "SHOW_IMAGE_TITLE2";
    public static final String SHOW_IMAGE_NUMBER  = "SHOW_IMAGE_NUM";
    public static final String SHOW_IMAGE_INFO    = "SHOW_IMAGE_INFO";

    public static final String NETWORK_TYPE       = "NETWORK_TYPE";
    public static final String GIF_NETWORK_TYPE   = "GIF_NETWORK_TYPE";
    public static final String AUTO_RELOAD_TIME   = "AUTO_RELOAD";
    public static final String STORAGE_TEMPLATE   = "STORAGE_";   // ( + _FAV, _DV, _IDA )

    public static final String SHOW_NOTES         = "SHOW_NOTE";
    public static final String AUTO_HIDE_NOTES    = "AUTO_HIDE_NOTE";

    public static final String DELETE_ALL         = "DELETE_ALL";

    public static final String APP_VERSION        = "VER";
    public static final String ERROR_FLAG         = "ERROR_FLAG";


    /* THEME (имена тем, из которых загружаются элементы) */

    public static final String THEME_DEFAULT      = "Default";
    public static final String THEME_APP_BACK     = "THEME_APP_BACK";

    /* DIALOG ARGS */

    public static final String DIALOG_ERR_TITLE   = "ERR_TITLE";
    public static final String DIALOG_ERR_MSG     = "ERR_MSG";
}
