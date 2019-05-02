import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.process.DocumentPreprocessor;

public class ModelInputOutput {

	public static NodeList ReadXMLNodeList(String filepath, String nodename) {
		try {
			File file = new File(filepath);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList nl = document.getElementsByTagName(nodename);
			return nl;
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {

		}
		return null;
	}

	public static DocumentPreprocessor ParseNodeString(org.w3c.dom.Node item) {
		Reader reader = new StringReader(item.getTextContent());
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		return dp;
	}

	public static <E extends Identifier> void PrintIdentifierList(List<E> l, String typename) {
		for (E e : l)
			System.out.println(e.toString(typename));
	}

	public static void PrintAgents(List<Agent> agents, List<FabulaElement> internals, List<FabulaElement> actions,
			List<Location> locations) {
		for (Agent a : agents)
			System.out.print(a.toString(internals, actions, locations));
	}

	public static void PrintAgentPointers(List<AgentPointer> ps, List<Agent> a) {
		for (AgentPointer p : ps)
			System.out.println(p.toString(a));
	}
	
	public static void PrintEvents(List<FabulaEvent> es, List<Agent> ag, List<FabulaElement> ac) {
		for(FabulaEvent e : es)
			System.out.println(e.toString(ag, ac));
	}

	public static void PrintEventProbabilities(Map<Integer, Double> eps, List<FabulaElement> as) {
		Set<Integer> keyset = eps.keySet();
		for(int i = keyset.size() - 1; i >= 0; --i) 
			System.out.printf("%-20s%s%n", as.get(i).name, eps.get(i).toString());
	}
}
