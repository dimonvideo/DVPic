package ua.cv.westward.dvpic.site;

/**
 * Класс статистической информации о загруженных с сайта картинках:
 * - общее количество картинок
 * - количество новых картинок
 * 
 * @author Vladimir Kuts
 */
public class SiteInfo {

    private int totalCount;
    private int newCount;
    
    public SiteInfo() {
        this.totalCount = 0;
        this.newCount = 0;
    }
    
    public SiteInfo( int totalCnt, int newCnt ) {
        this.totalCount = totalCnt;
        this.newCount = newCnt;
    }
    
    /**
     * Сохранить значения счетчиков картинок.
     * @param totalCnt Общее количество картинок.
     * @param newCnt Число новых картинок.
     */
    public void setStatCounters( int totalCnt, int newCnt ) {
        this.totalCount = totalCnt;
        this.newCount = newCnt;
    }
    
    public int getTotalCounter()    { return totalCount; }
    public int getNewCounter()      { return newCount; }
}
