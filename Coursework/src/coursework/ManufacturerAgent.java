package coursework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Supplier;

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
import ontologie.elements.Battery;
import ontologie.elements.CustomerOrder;
import ontologie.elements.Item;
import ontologie.elements.RAM;
import ontologie.elements.Screen;
import ontologie.elements.SupOrderAct;
import ontologie.elements.Sell;
import ontologie.elements.Storage;
import ontologie.elements.SupplierOrder;
import ontologie.elements.*;

public class ManufacturerAgent extends Agent{
	private ArrayList<AID> customers = new ArrayList();
	private AID supplier;
	private AID cheapSupplier;
	private ArrayList<CustomerOrder> recievedOrders = new ArrayList();
	private HashMap<Integer, CustomerOrder> acceptedOrders = new HashMap();
	private ArrayList<Integer> buildSchedule = new ArrayList();//orderno, day
	private AID tickerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = Ontologie.getInstance();
	private final int productionCapacity = 50;
	private ArrayList<Integer> dailyprod = new ArrayList();//day, prod quota used
	public Warehouse warehouse = new Warehouse();
	private int OrderQuantity = 0;
	private int day = 0;
	private int orderNo = 0;
	private HashMap<CustomerOrder, Integer> orderSchedule = new HashMap();
	
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
		for(int i= 0; i<100; i++) {
		dailyprod.add(i, 50);
		}
		
		addBehaviour(new TickerWaiter(this));
		addBehaviour(new OrderHandler());
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
					doWait(5000);//wait to ensure all orders recieved
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
					supplier = agentsType1[i].getName();
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
					cheapSupplier = agentsType1[i].getName();
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
			recievedOrders.clear();
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
						if (action instanceof CustomerOrder) {
							CustomerOrder order = (CustomerOrder)action;
							//CustomerOrder cust = order.getItem();
							OrderQuantity = order.getQuantity();
							System.out.println("test: " + order.getQuantity()+" " + order.getDueIn()+" "+order.getGrossProfit());
							recievedOrders.add(order);
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
	
	public class OrderDecider extends OneShotBehaviour{
		public OrderDecider(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			for(int i = 0; i<recievedOrders.size(); i++) {
				if(recievedOrders.get(i).getDueIn()<4) {
					recievedOrders.get(i).setFastTurnAround(true);
					if(recievedOrders.get(i).getQuantity()>recievedOrders.get(i).getDueIn()*productionCapacity) {
						recievedOrders.get(i).setAccepted(false);
						//continue;//mark accepted false
					}
					else {//can produce, must use expensive supplier
						if(recievedOrders.get(i).getRam().getRAMSize()==4) {
							recievedOrders.get(i).getRam().setPrice(30);
						}
						else {
							recievedOrders.get(i).getRam().setPrice(60);
						}
						if(recievedOrders.get(i).getStorage().getStorageSize()==64) {
							recievedOrders.get(i).getStorage().setPrice(25);
						}
						else {
							recievedOrders.get(i).getStorage().setPrice(50);
						}
						recievedOrders.get(i).setNetCost(recievedOrders.get(i).getBattery().getPrice()+recievedOrders.get(i).getScreen().getPrice()+recievedOrders.get(i).getRam().getPrice()+recievedOrders.get(i).getStorage().getPrice());
						if(recievedOrders.get(i).getNetCost()<recievedOrders.get(i).getUnitPrice()) {
							recievedOrders.get(i).setAccepted(true);
						}
					}
				}
				else {
					recievedOrders.get(i).setFastTurnAround(false);
					if(recievedOrders.get(i).getQuantity()<(recievedOrders.get(i).getDueIn()-4)*productionCapacity) {//determine if possible to order from the cheap supplier
						if(recievedOrders.get(i).getRam().getRAMSize()==4) {
							recievedOrders.get(i).getRam().setPrice(20);
						}
						else {
							recievedOrders.get(i).getRam().setPrice(35);
						}
						if(recievedOrders.get(i).getStorage().getStorageSize()==64) {
							recievedOrders.get(i).getStorage().setPrice(15);
						}
						else {
							recievedOrders.get(i).getStorage().setPrice(40);
						}
						recievedOrders.get(i).setNetCost(recievedOrders.get(i).getBattery().getPrice()+recievedOrders.get(i).getScreen().getPrice()+recievedOrders.get(i).getRam().getPrice()+recievedOrders.get(i).getStorage().getPrice());
						if(recievedOrders.get(i).getNetCost()<recievedOrders.get(i).getUnitPrice()) {
							recievedOrders.get(i).setAccepted(true);
						}
					}
					else if(recievedOrders.get(i).getQuantity()<recievedOrders.get(i).getDueIn()*productionCapacity) {//if not but still able to produce same as case 1
						if(recievedOrders.get(i).getRam().getRAMSize()==4) {
							recievedOrders.get(i).getRam().setPrice(30);
						}
						else {
							recievedOrders.get(i).getRam().setPrice(60);
						}
						if(recievedOrders.get(i).getStorage().getStorageSize()==64) {
							recievedOrders.get(i).getStorage().setPrice(25);
						}
						else {
							recievedOrders.get(i).getStorage().setPrice(50);
						}
						recievedOrders.get(i).setNetCost(recievedOrders.get(i).getBattery().getPrice()+recievedOrders.get(i).getScreen().getPrice()+recievedOrders.get(i).getRam().getPrice()+recievedOrders.get(i).getStorage().getPrice());
						if(recievedOrders.get(i).getNetCost()<recievedOrders.get(i).getUnitPrice()) {
							recievedOrders.get(i).setAccepted(true);
						}
					}
					else {
						recievedOrders.get(i).setAccepted(false);
					}
				}
			}
			for(int i = recievedOrders.size(); i> 0; i--) {//remove those not accepted
				if(recievedOrders.get(i).isAccepted()==false) {
					recievedOrders.remove(i);
				}
			}
			if(recievedOrders.size()>1) {//maybe replace with scheduler
				for(int i = 0; i<recievedOrders.size(); i++) {
				
				}
			}
		}
	}
	
	public class ScheduleManager extends OneShotBehaviour{
		public ScheduleManager(Agent a) {
			super(a);
		}

		@Override
		public void action() {//need to add unique order number to order
			
			
		}
		
	}
	
	public class GenerateOrderSupplier extends OneShotBehaviour{
		public GenerateOrderSupplier(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			
			SupplierOrder order = new SupplierOrder();
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
			order.setQuantity(OrderQuantity);
			
			
			ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
			enquiry.setLanguage(codec.getName());
			enquiry.setOntology(ontology.getName());
			enquiry.addReceiver(supplier);
			
			SupOrderAct order1 = new SupOrderAct();
			order1.setBuyer(myAgent.getAID());
			order1.setItem(order);
			
			Action request = new Action();
			request.setAction(order1);
			request.setActor(supplier); // the agent that you request to perform the action
			try {
			 // Let JADE convert from Java objects to string
			 getContentManager().fillContent(enquiry, request); //send the wrapper object
			 
			 send(enquiry);
			}
			catch (CodecException ce) {
			 ce.printStackTrace();
			}
			catch (OntologyException oe) {
			 oe.printStackTrace();
			} 
		}
	}
	
	public class GenerateOrderCheap extends OneShotBehaviour{
		public GenerateOrderCheap(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			
			SupplierOrder order = new SupplierOrder();
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
			order.setQuantity(OrderQuantity);
			
			
			ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
			enquiry.setLanguage(codec.getName());
			enquiry.setOntology(ontology.getName());
			enquiry.addReceiver(cheapSupplier);
			
			SupOrderAct order1 = new SupOrderAct();
			order1.setBuyer(myAgent.getAID());
			order1.setItem(order);
			
			Action request = new Action();
			request.setAction(order1);
			request.setActor(cheapSupplier); // the agent that you request to perform the action
			try {
			 // Let JADE convert from Java objects to string
			 getContentManager().fillContent(enquiry, request); //send the wrapper object
			 
			 send(enquiry);
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
			recievedOrders.clear();
			day++;

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
