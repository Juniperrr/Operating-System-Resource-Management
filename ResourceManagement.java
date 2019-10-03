package Lab3;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ResourceManagement {
	static int numTasks;
	static int numResourceTypes;
	static int[] resourcesAvailable;
	static Task[] tasks;
	
	//Initially stores tasks from tasks[]. Later stores tasks that are deadlocked first then others.
	static ArrayList<Task> activeTasksList = new ArrayList<Task>(); 
	static ArrayList<Task> deadlockedTasksList = new ArrayList<Task>();
	//Temporarily stores tasks that are not deadlocked.
	static ArrayList<Task> tempTasksList = new ArrayList<Task>(); 
	
	static String[][] optimisticSummaryTable;
	static String[][] bankerSummaryTable;
	
	//Temporarily stores resources that were released by a task before adding them to resourcesAvailable[].
	static int[] freedResources = new int[numResourceTypes]; 
	
	public static void main(String[] args) throws FileNotFoundException {
		if(args.length == 0) {
			System.out.println("Please provide one argument to the program which is the input file to run for the Simultion");
		}
		else {}
		String fileName = args[0];
		//String fileName = "/Users/junipersohn/eclipse-workspace/Lab3Juniper/src/input-13.txt"; --> kept this for a final check on Eclipse IDE.
		readInput(fileName);
		optimisticRM();
		
		//Stores each task instances to show time statistics of each task.
		Task[] tempTaskStatsFIFO = new Task [numTasks]; 
		//Stores total time statistics of all tasks.
		int[] totalStatsFIFO = new int[2]; 
	
		int totalTime = 0;
		int totalWaitTime = 0;
		int avgWaitPercentFIFO = 0;
		int completedTasks = 0;
		for (int i = 0; i < numTasks; i++) {
			tempTaskStatsFIFO[i]=tasks[i];
			//Calculate total time statistics for FIFO.
			if (tasks[i].aborted == false) {
				completedTasks = completedTasks+1;
				totalTime += tasks[i].timeTaken;
				totalWaitTime += tasks[i].waitTime;
			}
		}
		avgWaitPercentFIFO = (int) ((double)totalWaitTime / (double)totalTime) * 100;
		totalStatsFIFO[0] = totalTime;
		totalStatsFIFO[1] = totalWaitTime;

		readInput(fileName);
		bankersRM();
		//Stores each task instances to show time statistics of each task.
		Task[] tempTaskStatsBK = new Task [numTasks];
		//Stores total time statistics of all tasks.
		int[] totalStatsBK = new int[2]; 
		
		totalTime = 0;
		totalWaitTime = 0;
		int avgWaitPercentBK = 0;
		completedTasks = 0;
		for (int i = 0; i < numTasks; i++) {
			tempTaskStatsBK[i]=tasks[i];
			//Calculate total time statistics for FIFO.
			if (tasks[i].aborted == false) {
				completedTasks = completedTasks+1;
				totalTime += tasks[i].timeTaken;
				totalWaitTime += tasks[i].waitTime;
			}
		}
		avgWaitPercentBK = (int)((double)totalWaitTime / (double)totalTime) * 100;
		totalStatsBK[0] = totalTime;
		totalStatsBK[1] = totalWaitTime;
		
		//Start printing the statistics.
		System.out.println();
		System.out.print("             FIFO             BANKER'S\n");
		//Statistics for individual tasks.
		for (int i = 0; i < numTasks; i++) {
			//For each task in FIFO.
			System.out.print("Task " + (i + 1) + "       ");
			if (tempTaskStatsFIFO[i].aborted) {
				System.out.print("aborted            ");
			}
			else {
				double waitPercentFIFO = ((double)tempTaskStatsFIFO[i].waitTime  / (double) tempTaskStatsFIFO[i].timeTaken) * 100;
				System.out.print(tempTaskStatsFIFO[i].timeTaken + "   " +
						tempTaskStatsFIFO[i].waitTime + "   " +
						(int) waitPercentFIFO + "%        ");
			}
			//For each task in BK.
			System.out.print("Task " + (i + 1) + "       ");
			if (tempTaskStatsBK[i].aborted) {
				System.out.println("aborted            ");
			}
			else {
				double waitPercentBK = ((double)tempTaskStatsBK[i].waitTime  / (double) tempTaskStatsBK[i].timeTaken) * 100;
				System.out.println(tempTaskStatsBK[i].timeTaken + "   " +
						tempTaskStatsBK[i].waitTime + "   " +
						(int) waitPercentBK + "%       ");
			}
		}  // end for
		//Total statistics.
		System.out.println("Total        " +totalStatsFIFO[0]+"   "+totalStatsFIFO[1]+"   "+avgWaitPercentFIFO+"%        Total        "+totalStatsBK[0]+"   "+totalStatsBK[1]+"   "+avgWaitPercentBK+"%\n");
	}
	
	//Executes optimistic resource management, which grants resources to a task if there are currently enough resources available.
	//Prone to having deadlock.
	public static void optimisticRM() {
		int currentCycle = 0;
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].undelayedCycle = -1;
			activeTasksList.add(tasks[i]);
		}
		
		//The algorithm executes as long as there are active tasks remaining.
		while (activeTasksList.size() > 0) {
			Task currentTask = null;
			
			//For each task.
			for (int j = 0; j < activeTasksList.size(); j++) {
				currentTask = activeTasksList.get(j);
				Activity activityData = currentTask.currentActivity;
				String whichActivity = activityData.activityType;
				
				switch(whichActivity) {
				case "initiate":
					currentTask.cycleInitiated = currentCycle;
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle){
						//Check the initial claim value.
						currentTask.updateClaimValue(currentTask, activityData);
						currentTask.moveToNextActivity();
						currentTask.currentDelay = currentTask.currentActivity.delay;
						tempTasksList.add(currentTask);
					} 
					break;
				case "request":
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle) {
						optimisticRequest(currentTask,activityData);
					}
					break;
				case "release":
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle) {
						release(currentTask, activityData);
					}
					break;
				case "terminate":
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle) {
						currentTask.terminated = true;
						currentTask.timeTaken = currentCycle;
					}		
					break;
				}
			}
			// Resolve deadlock if present
			if ((deadlockedTasksList.size() > 0) && (tempTasksList.size() == 0)) {
				resolveDeadlock();
			}
			// Add back the released resources to resourcesAvailable[]
			for (int i = 0; i < numResourceTypes; i++) {
				resourcesAvailable[i] += freedResources[i];
				freedResources[i] = 0;
			}
			//Update active tasks in an order of: deadlocked --> non-deadlocked.
			activeTasksList.clear();
			//Clear the other lists.
			activeTasksList.addAll(deadlockedTasksList);
			activeTasksList.addAll(tempTasksList);
			deadlockedTasksList.clear();
			tempTasksList.clear();
			
			currentCycle++;
		} // end while	
	}

	//Executes bankers resource management, which is a conservative algorithm.
	//Pessimistic: receives the maximum (=initial) claim and aborts a task straight away if the claim is bigger than resources available.
	//Does not have any deadlock.
	public static void bankersRM() {
		int currentCycle = 0;
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].undelayedCycle = -1;
			activeTasksList.add(tasks[i]);
		}
		
		while (activeTasksList.size() > 0) {
			Task currentTask = null;
			for (int j = 0; j < activeTasksList.size(); j++) {
				currentTask = activeTasksList.get(j);
				
				Activity activityData = currentTask.currentActivity;
				String whichActivity = activityData.activityType;
				
				switch(whichActivity) {
				case "initiate":
					currentTask.cycleInitiated = currentCycle;
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle){
						currentTask.updateClaimValue(currentTask, activityData);
						// Check if claim is higher than resources available; abort if so.
						for (int i = 0; i < numResourceTypes; i++) {
							if (currentTask.resourcesNeeded[i] > resourcesAvailable[i]) {
								currentTask.aborted = true;
								System.out.printf("Banker aborts task %d before run begins:\n\t"
										+"claim for resource %d (%d) exceeds number of units present"
										+"(%d)\n", currentTask.id, i+1, currentTask.resourcesNeeded[i], resourcesAvailable[i]);
							}
						}
						if (currentTask.aborted == false) {
							currentTask.moveToNextActivity();
							currentTask.currentDelay = currentTask.currentActivity.delay;
							tempTasksList.add(currentTask);
						}
					} 
					break;
				case "request":
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle) {
						bankersRequest(currentTask,activityData);
					}
					break;
				case "release":
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle) {
						release(currentTask, activityData);
					}
					break;
				case "terminate":
					if(!checkForDelay(currentTask, currentCycle) && currentTask.undelayedCycle != currentCycle) {
						currentTask.terminated = true;
						currentTask.timeTaken = currentCycle;
					}		
					break;
				}
			}
			// If there are blocked tasks there cannot be a deadlock in case of Banker's Algorithm, So, just release the resources that it is holding.
			if ((deadlockedTasksList.size() > 0) && (tempTasksList.size() == 0)) {
				for (int j = 0; j < deadlockedTasksList.size(); j++) {
					if (deadlockedTasksList.get(j).aborted == true) {
						System.out.printf("During cycle %d-%d of Banker's algorithms" 
					+ " Task %d's request exceeds its claim; aborted;"
					+ "\n", currentCycle, currentCycle+1, deadlockedTasksList.get(j).id);
						for (int k = 0; k < numResourceTypes; k++) {
							System.out.printf(" %d units available next cycle\n", deadlockedTasksList.get(j).resourcesOwned[k]);
							resourcesAvailable[k] += deadlockedTasksList.get(j).resourcesOwned[k];
							deadlockedTasksList.get(j).resourcesOwned[k] = 0;
						}
						//Then delete the deadlocked task.
						deadlockedTasksList.remove(deadlockedTasksList.get(j));
						break;
						
					}
				}
			}
			// Add back the released resources to resourcesAvailable[]
			for (int i = 0; i < numResourceTypes; i++) {
				resourcesAvailable[i] += freedResources[i];
				freedResources[i] = 0;
			}
			activeTasksList.clear();
			activeTasksList.addAll(deadlockedTasksList);
			activeTasksList.addAll(tempTasksList);
			deadlockedTasksList.clear();
			tempTasksList.clear();
			currentCycle += 1;
		} // end while
	}

	//Resolve deadlock by deleting the deadlocked task with a lowest id and releasing all of its resources to the pool of total avaiable resources.
	//The resources that used to belong to the deleted task can now be used for other deadlocked tasks that can potentially be un-deadlocked. 
	public static void resolveDeadlock() {
		if (deadlockedTasksList.size() != 0) {
			//Executes only if deadlocked list has more than one task.
			int numOfBlockedTasks = deadlockedTasksList.size() - 1;
			
			for (int i = 0; i < numOfBlockedTasks; i++) {
				Task lowestIDTask = findLowestID(deadlockedTasksList);
				lowestIDTask.aborted = true;
				lowestIDTask.completed = true;
				lowestIDTask.terminated = true;
				
				//Free the resources.
				for (int j = 0; j < lowestIDTask.resourcesOwned.length; j++) {
					int unitsReleased = lowestIDTask.resourcesOwned[j];
					freedResources[j] += unitsReleased;
					lowestIDTask.resourcesOwned[j] = 0;
				}
				
				//Remove the deadlocked tasks.
				deadlockedTasksList.remove(lowestIDTask);
			}
		}
	}
	
	//Helps a task to release its resources - according to its command.
	public static void release(Task currentTask, Activity activityData) {
		int whichResource = activityData.resourceType;
		int unitsOwned = currentTask.resourcesOwned[whichResource-1];
		int unitsToRelease = activityData.value;
		
		//Accept release if the units the task wants to release is a valid command.
		if (unitsOwned >= unitsToRelease) {
			freedResources[whichResource-1] += unitsToRelease;
			currentTask.resourcesOwned[whichResource-1] -= unitsToRelease;
			currentTask.resourcesNeeded[whichResource-1] += unitsToRelease;
			currentTask.moveToNextActivity();
			currentTask.currentDelay = currentTask.currentActivity.delay;
			tempTasksList.add(currentTask);
		}
		//If the task doesn't own enough resources to release, it has to wait.
		else {
			currentTask.waitTime += 1;
			deadlockedTasksList.add(currentTask); //Will be resolved after switch
		}	
	}
	
	//Used for bankers algorithm
	//Used to run a simulation to check whether the a task's request is safe to process or not.
	public static boolean isSafeState(int idRequesting, int whichResource, int howManyResourecs) {
		//A simulation task has the name "local" attached to it.
		Task localTaskRequesting = new Task();
		ArrayList<Task>localTasks = new ArrayList<Task>();
		
		for (int i = 0; i < activeTasksList.size(); i++) {
			localTasks.add(new Task());
			localTasks.get(i).id = activeTasksList.get(i).id;
			localTasks.get(i).resourcesNeeded = activeTasksList.get(i).resourcesNeeded;
			localTasks.get(i).resourcesOwned = activeTasksList.get(i).resourcesOwned;
		}
		
		for (int i = 0; i < activeTasksList.size(); i++) {
			if (activeTasksList.get(i).id == idRequesting) {
				localTaskRequesting.id = activeTasksList.get(i).id;
				localTaskRequesting.resourcesNeeded = activeTasksList.get(i).resourcesNeeded;
				localTaskRequesting.resourcesOwned = activeTasksList.get(i).resourcesOwned;
			}
		}
		// Create copy of resources (ensure no modification of original data structure)
		int[] localResourcesAvailable = new int[numResourceTypes];
		for (int i = 0; i < numResourceTypes; i++) {
			localResourcesAvailable[i] = resourcesAvailable[i];
		}
		
		//"Grant" the resources to the localTaskRequesting
		localResourcesAvailable[whichResource-1] -= howManyResourecs;
		localTaskRequesting.resourcesOwned[whichResource-1] += howManyResourecs;
		localTaskRequesting.resourcesNeeded[whichResource-1] -= howManyResourecs;
		
		// Check if the resource request leads to a safe (return true) or unsafe state (return false)
		boolean safeStateIsPossible = true; //Evaluates if a safe state is possible
		boolean taskIsCompletable = false; // Evaluates if a certain task can eventually complete
		
		// If the while loop is completed and tasks.size() > 0, then we have an unsafe state
		while (safeStateIsPossible == true) {
			safeStateIsPossible = false;//Unsafe state until proven otherwise
			
			for (int j = 0; j < localTasks.size(); j++) {
				taskIsCompletable = true; // Task is completable until proven otherwise
				
				//Check if the current task is completable
				for (int k = 0; k < numResourceTypes; k++) {
					if (localTasks.get(j).resourcesNeeded[k] > localResourcesAvailable[k]) {
						taskIsCompletable = false;
					}
				}
				//If at least one task is completable, the simulation could be in a safe state
				if (taskIsCompletable== true) {
					//Report potential safe state
					safeStateIsPossible = true;
					//Give resources back (simulate resource return)
					for (int k = 0; k < numResourceTypes; k++) {
						localResourcesAvailable[k] += localTasks.get(j).resourcesOwned[k];
					}
					//Remove "finished" task from the Arraylist
					localTasks.remove(localTasks.get(j));
				}
			}
			//if localTasks[] is empty, then simulation state is safe
			if (localTasks.size() == 0) {
				return true;
			}
		}
		//If this line is reached, localTasks is not empty, and the state is unsafe.
		return false;
	}
	
	//Helps a task to request for bankers algorithm.
	public static void bankersRequest(Task currentTask, Activity activityData) {
		int whichResource = activityData.resourceType;
		int howManyResources = activityData.value;
		int unitsAvailable = resourcesAvailable[whichResource-1];
		
		//Check if the request is illegal
		if (currentTask.resourcesNeeded[whichResource-1] < howManyResources) {
			currentTask.aborted = true;
		}
		
		// Check if the request is unsafe
		int unitsNeeded = currentTask.resourcesNeeded[whichResource-1];
		int unitsOwned = currentTask.resourcesOwned[whichResource-1];
		
		//Check if the request is safe or not -- if executing the request will result in a safe state, then it is a safe request.
		boolean isSafeRequest = isSafeState(currentTask.id, whichResource, howManyResources);
		
		//Restores the values prior to running the simulation
		currentTask.resourcesNeeded[whichResource-1] = unitsNeeded;
		currentTask.resourcesOwned[whichResource-1] = unitsOwned;
		
		//Requests.
		if (currentTask.aborted == false) {
			// Grant request in this case.
			if (unitsAvailable >= howManyResources && isSafeRequest == true) {
				resourcesAvailable[whichResource-1] -= howManyResources;
				currentTask.resourcesOwned[whichResource-1] += howManyResources;
				currentTask.resourcesNeeded[whichResource-1] -= howManyResources;
				currentTask.moveToNextActivity();
				currentTask.currentDelay = currentTask.currentActivity.delay;
				tempTasksList.add(currentTask);
			}
			// Reject request (unsafe) in this case.
			else if (unitsAvailable >= howManyResources && isSafeRequest == false) {
				currentTask.waitTime++;
				deadlockedTasksList.add(currentTask);
			}
			else {
				currentTask.waitTime++;
				deadlockedTasksList.add(currentTask);
			}
		}
		else {
			currentTask.aborted = true;
			currentTask.terminated = true;
			deadlockedTasksList.add(currentTask);
		}
	}
	
	//Helps a task to request for optimistic algorithm.
	public static void optimisticRequest(Task currentTask, Activity activityData) {
		int whichResource = activityData.resourceType;
		int howManyResources = activityData.value;
		
		//Requests in this case.
		if (resourcesAvailable[whichResource-1] >= howManyResources) {
			currentTask.resourcesOwned[whichResource-1] += howManyResources;
			currentTask.resourcesNeeded[whichResource-1] -= howManyResources;
			resourcesAvailable[whichResource-1] -= howManyResources;
			currentTask.moveToNextActivity();
			currentTask.currentDelay = currentTask.currentActivity.delay;
			tempTasksList.add(currentTask);
		}
		//Wait in this case.
		else {
			currentTask.waitTime++;
			deadlockedTasksList.add(currentTask);
		}
	}
	
	//Finds a task with the minimum id in the list.
	public static Task findLowestID(ArrayList<Task> tasksList) {
		Task lowestIDTask = tasksList.get(0);
		for (int i = 1; i < tasksList.size(); i++) {
			if (lowestIDTask.id > tasksList.get(i).id) {
				lowestIDTask = tasksList.get(i);
			}
		}
		return lowestIDTask;
	}
	
	//Checks if a task has to wait before processnig its command.
	public static boolean checkForDelay(Task currentTask, int currentCycle) {
		if (currentTask.currentActivity.delay > 0) {
			currentTask.delayed = true;
			currentTask.currentActivity.delay--;
			//If the task was just undelayed.
			if (currentTask.currentActivity.delay == 0) {
				currentTask.delayed = false;
				currentTask.undelayedCycle = currentCycle;
			}
			tempTasksList.add(currentTask);
		}
		else {
			currentTask.delayed = false;
		}
		return currentTask.delayed;
	}
	
	//Reads an input file --> done sepaprately for the two different resource management algorithms. 
	public static void readInput(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		String activityType = "";
		int taskNum = 0;
		int delay = 0;
		int resourceType = 0;
		int value = 0;

		try {
			Scanner fileInput = new Scanner(file);
			//Read T
			numTasks = fileInput.nextInt(); 
			
			//Initialize the array of tasks.
			tasks = new Task[numTasks];	
			optimisticSummaryTable = new String[numTasks][3];
			bankerSummaryTable = new String[numTasks][3];
			
			//Read R
			numResourceTypes = fileInput.nextInt();
			//Initialize an integer array of resource types
			resourcesAvailable = new int[numResourceTypes];
			
			//Initialize pending resource gain array as array of zeroes
			freedResources = new int[numResourceTypes];
			for (int i = 0; i < resourcesAvailable.length; i++) {
				//Number of total existing units for each resource
				resourcesAvailable[i] = fileInput.nextInt(); 
				freedResources[i] = 0;
			}
			//Initialize an id, and the number of resource type for each task
			for (int i = 0; i < tasks.length; i++) {
				tasks[i] = new Task((i+1), numResourceTypes);
			}
			
			// Fill the tasks with its activities from the input				
			while (fileInput.hasNext()) {
				// Retrieve activity information from input
				activityType = fileInput.next();
				taskNum = fileInput.nextInt();
				delay = fileInput.nextInt();
				resourceType = fileInput.nextInt();
				value = fileInput.nextInt();

				// Add activity to the appropriate task
				tasks[taskNum-1].addActivityForTask(activityType, delay, (resourceType), value);
			}

			//Set current activity (start activity) for the tasks??
			for (int i = 0; i < tasks.length; i++) {
				tasks[i].currentActivity = tasks[i].activitiesList.get(0);
				tasks[i].currentDelay = tasks[i].currentActivity.delay;
			}

			fileInput.close();	

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
