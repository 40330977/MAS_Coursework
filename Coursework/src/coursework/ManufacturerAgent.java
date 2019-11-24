package coursework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

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
	private ArrayList<AID> customers = new ArrayList<AID>();
	private AID supplier /*= new AID("supp lier 1", AID.ISLOCALNAME)*/;
	private AID cheapSupplier /*= new AID("cheap supplier", AID.ISLOCALNAME)*/;
	private ArrayList<CustomerOrder> recievedOrders = new ArrayList();
	private ArrayList<CustomerOrder> acceptedOrders = new ArrayList();
	
	private AID tickerAgent/*= new AID("ticker", AID.ISLOCALNAME)*/;
	private Codec codec = new SLCodec();
	private Ontology ontology = Ontologie.getInstance();
	private final int productionCapacity = 50;
	private ArrayList<Integer> dailyprod = new ArrayList();//day, prod quota used
	public Warehouse warehouse = new Warehouse();
	private int OrderQuantity = 0;
	private int day = 1;
	private int orderNo = 0;
	private HashMap<CustomerOrder, Integer> orderSchedule = new HashMap();
	private HashMap<CustomerOrder, Integer> orderScheduleCheap = new HashMap();
	private ArrayList<Integer> buildSchedule = new ArrayList();//index day, entry production days left
	private CustomerOrder bestOrder;
	//private ArrayList<CustomerOrder> Cust;
	private ArrayList<CustomerOrder> daysOrders = new ArrayList();
	private ArrayList<CustomerOrder> daysCheapOrders = new ArrayList();
	
	protected void setup() {
		//System.out.println(supplier.toString());
		System.out.println("man test 1");
		bestOrder = new CustomerOrder();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		for(int i = 0; i<101; i++) {
			dailyprod.add(productionCapacity);
		}
		//add this agent to the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
			System.out.println("man test 2");
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		for(int i= 0; i<100; i++) {
		dailyprod.add(i, 50);
		}
		doWait(240000);
		//addBehaviour(new FindCustomer(this));
		addBehaviour(new TickerWaiter(this));
		System.out.println("man test 3");
		
		
			/*DFAgentDescription sellerTemplate = new DFAgentDescription();
			ServiceDescription sd1 = new ServiceDescription();
			sd1.setType("customer");
			//sd1.setName("coursework");
			sellerTemplate.addServices(sd1);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(this,sellerTemplate); 
				for(int i=0; i<agentsType1.length-1; i++){
					customers.add(agentsType1[i].getName());
					
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}
			System.out.println(customers.toString());*/
		
		//addBehaviour(new FindSupplier(this));
		//addBehaviour(new FindCheapSupplier(this));
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
					System.out.println("man test 4");
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub-behaviours will execute in the order they are added
					dailyActivity.addSubBehaviour(new FindCustomer(myAgent));
					//System.out.println("man test 5");
					//System.out.println(customers.get(0).toString());
					dailyActivity.addSubBehaviour(new FindSupplier(myAgent));
					//System.out.println("man test 6");
					//System.out.println(supplier.toString());
					dailyActivity.addSubBehaviour(new FindCheapSupplier(myAgent));
					//System.out.println("man test 7");
					//System.out.println(cheapSupplier.toString());
					//doWait(5000);//wait to ensure all orders recieved
					System.out.println("prints before scheduler");
					dailyActivity.addSubBehaviour(new OrderHandler(myAgent));
					doWait(5000);
					if(recievedOrders!= null) {
					dailyActivity.addSubBehaviour(new OrderDecider(myAgent));
					dailyActivity.addSubBehaviour(new ScheduleManager(myAgent));
					dailyActivity.addSubBehaviour(new OrderSender(myAgent));
					dailyActivity.addSubBehaviour(new GenerateOrderSupplier(myAgent));
					dailyActivity.addSubBehaviour(new GenerateOrderCheap(myAgent));
					}
					//doWait(5000);//wait to ensure all orders recieved
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
			//sd.setName("coursework");
			sellerTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,sellerTemplate); 
				for(int i=0; i<agentsType1.length; i++){
					customers.add(agentsType1[i].getName());
					//System.out.println(customers.toString());
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}
			System.out.println(customers.toString());
		}
	}
	
	public class FindSupplier extends OneShotBehaviour {

		public FindSupplier(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("supplier finder test");
			DFAgentDescription supplierTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("supplier");
			//sd.setName("coursework");
			supplierTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType2  = DFService.search(myAgent,supplierTemplate); 
				for(int i=0; i<agentsType2.length; i++){
					supplier = agentsType2[i].getName();
					
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}
			System.out.println("find sup " +supplier.getName());
		}
	}
	
	public class FindCheapSupplier extends OneShotBehaviour {

		public FindCheapSupplier(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			System.out.println("cheap supplier finder test");
			DFAgentDescription cheapsupTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("cheap supplier");
			//sd.setName("coursework");
			cheapsupTemplate.addServices(sd);
			try{
				//sellers.clear();
				DFAgentDescription[] agentsType1  = DFService.search(myAgent,cheapsupTemplate); 
				for(int i=0; i<agentsType1.length; i++){
					cheapSupplier = agentsType1[i].getName();
					
				}
			}
			catch(FIPAException e) {
				e.printStackTrace();
			}
			System.out.println("cheap find sup " + cheapSupplier.getName());
		}
	}
	
	private class OrderHandler extends Behaviour{
		Integer NumberOfOrders = 0;
		public OrderHandler(Agent a) {
			super(a);
		}
		@Override
		public void action() {
			//recievedOrders.clear();
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				/*if(customers == null) {
					customers.put(0,  msg.getSender());
					System.out.println(customers.get(0).getName());
				}*/
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
							NumberOfOrders++;
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

		@Override
		public boolean done() {
			if(NumberOfOrders >= customers.size()-1) {
			return true;}
			else {return true;}
		}

	}
	
	public class OrderDecider extends OneShotBehaviour{
		public OrderDecider(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			System.out.println("behaviour hit test");
			for(int i = 0; i<recievedOrders.size(); i++) {
				System.out.println("test!!!: " + recievedOrders.get(i).getRam().getRAMSize());
				if(recievedOrders.get(i).getDueIn()<4) {
					recievedOrders.get(i).setFastTurnAround(true);
					if(recievedOrders.get(i).getQuantity()>recievedOrders.get(i).getDueIn()*productionCapacity) {
						recievedOrders.get(i).setAccepted(false);
						System.out.println("False too large an order");
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
							System.out.println("True Fast Turn Around");
						}
						else {
							recievedOrders.get(i).setAccepted(false);
							System.out.println("this may be breaking it");
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
							System.out.println("True cheap order");
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
							recievedOrders.get(i).setFastTurnAround(true);
							System.out.println("True but like first case");
						}
					}
					else {
						recievedOrders.get(i).setAccepted(false);
						System.out.println("False to large an order");
					}
				}
			}
			if(recievedOrders.size()>0) {
			for(int i = recievedOrders.size() - 1; i> 0; i--) {//remove those not accepted
				if(recievedOrders.get(i).isAccepted()==false) {
					recievedOrders.remove(i);
				}
			}}
			//if(recievedOrders.size()>1) {//maybe replace with scheduler
				//for(int i = 0; i<recievedOrders.size(); i++) {
				
				//}
			//}
		}
	}
	
	public class ScheduleManager extends OneShotBehaviour{
		public ScheduleManager(Agent a) {
			super(a);
		}

		@Override
		public void action() {//need to add unique order number to order orderSchedule dailyProd
			//while(recievedOrders.size()>0) {
				for(CustomerOrder order : recievedOrders) {
					if(order.getNetProfit()>bestOrder.getNetProfit()) {
						bestOrder = order;
					}
				}
				if(bestOrder.isFastTurnAround()) {
					if(bestOrder.getQuantity()<=dailyprod.get(day+1)) {
						int remprod = dailyprod.get(day+1) - bestOrder.getQuantity();//add build schedule
						dailyprod.set(day + 1, remprod);
						orderSchedule.put(bestOrder, day);//ordersched switch + remove from recieved orders
						acceptedOrders.add(bestOrder);
						recievedOrders.remove(bestOrder);//message accepted
						System.out.println("Fast turn around sheduled");
					}//add else if to check next day and add if net profit is high enough also reject
				}
				if(!bestOrder.isFastTurnAround()) {
					if(bestOrder.getQuantity()<=dailyprod.get(day + 4)) {
						int remprod = dailyprod.get(day + 4)- bestOrder.getQuantity();
						dailyprod.set(day + 4, remprod);
						orderSchedule.put(bestOrder, day + 3);//ordersched switch + remove from recieved orders
						orderScheduleCheap.put(bestOrder, day);
						acceptedOrders.add(bestOrder);
						recievedOrders.remove(bestOrder);//message accepted
						System.out.println("cheap turn around sheduled");
					}
				}
			
			//}
		}
		
	}
	
	public class OrderSender extends OneShotBehaviour{
		public OrderSender(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			for(CustomerOrder order : acceptedOrders) {
				if(orderSchedule.get(order) != null && orderSchedule.get(order) == day) {
					System.out.println("Sending expensive");
					//myAgent.addBehaviour(new GenerateOrderSupplier(myAgent, order));
					daysOrders.add(order);
				}
				if(orderScheduleCheap.get(order) != null && orderScheduleCheap.get(order) == day) {
					System.out.println("Sending cheap");
					//myAgent.addBehaviour(new GenerateOrderCheap(myAgent, order));
					daysCheapOrders.add(order);
				}
			}
			
			
			
			
		}
	}
	
	public class GenerateOrderSupplier extends OneShotBehaviour{
		public GenerateOrderSupplier(Agent a/*, ArrayList<CustomerOrder> c*/) {
			super(a);
			//Cust = c;
		}
		
		@Override
		public void action() {
			if(daysOrders!=null) {
			for(CustomerOrder Cust : daysOrders) {
			SupplierOrder order = new SupplierOrder();
			order.setScreen(new Screen());
			order.setBattery(new Battery());
			order.setRam(new RAM());
			order.setStorage(new Storage());
			
			
			order.getScreen().setDisplaySize(Cust.getScreen().getDisplaySize());
			order.getScreen().setPrice(Cust.getScreen().getPrice());
			order.getBattery().setBatteryLife(Cust.getBattery().getBatteryLife());
			order.getBattery().setPrice(Cust.getBattery().getPrice());
			
			if(Cust.isFastTurnAround()) {
				order.getRam().setRAMSize(Cust.getRam().getRAMSize());
				order.getRam().setPrice(Cust.getRam().getPrice());
				order.getStorage().setStorageSize(Cust.getStorage().getStorageSize());
				order.getStorage().setPrice(Cust.getStorage().getPrice());
			}

			order.setQuantity(Cust.getQuantity());
			order.setDueIn(day+1);
			//order.setBuyer(Cust.getBuyer());
			
			
			ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
			enquiry.setLanguage(codec.getName());
			enquiry.setOntology(ontology.getName());
			enquiry.addReceiver(supplier);
			
			/*SupOrderAct order1 = new SupOrderAct();
			order1.setBuyer(myAgent.getAID());
			order1.setItem(order);*/
			
			Action request = new Action();
			request.setAction(order);
			request.setActor(supplier); // the agent that you request to perform the action
			try {
			 // Let JADE convert from Java objects to string
			 getContentManager().fillContent(enquiry, request); //send the wrapper object
			 
			 send(enquiry);
			 System.out.println("Supplier Sent");
			}
			catch (CodecException ce) {
			 ce.printStackTrace();
			}
			catch (OntologyException oe) {
			 oe.printStackTrace();
			} 
			
		}}}
	}
	
	public class GenerateOrderCheap extends OneShotBehaviour{
		public GenerateOrderCheap(Agent a/*, CustomerOrder c*/) {
			super(a);
			//Cust = c;
		}
		
		@Override
		public void action() {
			if(daysCheapOrders!=null) {
			for(CustomerOrder Cust : daysCheapOrders) {
				Cust.getBattery();
			SupplierOrder order = new SupplierOrder();
			//order.setScreen(new Screen());
			//order.setBattery(new Battery());
			//System.out.println("cheap order pass check: " + Cust.getRam().getRAMSize());
			order.setRam(new RAM());
			order.setStorage(new Storage());

			order.getRam().setRAMSize(Cust.getRam().getRAMSize());
			
			order.getStorage().setStorageSize(Cust.getStorage().getStorageSize());
	
			order.setQuantity(Cust.getQuantity());
			order.setDueIn(day+4);
			
			//order.setBuyer(Cust.getBuyer());
			
			
			ACLMessage enquiry = new ACLMessage(ACLMessage.REQUEST);
			enquiry.setLanguage(codec.getName());
			enquiry.setOntology(ontology.getName());
			enquiry.addReceiver(cheapSupplier);
			
			/*SupOrderAct order1 = new SupOrderAct();
			order1.setBuyer(myAgent.getAID());
			order1.setItem(order);*/
			
			Action request = new Action();
			request.setAction(order);
			request.setActor(cheapSupplier); // the agent that you request to perform the action
			try {
			 // Let JADE convert from Java objects to string
			 getContentManager().fillContent(enquiry, request); //send the wrapper object
			 
			 send(enquiry);
			 System.out.println("Cheap Supplier Sent");
			}
			catch (CodecException ce) {
			 ce.printStackTrace();
			}
			catch (OntologyException oe) {
			 oe.printStackTrace();
			} 
			}
		}}
	}
	public class recieveSupplies extends Behaviour{
		Integer NumberOfOrders = 0;
		public recieveSupplies(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			//recievedOrders.clear();
			//This behaviour should only respond to REQUEST messages
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); 
			ACLMessage msg = receive(mt);
			if(msg != null){
				/*if(customers == null) {
					customers.put(0,  msg.getSender());
					System.out.println(customers.get(0).getName());
				}*/
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
							System.out.println("test: " + order.getQuantity()+" " + order.getDueIn());
							//recievedOrders.add(order);
							NumberOfOrders++;
							//update warehouse stock: check for nulls then if else for incrementing stock by type
							
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

		@Override
		public boolean done() {
			return false;
		}
	}
	
public class EndDay extends OneShotBehaviour {
		
		public EndDay(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			SupplierOrder finisher1 = new SupplierOrder();
			finisher1.setFinishOrder(true);
			ACLMessage finish1 = new ACLMessage(ACLMessage.REQUEST);
			finish1.setLanguage(codec.getName());
			finish1.setOntology(ontology.getName());
			finish1.addReceiver(cheapSupplier);
			Action request1 = new Action();
			request1.setAction(finisher1);
			request1.setActor(cheapSupplier);
			
			//try {
			//finish.setContentObject(true);
			finish1.setConversationId("finished-ordering1");
			//finish1.addReceiver(cheapSupplier);
			//finish1.addReceiver(supplier);
			//finish.setContent("Finish");
			try {
				 // Let JADE convert from Java objects to string
				 getContentManager().fillContent(finish1, request1); //send the wrapper object
				 
				 send(finish1);
				 System.out.println("finisher1 sent");
				}
				catch (CodecException ce) {
				 ce.printStackTrace();
				}
				catch (OntologyException oe) {
				 oe.printStackTrace();
				} 
			
			SupplierOrder finisher = new SupplierOrder();
			finisher.setFinishOrder(true);
			ACLMessage finish = new ACLMessage(ACLMessage.REQUEST);
			finish.setLanguage(codec.getName());
			finish.setOntology(ontology.getName());
			finish.addReceiver(supplier);
			Action request = new Action();
			request.setAction(finisher);
			request.setActor(supplier);
			System.out.println("finisher suplier test: " + supplier.getName());
			//try {
			//finish.setContentObject(true);
			finish.setConversationId("finished-ordering");
			//finish.addReceiver(cheapSupplier);
			//finish.addReceiver(supplier);
			//finish.setContent("Finish");
			try {
				 // Let JADE convert from Java objects to string
				 getContentManager().fillContent(finish, request); //send the wrapper object
				 
				 send(finish);
				 System.out.println("finisher sent");
				}
				catch (CodecException ce) {
				 ce.printStackTrace();
				}
				catch (OntologyException oe) {
				 oe.printStackTrace();
				} 
			
			
			
			//myAgent.send(finish);
			recievedOrders.clear();
			daysOrders.clear();
			daysCheapOrders.clear();
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
			System.out.println("day over. " + day + " is new day");
			}
		
	}

}
