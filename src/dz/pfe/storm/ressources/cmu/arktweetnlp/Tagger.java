package dz.pfe.storm.ressources.cmu.arktweetnlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dz.pfe.storm.ressources.cmu.arktweetnlp.impl.Model;
import dz.pfe.storm.ressources.cmu.arktweetnlp.impl.ModelSentence;
import dz.pfe.storm.ressources.cmu.arktweetnlp.impl.Sentence;
import dz.pfe.storm.ressources.cmu.arktweetnlp.impl.features.FeatureExtractor;
import dz.pfe.storm.ressources.MotTag;

/**
 * Tagger object -- wraps up the entire tagger for easy usage from Java.
 *
 * To use:
 *
 * (1) call loadModel().
 *
 * (2) call tokenizeAndTag() for every tweet.
 *
 * See main() for example code.
 *
 * (Note RunTagger.java has a more sophisticated runner.
 * This class is intended to be easiest to use in other applications.)
 */
public class Tagger {
	public Model model;
	public FeatureExtractor featureExtractor;

	/**
	 * Loads a model from a file.  The tagger should be ready to tag after calling this.
	 *
	 * @param modelFilename
	 * @throws IOException
	 */
	public synchronized void loadModel(String modelFilename) throws IOException {
		Model mod = new Model();
		model = mod.loadModelFromText(modelFilename);
		featureExtractor = new FeatureExtractor(model, false);
	}

	/**
	 * One token and its tag.
	 **/

	public class TaggedToken {
		public String token;
		public String tag;
	}


	/**
	 * Run the tokenizer and tagger on one tweet's text.
	 **/
	public ArrayList<MotTag> tokenizeAndTag(String text) {
		if (model == null) throw new RuntimeException("Must loadModel() first before tagging anything");
		Twokenize tk = new Twokenize();
		List<String> tokens = tk.tokenizeRawTweetText(text);

		Sentence sentence = new Sentence();
		sentence.tokens = tokens;
		ModelSentence ms = new ModelSentence(sentence.T());
		featureExtractor.computeFeatures(sentence, ms);
		model.greedyDecode(ms, false);

		ArrayList<MotTag> taggedTokens = new ArrayList<MotTag>();

		for (int t=0; t < sentence.T(); t++) {
			TaggedToken tt = new TaggedToken();
			MotTag mt= new MotTag(tokens.get(t), model.labelVocab.name( ms.labels[t] ));

			taggedTokens.add(mt);
		}

		return taggedTokens;
	}

	/**
	 * Illustrate how to load and call the POS tagger.
	 * This main() is not intended for serious use; see RunTagger.java for that.
	 **/
	public ArrayList<MotTag> tagTweet(String tweet) throws IOException {
		//if (args.length < 1) {
		//	System.out.println("Supply the model filename as first argument.");
		//}
		String modelFilename = "/home/samy/Workspaces/topology_pfe/Dictionnaires/model.20120919";//args[0];

		Tagger tagger = new Tagger();
		tagger.loadModel(modelFilename);

		String text = tweet;// "RT @DjBlack_Pearl: wat muhfuckaz wearin 4 the lingerie party?????";
		ArrayList<MotTag> taggedTokens = tagger.tokenizeAndTag(text);

		/*for (MotTag token : taggedTokens) {
			System.out.printf("\n\n\n%s\t%s\n\n\n\n", token.getTag(), token.getMot());
		}*/
		return taggedTokens;
	}

}
