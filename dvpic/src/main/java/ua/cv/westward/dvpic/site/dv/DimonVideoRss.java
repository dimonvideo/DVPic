package ua.cv.westward.dvpic.site.dv;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Объект RSS ленты сайта dimonvideo.ru  
 * @author Vladimir Kuts
 * 
 * Useful links: Simple XML
 * http://simple.sourceforge.net/home.php
 * http://www.ibm.com/developerworks/library/x-simplexobjs/index.html
 */
@Root(name="feed")
public class DimonVideoRss {

    @ElementList(inline=true)
    public List<DimonVideoImage> items;    
    
    public List<DimonVideoImage> getList()   { return items; }
}
