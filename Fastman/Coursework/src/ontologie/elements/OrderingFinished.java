package ontologie.elements;

import jade.content.AgentAction;

public class OrderingFinished implements AgentAction{
	private String finisher;

	public String getFinisher() {
		return finisher;
	}

	public void setFinisher(String finisher) {
		this.finisher = finisher;
	}
}
