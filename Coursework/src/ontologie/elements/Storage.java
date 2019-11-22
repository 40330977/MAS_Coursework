package ontologie.elements;

import jade.content.onto.annotations.Slot;

public class Storage extends Item{
	private int storageSize;

	//@Slot(mandatory = true)
	public int getStorageSize() {
		return storageSize;
	}

	public void setStorageSize(int storageSize) {
		this.storageSize = storageSize;
	}

}
