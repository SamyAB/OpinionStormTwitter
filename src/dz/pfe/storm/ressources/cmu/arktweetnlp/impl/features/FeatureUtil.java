package dz.pfe.storm.ressources.cmu.arktweetnlp.impl.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.twitter.Regex;

import dz.pfe.storm.ressources.cmu.arktweetnlp.Twokenize;

//import com.twitter.Regex;



/**
 * String normalizations and other shared utilities for feature computation.
 **/
public class FeatureUtil {

	public Pattern URL ;//= Pattern.compile(Twokenize.OR(Twokenize.url, Twokenize.Email));
	public Pattern justbase ;//= Pattern.compile("(?!www\\.|ww\\.|w\\.|@)[a-zA-Z0-9]+\\.[A-Za-z0-9\\.]+");

//	Pattern URL = Pattern.compile(Twokenize.url);
	public FeatureUtil(){
		Twokenize to= new Twokenize();
		URL= Pattern.compile(to.OR(to.url, to.Email));
		justbase= Pattern.compile("(?!www\\.|ww\\.|w\\.|@)[a-zA-Z0-9]+\\.[A-Za-z0-9\\.]+");
	}

	public ArrayList<String> normalize(List<String> toks){
		ArrayList<String> normtoks = new ArrayList<String>(toks.size());
		for (String s:toks){
			normtoks.add(normalize(s));
		}
		return normtoks;
	}

	/**
	 * @param str
	 * @return Lowercase normalized string
	 */
	public String normalize(String str) {
	    str = str.toLowerCase();
		if (URL.matcher(str).matches()){
	    	String base = "";
	    	Matcher m = justbase.matcher(str);
	    	if (m.find())
	    		base=m.group().toLowerCase();
	    	return "<URL-"+base+">";
	    }
	    if (Regex.VALID_MENTION_OR_LIST.matcher(str).matches())
	    	return "<@MENTION>";
	    return str;
	}

	public ArrayList<String> normalizecap(List<String> toks){
		ArrayList<String> normtoks = new ArrayList<String>(toks.size());
		for (String s:toks){
			normtoks.add(normalizecap(s));
		}
		return normtoks;
	}

	//same as normalize but retains capitalization
	public String normalizecap(String str) {
		if (URL.matcher(str).matches()){
	    	String base = "";
	    	Matcher m = justbase.matcher(str);
	    	if (m.find())
	    		base=m.group().toLowerCase();
	    	return "<URL-"+base+">";
	    }

	    if (Regex.VALID_MENTION_OR_LIST.matcher(str).matches())
	    	return "<@MENTION>";
	    return str;
	}


	Pattern repeatchar = Pattern.compile("([\\w])\\1{1,}");
	Pattern repeatvowel = Pattern.compile("(a|e|i|o|u)\\1+");

	public ArrayList<String> fuzztoken(String tok, boolean apos) {
	    ArrayList<String> fuzz = new ArrayList<String>();
	    fuzz.add(tok.replaceAll("[‘’´`]", "'").replaceAll("[“”]", "\""));
	    fuzz.add(tok);
	    fuzz.add(repeatchar.matcher(tok).replaceAll("$1"));//omggggggg->omg
	    fuzz.add(repeatchar.matcher(tok).replaceAll("$1$1"));//omggggggg->omgg
	    fuzz.add(repeatvowel.matcher(tok).replaceAll("$1"));//heeellloooo->helllo
	    if (apos && !(tok.startsWith("<URL"))){
	    	fuzz.add(tok.replaceAll("\\p{Punct}", ""));//t-swift->tswift
	    	//maybe a bad idea (bello's->bello, re-enable->re, croplife's->'s)
	    	fuzz.addAll(Arrays.asList(tok.split("\\p{Punct}")));
	    }
	    return fuzz;
	}

}
