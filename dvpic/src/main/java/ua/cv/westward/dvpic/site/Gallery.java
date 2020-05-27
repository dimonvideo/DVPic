package ua.cv.westward.dvpic.site;

import ua.cv.westward.dvpic.R;

/**
 * Константные параметры папок главной страницы приложения
 * @author Vladimir Kuts
 */
public enum Gallery {

    NEW    ( R.menu.favorites_menu, R.string.msg_gallery_empty_new ),
    FAV    ( R.menu.favorites_menu, R.string.msg_gallery_empty_fav ),
    DV     ( R.menu.viewer_menu,    R.string.msg_gallery_empty     ),
    IDA    ( R.menu.viewer_menu,    R.string.msg_gallery_empty     ),
    DEMO   ( R.menu.viewer_menu,    R.string.msg_gallery_empty     ),
    INPIC  ( R.menu.viewer_menu,    R.string.msg_gallery_empty     ),
    FISHKI ( R.menu.viewer_menu,    R.string.msg_gallery_empty     );

    private final int   systemMenuID;       // R.menu id системного меню
    private final int   emptyGalleryMsgID;  // R.string id сообщения для пустой галереи

    private Gallery( int sysmenu, int empty ) { //, int btnid ) {
        this.systemMenuID = sysmenu;
        this.emptyGalleryMsgID = empty;
    }

    /**
     * Получить ID системного меню галереи.
     * @return
     */
    public int getSystemMenu() {
        return systemMenuID;
    }

    /**
     * Получить ID сообщения для пустой галереи
     * @return
     */
    public int getEmptyMessage() {
        return emptyGalleryMsgID;
    }

    /**
    * Получить условие "галерея должна показывать только новые картинки"
    * @return
    */
    public boolean onlyNewImages() {
        return name().equals( "NEW" ) ? true : false;
    }

    /**
     * Returns this gallery ID.
     * @return
     */
    public String getID() {
        return name();
    }
}
