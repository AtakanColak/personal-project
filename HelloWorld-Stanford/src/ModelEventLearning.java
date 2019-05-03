import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelEventLearning {

	private static double P(List<FabulaEvent> events, List<FabulaElement> actions, String action_name) {
		double ctr = 0;
		for(FabulaEvent e : events)
			if(actions.get(e.action_id).name.equals(action_name))
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
	
	private static Integer C(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b) {
		Integer coref_count = 0;
		for(Integer i1 = 0; i1 < events.size(); ++i1) {
			boolean i1isA = true;
			FabulaEvent e1 = events.get(i1);
			if(e1.action_id != a.action_id && e1.action_id != b.action_id) continue;
			if(e1.action_id == b.action_id) i1isA = false; 
			for(Integer i2 = i1; i2 < events.size(); ++i2) {
				FabulaEvent e2 = events.get(i2);
				if((e1.story_id != e2.story_id) || (i1isA && e2.action_id != b.action_id) || (!i1isA && e2.action_id != a.action_id)) continue;
				for(Integer aid : e1.subject_agent_ids)
					if(e2.subject_agent_ids.contains(aid)) {
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
		double d = 0;
		for (FabulaEvent e1 : events)
			for (FabulaEvent e2 : events) 
				d += C(events, e1, e2);
		return d;
	}

	private static double P(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b, double exyedf) {
		double n = C(events, a, b);
//		if(exyedf == 0) return 0;
		return (n / exyedf);
	} 
	
	private static double PMI(List<FabulaEvent> events, FabulaEvent a, FabulaEvent b, double exyedf, double Pa, double Pb) {
		double n = P(events, a, b, exyedf);
		double d = (Pa * Pb);
		double r = n / d;
		if(n == 0) return 0;
		double pmi = Math.log10(r);
		pmi = Math.abs(pmi);
//		if(((Double)pmi).isInfinite()) {
//			System.out.println("ERROR of INFINITY");
//			System.out.println("n is " + n);
//			System.out.println("d is " + d);
//			System.out.println("r is " + d);
//			System.out.println("pmi is " + pmi);
//		}
		return pmi;
	}
	
//	private static double ulnec_numerator_p(List<FabulaEvent> events, Integer story_count, FabulaEvent a,
//			FabulaEvent b) {
//		Integer numerator = ulnec_numerator_c(events, story_count, a, b);
//		Integer denominator = 0;
//		for (FabulaEvent e1 : events) {
//			for (FabulaEvent e2 : events) {
////				if(e1.id == e2.id && e1.story_id == e2.story_id) continue;
//				denominator += ulnec_numerator_c(events, story_count, e1, e2);
//			}
//		}
//		double n = (double) numerator;
//
//		double d = (double) denominator;
//		if (d == 0)
//			return 0;
//		return (1 / (d - n));
//	}

//	private static double pmi(List<FabulaElement> actions, List<FabulaEvent> events, Integer story_count, FabulaEvent a,
//			FabulaEvent b) {
//		double n = ulnec_numerator_p(events, story_count, a, b);
//		double d = occurence_probability(actions, events, a) * occurence_probability(actions, events, b);
//		return Math.abs(Math.log10(n / d));
//	}

	public static Map<Integer, Double> EP(FabulaEvent a, List<FabulaEvent> events, List<FabulaElement> actions) {
		//PRECOMPUTATION
		Double exyedf = EXYEDF(events);
		Map<Integer, Double> PS = new HashMap<>();
		for(FabulaEvent event : events) 
			PS.put(event.id, P(events, actions, actions.get(event.action_id).name));
		Map<Integer, Double> PMIs = new HashMap<>();
		for(FabulaEvent b : events) {
			if(a.id == b.id) continue;
			Double PMI = PMI(events, a, b, exyedf, PS.get(a.id), PS.get(b.id));
			
			PMIs.put(b.action_id, PMI);
		}
		Map<Integer, Double> SORTED = PMIs.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
		return SORTED;
	}
	
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
