package ontologie.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class SupplierOrder implements AgentAction{
	private AID buyer;
	private Screen screen;
	private Battery battery;
	private RAM ram;
	private Storage storage;
	private int quantity;
	private boolean finishOrder = false;
	private boolean fullfilled = false;
	private int dueIn;
	private String orderID;
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
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
	public boolean isFinishOrder() {
		return finishOrder;
	}
	public void setFinishOrder(boolean finishOrder) {
		this.finishOrder = finishOrder;
	}
	public int getDueIn() {
		return dueIn;
	}
	public void setDueIn(int dueIn) {
		this.dueIn = dueIn;
	}
	public boolean isFullfilled() {
		return fullfilled;
	}
	public void setFullfilled(boolean fullfilled) {
		this.fullfilled = fullfilled;
	}
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

}
