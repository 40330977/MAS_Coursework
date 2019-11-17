package ontologie.elements;

import jade.content.AgentAction;
import jade.content.Predicate;
import jade.content.schema.AgentActionSchema;
import jade.core.AID;

public class SupOrderAct implements AgentAction{
	private AID buyer;
	private SupplierOrder item;
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	public SupplierOrder getItem() {
		return item;
	}
	public void setItem(SupplierOrder item) {
		this.item = item;
	}

}