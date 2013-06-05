/**
 * 
 */
package org.tiago.bobby.graphdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tiago.bobby.types.Person;
import org.tiago.bobby.types.Suggestion;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query.Compare;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author tfr_souza
 *
 */
public class ManagedBobbyGraphDB {

	private TitanGraph bobbyGraph;
	
	public ManagedBobbyGraphDB() {
		bobbyGraph = SimpleBobbyGraphDBFactory.getInstance();
	}
	
	public String addPerson(long id, String name) {
		Person exists = load(id);
		if (id < 0) return "INVALID ID: NEGATIVE";
		if (exists.getId() == -1) {
			Vertex person = bobbyGraph.addVertex(null);
			person.setProperty("facebook_id", id);
			person.setProperty("name", name);
			bobbyGraph.commit();
			return "CREATED 201";
		} else {
			return "ID ALREADY EXISTS";
		}
	}
	
	public String addFriend(long id, long idFriend) {
//		Vertex person = bobbyGraph.query().has("facebook_id", Compare.EQUAL, id).vertices().iterator().next();
		Vertex person = load(new Long(id));
//		Vertex personFriend = bobbyGraph.query().has("facebook_id", Compare.EQUAL, idFriend).vertices().iterator().next();
		Vertex personFriend = load(new Long(idFriend));
		
		if (person == null) return "PERSON NOT FOUND";
		if (personFriend == null) return "FRIEND NOT FOUND";
		
		person.addEdge("knows", personFriend);
		personFriend.addEdge("knows", person);		
		bobbyGraph.commit();
		return "CREATED 201";
	}
	
	public List<Person> listFriends(long id) {
		Vertex person = load(new Long(id));
		List<Person> list = new ArrayList<Person>();
//		Vertex friend = null;
		Person personF = null;
		
//		for (Edge edge : person.query().direction(Direction.OUT).labels("knows").edges()) {
		for (Vertex friend : person.getVertices(Direction.OUT, "knows")) {
//			friend = edge.getVertex(Direction.IN);
			personF = new Person((String)friend.getProperty("name"), (Long)friend.getProperty("facebook_id"));
			list.add(personF);
		}
		
		return list;
	}
	
	public List<Person> recommendations(long id) {
//		List<Suggestion> listSuggests = new ArrayList<Suggestion>();
		List<Person> listSuggests = new ArrayList<Person>();
		List<Person> listSuggestsTemp = new ArrayList<Person>();
		List<Person> friends = listFriends(id);
		Map<Long, Integer> suggests = new HashMap<Long, Integer>();
		Vertex person = load(new Long(id));
		Person fSuggest = null;
		
		Person personSB = null;
		for (Vertex suggest : person.getVertices(Direction.OUT, "suggested")) {
			personSB = new Person((String)suggest.getProperty("name"), (Long)suggest.getProperty("facebook_id"));
			listSuggestsTemp.add(personSB);
		}
		
		for (Vertex friend : person.getVertices(Direction.OUT, "knows")) {
			for (Vertex friendSuggest : friend.getVertices(Direction.OUT, "knows")) {
				fSuggest = new Person((String)friendSuggest.getProperty("name"), (Long)friendSuggest.getProperty("facebook_id"));
				if (!friends.contains(fSuggest)) {
					if (suggests.get(fSuggest.getId()) == null) {
						if (((Long)person.getProperty("facebook_id")).equals(fSuggest.getId()) || listSuggestsTemp.contains(fSuggest)) {
							suggests.put(fSuggest.getId(), 1);
							continue;
						}
						suggests.put(fSuggest.getId(), 1);
						person.addEdge("suggested", friendSuggest);
						bobbyGraph.commit();
					} else {
						Integer newSuggest = suggests.get(fSuggest.getId()) + 1;
						suggests.put(fSuggest.getId(), newSuggest);
					}
				}
			}
		}
		
		Person personS = null;
		Suggestion suggested = null;
		for (Vertex suggest : person.getVertices(Direction.OUT, "suggested")) {
			personS = new Person((String)suggest.getProperty("name"), (Long)suggest.getProperty("facebook_id"));
//			suggested = new Suggestion(personS, suggests.get(personS.getId()));
			listSuggests.add(personS);
		}
		
		return listSuggests;
	}
	
	public Person load(long id) {
		Person person = null;
		if (bobbyGraph.query().has("facebook_id", Compare.EQUAL, id).vertices().iterator().hasNext()) {
			Vertex v = bobbyGraph.query().has("facebook_id", Compare.EQUAL, id).vertices().iterator().next();
			person = new Person((String)v.getProperty("name"), (Long)v.getProperty("facebook_id"));
		} else {
			person = new Person("pessoa nao encontrada", -1);
		}
		return person;
	}
	
	public Vertex load(Long id) {
		return bobbyGraph.query().has("facebook_id", Compare.EQUAL, id).vertices().iterator().next();
	}
	
	public void close() {
		if (bobbyGraph != null && bobbyGraph.isOpen()) bobbyGraph.shutdown();
	}
}
