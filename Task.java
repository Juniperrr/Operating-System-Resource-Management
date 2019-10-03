package Lab3;
import java.util.ArrayList;

//Stores properties of each task.
public class Task {
	// Activity Related Variables for that Task
	ArrayList<Activity> activitiesList;
	Activity currentActivity;
	//activity index in activities list.
	int activityIndex;
	
	// Resources related variables for that Task
	int[] resourcesOwned;
	int[] resourcesNeeded;
	
	// Stats related variables for that Task
	int id;  // TaskNum
	int currentDelay;
	int undelayedCycle;
	int cycleInitiated;
	int timeTaken; 
	
	int waitTime;
	double waitPercent;
	
	// Activity Type/Status related variables for that Task
    boolean terminated, aborted, delayed, completed;

    //Default constructor
	public Task() {
		activitiesList = new ArrayList<Activity>();    
		currentActivity = new Activity();
		activityIndex = 0;
		
		resourcesOwned = new int[0];
		resourcesNeeded = new int[0];
		id = -1;
		
		currentDelay = 0;
		undelayedCycle = -1;
		cycleInitiated = 0;
		timeTaken = 0; //For summary
	    waitTime = 0; //For summary
	    waitPercent = 0; //For summary
	    
	    delayed = false;
	    aborted = false;
	    completed = false;
	    terminated = false;
	}
	
	//Constructor
	public Task(int id, int numOfResourceTypes) {
		activitiesList = new ArrayList<Activity>();
		currentActivity = new Activity();
		
		activityIndex = 0;
		currentDelay = 0;
		
		resourcesOwned = new int[numOfResourceTypes];
		resourcesNeeded = new int[numOfResourceTypes];
		
		for(int i=0; i<numOfResourceTypes; i++) {
			resourcesOwned[i] = 0;
			resourcesNeeded[i] = 0;
		}
		
		this.id = id;
	}
	
	//Add an activity to a task's activities list.
	public void addActivityForTask(String activityType, int delay, int resourceType, int value) {
		Activity newActivity = new Activity (activityType, delay, resourceType, value);
		activitiesList.add(newActivity);
	}
	
	//Update a task's claim value.
	public void updateClaimValue(Task currentTask, Activity activityData) {
		int whichResource = activityData.resourceType;
		int howManyResources = activityData.value;
		currentTask.resourcesNeeded[whichResource-1] = howManyResources;
	}
	
	//Assigns a task's next activity as a current activity.
	public void moveToNextActivity() {
		activityIndex++;
		currentActivity = activitiesList.get(activityIndex);
	}
}
