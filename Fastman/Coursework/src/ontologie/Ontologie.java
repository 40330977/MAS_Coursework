package ontologie;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class Ontologie extends BeanOntology{

	
		
		private static Ontology theInstance = new Ontologie("my_ontology");
		
		public static Ontology getInstance(){
			return theInstance;
		}
		//singleton pattern
		private Ontologie(String name) {
			super(name);
			try {
				add("ontologie.elements");
			} catch (BeanOntologyException e) {
				e.printStackTrace();
			}
		}
	

	
	
}
