package ontologie.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class SupplierOrder implements AgentAction{
	//private AID buyer;
	private Screen screen;
	private Battery battery;
	private RAM ram;
	private Storage storage;
	private int quantity;
	/*public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}*/
	public Screen getScreen() {
		return screen;
	}
	public void setScreen(Screen screen) {
		this.screen = screen;
	}
	public Battery getBattery() {
		return battery;
	}
	public void setBattery(Battery battery) {
		this.battery = battery;
	}
	public RAM getRam() {
		return ram;
	}
	public void setRam(RAM ram) {
		this.ram = ram;
	}
	public Storage getStorage() {
		return storage;
	}
	public void setStorage(Storage storage) {
		this.storage = storage;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
