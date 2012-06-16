package org.dragon.vince;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageRepository {
	public List<ImageInfo> images;
	public Map<Integer, List<ImageInfo>> imagesIndexed;
	
	public ImageRepository() {
		this.imagesIndexed = new HashMap<Integer, List<ImageInfo>>();
		this.images = new ArrayList<ImageInfo>();
	}
	
}
