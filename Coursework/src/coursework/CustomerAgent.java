package coursework;

import java.util.ArrayList;
import java.util.HashMap;

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
import ontologie.elements.Battery;
import ontologie.elements.CustomerOrder;
import ontologie.elements.RAM;
import ontologie.elements.Screen;
import ontologie.elements.Sell;
import ontologie.elements.Storage;
import ontologie.elements.*;

public class CustomerAgent extends Agent{
	private AID manufacturer;
	private AID tickerAgent;
	private int numQueriesSent;
	private Codec codec = new SLCodec();
	private Ontology ontology = Ontologie.getInstance();
	private boolean orderAccepted = false;
	private int orderNo = 0;
	
	

	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("customer");
		sd.setName(getLocalName() + "-customer-agent");
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
					dailyActivity.addSubBehaviour(new FindManufacturer(myAgent));
					doWait(1000);
					dailyActivity.addSubBehaviour(new GenerateOrder(myAgent));
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
					System.out.println(manufacturer);
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}

		}
	}
	
public class GenerateOrder extends OneShotBehaviour{
	public GenerateOrder(Agent a) {
		super(a);
	}
	
	@Override
	public void action() {
		
		CustomerOrder order = new CustomerOrder();
		order.setScreen(new Screen());
		order.setBattery(new Battery());
		order.setRam(new RAM());
		order.setStorage(new Storage());
		
		if(Math.random()<0.5) {
			order.getScreen().setDisplaySize(5);
			order.getScreen().setPrice(100);
			order.getBattery().setBatteryLife(2000);
			order.getBattery().setPrice(70);
		}
		else {
			order.getScreen().setDisplaySize(7);
			order.getScreen().setPrice(150);
			order.getBattery().setBatteryLife(3000);
			order.getBattery().setPrice(100);
		}
		if(Math.random()<0.5) {
			order.getRam().setRAMSize(4);
		}
		else {
			order.getRam().setRAMSize(8);
		}
		if(Math.random()<0.5) {
			order.getStorage().setStorageSize(64);
		}
		else {
			order.getStorage().setStorageSize(256);
		}
		order.setQuantity((int) Math.floor(1+50*Math.random()));
		order.setUnitPrice((int) Math.floor(100+500*Math.random()));
		order.setDueIn((int) Math.floor(1+10*Math.random()));
		order.setLatePenalty(order.getQuantity()*(int) Math.floor(1+50*Math.random()));
		order.setAccepted(false);
		order.setOrderID(myAgent.getAID().toString() + orderNo);
		orderNo++;
		
		ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
		enquiry.setLanguage(codec.getName());
		enquiry.setOntology(ontology.getName());
		enquiry.addReceiver(manufacturer);
		
		//Sell order1 = new Sell();
		order.setBuyer(myAgent.getAID());
		//order1.setItem(order);
		
		Action request = new Action();
		request.setAction(order);
		request.setActor(manufacturer); // the agent that you request to perform the action
		try {
		 // Let JADE convert from Java objects to string
		 getContentManager().fillContent(enquiry, request); //send the wrapper object
		 
		 send(enquiry);
		 System.out.println("customer order sent");
		}
		catch (CodecException ce) {
		 ce.printStackTrace();
		}
		catch (OntologyException oe) {
		 oe.printStackTrace();
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
			System.out.println("day over cust");
			}
		
	}

}
