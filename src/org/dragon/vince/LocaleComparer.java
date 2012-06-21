package org.dragon.vince;

import java.util.Comparator;
import java.util.Locale;

public class LocaleComparer implements Comparator<Locale> {

	public int compare(Locale lhs, Locale rhs) {
		return lhs.getDisplayName().compareTo(rhs.getDisplayName());
	}

}
