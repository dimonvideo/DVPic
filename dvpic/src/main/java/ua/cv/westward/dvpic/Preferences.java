package ua.cv.westward.dvpic;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import ua.cv.westward.dvpic.site.Gallery;
import ua.cv.westward.dvpic.site.SiteParameters;

public class Preferences {

	private static final String[] SITES = { "DV" };

	private static Preferences mInstance = null;
	private final Context mContext;
	// private SharedPreferences mPrefs;

	protected Preferences(Context context) {
		mContext = context.getApplicationContext();
		// mPrefs = mContext.getSharedPreferences( PrefKeys.NAME, Context.MODE_PRIVATE );
	}

	public static synchronized Preferences getInstance(Context context) {
		if (null == mInstance) {
			mInstance = new Preferences(context);
		}
		return mInstance;
	}

	/**
	 * Return the enabled site ids array.
	 * 
	 */
	public String[] getSiteIds() {
		ArrayList<String> list = new ArrayList<String>(4);
		for (String site : SITES) {
			SiteParameters sp = new SiteParameters(mContext, site);
			if (sp.getStorageSize() > 0) {
				list.add(site);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Return the widget's onClick action Intent.
	 * 
	 * @return
	 */
	public Intent getWidgetOnClickIntent() {
		SharedPreferences mPrefs = mContext.getSharedPreferences(PrefKeys.NAME, Context.MODE_PRIVATE);
		String action = mPrefs.getString(PrefKeys.WIDGET_ONCLICK, "HOME");

		if (action.equals("NEW")) {
			Intent i = new Intent(mContext, FlipViewerActivity.class);
			i.putExtra(PrefKeys.INTENT_GALLERY_ID, Gallery.NEW.name());
			return i;
		} else {
			return new Intent(mContext, DVPicActivity.class);
		}
	}
}
