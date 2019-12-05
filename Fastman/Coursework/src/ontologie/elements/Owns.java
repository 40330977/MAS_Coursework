package ontologie.elements;

import jade.content.Predicate;
import jade.core.AID;

public class Owns implements Predicate{
	private AID owner;
	private CustomerOrder item;
	public AID getOwner() {
		return owner;
	}
	public void setOwner(AID owner) {
		this.owner = owner;
	}
	public CustomerOrder getItem() {
		return item;
	}
	public void setItem(CustomerOrder item) {
		this.item = item;
	}

}
