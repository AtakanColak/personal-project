import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelEventLearning {

	private static double P(List<FabulaEvent> events, List<FabulaElement> actions, String action_name) {
		double ctr = 0;
		for (FabulaEvent e : events)
			if (actions.get(e.action_id).name.equals(action_name))
				ctr++;
		return (ctr / events.size());
	}

//	private static double occurence_probability(List<FabulaElement> actions, List<FabulaEvent> events, FabulaEvent a) {
//		Integer ctr = 0;
//		String aname = actions.get(a.action_id).name;
//		for (FabulaEvent e : events) {
//			if (actions.get(e.action_id).name.equals(aname) || (a.story_id == e.story_id && a.action_id == e.action_id))
//				ctr++;
//		}
//		return (((double) ctr) / (double) events.size());
//	}

	private static Map<Integer, List<Integer>> CS(List<FabulaEvent> events) {
		Map<Integer, List<Integer>> map = new HashMap();
		for (FabulaEvent e : events) {
			for (Integer agent : e.subject_agent_ids) {
				List<Integer> eventlist = map.get(agent);
				if (eventlist == null)
					eventlist = new ArrayList<Integer>();
				eventlist.add(e.action_id);
				map.put(agent, eventlist);
			}
		}
		return map;
	}

	private static Integer C(Map<Integer, List<Integer>> CS, FabulaEvent a, FabulaEvent b) {
		Integer coref_count = 1;
		for (Integer agent : CS.keySet()) {
			List<Integer> actionlist = CS.get(agent);
			if(actionlist.contains(a.action_id) && actionlist.contains(b.action_id)) coref_count++;
		}
		return coref_count;
	}

	private static Integer C(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b) {
		Integer coref_count = 1;
		for (Integer i1 = 0; i1 < events.size(); ++i1) {
			boolean i1isA = true;
			FabulaEvent e1 = events.get(i1);
			if (e1.action_id != a.action_id && e1.action_id != b.action_id)
				continue;
			if (e1.action_id == b.action_id)
				i1isA = false;
			for (Integer i2 = i1; i2 < events.size(); ++i2) {
				FabulaEvent e2 = events.get(i2);
				if ((e1.story_id != e2.story_id) || (i1isA && e2.action_id != b.action_id)
						|| (!i1isA && e2.action_id != a.action_id))
					continue;
				for (Integer aid : e1.subject_agent_ids)
					if (e2.subject_agent_ids.contains(aid)) {
						coref_count++;
						break;
					}
			}
		}
		return coref_count;
	}

//	private static Integer ulnec_numerator_c(List<FabulaEvent> events, Integer story_count, FabulaEvent a,
//			FabulaEvent b) {
//		Integer coref = 0;
////		for (Integer i = 0; i < story_count; ++i) {
//		for (FabulaEvent e : events) {
//			if (e.action_id != b.action_id)
//				continue;
//			for (FabulaEvent ea : events) {
//				if (ea.action_id != a.action_id || ea.story_id != e.story_id)
//					continue;
//				for (Integer x : e.subject_agent_ids) {
//					if (ea.subject_agent_ids.contains(x)) {
//						coref++;
//						break;
//					}
//				}
//			}
//		}
////		}
//		return coref;
//	}

	private static double EXYEDF(List<FabulaEvent> events) {
		Map<Integer, List<Integer>> CS = CS(events);
		double d = 0;
		for (FabulaEvent e1 : events)
			for (FabulaEvent e2 : events)
				d += C(CS, e1, e2);
		return d;
	}

	private static double P(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b, double exyedf) {
		double n = C(events, a, b);
//		if(exyedf == 0) return 0;
		return (n / exyedf);
	}

	private static double PMI(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b, double exyedf, double Pa,
			double Pb) {
		double n = P(events, a, b, exyedf);
		double d = (Pa * Pb);
		double r = n / d;
//		if(n == 0) return 0;
		double pmi = Math.log10(r);
		pmi = Math.abs(pmi);
		if (((Double) pmi).isInfinite()) {
			return 0;
//			System.out.println("ERROR of INFINITY");
//			System.out.println("n is " + n);
//			System.out.println("d is " + d);
//			System.out.println("r is " + d);
//			System.out.println("pmi is " + pmi);
		}
		return pmi;
	}
	
//	private static double PMI(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b, double exyedf, double Pa,
//			double Pb, Map<Integer, List<Integer>> CS) {
//		double n = (C(CS, a, b) / exyedf);//P(events, a, b, exyedf);
//		double d = (Pa * Pb);
//		double r = n / d;
////		if(n == 0) return 0;
//		double pmi = Math.log10(r);
//		pmi = Math.abs(pmi);
//		if (((Double) pmi).isInfinite()) {
//			return 0;
//		}
//		return pmi;
//	}

	private static Map<Integer, Double> PS(List<FabulaEvent> events) {
		Map<Integer, Double> map = new HashMap<>();
		for (FabulaEvent event : events) {
			Double d = map.get(event.action_id);
			if (d == null)
				d = (double) 0;
			d += 1;
			map.put(event.action_id, d);
		}
		for (Integer key : map.keySet()) {
			Double d = map.get(key);
			d /= events.size();
			map.put(key, d);
		}
		return map;
	}

	public static Map<Integer, Double> EP(FabulaEvent a, List<FabulaEvent> events, List<FabulaElement> actions) {
		// PRECOMPUTATION
		Double exyedf = EXYEDF(events);
		Map<Integer, Double> PS = PS(events);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date)); // 2016/11/16 12:08:43
		System.out.println("End of precomputation.");
		Map<Integer, Double> PMIs = new HashMap<>();
		List<Integer> already_calculated = new ArrayList<Integer>();
		for (FabulaEvent b : events) {
			if (already_calculated.contains(b.action_id) || a.id == b.id)
				continue;
			Double PMI = PMI(events, a, b, exyedf, PS.get(a.action_id), PS.get(b.action_id));
			PMIs.put(b.action_id, PMI);
		}
		Map<Integer, Double> SORTED = PMIs.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
		return SORTED;
	}

	public static Map<Integer, Map<Integer, Double>> AllEP(List<FabulaEvent> events,  List<FabulaElement> actions) {
		//PRECOMPUTATION
		Map<Integer, Map<Integer, Double>> list = new HashMap<>();
		Double exyedf = EXYEDF(events);
		Map<Integer, Double> PS = PS(events);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date)); // 2016/11/16 12:08:43
		System.out.println("End of precomputation.");
		//COMPUTATION
		double ctr = 0;
		for(FabulaEvent a : events) {
			if(list.containsKey(a.action_id)) continue;
			Map<Integer, Double> PMIs = new HashMap<>();
			for (FabulaEvent b : events) {
				if (PMIs.containsKey(b.action_id) || a.id == b.id)
					continue;
				Double PMI = PMI(events, a, b, exyedf, PS.get(a.action_id), PS.get(b.action_id));
				PMIs.put(b.action_id, PMI);
			}
			list.put(a.action_id, PMIs);
			ctr++;
			System.out.println(ctr + "/" + actions.size() + " is completed");
		}
		date = new Date();
		System.out.println(dateFormat.format(date)); // 2016/11/16 12:08:43
		System.out.println("End of Computation.");
		return list;
	}
	
//	public static Map<Integer, Map<Integer, Double>> AllProbabilities(List<FabulaEvent> events, List<FabulaElement> actions) {
//		Map<Integer, Map<Integer, Double>> superset = new HashMap<>();
//		float ctr = 0;
//		for(FabulaEvent e : events) {
//			superset.put(e.id, EP(e, events, actions));
//			ctr += 100;
//			System.out.println((ctr / events.size()) + "% done");
//		}
//		return null;
//	}

//	public static Map<Integer, Double> EventProbabilities(FabulaEvent a, List<FabulaEvent> es, List<FabulaElement> as,
//			Integer story_count) {
//		Map<Integer, Double> pmis = new HashMap<>();
//		for (FabulaEvent b : es) {
//			if (a.id == b.id)
//				continue;
//			double pmi = pmi(as, es, story_count, a, b);
//			pmis.put(b.action_id, pmi);
//		}
//		Map<Integer, Double> sorted_pmis = pmis.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
//		return sorted_pmis;
//	}

//	System.out.println("PMI values for " + actions.get(a.action_id).name);
//	
//	for (FabulaEvent e : events) {
//		if (a.id == e.id)
//			continue;
//		
//		
//	}
//	Map<Integer, Double> sorted_pmis = pmis.entrySet().stream().sorted(Map.Entry.comparingByValue())
//			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
//	for (Integer aid : sorted_pmis.keySet()) {
//		String format = "%-40s%s%n";
//		System.out.printf(format, actions.get(aid).name, sorted_pmis.get(aid).toString());
//	}
}
