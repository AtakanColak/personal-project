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

	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

		agent_stack = new ArrayList<AgentPointer>();
		agents = new ArrayList<Agent>();
		actions = new ArrayList<FabulaElement>();
		internals = new ArrayList<FabulaElement>();

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
		Tools.PrintFabulaList(actions, "actions");
		Tools.PrintAgents(agents, actions);
		Tools.PrintStack(agent_stack, agents);
	}

	
	private static void AddAction(String name) {
		Integer action_index = Tools.ElementListIndex(actions, name);
		if (action_index == -1) {
			FabulaElement a = new FabulaElement();
			a.name = name;
			a.id = actions.size();
			a.type = FabulaElement.ElementType.Action;
			actions.add(a);
			action_index = a.id;
		}
		Integer retaring_index = agent_stack.size() - 1;
		Integer last_depth = agent_stack.get(retaring_index).sentence_depth;
		while(retaring_index >= 0) {
			AgentPointer pointer = agent_stack.get(retaring_index);
			if(pointer.sentence_depth != last_depth) break;
			for (Integer agent_index : pointer.agent_index) {
				Agent agent = agents.get(agent_index);
				agent.actions.add(action_index);
				agents.set(agent_index, agent);
			}	
			retaring_index--;
		}
		
	}

	private static void AddPointer(String name, Integer si, Integer sd) {
		Integer index = Tools.AgentIndex(agents, name);
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
			nested_switch_np(t, si, sd);
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
