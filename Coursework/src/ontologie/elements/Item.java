package ontologie.elements;

import  jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class Item implements Concept{
	private int price;
	
	@Slot(mandatory = true)
	public int getPrice() 
	{
		return price;
	}
	
	public void setPrice(int price) 
	{
		this.price = price;
	}
}
