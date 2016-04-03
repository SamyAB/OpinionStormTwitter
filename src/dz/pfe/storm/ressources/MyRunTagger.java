package dz.pfe.storm.ressources;

import java.io.IOException;
import java.io.PrintStream;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;

public class MyRunTagger {
	Tagger tagger;
	public String[][] taggedWords;
	// Commandline I/O-ish options
	public String inputFormat = "auto";
	public String outputFormat = "auto";
	int inputField = 1;

	public String tweet="";
	/** Can be either filename or resource name **/
	public String modelFilename = "/cmu/arktweetnlp/model.20120919";

	public boolean noOutput = false;
	public boolean justTokenize = false;

	public static enum Decoder { GREEDY, VITERBI };
	public Decoder decoder = Decoder.GREEDY;
	public boolean showConfidence = true;

	PrintStream outputStream;
	Iterable<Sentence> inputIterable = null;

	int numTokensCorrect = 0;
	int numTokens = 0;
	int oovTokensCorrect = 0;
	int oovTokens = 0;
	int clusterTokensCorrect = 0;
	int clusterTokens = 0;

	public MyRunTagger() {

	}

	public String[][] runTagger() throws IOException, ClassNotFoundException {
		tagger = new Tagger();

		tagger.loadModel(modelFilename);

		this.inputFormat="text";
		String text=this.tweet;

		Sentence sentence = new Sentence();

		sentence.tokens = Twokenize.tokenizeRawTweetText(text);
		ModelSentence modelSentence = null;

		if (sentence.T() > 0 && !justTokenize) {
			modelSentence = new ModelSentence(sentence.T());
			tagger.featureExtractor.computeFeatures(sentence, modelSentence);
			tagger.model.greedyDecode(modelSentence, showConfidence);
		}
		return outputJustTagging(sentence, modelSentence);
	}

	/**
	 * assume mSent's labels hold the tagging.
	 */
	public String[][] outputJustTagging(Sentence lSent, ModelSentence mSent) {
		taggedWords=new String[lSent.T()][2];
		for (int t=0; t < lSent.T(); t++) {
			taggedWords[t][0]=lSent.tokens.get(t);
			taggedWords[t][1]=tagger.model.labelVocab.name(mSent.labels[t]);

		}
		return taggedWords;
	}

	public static String[][] tagTweet(String outputFormat,String tweet) throws IOException, ClassNotFoundException {
		MyRunTagger tagger = new MyRunTagger();

		tagger.outputFormat=outputFormat;
		tagger.tweet=tweet;

		return tagger.runTagger();
	}
}
