package pl.edu.agh.kis.dataretrieval.configuration.searching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CaseData {

	private Map<String, List<String>> values = new HashMap<String, List<String>>();
	private boolean defaultCase = false;

	public static CaseData getDefaultCaseData() {
		CaseData caseData = new CaseData();
		caseData.setDefaultCase(true);
		return caseData;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CaseData) {
			if (defaultCase == true && ((CaseData) obj).isDefaultCase() == true) {
				return true;
			} else {
				return compareValues(((CaseData) obj).getValues());
			}
		} else {
			return false;
		}
	}

	private boolean compareValues(Map<String, List<String>> otherValues) {
		for (Entry<String, List<String>> entry : values.entrySet()) {
			if (otherValues.containsKey(entry.getKey())) {
				if (!otherValues.get(entry.getKey()).containsAll(
						entry.getValue())) { // np checkboxy moga zawietaæ kilka
												// wartosci dlatego containsAll
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	// Odwrotna wersja porownania (przy porownianiu map.get)
	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// private boolean compareValues(Map obj){
	// Set<Entry> set = obj.entrySet();
	// for (Entry entry: set){
	// if (entry.getKey() instanceof String) {
	// if( values.containsKey(entry.getKey())){
	// if (entry.getValue() instanceof Collection){
	// if (!values.get(entry.getKey()).containsAll((Collection<?>)
	// entry.getValue())){
	// return false;
	// }
	// }else if(entry.getValue() instanceof String){
	// if (!values.get(entry.getKey()).contains(entry.getValue())){
	// return false;
	// }
	// }else{
	// return false;
	// }
	// }
	// }else{
	// return false;
	// }
	// }
	// return true;
	// }

	public CaseData() {
		super();
		this.values = new HashMap<String, List<String>>();
	}

	public CaseData(Map<String, List<String>> values) {
		super();
		this.values = values;
	}

	public Map<String, List<String>> getValues() {
		return values;
	}

	public void setValues(Map<String, List<String>> values) {
		this.values = values;
	}

	public void addValues(Map<String, List<String>> values) {
		if (this.values != null) {
			this.values.putAll(values);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void addValue(String fieldRef, List<String> fieldValues) {
		values.put(fieldRef, fieldValues);
	}

	public boolean isDefaultCase() {
		return defaultCase;
	}

	public void setDefaultCase(boolean defaultCase) {
		this.defaultCase = defaultCase;
	}

}
