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
	private static List<FabulaEvent> events;
	
	private static void InitLists() {
		agent_stack = new ArrayList<AgentPointer>();
		agents = new ArrayList<Agent>();
		actions = new ArrayList<FabulaElement>();
		internals = new ArrayList<FabulaElement>();
		events = new ArrayList<FabulaEvent>();
		
		locations = new ArrayList<Location>();
		locations.add(new Location(0, "INIT"));
	}
	
	private static void PrintLists() {
		Tools.PrintAgents(agents, actions);
		Tools.PrintListThatExtendsIdentifier(actions, "actions");
		Tools.PrintListThatExtendsIdentifier(locations, "locations");
		Tools.PrintStack(agent_stack, agents);
		Tools.PrintListThatExtendsIdentifier(events, "events");
	}
	
	public static void main(String[] args) {
		InitLists();
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		TreebankLanguagePack tlp = lp.treebankLanguagePack();
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}
		int ctr = 0;
		for (List<HasWord> sentence : new DocumentPreprocessor(path)) {
			Tree parse = lp.apply(sentence);
			parse.pennPrint();
			HandleSentence(parse, ctr, 0);
			ctr++;
		}
		PrintLists();
	}

//	3rd of May  - AI CW
//	13th of May - Individual Project
//	20th of May - AI Exam
//	27th of May - Web Tech Subm

	private static void Action(String name) {
		FabulaEvent event = new FabulaEvent();
		event.name = name + " by";
		Integer action_index = Tools.AddFabulaElement(actions, FabulaElement.ElementType.Action, name);
		event.action_id = action_index;
		AgentPointer last_pointer = agent_stack.get(agent_stack.size() - 1);
		for (Integer index : last_pointer.agent_index) {
			Agent a = agents.get(index);
			a.actions.add(action_index);
			agents.set(index, a);
			event.name += " " + a.name;
			event.subject_agent_ids.add(index);
		}
		Tools.AddToListThatExtendsIdentifier(events, event);
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


}
