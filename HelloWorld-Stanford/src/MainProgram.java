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

class MainProgram {

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
			HandleSentence(parse, ctr, 0);
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
		Agent a = new Agent(agents.size(), name);
		try {
			a = agents.get(index);
		} catch (Exception e) {
			agents.add(a);
		} finally {
			Integer pointer_index = -1;// IndexPointer(si, sd);
			if (pointer_index == -1) {
				agent_stack.add(new AgentPointer(a.id, si, sd));
			} else {
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

//	private static String GetNameFromNP(Tree np) {
//		boolean hasNN = Tools.HasPhrase(np, "NN") || Tools.HasPhrase(np, "NNP") || Tools.HasPhrase(np, "NNS");
//		if (!hasNN) {
//			return "NP";
//		}
//		List<Tree> nns = new ArrayList<Tree>();
//		Tools.GetPhrase(nns, np, new String[] { "NN", "NNP", "NNS" });
//		StringBuilder name_builder = new StringBuilder();
//		name_builder.append(nns.get(0).lastChild().label().toString());
//		for (int i = 1; i < nns.size(); ++i) {
//			name_builder.append(" " + nns.get(i).lastChild().label().toString());
//		}
//		return name_builder.toString();
//	}

	private static String GetNameFromNP(Tree[] np) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < np.length; ++i) {
			String tag = np[i].label().toString();
			switch (tag) {
			case "NP":
				return null;
			case "NN":
			case "NNP":
			case "NNS":
				if (sb.length() != 0)
					sb.append(" ");
				sb.append(np[i].children()[0].label().toString());
				break;
			default:
				break;
			}
		}
		return sb.toString();
	}

//	private static void HandleNP(Tree np, Integer si, Integer sd) {
//		String name = GetNameFromNP(np);
//		if (name.equals("NP")) {
////			System.out.println("REACHED NP");
//			List<Tree> subnps = new ArrayList<Tree>();
//			Tools.GetPhrase(subnps, np, new String[] { "NP" });
//			if (subnps.size() != 0)
//				for (Tree t : subnps)
//					HandleNP(t, si, sd + 1);
//			else {
////				System.out.println("REACHED PRP");
//				Tools.GetPhrase(subnps, np, new String[] { "PRP" });
//				if (subnps.size() != 0) {
//					for (Tree t : subnps) {
//						System.out.println("REACHED PRP");
//					}
////						nested_switch_prp(np, si, sd);
//				} else {
//					System.out.println("REACHED NON PRP NON NP NP");
//				}
//			}
//		} else {
//			AddPointer(name, si, sd);
//		}
//	}

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

//	private static void HandleVP(Tree vp, Integer si, Integer sd) {
//		Tree[] c = vp.children();
//		for (int i = 0; i < c.length; ++i) {
//			String tag = c[i].label().toString();
//			switch (tag) {
//			case "VBD":
//			case "VBG":
//				String verb = c[i].firstChild().label().toString();
//				Action(verb);
//				break;
//
//			case "VP":
//				HandleVP(c[i], si, sd + 1);
//				break;
//
//			case "NP":
//			case "PP":
//				String loc_nam = GetNameFromNP(c[i].lastChild());
//				if (tag.equals("NP"))
//					loc_nam = GetNameFromNP(c[i]);
//				List<Integer> agent_indices = GetSentenceNP(si - 1);
//				System.out.println("si is " + si);
//				for (Integer index : agent_indices) {
//					System.out.println("Has an index");
//					Agent a = agents.get(index);
//					Integer loc_index = Tools.AddLocation(locations, a.location, loc_nam);
//					a.location = loc_index;
//					agents.set(index, a);
//				}
//				break;
//			}
//		}
//	}

	private static void HandleSentenceVP(Tree c, Integer si, Integer sd) {
		for (int i = 0; i < c.children().length; ++i) {
			Tree child = c.children()[i];
			String tag = child.label().toString();
			switch (tag) {
			case "VBD":
			case "VBG":
			case "VB":
				Action(child.firstChild().label().toString());
				break;
			case "ADVP":
			case "PP":
				String loc_nam = GetNameFromNP(child.lastChild().children());
				List<Integer> agentsAtLocation = agent_stack.get(agent_stack.size() - 1).agent_index;
				for (Integer agent_index : agentsAtLocation) {
					Agent agent = agents.get(agent_index);
					Integer new_loc_id = Tools.AddLocation(locations, agent.location, loc_nam);
					agent.location = new_loc_id;
					agents.set(agent_index, agent);
					break;
				}

			case "VP":
				HandleSentenceVP(child, si, sd + 1);
				break;
			}
		}
	}

	private static void HandleSentenceNP(Tree c, Integer si, Integer sd) {
		String sen_np = GetNameFromNP(c.children());
		AgentPointer sentence_pointer = new AgentPointer(si, sd);
		// If it has child NPs, you can go below one more level
		if (sen_np == null) {
			for (int i = 0; i < c.children().length; ++i) {
				if (c.children()[i].label().toString().equals("NP")) {
					sen_np = GetNameFromNP(c.children()[i].children());
					int agent_index = Tools.AgentIndex(agents, sen_np);
					if (agent_index == -1) {
						agent_index = agents.size();
						Agent n = new Agent(agent_index, sen_np);
						agents.add(n);
					}
					sentence_pointer.agent_index.add(agent_index);
				}
			}
			agent_stack.add(sentence_pointer);
		} else if (!sen_np.equals(""))
			AddPointer(sen_np, si, sd);
	}

	private static void HandleSentence(Tree s, Integer si, Integer sd) {

		for (int i = 0; i < s.children().length; ++i) {
			Tree c = s.children()[i];
			String cTag = c.label().toString();
			switch (cTag) {
			case "ROOT":
			case "S":
			case "SBAR":
				HandleSentence(c, si, sd + 1);
				break;
			case "NP":
				HandleSentenceNP(c, si, sd);
				break;
//				HandleNP(c, si, 1);
			case "VP":
				HandleSentenceVP(c, si, 1);
				break;
			default:

				break;
			}
		}
//		si++;
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
		System.out.println(t.label().toString());
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
}
