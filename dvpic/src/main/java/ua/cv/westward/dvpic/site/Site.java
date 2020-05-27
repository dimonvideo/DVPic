package ua.cv.westward.dvpic.site;

import ua.cv.westward.dvpic.site.dv.DimonVideoHelper;

public enum Site {

    DV (
        "DimonVideo.ru",
        DimonVideoHelper.class,
        "DimonVideo",
        "https://api.dimonvideo.ru/smart/gapi.php?op=1",
        "https://dimonvideo.ru/gallery/",
        false
    ),
    FAV(
        "Избранное", null,
        "Favorites",
        null,
        null,
        false
    );

    private final String    title;          // наименование сайта
    private final Class<?>  helper;         // класс загрузчика изображений
    private final String    imagesFolder;   // каталог на SD card для загрузки
                                            //   изображений (относительный путь)
    private final String    feedURL;        // URL загрузки списка изображений
    private final String    pageURL;        // URL веб-страницы изображения (null - нет страницы)
    private final boolean   canExclude;     // сайт можно исключить из просмотра/загрузки

    private Site( String title, Class<?> helper, String folder,
            String feed, String pageURL, boolean canExclude ) {
        this.title = title;
        this.helper = helper;
        this.imagesFolder = folder;
        this.feedURL = feed;
        this.pageURL = pageURL;
        this.canExclude = canExclude;
    }

    public String getTitle() {
        return title;
    }

    public Class<?> getHelper() {
        return helper;
    }

    public String getFolder() {
        return imagesFolder;
    }

    public String getFeedURL() {
        return feedURL;
    }

    public String getPageURL() {
        return pageURL;
    }

    public boolean canExclude() {
        return canExclude;
    }
}