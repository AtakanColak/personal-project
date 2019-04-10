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
	private static List<FabulaElement> actions;
	private static List<FabulaElement> internals;
	private static List<Location> locations;

	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

		agent_stack = new ArrayList<AgentPointer>();
		agents = new ArrayList<Agent>();
		actions = new ArrayList<FabulaElement>();
		internals = new ArrayList<FabulaElement>();
		locations = new ArrayList<Location>();
		locations.add(new Location(0, "INIT"));

		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}
		int ctr = 0;
		for (List<HasWord> sentence : new DocumentPreprocessor(path)) {
			Tree parse = lp.apply(sentence);
			parse.pennPrint();
//			Formalise(parse, ctr, 0);
			HandleSentence(parse, ctr);
			ctr++;

		}
		Tools.PrintFabulaList(actions, "actions");
		Tools.PrintAgents(agents, actions);
		Tools.PrintLocations(locations);
		Tools.PrintStack(agent_stack, agents);
	}

//	3rd of May  - AI CW
//	13th of May - Individual Project
//	20th of May - AI Exam
//	27th of May - Web Tech Subm

	private static void AddAction(String name) {
		Integer index = Tools.AddFabulaElement(actions, FabulaElement.ElementType.Action, name);
		Integer retaring_index = agent_stack.size() - 1;
		Integer last_depth = agent_stack.get(retaring_index).sentence_depth;
		while (retaring_index >= 0) {
			AgentPointer pointer = agent_stack.get(retaring_index);
			if (pointer.sentence_depth != last_depth)
				break;
			for (Integer agent_index : pointer.agent_index) {
				Agent agent = agents.get(agent_index);
				agent.actions.add(index);
				agents.set(agent_index, agent);
			}
			retaring_index--;
		}
	}

	private static void Action(String name) {
		Integer action_index = Tools.AddFabulaElement(actions, FabulaElement.ElementType.Action, name);
		AgentPointer last_pointer = agent_stack.get(agent_stack.size() - 1);
		for (Integer index : last_pointer.agent_index) {
			Agent a = agents.get(index);
			a.actions.add(action_index);
			agents.set(index, a);
		}
	}

	private static Integer IndexPointer(Integer si, Integer sd) {
		for (int i = 0; i < agent_stack.size(); ++i) {
			if (agent_stack.get(i).sentence_depth == sd && agent_stack.get(i).sentence_index == si)
				return i;
		}
		return -1;
	}

	private static void AddPointer(String name, Integer si, Integer sd) {
		Integer index = Tools.AgentIndex(agents, name);
		Agent a = new Agent(agents.size(), name) ;
		try {
			a = agents.get(index);
		}
		catch (Exception e) {
			agents.add(a);
		}
		finally {
			Integer pointer_index = IndexPointer(si, sd);
			if(pointer_index == -1) {
				agent_stack.add(new AgentPointer(a.id, si, sd));
			}
			else {
				AgentPointer p = agent_stack.get(pointer_index);
				p.agent_index.add(a.id);
				agent_stack.set(pointer_index, p);
			}
		}
	}

	private static String LabelOfLastChild(Tree t) {
		List<Tree> children = t.getChildrenAsList();
		Tree last = children.get(children.size() - 1);
		return last.getChild(0).label().toString();
	}

	private static String GetNameFromNP(Tree np) {
		boolean hasNN = Tools.HasPhrase(np, "NN") || Tools.HasPhrase(np, "NNP") || Tools.HasPhrase(np, "NNS");
		if (!hasNN) {
			return "NP";
		}
		List<Tree> nns = new ArrayList<Tree>();
		Tools.GetPhrase(nns, np, new String[] { "NN", "NNP", "NNS" });
		StringBuilder name_builder = new StringBuilder();
		name_builder.append(nns.get(0).lastChild().label().toString());
		for (int i = 1; i < nns.size(); ++i) {
			name_builder.append(" " + nns.get(i).lastChild().label().toString());
		}
		return name_builder.toString();
	}

	private static void HandleNP(Tree np, Integer si, Integer sd) {
		String name = GetNameFromNP(np);
		if (name.equals("NP")) {
			List<Tree> subnps = new ArrayList<Tree>();
			Tools.GetPhrase(subnps, np, new String[] { "NP" });
			if (subnps.size() != 0)
				for (Tree t : subnps)
					HandleNP(t, si, sd + 1);
			else {
				Tools.GetPhrase(subnps, np, new String[] { "PRP" });
				if (subnps.size() != 0) {
					for (Tree t : subnps)
						nested_switch_prp(np, si, sd);
				} else {
					System.out.println("REACHED NON PRP NON NP NP");
				}
			}
		} else {
			AddPointer(name, si, sd);
		}
	}

	private static List<Integer> GetSentenceNP(Integer si) {
		List<Integer> indices = new ArrayList<Integer>();
		for (AgentPointer ap : agent_stack) {
			if (ap.sentence_index == si && ap.sentence_depth == 2) {
				for (Integer index : ap.agent_index)
					indices.add(index);
			}
		}
		return indices;
	}

	private static void HandleVP(Tree vp, Integer si, Integer sd) {
		Tree[] c = vp.children();
		for (int i = 0; i < c.length; ++i) {
			String tag = c[i].label().toString();
			switch (tag) {
			case "VBD":
			case "VBG":
				String verb = c[i].firstChild().label().toString();
				Action(verb);
				break;

			case "VP":
				HandleVP(c[i], si, sd + 1);
				break;

			case "NP":
				HandleNP(c[i], si, sd + 1);
				break;

			case "PP":
				String loc_nam = GetNameFromNP(c[i].lastChild());
				Integer loc_id = locations.size();
				locations.add(new Location(loc_id, loc_nam));
				List<Integer> agent_indices = GetSentenceNP(si);
				for (Integer index : agent_indices) {
					Agent a = agents.get(index);
					a.location = loc_id;
					agents.set(index, a);
				}
				break;
			}
		}
	}

	private static void HandleSentence(Tree s, Integer si) {
		boolean hasS = Tools.HasPhrase(s, "S");
		if (hasS) {
			List<Tree> Ss = new ArrayList<Tree>();
			Tools.GetPhrase(Ss, s, "S");
			for (Tree c : Ss)
				HandleSentence(c, si++);
			return;
		}
		boolean hasNP = Tools.HasPhrase(s, "NP");
		if (hasNP) {
			List<Tree> nps = new ArrayList<Tree>();
			Tools.GetPhrase(nps, s, "NP");
			for (Tree c : nps)
				HandleNP(c, si, 1);

		} else {
			System.out.println("THIS SENTENCE DOESNT HAVE NP");
		}

		boolean hasVP = Tools.HasPhrase(s, "VP");
		if (hasVP) {
			List<Tree> vps = new ArrayList<Tree>();
			Tools.GetPhrase(vps, s, "VP");
			for (Tree c : vps)
				HandleVP(c, si, 1);
		} else {
			System.out.println("THIS SENTENCE DOESNT HAVE VP");
		}
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
			// GET LAST SENTENCE
			AgentPointer last_pointer = agent_stack.get(agent_stack.size() - 1);
			Integer last_sentence_index = last_pointer.sentence_index;
			Integer min_depth = last_pointer.sentence_depth;
			// GET MINIMUM DEPTH
			for (int i = 0; i < agent_stack.size(); ++i) {
				AgentPointer pointer = agent_stack.get(i);
				if (pointer.sentence_index != last_sentence_index)
					continue;
				if (pointer.sentence_depth < min_depth)
					min_depth = pointer.sentence_depth;
			}
			// ADD POINTER TO ALL OF THEM
			AgentPointer motherpointer = new AgentPointer();
			motherpointer.sentence_index = si;
			motherpointer.sentence_depth = sd;
			for (int i = 0; i < agent_stack.size(); ++i) {
				AgentPointer pointer = agent_stack.get(i);
				if (pointer.sentence_index == last_sentence_index && pointer.sentence_depth == min_depth) {
					for (Integer integer : pointer.agent_index)
						motherpointer.agent_index.add(integer);
				}
			}
			agent_stack.add(motherpointer);
			break;
		}
	}

	private static void nested_switch_np(Tree t, Integer si, Integer sd) {
		String result = NPsNN(t);
		switch (result) {
		case "NP":
			break;
		case "PRP":
			nested_switch_prp(t, si, sd);
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
			HandleNP(t, si, sd);
			// nested_switch_np(t, si, sd);
		}
			break;
		case "VBG":
		case "VBD": {
			String action_name = t.getChild(0).label().toString();

			AddAction(action_name);
		}
			break;
		}
		for (Tree c : t.getChildrenAsList())
			Formalise(c, si, sd + 1);
	}
}
