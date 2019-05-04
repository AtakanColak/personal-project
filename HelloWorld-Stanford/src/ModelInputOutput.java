import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
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
		for (FabulaEvent e : es)
			System.out.println(e.toString(ag, ac));
	}

	public static void PrintEventProbabilities(String name, Map<Integer, Double> eps, List<FabulaElement> as) {
		Set<Integer> keyset = eps.keySet();
		List<String> reverse_printing = new ArrayList<String>();
		for (Integer key : keyset) {
			reverse_printing.add(String.format("%-20s%s%n", as.get(key).name, eps.get(key).toString()));
		}
		Collections.reverse(reverse_printing);
		System.out.println("Event probabilities with known event " + name);
		for (String s : reverse_printing)
			System.out.print(s);
//			System.out.printf("%-20s%s%n", as.get(key).name, eps.get(key).toString());

	}

//	public static void SaveEventProbabilities(String filename, Map<Integer, Double> probabilities) {
//		
//	}
	
	public static Object ReadObject(String filename) {
		try {
			File file = new File(filename);
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);
			Object o =  oi.readObject();
			oi.close();
			fi.close();
			return o;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public static void WriteObject(String filename, Object obj) {
		try {
		    File file = new File(filename);
		    file.createNewFile();
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);
			o.writeObject(obj);
			o.close();
			f.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
//	@SuppressWarnings("unchecked")
//	public static Map<Integer, Double> LoadEventProbabilities(String filename) {
//		
//	}
}
