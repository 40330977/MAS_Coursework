package ontologie.elements;

import  jade.content.Concept;
import jade.content.onto.annotations.Slot;

public class Screen extends Item{
	private int displaySize;

	@Slot(mandatory = true)
	public int getDisplaySize() {
		return displaySize;
	}

	public void setDisplaySize(int displaySize) {
		this.displaySize = displaySize;
	}

}
