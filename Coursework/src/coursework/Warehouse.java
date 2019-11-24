package coursework;

public class Warehouse {
	private int screens5=0;
	private int screens7=0;
	private int storage64=0;
	private int storage256=0;
	private int ram4=0;
	private int ram8=0;
	private int battery2000=0;
	private int battery3000=0;
	private int unitStorageCost = 5;
	
	public int getScreens5() {
		return screens5;
	}
	public void setScreens5(int screens5) {
		this.screens5 = screens5;
	}
	public int getScreens7() {
		return screens7;
	}
	public void setScreens7(int screens7) {
		this.screens7 = screens7;
	}
	public int getStorage64() {
		return storage64;
	}
	public void setStorage64(int storage64) {
		this.storage64 = storage64;
	}
	public int getStorage256() {
		return storage256;
	}
	public void setStorage256(int storage256) {
		this.storage256 = storage256;
	}
	public int getRam4() {
		return ram4;
	}
	public void setRam4(int ram4) {
		this.ram4 = ram4;
	}
	public int getRam8() {
		return ram8;
	}
	public void setRam8(int ram8) {
		this.ram8 = ram8;
	}
	public int getBattery2000() {
		return battery2000;
	}
	public void setBattery2000(int battery2000) {
		this.battery2000 = battery2000;
	}
	public int getBattery3000() {
		return battery3000;
	}
	public void setBattery3000(int battery3000) {
		this.battery3000 = battery3000;
	}
	
	public int StorageCost() {
		int total = getScreens5() + getScreens7() + getBattery2000() + getBattery3000() + getRam4() + getRam8() + getStorage64() + getStorage256();
		total *= unitStorageCost;
		return total;
	}
}
