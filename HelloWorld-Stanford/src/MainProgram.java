import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.*;

class MainProgram {

	// Doesn't do anything when a combination is reached as it will iterate through
	// and add characters
	// Uses a stack in order to point to corresponding agents and avoid repitition

	private static StringBuilder log;
	private static Integer logctr;
	private static Integer story_id;

	private static String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private static String path = "Aesop/005";
	private static String demades = "The orator Demades was trying to address his Athenian audience. When he failed to get their attention, he asked if he might tell them an Aesop's fable. The audience agreed, so Demades began his story. 'The goddess Demeter, a swallow, and an eel were walking together down the road. When they reached a river, the swallow flew up in the air and the eel jumped into the water.' Demades then fell silent. The audience asked, 'And what about the goddess Demeter?' 'As for Demeter,' Demades replied, 'she is angry at all of you for preferring Aesop's fables to politics!' ";

	private static List<AgentPointer> agent_stack;
	private static List<Agent> agents;
	private static List<FabulaElement> actions;
	private static List<FabulaElement> internals;
	private static List<Location> locations;
	private static List<FabulaEvent> events;
	private static List<Perception> perceptions;
	private static List<FabulaElement> goals;

	private static void InitLists() {
		log = new StringBuilder();
		logctr = 0;
		story_id = 0;
		agent_stack = new ArrayList<AgentPointer>();
		agents = new ArrayList<Agent>();
		actions = new ArrayList<FabulaElement>();
		internals = new ArrayList<FabulaElement>();
		events = new ArrayList<FabulaEvent>();
		perceptions = new ArrayList<Perception>();
		goals = new ArrayList<FabulaElement>();

		locations = new ArrayList<Location>();
		locations.add(new Location(0, "INIT"));
	}

	private static void PrintLists() {

//		ModelInputOutput.PrintIdentifierList(actions, "Action");
//		ModelInputOutput.PrintIdentifierList(internals, "Internal");
//		ModelInputOutput.PrintIdentifierList(goals, "Goal");
//		ModelInputOutput.PrintIdentifierList(locations, "Location");
//		ModelInputOutput.PrintIdentifierList(perceptions, "Perception");
//		ModelInputOutput.PrintEvents(events, agents, actions);
//		ModelInputOutput.PrintAgents(agents, internals, actions, locations);
//		ModelInputOutput.PrintAgentPointers(agent_stack, agents);
//		System.out.println(events.get(15).toString(agents, actions));
//		FabulaEvent selected = events.get(15);
//		Map<Integer, Double> EPS = ModelEventLearning.EP(selected, events, actions);
//		ModelInputOutput.WriteObject("asked.txt", EPS);
//		ModelInputOutput.PrintEventProbabilities(EPS, actions);
//		Map<Integer, Map<Integer, Double>> superlist = ModelEventLearning.AllProbabilities(events, actions);
//		ModelInputOutput.SaveEventProbabilities(actions.get(selected.action_id).name , EPS);
//		ModelInputOutput.PrintEventProbabilities(ModelEventLearning.EventProbabilities(events.get(15), events, actions, story_id + 1), actions);
//		System.out.print(log.toString());

//		FabulaEvent a = events.get(0);
//		
	}
	
	@SuppressWarnings("unchecked")
	private static void LoadData() {
//		agents = (List<Agent>) ModelInputOutput.ReadObject("Data/agents.txt");
		events  = (List<FabulaEvent>) ModelInputOutput.ReadObject("Data/events.txt");
		actions = (List<FabulaElement>) ModelInputOutput.ReadObject("Data/actions.txt");
	}

	private static void CalcPMI(FabulaEvent e) {
		Map<Integer, Double> list = ModelEventLearning.EP(e, events, actions);
		ModelInputOutput.WriteObject("PMI/" + actions.get(e.action_id).name + ".txt", list);
		ModelInputOutput.PrintEventProbabilities(list, actions);
	}
	
	private static void LoadPMI(FabulaEvent e) {
		Map<Integer, Double> list = (Map<Integer, Double>) ModelInputOutput.ReadObject("PMI/" + actions.get(e.action_id).name + ".txt");
		ModelInputOutput.PrintEventProbabilities(list, actions);
	}
	
	private static void SaveData() {
		ModelInputOutput.WriteObject("Data/agents.txt", agents);
		ModelInputOutput.WriteObject("Data/pointers.txt", agent_stack);
		ModelInputOutput.WriteObject("Data/goals.txt", goals);
		ModelInputOutput.WriteObject("Data/internals.txt", internals);
		ModelInputOutput.WriteObject("Data/actions.txt", actions);
		ModelInputOutput.WriteObject("Data/events.txt", events);
		ModelInputOutput.WriteObject("Data/locations.txt", locations);
		ModelInputOutput.WriteObject("Data/perceptions.txt", perceptions);
	}
	
	private static void ReadFables() {
		InitLists();
		NodeList nl = ModelInputOutput.ReadXMLNodeList("Aesop/aesop.xml", "fable");
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		// For each story
		for (int i = 0; i < nl.getLength(); i++) {
			int sictr = 0;
			// For each sentence
			for (List<HasWord> sentence : ModelInputOutput.ParseNodeString(nl.item(i))) {
				Tree parse = lp.apply(sentence);
//				parse.pennPrint();
				HandleSentence(parse, sictr, 0);
				sictr++;
			}
			story_id++;
		}
	}
	
	private static void Log(String name) {
		log.append("log[" + String.format("%4s", logctr.toString()) + "]: " + name + "\n");
		logctr++;
	}

	public static void main(String[] args) {
		int mode = 1;
		int eventid = 13;
//		Map<Integer, Double> asked = (Map<Integer, Double>) ModelInputOutput.ReadObject("asked.txt");
//		actions = (List<FabulaElement>) ModelInputOutput.ReadObject("actions.txt");
//		System.out.println("Probabilities for asked;");
//		ModelInputOutput.PrintEventProbabilities(asked, actions);
//		if (true) return;
		switch (mode) {
		case 0:
			ReadFables();
			SaveData();
			break;
		case 1: {
			LoadData();
			FabulaEvent e = events.get(eventid);
			CalcPMI(e);
			break;}
		case 2: {
			LoadData();
			FabulaEvent e = events.get(eventid);
			LoadPMI(e);
			break;}
		}
		
		System.out.println("End of program execution.");
	}

//	8th  of May - AI CW
//	13th of May - Individual Project
//	20th of May - AI Exam
//  22th of May - Returning Home
//	27th of May - Web Tech Subm

	private static void IdentifyAction(String word) {
		// Initialize event
		FabulaEvent event = new FabulaEvent(story_id);
		event.id = events.size();
		event.story_id = story_id;
		// Add action if it doesn't exist
		Integer actionIndex = ModelElementTools.Identify(actions, FabulaElement.ElementType.Action, word);
		Log(actions.get(actionIndex).toString("Action"));
		// Get last agent pointer and update their action list
		AgentPointer last_pointer = agent_stack.get(agent_stack.size() - 1);
		for (Integer index : last_pointer.agent_index) {
			event.subject_agent_ids.add(index);
			Agent a = agents.get(index);
			a.actions.add(actionIndex);
			agents.set(index, a);
		}
		// Add event
		event.action_id = actionIndex;
		events.add(event);
//		System.out.println(agents.size());
//		System.out.println(actions.size());
//		System.out.println(event.action_id);
//		System.out.println(event.subject_agent_ids.toString());
		Log(event.toString(agents, actions));
		// For each agent doing the action, get observers at their locations
		IdentifyPerceptions(event);
	}

	private static void IdentifyPerceptions(FabulaEvent e) {
		for (Integer s : e.subject_agent_ids) {
			Agent subject = agents.get(s);
			for (Agent a : agents) {
				// Skip if different story, if different location, if same agent, if already a
				// subject
				if (subject.story_id != a.story_id || subject.location != a.location || subject.id == a.id
						|| e.subject_agent_ids.contains(a.id))
					continue;
				Perception p = new Perception();
				p.id = perceptions.size();
				p.story_id = story_id;
				p.actionID = e.action_id;
				p.subjectID = s;
				p.observerID = a.id;
				p.name = actions.get(p.actionID).name + ", subject " + subject.name + ", observer " + a.name;
				perceptions.add(p);
				Log(p.toString("Perception"));
			}
		}
	}

//	private static void Action(String name) {
////		System.out.println("1");
//		FabulaEvent event = new FabulaEvent(story_id);
//		event.name = name + " by";
////		System.out.println("2");
//		Integer action_index = Tools.AddFabulaElement(actions, FabulaElement.ElementType.Action, name);
//
//		Log("Action : " + actions.get(action_index).name);
//		event.action_id = action_index;
////		System.out.println("3");
//		AgentPointer last_pointer = agent_stack.get(agent_stack.size() - 1);
////		System.out.println("4");
//		for (Integer index : last_pointer.agent_index) {
//			Agent a = agents.get(index);
//			a.actions.add(action_index);
//			agents.set(index, a);
//			event.name += " " + a.name;
//			event.subject_agent_ids.add(index);
//		}
//		Tools.AddToListThatExtendsIdentifier(events, event);
//		Log("Event : " + event.name);
//		for (Integer subject_agent : event.subject_agent_ids) {
////			System.out.println("subject_agent is " + agents.get(subject_agent).name + " at location " +  agents.get(subject_agent).location);
////			Tools.PrintAgents(agents, actions);
//			List<Integer> observers = Tools.AgentIndicesAtLocation(agents, agents.get(subject_agent).location); // Tools.ObserverAgentsAtLocation(agents,
//																												// event,
//																												// agents.get(subject_agent).location);
//			for (Integer observer : observers) {
//				if (observer == subject_agent)
//					continue;
//				Perception op = new Perception();
//				op.id = perceptions.size();
//				op.actionID = event.action_id;
//				op.subjectID = subject_agent;
//				op.observerID = observer;
//				
//				perceptions.add(op);
//				Log("Perception : " + op.name);
//			}
//		}
//	}

//	static int iactr = 0;
	private static void IdentifyAgent(String name, Integer si, Integer sd) {
		
		
		Integer agent_index = ModelElementTools.IndexByNameContains(agents, story_id, name);
		if (agent_index == null) {
			agent_index = agents.size();
			Agent n = new Agent(agent_index, name);
			n.story_id = story_id;
			agents.add(n);
			
			Log(agents.get(agent_index).toString("Agent"));
		}
		Agent a = agents.get(agent_index);
		AgentPointer ap = new AgentPointer(agent_index, si, sd);
		agent_stack.add(ap);
//		System.out.println("Agent Identify " + agents.size() + " " + agent_index);
//		iactr++;
		Log(ap.toString(agents));
	}

//	private static void AddPointer(String name, Integer si, Integer sd) {
//		Integer index = Tools.AgentIndex(agents, name);
//		Agent a = new Agent(agents.size(), name);
//		try {
//			a = agents.get(index);
//		} catch (Exception e) {
//			agents.add(a);
//			Log("Agent : " + a.name);
//		} finally {
//			Integer pointer_index = -1;// IndexPointer(si, sd);
//			if (pointer_index == -1) {
//				agent_stack.add(new AgentPointer(a.id, si, sd));
//
//			} else {
//				AgentPointer p = agent_stack.get(pointer_index);
//				p.agent_index.add(a.id);
//				agent_stack.set(pointer_index, p);
//			}
//			Log("Agent Pointer : " + agents.get(a.id).name);
//		}
//	}

	private static String GetName(Tree np, boolean goal) {
		Tree[] npc = np.children();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < npc.length; ++i) {
			String tag = npc[i].label().toString();
			switch (tag) {
			case "NP":
				if (!goal)
					return null;
			case "PP":
				if (sb.length() != 0)
					sb.append(" ");
				sb.append(GetName(npc[i], true));
				break;

			case "IN":
				if (!goal) {
					break;
				}
			case "EX":
			case "NN":
			case "NNPS":
			case "NNP":
			case "NNS":
				if (sb.length() != 0)
					sb.append(" ");
				sb.append(npc[i].children()[0].label().toString());
				break;
			}
		}
		return sb.toString();
	}

//	private static String GetNameFromNP(Tree[] np) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < np.length; ++i) {
//			String tag = np[i].label().toString();
//			switch (tag) {
//			case "NP":
//				return null;
//			case "EX":
//			case "NN":
//			case "NNPS":
//			case "NNP":
//			case "NNS":
//				if (sb.length() != 0)
//					sb.append(" ");
//				sb.append(np[i].children()[0].label().toString());
//				break;
//			default:
//				break;
//			}
//		}
//		return sb.toString();
//	}
//
//	private static String GetNameFromNPGoal(Tree nP) {
//		Tree[] np = nP.children();
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < np.length; ++i) {
//			String tag = np[i].label().toString();
//			switch (tag) {
//			case "PP":
//			case "NP":
//				if (sb.length() != 0)
//					sb.append(" ");
//				sb.append(GetNameFromNPGoal(np[i]));
//				break;
//			case "EX":
//			case "NN":
//			case "NNPS":
//			case "NNP":
//			case "NNS":
//			case "IN":
//				if (sb.length() != 0)
//					sb.append(" ");
//				sb.append(np[i].children()[0].label().toString());
//				break;
//			default:
//				break;
//			}
//		}
//		return sb.toString();
//	}

	private static void IdentifyGoal(String name) {
		Integer index = ModelElementTools.IndexByNameEquals(goals, name);
		if (index == null) {
			index = goals.size();
			FabulaElement goal = new FabulaElement();
			goal.name = name;
			goal.id = index;
			goal.type = FabulaElement.ElementType.Goal;
			goals.add(goal);
		}
		Log(goals.get(index).toString("Goal"));
	}

	private static void HandleSentenceVP(Tree c, Integer si, Integer sd) {
		for (int i = 0; i < c.children().length; ++i) {
			Tree child = c.children()[i];
			String tag = child.label().toString();
			switch (tag) {
			case "TO":
				try {
				Tree subVP = c.children()[i + 1];
				String verb = subVP.firstChild().firstChild().label().toString();
				String noun = GetName(subVP.children()[1], true);
				IdentifyGoal(verb + " " + noun);
				}
				catch (Exception e){
					System.out.println("Out of bounds at story " + story_id + " | sentence " + si );
				}
				finally {}
				break;
			case "VBZ":
			case "VBD":
			case "VBG":
			case "VBN":
			case "VB":
				IdentifyAction(child.firstChild().label().toString());
				break;
			case "ADVP":
			case "PP":
				String loc_nam = GetName(child.lastChild(), false);
				if (loc_nam == null || loc_nam.equals("")) {
					if (child.children().length > 0)
						HandleSentenceVP(child.lastChild(), si, sd + 1);
					break;
				}
				for (Integer agent_at_location : agent_stack.get(agent_stack.size() - 1).agent_index) {
					Agent agent = agents.get(agent_at_location);
					Integer new_loc_id = ModelElementTools.IdentifyLocation(locations, story_id, agent.location,
							loc_nam);
					Log("Location change : " + agent.name + " at " + locations.get(agent.location).name + " moved to "
							+ locations.get(new_loc_id).name);
					agent.location = new_loc_id;
					agents.set(agent_at_location, agent);
				}
				for (Object o : agents.toArray()) {
					Agent a = (Agent) o;
					if (a.name.contains(loc_nam)) {
						IdentifyAgent(a.name, si, sd);
					}
				}
				break;
			case "ADJP":
				String adj_nam = child.firstChild().firstChild().label().toString();
				Integer internal_index = ModelElementTools.IndexByNameEquals(internals, adj_nam);
				if (internal_index == null) {
					FabulaElement adj = new FabulaElement();
					adj.type = FabulaElement.ElementType.Internal;
					adj.id = internals.size();
					internal_index = adj.id;
					adj.name = adj_nam;
					internals.add(adj);
					Log(adj.toString("Internal"));
				}
				for (Integer agentID : agent_stack.get(agent_stack.size() - 1).agent_index) {
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

//	private static void HandleSentenceVP(Tree c, Integer si, Integer sd) {
//		for (int i = 0; i < c.children().length; ++i) {
//			Tree child = c.children()[i];
//			String tag = child.label().toString();
//			switch (tag) {
//			case "TO":
//				Tree subVP = c.children()[i + 1];
//				String verb = subVP.firstChild().firstChild().label().toString();
//				String NN = GetNameFromNPGoal(subVP.children()[1]);
////				if(NN == null) NN = GetNameFromNP(subVP.lastChild().firstChild().children());
//				FabulaElement goal = new FabulaElement();
//				goal.id = goals.size();
//				goal.name = verb + " " + NN;
//				goals.add(goal);
//				Log("Goal : " + goal.name);
//				break;
//			case "VBZ":
//			case "VBD":
//			case "VBG":
//			case "VBN":
//			case "VB":
//				Action(child.firstChild().label().toString());
//				break;
//			case "ADVP":
//			case "PP":
//				String loc_nam = GetNameFromNP(child.lastChild().children());
//				if (loc_nam == null || loc_nam.equals("")) {
//					if (child.children().length > 0)
//						HandleSentenceVP(child.lastChild(), si, sd + 1);
//					break;
//				}
//				;
//				List<Integer> agentsAtLocation = agent_stack.get(agent_stack.size() - 1).agent_index;
//				for (Integer agent_index : agentsAtLocation) {
//					Agent agent = agents.get(agent_index);
//					Integer new_loc_id = Tools.AddLocation(locations, agent.location, loc_nam);
//					Log("Location change : " + agent.name + " at " + locations.get(agent.location).name + " moved to "
//							+ locations.get(new_loc_id).name);
//					agent.location = new_loc_id;
//					agents.set(agent_index, agent);
////					break;
//				}
//				for (Agent a : agents) {
//					if (a.name.contains(loc_nam)) {
//						AddPointer(a.name, si, sd);
//					}
//				}
//				break;
//			case "ADJP":
//				String adj_nam = child.firstChild().firstChild().label().toString();
//				Integer internal_index = Tools.IndexAtListThatExtendsIdentifier(internals, adj_nam);
////				System.out.println("adjnam is " + adj_nam + " and internal index is " + internal_index);
//				if (internal_index == -1) {
//					FabulaElement adj = new FabulaElement();
//					adj.type = FabulaElement.ElementType.Internal;
//					adj.id = internals.size();
//					internal_index = adj.id;
////					System.out.println("new id is " + adj.id);
//					adj.name = adj_nam;
//					internals.add(adj);
//					Log("Internal : " + adj.name);
//				}
//				for (Integer agentID : agent_stack.get(agent_stack.size() - 1).agent_index) {
////					System.out.println("agentID is " + agentID);
//					Agent a = agents.get(agentID);
//					a.internals.add(internal_index);
//					agents.set(agentID, a);
//				}
//				break;
//			case "SBAR":
//			case "S":
//			case "VP":
//				HandleSentenceVP(child, si, sd + 1);
//				break;
//			}
//		}
//	}

	private static void HandleSentenceNP(Tree c, Integer si, Integer sd) {
		if (c.firstChild().label().toString().equals("PRP") && c.children().length == 1 && agent_stack.size() == 0) {
			IdentifyAgent(c.firstChild().firstChild().label().toString(), si, sd);
		}
		if (c.firstChild().firstChild().label().toString().equals("I")) {
			for (int i = agent_stack.size() - 2; i > -1; i--) {
				if ((agent_stack.get(i).sentence_index == si - 1 || (agent_stack.get(i).sentence_index == si))
						&& agent_stack.get(i).agent_index.size() == 1) {
					IdentifyAgent(agents.get(agent_stack.get(i).agent_index.get(0)).name, si, sd);
					break;
				}
			}
		}
		String sen_np = GetName(c, false);
		AgentPointer sentence_pointer = new AgentPointer(si, sd);
		// If it has child NPs, you can go below one more level
		if (sen_np == null) {
			for (int i = 0; i < c.children().length; ++i) {
				if (c.children()[i].label().toString().equals("NP")) {
					sen_np = GetName(c.children()[i], false);
					if (sen_np == null)
						continue;
					Integer agent_index = ModelElementTools.IndexByNameContains(agents, story_id, sen_np);//Tools.AgentIndex(agents, sen_np);
					if (agent_index == null) {
						agent_index = agents.size();
						Agent n = new Agent(agent_index, sen_np);
						n.story_id = story_id;
						agents.add(n);
						Log(agents.get(agent_index).toString("Agent"));
					}
//					System.out.println("Reference to" + agent_index);
					sentence_pointer.agent_index.add(agent_index);
				}
			}
			agent_stack.add(sentence_pointer);
//			StringBuilder sbi = new StringBuilder();
//			sbi.append("Agent Pointer : ");
//			for (Integer aid : sentence_pointer.agent_index) {
//				sbi.append(agents.get(aid).name + ", ");
//			}
			Log(sentence_pointer.toString(agents));
		} else if (!sen_np.equals(""))
			IdentifyAgent(sen_np, si, sd);
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
