package coursework;

import java.util.ArrayList;
import java.util.HashMap;

import coursework.CustomerAgent.EndDay;
import coursework.CustomerAgent.FindManufacturer;
import coursework.CustomerAgent.TickerWaiter;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ontologie.Ontologie;
import ontologie.elements.CustomerOrder;
import ontologie.elements.Item;
import ontologie.elements.Sell;

public class ManufacturerAgent extends Agent{
	private ArrayList<AID> customers;
	private ArrayList<AID> suppliers;
	private AID tickerAgent;
	private int numQueriesSent;
	private Codec codec = new SLCodec();
	private Ontology ontology = Ontologie.getInstance();
	private int productionCapacity = 50;
	Warehouse warehouse = new Warehouse();
	
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}


		
		addBehaviour(new TickerWaiter(this));
	}


	@Override
	protected void takeDown() {
		//Deregister from the yellow pages
		try{
			DFService.deregister(this);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
	}
	
	public class TickerWaiter extends CyclicBehaviour {

		//behaviour to wait for a new day
		public TickerWaiter(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt); 
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new day")) {
					//spawn new sequential behaviour for day's activities
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub-behaviours will execute in the order they are added
					dailyActivity.addSubBehaviour(new FindCustomer(myAgent));
					dailyActivity.addSubBehaviour(new FindSupplier(myAgent));
					dailyActivity.addSubBehaviour(new FindCheapSupplier(myAgent));
					//dailyActivity.addSubBehaviour(new SendEnquiries(myAgent));
					//dailyActivity.addSubBehaviour(new CollectOffers(myAgent));
					dailyActivity.addSubBehaviour(new EndDay(myAgent));
					myAgent.addBehaviour(dailyActivity);
				}
				else {
					//termination message to end simulation
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
		}

	}
	
	public class FindCustomer extends OneShotBehaviour {

		public FindCustomer(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			DFAgentDescription sellerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("customer");
			sellerTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,sellerTemplate); 
				for(int i=0; i<agentsType1.length-1; i++){
					customers.add(agentsType1[i].getName());
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}

		}
	}
	
	public class FindSupplier extends OneShotBehaviour {

		public FindSupplier(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			DFAgentDescription supplierTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("supplier");
			supplierTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,supplierTemplate); 
				for(int i=0; i<agentsType1.length-1; i++){
					suppliers.add(agentsType1[i].getName());
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}

		}
	}
	
	public class FindCheapSupplier extends OneShotBehaviour {

		public FindCheapSupplier(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			DFAgentDescription cheapsupTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("cheap supplier");
			cheapsupTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,cheapsupTemplate); 
				for(int i=0; i<agentsType1.length-1; i++){
					suppliers.add(agentsType1[i].getName());
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}

		}
	}
	
	private class OrderHandler extends CyclicBehaviour{
		@Override
		public void action() {
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				try {
					ContentElement ce = null;
					System.out.println(msg.getContent()); //print out the message content in SL

					// Let JADE convert from String to Java objects
					// Output will be a ContentElement
					ce = getContentManager().extractContent(msg);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof Sell) {
							Sell order = (Sell)action;
							CustomerOrder cust = order.getItem();
							System.out.println("test" + cust.getDueIn());
							//Item it = order.getItem();
							// Extract the CD name and print it to demonstrate use of the ontology
							//if(it instanceof CD){
								//CD cd = (CD)it;
								//check if seller has it in stock
								//if(itemsForSale.containsKey(cd.getSerialNumber())) {
									//System.out.println("Selling CD " + cd.getName());
								//}
								//else {
									//System.out.println("You tried to order something out of stock!!!! Check first!");
								//}

							//}
						}

					}
				}

				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}

			}
			else{
				block();
			}
		}

	}
	
public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);
			//send a message to each seller that we have finished
			//ACLMessage sellerDone = new ACLMessage(ACLMessage.INFORM);
			//sellerDone.setContent("done");
			//for(AID seller : sellers) {
				//sellerDone.addReceiver(seller);
			
			//myAgent.send(sellerDone);
			System.out.println("day over");
			}
		
	}

}
