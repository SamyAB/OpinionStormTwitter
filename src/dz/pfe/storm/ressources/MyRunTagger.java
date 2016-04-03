
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import cmu.arktweetnlp.impl.features.FeatureExtractor;
import cmu.arktweetnlp.impl.features.WordClusterPaths;
import cmu.arktweetnlp.io.CoNLLReader;
import cmu.arktweetnlp.io.JsonTweetReader;
import cmu.arktweetnlp.util.BasicFileIO;
import edu.stanford.nlp.util.StringUtils;

/**
 * Commandline interface to run the Twitter POS tagger with a variety of possible input and output formats.
 * Also does basic evaluation if given labeled input text.
 *
 * For basic usage of the tagger from Java, see instead Tagger.java.
 */
public class MyRunTagger {
	Tagger tagger;
	public String[][] taggedWords;
	// Commandline I/O-ish options
	public String inputFormat = "auto";
	public String outputFormat = "auto";
	int inputField = 1;

	public String inputFilename;
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

	// Evaluation stuff
	private static HashSet<String> _wordsInCluster;
	// Only for evaluation mode (conll inputs)
	int numTokensCorrect = 0;
	int numTokens = 0;
	int oovTokensCorrect = 0;
	int oovTokens = 0;
	int clusterTokensCorrect = 0;
	int clusterTokens = 0;

	public static void die(String message) {
		// (BTO) I like "assert false" but assertions are disabled by default in java
		System.err.println(message);
		System.exit(-1);
	}
	public MyRunTagger() throws UnsupportedEncodingException {
		// force UTF-8 here, so don't need -Dfile.encoding
		this.outputStream = new PrintStream(System.out, true, "UTF-8");
	}
	public void detectAndSetInputFormat(String tweetData) throws IOException {
		JsonTweetReader jsonTweetReader = new JsonTweetReader();
		if (jsonTweetReader.isJson(tweetData)) {
			//System.err.println("Detected JSON input format");
			inputFormat = "json";
		} else {
			//System.err.println("Detected text input format");
			inputFormat = "text";
		}
	}

	public String[][] runTagger() throws IOException, ClassNotFoundException {
		//System.out.println("dans runtagger()");
		tagger = new Tagger();
		if (!justTokenize) {
			//System.out.println("just tokenize ?");
			tagger.loadModel(modelFilename);
		}

		if (inputFormat.equals("conll")) {
			//System.out.println("inputFormat conll ?");
			return runTaggerInEvalMode();

		}
		//System.out.println("Apres les premier ifs");
		JsonTweetReader jsonTweetReader = new JsonTweetReader();

		//LineNumberReader reader = new LineNumberReader(BasicFileIO.openFileToReadUTF8(inputFilename));
		String line;
		long currenttime = System.currentTimeMillis();
		int numtoks = 0;
		//while ( (line = reader.readLine()) != null) {
			String[] parts = this.tweet.split("\t");
			//String tweetData = parts[inputField-1];

			//if (reader.getLineNumber()==1) {
				if (inputFormat.equals("auto")) {
					//System.out.println("I guess j'arrive ici a un certain moment...");
				    //System.out.println(tweet);
					detectAndSetInputFormat(this.tweet/*tweetData*/);
				}
			//}

			String text;
			if (inputFormat.equals("json")) {
				text = jsonTweetReader.getText(tweet);
				if (text==null) {
					System.err.println("Warning, null text (JSON parse error?), using blank string instead");
					text = "";
				}
			} else {
				//System.out.println("J'imagine que l'input format est texte maintenant.");
				text = this.tweet;//tweetData;
			}

			Sentence sentence = new Sentence();

			sentence.tokens = Twokenize.tokenizeRawTweetText(text);
			ModelSentence modelSentence = null;

			if (sentence.T() > 0 && !justTokenize) {
				modelSentence = new ModelSentence(sentence.T());
				//System.out.println("Sur le point de rentré dans featureExtractor something...;A;");
				tagger.featureExtractor.computeFeatures(sentence, modelSentence);
				goDecode(modelSentence);
				//System.out.println("Ici ? ");
			}

			if (outputFormat.equals("conll")) {
				//System.out.println("Apres a la fin je pense que c'est la vu que output est conll.");
				return outputJustTagging(sentence, modelSentence);
			} else {
				outputPrependedTagging(sentence, modelSentence, justTokenize, tweet);
			}
			numtoks += sentence.T();
		//}
		long finishtime = System.currentTimeMillis();
		/*System.err.printf("Tokenized%s %d tweets (%d tokens) in %.1f seconds: %.1f tweets/sec, %.1f tokens/sec\n",
				justTokenize ? "" : " and tagged",
				reader.getLineNumber(), numtoks, (finishtime-currenttime)/1000.0,
				reader.getLineNumber() / ((finishtime-currenttime)/1000.0),
				numtoks / ((finishtime-currenttime)/1000.0)
		);*/
		//reader.close();
		return null;
	}

	/** Runs the correct algorithm (make config option perhaps) **/
	public void goDecode(ModelSentence mSent) {
		if (decoder == Decoder.GREEDY) {
			tagger.model.greedyDecode(mSent, showConfidence);
		} else if (decoder == Decoder.VITERBI) {
//			if (showConfidence) throw new RuntimeException("--confidence only works with greedy decoder right now, sorry, yes this is a lame limitation");
			tagger.model.viterbiDecode(mSent);
		}
	}

	public String[][] runTaggerInEvalMode() throws IOException, ClassNotFoundException {
		//System.out.println("A quel moment j'arrive ici tho' ? ");
		long t0 = System.currentTimeMillis();
		int n=0;
		String[] lignes=tweet.split("\n");
		String line;

		ArrayList<Sentence> sentences = new ArrayList<Sentence>();

		ArrayList<String> curLines = new ArrayList<String>();


		for(int i=0; i<lignes.length;i++){
			line=lignes[i];
			if (line.matches("^\\s*$")) {
				if (curLines.size() > 0) {
					// Flush

					Sentence s = new Sentence();

					for (String l : curLines) {
						String[] parts = l.split("\t");
						assert parts.length == 2;
						s.tokens.add( parts[0].trim() );
						s.labels.add( parts[1].trim() );
					}

					sentences.add(s);
					curLines.clear();
				}
			} else {
				curLines.add(line);
			}

		}
		if (curLines.size() > 0) {
			Sentence s = new Sentence();

			for (String l : curLines) {
				String[] parts = l.split("\t");
				assert parts.length == 2;
				s.tokens.add( parts[0].trim() );
				s.labels.add( parts[1].trim() );
			}

			sentences.add(s);
		}



		List<Sentence> examples = sentences;//CoNLLReader.readFile(inputFilename);
		inputIterable = examples;

		int[][] confusion = new int[tagger.model.numLabels][tagger.model.numLabels];

		for (Sentence sentence : examples) {
			n++;

			ModelSentence mSent = new ModelSentence(sentence.T());
			tagger.featureExtractor.computeFeatures(sentence, mSent);
			goDecode(mSent);

			if ( ! noOutput) {
				return outputJustTagging(sentence, mSent);
			}
			evaluateSentenceTagging(sentence, mSent);
			//evaluateOOV(sentence, mSent);
			//getconfusion(sentence, mSent, confusion);
		}

		/*System.err.printf("%d / %d correct = %.4f acc, %.4f err\n",
				numTokensCorrect, numTokens,
				numTokensCorrect*1.0 / numTokens,
				1 - (numTokensCorrect*1.0 / numTokens)
		);
		*/
		double elapsed = ((double) (System.currentTimeMillis() - t0)) / 1000.0;
		/*System.err.printf("%d tweets in %.1f seconds, %.1f tweets/sec\n",
				n, elapsed, n*1.0/elapsed);
		*/
		return null;

/*		System.err.printf("%d / %d cluster words correct = %.4f acc, %.4f err\n",
				oovTokensCorrect, oovTokens,
				oovTokensCorrect*1.0 / oovTokens,
				1 - (oovTokensCorrect*1.0 / oovTokens)
		);	*/
/*		int i=0;
		System.out.println("\t"+tagger.model.labelVocab.toString().replaceAll(" ", ", "));
		for (int[] row:confusion){
			System.out.println(tagger.model.labelVocab.name(i)+"\t"+Arrays.toString(row));
			i++;
		}		*/
	}

	private void evaluateOOV(Sentence lSent, ModelSentence mSent) throws FileNotFoundException, IOException, ClassNotFoundException {
		for (int t=0; t < mSent.T; t++) {
			int trueLabel = tagger.model.labelVocab.num(lSent.labels.get(t));
			int predLabel = mSent.labels[t];
			if(wordsInCluster().contains(lSent.tokens.get(t))){
				oovTokensCorrect += (trueLabel == predLabel) ? 1 : 0;
				oovTokens += 1;
			}
		}
    }
	private void getconfusion(Sentence lSent, ModelSentence mSent, int[][] confusion) {
		for (int t=0; t < mSent.T; t++) {
			int trueLabel = tagger.model.labelVocab.num(lSent.labels.get(t));
			int predLabel = mSent.labels[t];
			if(trueLabel!=-1)
				confusion[trueLabel][predLabel]++;
		}


    }
	public void evaluateSentenceTagging(Sentence lSent, ModelSentence mSent) {
		for (int t=0; t < mSent.T; t++) {
			int trueLabel = tagger.model.labelVocab.num(lSent.labels.get(t));
			int predLabel = mSent.labels[t];
			numTokensCorrect += (trueLabel == predLabel) ? 1 : 0;
			numTokens += 1;
		}
	}

	private String formatConfidence(double confidence) {
		// too many decimal places wastes space
		return String.format("%.4f", confidence);
	}

	/**
	 * assume mSent's labels hold the tagging.
	 */
	public String[][] outputJustTagging(Sentence lSent, ModelSentence mSent) {
		// mSent might be null!

		if (outputFormat.equals("conll")) {
			taggedWords=new String[lSent.T()][2];
			for (int t=0; t < lSent.T(); t++) {
				taggedWords[t][0]=lSent.tokens.get(t);
				taggedWords[t][1]=tagger.model.labelVocab.name(mSent.labels[t]);

			}
	 /*		for(int i=0; i< taggedWords.length; i++)
			{
				System.out.println(taggedWords[i][0]+" "+ taggedWords[i][1]);
			}
	*/
			outputStream.println("");
			return taggedWords;
		}
		else {
			die("bad output format for just tagging: " + outputFormat);
		}
		return taggedWords;
	}
	/**
	 * assume mSent's labels hold the tagging.
	 *
	 * @param lSent
	 * @param mSent
	 * @param inputLine -- assume does NOT have trailing newline.  (default from java's readLine)
	 */
	public void outputPrependedTagging(Sentence lSent, ModelSentence mSent,
			boolean suppressTags, String inputLine) {
		// mSent might be null!

		int T = lSent.T();
		String[] tokens = new String[T];
		String[] tags = new String[T];
		String[] confs = new String[T];
		for (int t=0; t < T; t++) {
			tokens[t] = lSent.tokens.get(t);
			if (!suppressTags) {
				tags[t] = tagger.model.labelVocab.name(mSent.labels[t]);
			}
			if (showConfidence) {
				confs[t] = formatConfidence(mSent.confidences[t]);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.join(tokens));
		sb.append("\t");
		if (!suppressTags) {
			sb.append(StringUtils.join(tags));
			sb.append("\t");
		}
		if (showConfidence) {
			sb.append(StringUtils.join(confs));
			sb.append("\t");
		}
		sb.append(inputLine);

		outputStream.println(sb.toString());
	}


	///////////////////////////////////////////////////////////////////


	public static String[][] tagTweet(String outputFormat,String tweet) throws IOException, ClassNotFoundException {

		MyRunTagger tagger = new MyRunTagger();

		tagger.outputFormat=outputFormat;
		tagger.tweet=tweet;
		tagger.finalizeOptions();

		return tagger.runTagger();
	}

	public void finalizeOptions() throws IOException {
		if (outputFormat.equals("auto")) {
			if (inputFormat.equals("conll")) {
				outputFormat = "conll";
			} else {
				outputFormat = "pretsv";
			}
		}
		if (showConfidence && decoder==Decoder.VITERBI) {
			System.err.println("Confidence output is unimplemented in Viterbi, turning it off.");
			showConfidence = false;
		}
		if (justTokenize) {
			showConfidence = false;
		}
	}

	public static void usage() {
		usage(null);
	}

	public static void usage(String extra) {
		System.out.println(
"RunTagger [options] [ExamplesFilename]" +
"\n  runs the CMU ARK Twitter tagger on tweets from ExamplesFilename, " +
"\n  writing taggings to standard output. Listens on stdin if no input filename." +
"\n\nOptions:" +
"\n  --model <Filename>        Specify model filename. (Else use built-in.)" +
"\n  --just-tokenize           Only run the tokenizer; no POS tags." +
"\n  --quiet                   Quiet: no output" +
"\n  --input-format <Format>   Default: auto" +
"\n                            Options: json, text, conll" +
"\n  --output-format <Format>  Default: automatically decide from input format." +
"\n                            Options: pretsv, conll" +
"\n  --input-field NUM         Default: 1" +
"\n                            Which tab-separated field contains the input" +
"\n                            (1-indexed, like unix 'cut')" +
"\n                            Only for {json, text} input formats." +
"\n  --word-clusters <File>    Alternate word clusters file (see FeatureExtractor)" +
"\n  --no-confidence           Don't output confidence probabilities" +
"\n  --decoder <Decoder>       Change the decoding algorithm (default: greedy)" +
"\n" +
"\nTweet-per-line input formats:" +
"\n   json: Every input line has a JSON object containing the tweet," +
"\n         as per the Streaming API. (The 'text' field is used.)" +
"\n   text: Every input line has the text for one tweet." +
"\nWe actually assume input lines are TSV and the tweet data is one field."+
"\n(Therefore tab characters are not allowed in tweets." +
"\nTwitter's own JSON formats guarantee this;" +
"\nif you extract the text yourself, you must remove tabs and newlines.)" +
"\nTweet-per-line output format is" +
"\n   pretsv: Prepend the tokenization and tagging as new TSV fields, " +
"\n           so the output includes a complete copy of the input." +
"\nBy default, three TSV fields are prepended:" +
"\n   Tokenization \\t POSTags \\t Confidences \\t (original data...)" +
"\nThe tokenization and tags are parallel space-separated lists." +
"\nThe 'conll' format is token-per-line, blank spaces separating tweets."+
"\n");

		if (extra != null) {
			System.out.println("ERROR: " + extra);
		}
		System.exit(1);
	}
	public static HashSet<String> wordsInCluster() {
		if (_wordsInCluster==null) {
			_wordsInCluster = new HashSet<String>(WordClusterPaths.wordToPath.keySet());
		}
		return _wordsInCluster;
	}
}
