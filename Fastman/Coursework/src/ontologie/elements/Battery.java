package ontologie.elements;

import jade.content.onto.annotations.Slot;

public class Battery extends Item{
	private int batteryLife;

	//@Slot(mandatory = true)
	public int getBatteryLife() {
		return batteryLife;
	}

	public void setBatteryLife(int batteryLife) {
		this.batteryLife = batteryLife;
	}

}
