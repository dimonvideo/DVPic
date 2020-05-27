package ua.cv.westward.dvpic.site.dv;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import ua.cv.westward.dvpic.db.DBAdapter;
import ua.cv.westward.dvpic.site.AbstractImage;
import android.content.ContentValues;

/**
 * Объект изображения сайта dimonvideo.ru.
 * Загрузить данные веб-сайта и сохранить объект в базе данных.
 * @author Vladimir Kuts
 */
@Root(name="item")
public class DimonVideoImage extends AbstractImage {

    private static final String SITE_ID = "DV";

    @Element(name="num")
    private String imageid;     // идентификатор изображения

    @Element(name="title")
    private String title;       // заголовок изображения

    @Element(name="data")
    private String siteDate;    // дата размещения на сайте

    @Element(name="name")
    private String author;      // автор

    @Element(name="link")
    private String link;        // URL изображения

    /**
     * Конструктор по умолчанию
     */
    public DimonVideoImage() {
        options |= WEB_PAGE;    // флаг "для картинки можно открыть веб-страницу"
    }

    /* GETTERS */

    /**
     * Получить SITE ID, номер сайта для записей базы данных.
     * @return
     */
    @Override
    public String getSiteID() { return SITE_ID; }

    /**
     * Получить идентификатор изображения
     * @return
     */
    @Override
    public String getImageID() {
        return imageid;
    }

    /**
     * Получить URL изображения
     * @return
     */
    @Override
    public String getLink() {
        return link;
    }

    /**
     * Получить набор значений для записи нового изображения в базу данных.
     * @return
     */
    @Override
    public ContentValues getNewValues() {
        // создать новый набор значений для записи в базу данных
        ContentValues newValues = super.getNewValues();
        // записать данные, за которые отвечает наследник базового класса
        newValues.put( DBAdapter.KEY_SITEID, SITE_ID );
        newValues.put( DBAdapter.KEY_IMAGEID, imageid );
        newValues.put( DBAdapter.KEY_TITLE, title );
        newValues.put( DBAdapter.KEY_SITE_DATE, getSiteDateMiliseconds() );
        newValues.put( DBAdapter.KEY_LINK, link );
        return newValues;
    }

    /*  SERVICE METHODS */

    private long getSiteDateMiliseconds() {
        return Long.valueOf(siteDate) * 1000;
    }
}
