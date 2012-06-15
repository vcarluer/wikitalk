package org.dragon.vince;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Page {
	public String id;
	public String originalText;
	public String workedText;
	public String title;
	public String redirect;
	public String[] splitSentence;
	public Map<Integer, String> sentences;
	public List<Link> links;
	public Map<Integer, List<Link>> linksIndexed;
	
	private String currentSentence;
	
	private static final Pattern LINK_REGEX = Pattern.compile("\\[\\[(.+?)\\]\\]");
	private static final Pattern REF_REGEX = Pattern.compile("<ref(.+?)/ref>");
	private static final Pattern REF2_REGEX = Pattern.compile("<ref(.+?)/>");
	private static final Pattern MODEL_REGEX = Pattern.compile("\\{\\{(.+?)\\}\\}");
	
	public Page() {
		this.sentences = new HashMap<Integer, String>();
		this.linksIndexed = new HashMap<Integer, List<Link>>(); 
		this.links = new ArrayList<Link>();
	}
	
	public static String removeRef(String text) {			
		String retText = text;
		List<String> linkFound = getTagValues(retText, REF_REGEX);
		for(String linkStr : linkFound) {
			retText = retText.replace("<ref" + linkStr + "/ref>", "");
		}
		linkFound = getTagValues(retText, REF2_REGEX);
		for(String linkStr : linkFound) {
			retText = retText.replace("<ref" + linkStr + "/>", "");
		}
		
		return retText;
	}
	
	public static String parseRef(String text) {
		return removeRef(text);
	}
	
	private List<Link> GetSentenceLinks() {			
		List<String> linkFound = getTagValues(this.currentSentence, LINK_REGEX);
		List<Link> newLinks = new ArrayList<Link>();
		for(String linkStr : linkFound) {
			Link link = null;
			if (linkStr.contains("|")) {
				String[] linkStrSplit = linkStr.split("\\|");
				if (linkStrSplit.length == 2) {
					link = new Link();
					link.link = linkStrSplit[0];
					link.label = linkStrSplit[1];						
				}
			} else {
				link = new Link();
				link.label = linkStr;
				link.link = linkStr;
			}
			
			String replaceString = "";
			if (link != null) {
				newLinks.add(link);
				this.links.add(link);
				link.idx = this.links.size() - 1;
				replaceString = link.label;
			}
			
			this.currentSentence = this.currentSentence.replace("[[" + linkStr + "]]", replaceString);
		}

		return newLinks;
	}
	
	public void parseLinks(int idx) {
		this.linksIndexed.put(idx, this.GetSentenceLinks());
	}
	
	private static List<String> getTagValues(final String str, final Pattern regEx) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = regEx.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}

	public void parseOriginalText() {
		this.originalText = this.workedText;
		if (this.redirect == null && this.workedText != null) {
			// for now external reference must be remove before split with . because of long ref
			this.workedText = parseRef(this.workedText);
			this.splitSentence = this.workedText.split("\\. ");
			int idx = 0;
			// special language
			if (this.splitSentence.length == 0) {
				this.splitSentence = new String[1];
				this.splitSentence[0] = this.workedText;
			} else {
				for(String sentence : this.splitSentence) {
					this.currentSentence = new String(sentence);
					this.parseLinks(idx);
					this.parseBoldAndOthers(idx);
					this.parseModel(idx);
					this.parseMenu(idx);			
					if (currentSentence.trim().length() > 0 && 
							!currentSentence.contains("{") && 
							!currentSentence.contains("}") &&
							!currentSentence.contains("|") && 
							!currentSentence.contains("[") && 
							!currentSentence.contains("]") &&
							!currentSentence.contains("<") &&
							!currentSentence.contains(">") &&
							!currentSentence.contains("&") &&
							!currentSentence.contains("/") &&
							!currentSentence.contains("_")) {
						
						this.sentences.put(idx, currentSentence);								
					}			
								
					idx++;
				}
			}
		}

	}
	
	private void parseBoldAndOthers(int idx) {
		// http://fr.wikipedia.org/wiki/Aide:Syntaxe
		// bold italic
		this.currentSentence = this.currentSentence.replaceAll("'''''", "");
		// bold
		this.currentSentence = this.currentSentence.replaceAll("'''", "");
		// italic
		this.currentSentence = this.currentSentence.replaceAll("''", "");				
		// poem
		this.currentSentence = this.currentSentence.replaceAll("<poem>", "");
		this.currentSentence = this.currentSentence.replaceAll("</poem>", "");
		// Puces
		this.currentSentence = this.currentSentence.replaceAll("### ", "");
		this.currentSentence = this.currentSentence.replaceAll("## ", "");
		this.currentSentence = this.currentSentence.replaceAll("# ", "");		
		this.currentSentence = this.currentSentence.replaceAll("\\*\\*\\* ", "");
		this.currentSentence = this.currentSentence.replaceAll("\\*\\* ", "");
		this.currentSentence = this.currentSentence.replaceAll("\\* ", "");
		this.currentSentence = this.currentSentence.replaceAll("#\\* ", "");
		// Others
		this.currentSentence = this.currentSentence.replaceAll("<br />", "");
		this.currentSentence = this.currentSentence.replaceAll("<center>", "");
		this.currentSentence = this.currentSentence.replaceAll("</center>", "");
		// Boites déroulante, tableaux, <span color, 
		this.currentSentence = this.currentSentence.replaceAll("<small>", "");
		this.currentSentence = this.currentSentence.replaceAll("</small>", "");
		this.currentSentence = this.currentSentence.replaceAll("<big>", "");
		this.currentSentence = this.currentSentence.replaceAll("</big>", "");
		this.currentSentence = this.currentSentence.replaceAll("<u>", "");
		this.currentSentence = this.currentSentence.replaceAll("</u>", "");
		this.currentSentence = this.currentSentence.replaceAll("<s>", "");
		this.currentSentence = this.currentSentence.replaceAll("</s>", "");
		// Indice, exposant
		// liens direct http:// sans []
		// Liens vers catégories, images
		// Dates
		// Unités
		// Mots magique
		//{{{
		this.currentSentence = this.currentSentence.replaceAll("\\{\\{\\{", "");
		this.currentSentence = this.currentSentence.replaceAll("\\}\\}\\}", "");
		// Dashes! (with no space)		
		this.currentSentence = this.currentSentence.replaceAll("(?<=[\\w&&[^\\s]])[-](?=[\\w&&[^\\s]])", "");
		
	}
	
	private void parseModel(int idx) {
		// Simple replace for now, see model here: http://fr.wikipedia.org/wiki/Aide:Syntaxe		
		// {{{ removed before
		List<String> linkFound = getTagValues(this.currentSentence, MODEL_REGEX);			
		for(String linkStr : linkFound) {
			String replaceString = "";
			if (linkStr.contains("|")) {
				String[] linkStrSplit = linkStr.split("\\|");
				boolean firstPassed = false;
				for (String val : linkStrSplit) {
					if (!firstPassed) {
						if (!val.equals("lang")) {
							// todo: read with proper language
							firstPassed = true;
						}							
					} else {
						replaceString += val + " ";
					}						
				}
			} else {
				// exposant or number (for now)
				replaceString = linkStr;
				if (replaceString.contains("formatnum:")) {
					replaceString = replaceString.replace("formatnum:", "");
				}										
			}
			
			this.currentSentence = this.currentSentence.replace("{{" + linkStr + "}}", replaceString);
		}
	}
	
	private void parseMenu(int idx) {
		if (this.currentSentence.contains("")) {
			// Only remove for now
			this.currentSentence = this.currentSentence.replaceAll("======", "");
			this.currentSentence = this.currentSentence.replaceAll("=====", "");
			this.currentSentence = this.currentSentence.replaceAll("====", "");
			this.currentSentence = this.currentSentence.replaceAll("===", "");
			this.currentSentence = this.currentSentence.replaceAll("==", "");
		}		
	}
}
