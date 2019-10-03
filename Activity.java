package Lab3;

//Stores properties of each activity of each task.
//Activity is one property of each task.
public class Activity {
	String activityType;
	int delay;
	int resourceType;
	int value;
	
	// default constructor 
	public Activity() {
		activityType = "";
		delay = 0;
		resourceType = 0;
		value = 0;
	}
	
	// constructor 
	public Activity(String type, int delay, int resourceType, int value) {
		super();
		this.activityType = type;
		this.delay = delay;
		this.resourceType = resourceType;
		this.value = value;
	}
}
