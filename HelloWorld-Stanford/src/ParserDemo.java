import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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

	// Doesn't do anything when a combination is reached as it will iterate through
	// and add characters
	// Uses a stack in order to point to corresponding agents and avoid repitition

	private static String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private static String path = "test.txt";

	private static List<AgentPointer> agent_stack;
	private static List<Agent> agents;
	private static List<Action> actions;
	private static List<InternalElement> internals;

	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

		agent_stack = new ArrayList<AgentPointer>();
		agents = new ArrayList<Agent>();
		actions = new ArrayList<Action>();
		internals = new ArrayList<InternalElement>();

		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}
		int ctr = 0;
		for (List<HasWord> sentence : new DocumentPreprocessor(path)) {
			Tree parse = lp.apply(sentence);
			parse.pennPrint();
			Formalise(parse, ctr, 0);

			ctr++;
		}
		PrintAgents();
		PrintStack();
	}

	private static Integer AgentIndex(String name) {
		for (int i = 0; i < agents.size(); ++i) {
			if (agents.get(i).name.equals(name))
				return i;
		}
		return -1;
	}

	private static void AddPointer(String name, Integer si, Integer sd) {
		Integer index = AgentIndex(name);
		if (index > -1) {
			agent_stack.add(new AgentPointer(index, si, sd));
		} else {
			Agent a = new Agent();
			a.id = agents.size();
			a.name = name;
			agent_stack.add(new AgentPointer(agents.size(), si, sd));
			agents.add(a);
		}
	}

	private static void PopPointer() {
		agent_stack.remove(agent_stack.size() - 1);
	}

	private static void PrintAgents() {
		for (int i = 0; i < agents.size(); ++i) {
			System.out.println("agents[" + i + "] :" + agents.get(i).name);
		}
	}

	private static void PrintStack() {
		for (int i = 0; i < agent_stack.size(); ++i) {
			AgentPointer cur = agent_stack.get(i);
			System.out.println("agent_stack[" + i + "] : (" + cur.sentence_index + "," + cur.sentence_depth + ", "
					+ agents.get(cur.agent_index).name + ")");
		}
	}

	private static String LabelOfLastChild(Tree t) {
		List<Tree> children = t.getChildrenAsList();
		Tree last = children.get(children.size() - 1);
		return last.getChild(0).label().toString();
	}

	private static String NPsNN(Tree t) {
		List<Tree> children = t.getChildrenAsList();
		Tree last = children.get(children.size() - 1);
		String last_tag = last.label().toString();
		switch (last_tag) {
		case "NN":
		case "NNS":
		case "NNP":
			return LabelOfLastChild(t);
		case "PRP":
			return "PRP";
		case "NP":
			return "NP";
		default:
			return "NULL";
		}

	}

	private static void nested_switch_prp(Tree t, Integer si, Integer sd) {
		String prp = LabelOfLastChild(t);
		switch (prp) {
		case "They":
		case "they":
			Integer last_sentence_index = agent_stack.get(agent_stack.size() - 1).sentence_index;
			break;
		}
		AddPointer(prp, si, sd);
	}
	
	private static void nested_switch_np(Tree t, Integer si, Integer sd) {
		String result = NPsNN(t);
		switch (result) {
		case "NP":
			break;
		case "PRP":
			nested_switch_prp(t,si,sd);
			break;
		default:
			AddPointer(result, si, sd);
			break;
		}
	}

	private static void Formalise(Tree t, Integer si, Integer sd) {
		String tag = t.label().toString();
		switch (tag) {
		case "NP": {
			nested_switch_np(t, si, sd);
		}
		}
		for (Tree c : t.getChildrenAsList())
			Formalise(c, si, sd + 1);
	}
}

//	private static int GetAgent(List<Agent> agents, String name) {
//		for (Agent a : agents)
//			if (a.name.equals(name))
//				return agents.indexOf(a);
//		return -1;
//	}
//
//	private static boolean HasAction(Agent agent, String action) {
//		for (String a : agent.actions)
//			if (a.equals(action))
//				return true;
//		return false;
//	}
//
//	private static Agent MergeAgent(Agent a, Agent b) {
//		for (String action : b.actions)
//			if (!HasAction(a, action))
//				a.actions.add(action);
//		return a;
//	}
//
//	private static String ChildTag(Tree t) {
//		return t.getChild(0).label().toString();
//	}
//
//	private static void Formalise(Tree t, List<Agent> agents, int sentence_index) {
//		String tag = t.label().toString();
//		switch (tag) {
//		case "NP": {
//			Agent a = new Agent();
//			agents.add(a);
//		}
//			break;
//		case "NN": {
//			String name = ChildTag(t);
//			agents.get(agents.size() - 1).name = name;
//		}
//			break;
//		case "JJ": {
//			agents.get(agents.size() - 1).ie.add(ChildTag(t));
//		}
//			break;
//		case "VBG":
//		case "VBD": {
//			agents.get(agents.size() - 1).actions.add(ChildTag(t));
//		}
//			break;
//		default:
//			break;
//		}
//		List<Tree> children = t.getChildrenAsList();
//		if (children.size() == 0)
//			return;
//		for (Tree c : children) {
//			Formalise(c, agents, sentence_index);
//		}
//
//		return;
//	}
//
////	private static void GetAgents(Tree t, List<Agent> agents, List<String> actions) {
////		String tag = t.label().toString();
////		if (tag.equals("NP"))
////			NP(t, agents);
//////		if(tag.equals("NN")) agents.add(new Agent(ChildTag(t)));
//////		if(tag.equals("JJ")) agents.get(agents.size() - 1).attributes.add(ChildTag(t));
////		if (tag.equals("VBD") || tag.equals("VBG"))
////			agents.get(agents.size() - 1).actions.add(ChildTag(t));
////		List<Tree> children = t.getChildrenAsList();
////		if (children.size() == 0)
////			return;
////		for (Tree c : children) {
////			GetAgents(c, agents, actions);
////		}
////		return;
////	}
//
//	public static void demoDP(LexicalizedParser lp, String filename) {
//		TreebankLanguagePack tlp = lp.treebankLanguagePack();
//		GrammaticalStructureFactory gsf = null;
//		if (tlp.supportsGrammaticalStructures()) {
//			gsf = tlp.grammaticalStructureFactory();
//		}
//		List<Agent> agents = new ArrayList<Agent>();
//		List<String> actions = new ArrayList<String>();
//		int ctr = 0;
//		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
//			Tree parse = lp.apply(sentence);
//			parse.pennPrint();
//			// GetAgents(parse, agents, actions);
//			Formalise(parse, agents, ctr);
//			ctr++;
//			if (gsf != null) {
////        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
////        Collection tdl = gs.typedDependenciesCCprocessed();
////        System.out.println(tdl);
////        System.out.println();
//			}
//		}
//		System.out.println();
////		System.out.println("Agents");
//		List<Agent> agents2 = new ArrayList<Agent>();
//		for (int i = 0; i < agents.size(); ++i) {
//			Agent a = agents.get(i);
//			int index = GetAgent(agents2, a.name);
//			if (index == -1)
//				agents2.add(a);
//			else {
//				agents2.set(index, MergeAgent(agents2.get(index), a));
//			}
//		}
//		agents.clear();
//		for (Agent a : agents2)
//			agents.add(a);
//		for (Agent a : agents) {
//			System.out.println("Agent Name : " + a.name);
//			for (String attribute : a.ie) {
//				System.out.println("Internal Element : " + attribute);
//			}
//			for (String attribute : a.actions) {
//				System.out.println("Action : " + attribute);
//			}
//		}
////		System.out.println();
////		System.out.println("Actions");
////		for(String a : actions)
////			System.out.println(a);
//	}
//
//	private ParserDemo() {
//	} // static methods only

//}
