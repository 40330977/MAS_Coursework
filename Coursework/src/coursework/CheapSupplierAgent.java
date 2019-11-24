package coursework;

import java.util.ArrayList;
import java.util.HashMap;

//import coursework.SupplierAgent.OrderHandler;
//import coursework.CustomerAgent.EndDay;
//import coursework.CustomerAgent.FindManufacturer;
//import coursework.CustomerAgent.TickerWaiter;
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
import ontologie.elements.OrderingFinished;
import ontologie.elements.SupplierOrder;

public class CheapSupplierAgent extends Agent{
	private AID manufacturer;
	private AID tickerAgent;
	private int numQueriesSent;
	private Codec codec = new SLCodec();
	private Ontology ontology = Ontologie.getInstance();
	private ArrayList<SupplierOrder> recievedOrders = new ArrayList();
	private boolean orderReciever = false;
	private int day = 1;

	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("cheap supplier");
		sd.setName(getLocalName() + "-cheap-supplier-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}


		
		addBehaviour(new TickerWaiter(this));
		//addBehaviour(new OrderHandler());
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
					dailyActivity.addSubBehaviour(new FindManufacturer(myAgent));
					//dailyActivity.addSubBehaviour(new SendEnquiries(myAgent));
					//dailyActivity.addSubBehaviour(new CollectOffers(myAgent));
					dailyActivity.addSubBehaviour(new OrderHandler());
					doWait(5000);
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
	
	public class FindManufacturer extends OneShotBehaviour {

		public FindManufacturer(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			DFAgentDescription sellerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("manufacturer");
			sellerTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,sellerTemplate); 
				for(int i=0; i<agentsType1.length; i++){
					manufacturer=agentsType1[i].getName(); // this is the AID
					System.out.println("CFM test: " + manufacturer.getName());
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}

		}
	}
	
	private class OrderHandler extends Behaviour{
		@Override
		public void action() {
			//recievedOrders.clear();
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
					System.out.println(ce);
					if(ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof SupplierOrder) {
							SupplierOrder order = (SupplierOrder)action;
							//CustomerOrder cust = order.getItem();
							//OrderQuantity = order.getQuantity();
							if(!order.isFinishOrder()) {
							System.out.println("cheap test: " + order.getQuantity());
							recievedOrders.add(order);
							}
							else {orderReciever = true;}
							
						}
						
						

					}
					//deal with bool set here
				
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

		@Override
		public boolean done() {
			
			return orderReciever;
		}

	}
	
	public class FulfillOrder extends OneShotBehaviour{
		public FulfillOrder(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			for(SupplierOrder order : recievedOrders) {
				if(order.getDueIn() == day) {
					order.setFullfilled(true);
					ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
					enquiry.setLanguage(codec.getName());
					enquiry.setOntology(ontology.getName());
					enquiry.addReceiver(manufacturer);
					
					/*SupOrderAct order1 = new SupOrderAct();
					order1.setBuyer(myAgent.getAID());
					order1.setItem(order);*/
					
					Action request = new Action();
					request.setAction(order);
					request.setActor(manufacturer); // the agent that you request to perform the action
					try {
					 // Let JADE convert from Java objects to string
					 getContentManager().fillContent(enquiry, request); //send the wrapper object
					 
					 send(enquiry);
					 System.out.println("Order Returned");
					}
					catch (CodecException ce) {
					 ce.printStackTrace();
					}
					catch (OntologyException oe) {
					 oe.printStackTrace();
					} 
				}
			}
				
		}
	}
	
public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			
			
		
			orderReciever = false;
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(tickerAgent);
			msg.setContent("done");
			myAgent.send(msg);
			day++;
			//send a message to each seller that we have finished
			//ACLMessage sellerDone = new ACLMessage(ACLMessage.INFORM);
			//sellerDone.setContent("done");
			//for(AID seller : sellers) {
				//sellerDone.addReceiver(seller);
			
			//myAgent.send(sellerDone);
			System.out.println("day over cheap");
			}
		
	}

}
