package ontologie.elements;

import jade.content.onto.annotations.Slot;

public class RAM extends Item{
	private int RAMSize;
	
	//@Slot(mandatory = true)
	public int getRAMSize() {
		return RAMSize;
	}

	public void setRAMSize(int rAMSize) {
		RAMSize = rAMSize;
	}

}
