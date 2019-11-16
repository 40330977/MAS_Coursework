package ontologie.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class Sell implements AgentAction{
	private AID buyer;
	private CustomerOrder item;
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	public CustomerOrder getItem() {
		return item;
	}
	public void setItem(CustomerOrder item) {
		this.item = item;
	}

}
