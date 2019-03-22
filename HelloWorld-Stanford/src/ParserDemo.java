import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.*;

class ParserDemo {

	public static void main(String[] args) {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		String path = "knight.txt";
		demoDP(lp, path);
	}

	private static String ChildTag(Tree t) {
		return t.getChild(0).label().toString();
	}

//	private static void NP(Tree t, List<Agent> agents) {
//		Agent a = new Agent();
//		for (Tree c : t.getChildrenAsList()) {
//			String tag = c.label().toString();
//			if (tag.equals("NN") || tag.equals("NNP"))
//				a.name = ChildTag(c);
//			if (tag.equals("JJ"))
//				a.ie.add(ChildTag(c));
////			for (Tree c2 : c.getChildrenAsList()) {
//
////			}
//
//		}
//		agents.add(a);
//	}

	private static void Formalise(Tree t, List<Agent> agents, int sentence_index) {
		String tag = t.label().toString();
		switch (tag) {
		case "NP": {
			Agent a = new Agent();
			agents.add(a);
		}
			break;
		case "NN": {
			agents.get(agents.size() - 1).name = ChildTag(t);
		}
			break;
		case "JJ": {
			agents.get(agents.size() - 1).ie.add(ChildTag(t));
		}
		break;
		case "VBG":
		case "VBD": {
			agents.get(agents.size() - 1).actions.add(ChildTag(t));
		} 
		break;
		default:
			break;
		}
		List<Tree> children = t.getChildrenAsList();
		if (children.size() == 0)
			return;
		for (Tree c : children) {
			Formalise(c, agents, sentence_index);
		}
		return;
	}

//	private static void GetAgents(Tree t, List<Agent> agents, List<String> actions) {
//		String tag = t.label().toString();
//		if (tag.equals("NP"))
//			NP(t, agents);
////		if(tag.equals("NN")) agents.add(new Agent(ChildTag(t)));
////		if(tag.equals("JJ")) agents.get(agents.size() - 1).attributes.add(ChildTag(t));
//		if (tag.equals("VBD") || tag.equals("VBG"))
//			agents.get(agents.size() - 1).actions.add(ChildTag(t));
//		List<Tree> children = t.getChildrenAsList();
//		if (children.size() == 0)
//			return;
//		for (Tree c : children) {
//			GetAgents(c, agents, actions);
//		}
//		return;
//	}

	public static void demoDP(LexicalizedParser lp, String filename) {
		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}
		List<Agent> agents = new ArrayList<Agent>();
		List<String> actions = new ArrayList<String>();
		int ctr = 0;
		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
			Tree parse = lp.apply(sentence);
			parse.pennPrint();
			//GetAgents(parse, agents, actions);
			Formalise(parse, agents, ctr);
			ctr++;
			if (gsf != null) {
//        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//        Collection tdl = gs.typedDependenciesCCprocessed();
//        System.out.println(tdl);
//        System.out.println();
			}
		}
		System.out.println();
//		System.out.println("Agents");
		for (Agent a : agents) {
			System.out.println("Agent Name : " + a.name);
			for (String attribute : a.ie) {
				System.out.println("Internal Element : " + attribute);
			}
			for (String attribute : a.actions) {
				System.out.println("Action : " + attribute);
			}
		}
//		System.out.println();
//		System.out.println("Actions");
//		for(String a : actions)
//			System.out.println(a);
	}

	private ParserDemo() {
	} // static methods only

}
