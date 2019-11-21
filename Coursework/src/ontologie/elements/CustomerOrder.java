package ontologie.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class CustomerOrder implements AgentAction{
	private AID buyer;
	private Screen screen;
	private Battery battery;
	private RAM ram;
	private Storage storage;
	private int quantity;
	private int unitPrice;
	private int dueIn;
	private int latePenalty;
	private boolean accepted;
	private int netCost;
	private boolean fastTurnAround;
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
	public int getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}
	public int getDueIn() {
		return dueIn;
	}
	public void setDueIn(int dueIn) {
		this.dueIn = dueIn;
	}
	public int getLatePenalty() {
		return latePenalty;
	}
	public void setLatePenalty(int latePenalty) {
		this.latePenalty = latePenalty;
	}
	public int getGrossProfit() {
		return getQuantity()*getUnitPrice();
	}
	public boolean isAccepted() {
		return accepted;
	}
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	public int getNetCost() {
		return netCost;
	}
	public void setNetCost(int netCost) {
		this.netCost = netCost;
	}
	public int getNetProfit() {
		return getGrossProfit() - getNetCost()*getQuantity();
	}
	public boolean isFastTurnAround() {
		return fastTurnAround;
	}
	public void setFastTurnAround(boolean fastTurnAround) {
		this.fastTurnAround = fastTurnAround;
	}
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

}
