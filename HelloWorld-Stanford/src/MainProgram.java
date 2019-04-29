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

	private static StringBuilder log;
	private static Integer logctr;
	
	private static String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private static String path = "Aesop/001";
	
	private static List<AgentPointer> agent_stack;
	private static List<Agent> agents;
	private static List<FabulaElement> actions;
	private static List<FabulaElement> internals;
	private static List<Location> locations;
	private static List<FabulaEvent> events;
	private static List<OutcomePerception> perceptions;
	private static List<FabulaElement> goals;

	private static void InitLists() {
		log = new StringBuilder();
		logctr = 0;
		agent_stack = new ArrayList<AgentPointer>();
		agents = new ArrayList<Agent>();
		actions = new ArrayList<FabulaElement>();
		internals = new ArrayList<FabulaElement>();
		events = new ArrayList<FabulaEvent>();
		perceptions = new ArrayList<OutcomePerception>();
		goals = new ArrayList<FabulaElement>();
		
		locations = new ArrayList<Location>();
		locations.add(new Location(0, "INIT"));
	}

	private static void PrintLists() {
//		Tools.PrintListThatExtendsIdentifier(internals, "internals");
//		Tools.PrintAgents(agents, actions, internals);
//		Tools.PrintListThatExtendsIdentifier(actions, "actions");
//		Tools.PrintListThatExtendsIdentifier(locations, "locations");
//		Tools.PrintStack(agent_stack, agents);
//		Tools.PrintListThatExtendsIdentifier(goals, "goals");
//		Tools.PrintListThatExtendsIdentifier(events, "events");
//		Tools.PrintListThatExtendsIdentifier(perceptions, "perception");
		System.out.print(log.toString());
	}
	
	private static void Log(String name) {
		Integer hundreds = (logctr / 100);
		Integer tens = (logctr % 100) / 10;
		Integer ones = logctr % 10;
		log.append( "log[" + hundreds.toString() + tens.toString() + ones.toString() + "]: " + name + "\n"); 
		logctr++;
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

//	3rd  of May - AI CW
//	13th of May - Individual Project
//	20th of May - AI Exam
//  22th of May - Returning Home
//	27th of May - Web Tech Subm

	private static void Action(String name) {
//		System.out.println("1");
		FabulaEvent event = new FabulaEvent();
		event.name = name + " by";
//		System.out.println("2");
		Integer action_index = Tools.AddFabulaElement(actions, FabulaElement.ElementType.Action, name);
	
		Log("Action : " + actions.get(action_index).name);
		event.action_id = action_index;
//		System.out.println("3");
		AgentPointer last_pointer = agent_stack.get(agent_stack.size() - 1);
//		System.out.println("4");
		for (Integer index : last_pointer.agent_index) {
			Agent a = agents.get(index);
			a.actions.add(action_index);
			agents.set(index, a);
			event.name += " " + a.name;
			event.subject_agent_ids.add(index);
		}
		Tools.AddToListThatExtendsIdentifier(events, event);
		Log("Event : " + event.name);
		for (Integer subject_agent : event.subject_agent_ids) {
//			System.out.println("subject_agent is " + agents.get(subject_agent).name + " at location " +  agents.get(subject_agent).location);
//			Tools.PrintAgents(agents, actions);
			List<Integer> observers = Tools.AgentIndicesAtLocation(agents, agents.get(subject_agent).location); // Tools.ObserverAgentsAtLocation(agents,
																												// event,
																												// agents.get(subject_agent).location);
			for (Integer observer : observers) {
				if (observer == subject_agent)
					continue;
				OutcomePerception op = new OutcomePerception();
				op.id = perceptions.size();
				op.actionID = event.action_id;
				op.subjectID = subject_agent;
				op.observerID = observer;
				op.name = actions.get(op.actionID).name + ", subject is " + agents.get(subject_agent).name
						+ ", observed by " + agents.get(observer).name;
				perceptions.add(op);
				Log("Perception : " + op.name);
			}
		}
	}

	private static void AddPointer(String name, Integer si, Integer sd) {
		Integer index = Tools.AgentIndex(agents, name);
		Agent a = new Agent(agents.size(), name);
		try {
			a = agents.get(index);
		} catch (Exception e) {
			agents.add(a);
			Log("Agent : " + a.name);
		} finally {
			Integer pointer_index = -1;// IndexPointer(si, sd);
			if (pointer_index == -1) {
				agent_stack.add(new AgentPointer(a.id, si, sd));
				
			} else {
				AgentPointer p = agent_stack.get(pointer_index);
				p.agent_index.add(a.id);
				agent_stack.set(pointer_index, p);
			}
			Log("Agent Pointer : " + agents.get(a.id).name);
		}
	}

	private static String GetNameFromNP(Tree[] np) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < np.length; ++i) {
			String tag = np[i].label().toString();
			switch (tag) {
			case "NP":
				return null;
			case "EX":
			case "NN":
			case "NNPS":
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
	
	private static String GetNameFromNPGoal(Tree nP) {
		Tree[] np = nP.children();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < np.length; ++i) {
			String tag = np[i].label().toString();
			switch (tag) {
			case "PP":
			case "NP":
				if (sb.length() != 0)
					sb.append(" ");
				sb.append(GetNameFromNPGoal(np[i]));
				break;
			case "EX":
			case "NN":
			case "NNPS":
			case "NNP":
			case "NNS":
			case "IN":
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
			case "TO":
				Tree subVP = c.children()[i + 1];
				String verb = subVP.firstChild().firstChild().label().toString();
				String NN = GetNameFromNPGoal(subVP.children()[1]);
//				if(NN == null) NN = GetNameFromNP(subVP.lastChild().firstChild().children());
				FabulaElement goal = new FabulaElement();
				goal.id = goals.size();
				goal.name = verb + " " + NN;
				goals.add(goal);
				Log("Goal : " + goal.name);
				break;
			case "VBZ":
			case "VBD":
			case "VBG":
			case "VBN":
			case "VB":
				Action(child.firstChild().label().toString());
				break;
			case "ADVP":
			case "PP":
				String loc_nam = GetNameFromNP(child.lastChild().children());
				if(loc_nam == null || loc_nam.equals("")) {if (child.children().length > 0)HandleSentenceVP(child.lastChild(), si, sd+1); break;};
				List<Integer> agentsAtLocation = agent_stack.get(agent_stack.size() - 1).agent_index;
				for (Integer agent_index : agentsAtLocation) {
					Agent agent = agents.get(agent_index);
					Integer new_loc_id = Tools.AddLocation(locations, agent.location, loc_nam);
					Log("Location change : " + agent.name + " at " + locations.get(agent.location).name +" moved to "+ locations.get(new_loc_id).name);
					agent.location = new_loc_id;
					agents.set(agent_index, agent);
//					break;
				}
				for(Agent a : agents) {
					if(a.name.contains(loc_nam)) {
						AddPointer(a.name, si, sd);
					}
				}
				break;
			case "ADJP":
				String adj_nam = child.firstChild().firstChild().label().toString();
				Integer internal_index = Tools.IndexAtListThatExtendsIdentifier(internals, adj_nam);
//				System.out.println("adjnam is " + adj_nam + " and internal index is " + internal_index);
				if (internal_index == -1) {
					FabulaElement adj = new FabulaElement();
					adj.type = FabulaElement.ElementType.Internal;
					adj.id = internals.size();
					internal_index = adj.id;
//					System.out.println("new id is " + adj.id);
					adj.name = adj_nam;
					internals.add(adj);
					Log("Internal : " + adj.name);
				}
				for(Integer agentID : agent_stack.get(agent_stack.size() - 1).agent_index) {
//					System.out.println("agentID is " + agentID);
					Agent a = agents.get(agentID);
					a.internals.add(internal_index);
					agents.set(agentID, a);
				}
				break;
			case "SBAR":
			case "S":
			case "VP":
				HandleSentenceVP(child, si, sd + 1);
				break;
			}
		}
	}

	private static void HandleSentenceNP(Tree c, Integer si, Integer sd) {
		if(c.firstChild().label().toString().equals("PRP") && c.children().length == 1 && agent_stack.size() == 0) {
			AddPointer(c.firstChild().firstChild().label().toString(), si, sd);
		}
		if(c.firstChild().firstChild().label().toString().equals("I")) {
			for(int i = agent_stack.size() - 2; i > -1; i--) {
				if((agent_stack.get(i).sentence_index == si - 1 || (agent_stack.get(i).sentence_index == si)) && agent_stack.get(i).agent_index.size() == 1) {
					AddPointer(agents.get(agent_stack.get(i).agent_index.get(0)).name, si, sd);
					break;
				}
			}
		}
		String sen_np = GetNameFromNP(c.children());
		AgentPointer sentence_pointer = new AgentPointer(si, sd);
		// If it has child NPs, you can go below one more level
		if (sen_np == null) {
			for (int i = 0; i < c.children().length; ++i) {
				if (c.children()[i].label().toString().equals("NP")) {
					sen_np = GetNameFromNP(c.children()[i].children());
					if(sen_np == null) continue;
					int agent_index = Tools.AgentIndex(agents, sen_np);
					if (agent_index == -1) {
						agent_index = agents.size();
						Agent n = new Agent(agent_index, sen_np);
						agents.add(n);
						Log("Agent : " + n.name);
					}
					sentence_pointer.agent_index.add(agent_index);
				}
			}
			agent_stack.add(sentence_pointer);
			StringBuilder sbi = new StringBuilder();
			sbi.append("Agent Pointer : " );
			for(Integer aid :sentence_pointer.agent_index) {
				sbi.append(agents.get(aid).name + ", ");
			}
			Log(sbi.toString());
		} else if (!sen_np.equals(""))
			AddPointer(sen_np, si, sd);
	}

	private static void HandleSentence(Tree s, Integer si, Integer sd) {

		for (int i = 0; i < s.children().length; ++i) {
			Tree c = s.children()[i];
			String cTag = c.label().toString();
			switch (cTag) {
			case "PRN":
			case "PP":
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
