package org.dragon.vince;

import java.util.Comparator;

public class LinkInfoComparer implements Comparator<LinkInfo> {
	public int compare(LinkInfo lhs, LinkInfo rhs) {
		return lhs.count.compareTo(rhs.count);
	}
}