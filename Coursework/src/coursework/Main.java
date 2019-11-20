package coursework;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {

	public static void main(String[] args) {
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		try{
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			int numCustomers = 5;
			AgentController customers;
			for(int i=0; i<numCustomers; i++) {
				customers = myContainer.createNewAgent("customer" + i, CustomerAgent.class.getCanonicalName(), null);
				customers.start();
			}
			/*AgentController CustomerAgent = myContainer.createNewAgent("customer", CustomerAgent.class.getCanonicalName(),
					null);
			CustomerAgent.start();*/
			AgentController ManufacturerAgent = myContainer.createNewAgent("manufacturer", ManufacturerAgent.class.getCanonicalName(),
					null);
			ManufacturerAgent.start();
			AgentController SupplierAgent = myContainer.createNewAgent("supplier", SupplierAgent.class.getCanonicalName(),
					null);
			SupplierAgent.start();
			AgentController CheapSupplierAgent = myContainer.createNewAgent("cheap supplier", CheapSupplierAgent.class.getCanonicalName(),
					null);
			CheapSupplierAgent.start();
			AgentController tickerAgent = myContainer.createNewAgent("ticker", DayTickerAgent.class.getCanonicalName(),
					null);
			tickerAgent.start();
			
		}
		catch(Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}


	}
}
