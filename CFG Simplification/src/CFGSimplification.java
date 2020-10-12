// Authors: Maik De Leon Lopez, Jason Mendez

import java.util.ArrayList;
import java.util.Scanner;

public class CFGSimplification {

	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		System.out.println("Please Enter The Number of Variables: ");
		int numVar = reader.nextInt();
		reader.nextLine();
		System.out.println("Please Enter the Alphabet: (separated by commas)");
		String alphabet = reader.nextLine();
		int alphaSize = 1;	// We must accept at least one alphabet letter for this to work
		for (int i = 0; i < alphabet.length(); i++) {
			if (alphabet.charAt(i) == ',') {	// we have 1+as many commas that we read
				alphaSize++;
			}
		}
		String[] alphaList = new String[alphaSize];	// Create an array of that size
		//System.out.println("alphaSize" + alphaSize); used to troubleshoot	
		int temp = 0;
		for (int i = 0; i < alphabet.length(); i++) {
			if (alphabet.charAt(i) == ',') {	// dont add commas to our alphabet array
				continue;
			} else if (alphabet.charAt(i) != '\0' && temp < alphaSize) {
				// System.out.println("adding");
				alphaList[temp] = "" + alphabet.charAt(i);	// if the character is not eol character and is part of our alphabet
				temp++;	// add that character to our array
			}
		}
		/*
		 * for (int i = 0; i < alphaSize; i++) { System.out.println(alphaList[i]); } used to troubleshoot
		 */
		System.out.println("Please Enter The Rules in the following format: Variable-rule1|rule2 "
				+ "\nWhere rule1 and rule2 can be a mix of terminals and variables (0 denotes empty rule) ");
		String[] rules = new String[numVar];	// hold the rules in a string array later we parse the array
		for (int i = 0; i < numVar; i++) {
			System.out.println("Enter Variable # " + i + " Rules");
			rules[i] = reader.next();	// ask the user for each variable's rules
		}
		int[] ruleCount = new int[numVar];	// count how many rules we have for each variable
		int totalRules = 0;	// the total amount of rules we have
		for (int i = 0; i < numVar; i++) {
			// System.out.println("Rule # "+i+" is: "+rules[i]);
			ruleCount[i] = numOccurrences(rules[i], '|');	// we count how many |'s we have to determine the number of rules
			totalRules += ruleCount[i];
			// System.out.println("Number of Rules is : "+ruleCount[i]);
		}
		String[][] rulesList = new String[numVar][totalRules + 1];	// Create a 2d array to hold the rules for each variable
		for (int i = 0; i < numVar; i++) {
			rulesList[i][0] = rules[i].substring(0, rules[i].indexOf('-'));	// add the variable to the 0th index of its row
			String currentRule = rules[i].substring(rules[i].indexOf('-') + 1);	// remove that part from our rule, we will continue to parse the string
			;
			for (int j = 1; j < ruleCount[i] + 1; j++) {
				if (currentRule.contains("|")) {	// if the rule contains a | we save the part that is before it as rule #j
					// System.out.println("current rule: "+currentRule+" splitindex :
					// "+currentRule.indexOf('|'));
					rulesList[i][j] = currentRule.substring(0, currentRule.indexOf('|'));
					currentRule = currentRule.substring(currentRule.indexOf('|') + 1);	// remove that part from our rule, continue to parse the string
				} else {
					rulesList[i][j] = currentRule;	// if there are no more |'s then we reached our last rule
				}
			}
		}
		// System.out.println("numVar = "+rules.length);	used to troubleshoot
		System.out.println("Rules Before Remove Empty-Rules: ");
		printRules(rulesList, numVar, ruleCount);
		rulesList = removeEmpty(rulesList, numVar, ruleCount, totalRules);
		System.out.println("Rules After Removing Empty-Rules");
		printRules(rulesList, numVar, ruleCount);
		rulesList = removeUseless(rulesList, numVar, ruleCount, alphaList, totalRules);
		System.out.println("Rules After Removing Useless-Rules");
		printRules(rulesList, numVar, ruleCount);
		reader.close();
	}

	// This method prints our rules in a format that was easy to maintain
	public static void printRules(String[][] rules, int numVar, int[] ruleCount) {
		String s = "";	// initialize an empty string
		for (int i = 0; i < rules.length; i++) {	// for as many variables as we have
			if (!rules[i][0].contentEquals("empty") && rules[i][1] != null) {	// if the variable is empty or if the first rule is null skip it
				s += ("Variable " + rules[i][0] + " Rules\n");	//if the variable exists continue printing
			}
			for (int j = 1; j < rules[i].length; j++) {	// for as many rules we have for each variable
				if (rules[i][j] == null || rules[i][j].contentEquals("empty") || rules[i][j].contains("empty")) {
					continue;	// if the rule is null or is empty dont print it
				}
				s += (rules[i][j] + "\n");	// otherwise print it
			}
		}
		System.out.println(s); //print our string

	}
	
	// This method removes any empty string "0" and replaces if needed with all possible combinations
	public static String[][] removeEmpty(String[][] rulesList, int numVar, int[] ruleCount, int totalRules) {
		String currentRule;	
		String[][] newRulesList = new String[numVar][(int) Math.pow(2, totalRules)];	// Our new rule list will have up to 2^#of total rules
		ArrayList<String> VariableToRemove = new ArrayList<String>();
		// System.out.println("Step 0: ");
		for (int i = 0; i < numVar; i++) {
			if (rulesList[i][1].contentEquals("0") && rulesList[i][2] == null) {	// if the first rule is empty and second rule is null (meaning it is the only rule and the only rule is empty)
				rulesList[i][1] = "empty";	// call that rule empty
				VariableToRemove.add(rulesList[i][0]);	// we will remove that variable later on
			}
		}
		for (int k = 0; k < VariableToRemove.size(); k++) {	// if we have any variables to remove
			for (int i = 0; i < numVar; i++) {	// for every variable
				if (VariableToRemove.get(k).contentEquals(rulesList[i][0])) {	// if the variable to remove matches that variable
					rulesList[i][0] = "empty";	// call that variable empty
				}
				for (int j = 1; j < ruleCount[i] + 1; j++) {	// for every rule in every variable
					if (rulesList[i][j].contains(VariableToRemove.get(k))) {	//if the variable to remove is in their rule
						rulesList[i][j] = rulesList[i][j].replaceAll(VariableToRemove.get(k), "");	// we replace all instances of the variable with an empty space (substitution)
					}
				}
			}
		}
		// System.out.println("Step 1: ");
		ArrayList<String> V2 = new ArrayList<String>();	// Initialize an array list 
		for (int i = 0; i < numVar; i++) {	// for every variable
			for (int j = 1; j < ruleCount[i] + 1; j++) {	// for every variable's rules
				currentRule = rulesList[i][j];	// set the current rule to the one we are visiting
				if (currentRule.contentEquals("0")) {	// if the current rule is 0 
					V2.add(rulesList[i][0]);	// add the variable to V2 and make it "empty"
					currentRule = "empty";
					rulesList[i][j] = currentRule;
				}
			}
		}
		/*System.out.println("V2 is: ");
		for (int i = 0; i < V2.size(); i++) {	used to troubleshoot
			System.out.println(V2.get(i));
		}*/
		// System.out.println("Step 2: ");
		for (int i = 0; i < numVar; i++) {	// for every variable
			for (int j = 1; j < ruleCount[i] + 1; j++) {	// for every variable's rules
				currentRule = rulesList[i][j];	// set the current rule to the one we are visiting
				for (int k = 0; k < V2.size(); k++) {	// for every variable in V2
					if ((currentRule = currentRule.replaceAll(V2.get(k), "")).contentEquals("")) {
						V2.add(rulesList[i][0]);	// if the current rule is of the form B-A1A2..An where An are in V2 then put B into V2
					}
				}
			}
		}
		for (int i = 0; i < numVar; i++) {	// for every Variable
			for (int j = 0; j < ruleCount[i] + 1; j++) {	// for every variable's rules
				newRulesList[i][j] = rulesList[i][j];	// Add that rule into our new rules List 
			}
		}
		// System.out.println("Step 4: ");
		for (int i = 0; i < newRulesList.length; i++) {	// for every variable
			for (int j = 1; j < ruleCount[i] + 1; j++) {	// for every variable's rules
				currentRule = newRulesList[i][j];	// save the rule as current rule
				if (currentRule != null) {	// if the rule is not null
					for (int k = 0; k < V2.size(); k++) {	// for every variable in V2
						if (currentRule.contains(V2.get(k))) {	// if the rule contains a variable in V2
							// add all possible combinations
							//System.out.println("add all possible combo");
							if (j + 1 < newRulesList[i].length && j < ruleCount[i] + 1) {	// if we are within range
								if (newRulesList[i][j + 1] == null) {	// if the next variable is null 
									//System.out.println("changing");
									newRulesList[i][j + 1] = newRulesList[i][j].replaceFirst(V2.get(k), "");//add a rule without our current rule without variable in V2
								} else {	// otherwise
									//System.out.println("looking for next empty space");
									int l = j;
									while (l < newRulesList[i].length && newRulesList[i][l] != null) {
										l++;
									}	// we look for the next empty space and add our rule there
									newRulesList[i][l] = newRulesList[i][j].replaceFirst(V2.get(k), "");
								}
							}
						}
					}
				}
			}
		}
		return newRulesList;
	}
	
	// This method removes all useless rules
	public static String[][] removeUseless(String[][] rulesList, int numVar, int[] ruleCount, String[] alphabet,
			int totalRules) {
		ArrayList<String> V1 = new ArrayList<String>(); // Step 1: initialize V1 to be empty
		String currentRule;
		// Step 2: add all variables whose rules are in the form of 0 and alphabet
		for (int i = 0; i < rulesList.length; i++) {	// for every variable
			for (int j = 1; j < rulesList[i].length; j++) {	// for every variable's rules
				currentRule = rulesList[i][j];	
				if (currentRule != null) {	// if the current rule is null skip
					for (int k = 0; k < alphabet.length; k++) {	// otherwise for every letter in our alphabet
						// System.out.println(alphabet[k]);
						if (currentRule.contains(alphabet[k])) {	// if the rule contains the letter
							currentRule = currentRule.replaceAll(("[" + alphabet[k] + "]"), "");	// replace the letter with ""
							// System.out.println(currentRule);
						}
					}
					if (currentRule.contentEquals("")) {	// if after removing all letters, we have "" then it was a rule with only terminals
						// System.out.println("h");
						V1.add(rulesList[i][0]);	// therefore we add this rule to V1
					}
				}
			}
		}
		/*
		 * System.out.println("V1: "); for (int i = 0; i < V1.size(); i++) {
		 * System.out.println(V1.get(i)); }
		 */
		for (int i = 0; i < rulesList.length; i++) {	// for every variable
			for (int j = 1; j < rulesList[i].length; j++) {	// for every variable's rules
				currentRule = rulesList[i][j];	
				if (currentRule != null) {	
					for (int k = 0; k < V1.size(); k++) {	// for every variable in V1
						currentRule = currentRule.replaceAll(("[" + V1.get(k) + "]"), "");	// replace the variable in V1 by ""
						// System.out.println(currentRule);
						for (int l = 0; l < alphabet.length; l++) {	// replace all terminals with ""
							currentRule = currentRule.replaceAll("[" + alphabet[l] + "]", "");
						}
					}
					if (currentRule.contentEquals("")) {	// if the current rule is now "" then it is in the form of 0 U alphabet U V1
						if (!V1.contains(rulesList[i][0])) {	// if the rule is already in V1 dont add 
							V1.add(rulesList[i][0]);
						}
					}
				}
			}
		}
		// Step 3 Keep Only Rules in {e}UalphabetUV1
		/*
		 * System.out.println("2nd V1: "); for (int i = 0; i < V1.size(); i++) {
		 * System.out.println(V1.get(i)); }
		 */
		String[][] newRulesList = new String[rulesList.length][(int) Math.pow(2, totalRules)];
		for (int i = 0; i < rulesList.length; i++) {
			newRulesList[i][0] = rulesList[i][0];
			for (int j = 1; j < rulesList[i].length; j++) {
				currentRule = rulesList[i][j];
				if (currentRule != null) {
					for (int k = 0; k < V1.size(); k++) {
						currentRule = currentRule.replaceAll(("[" + V1.get(k) + "]"), "");
						for (int l = 0; l < alphabet.length; l++) {
							currentRule = currentRule.replaceAll("[" + alphabet[l] + "]", "");
						}
					}
					if (currentRule.contentEquals("")) {
						newRulesList[i][j] = rulesList[i][j]; // we only store the rules in newRulesList if they are in the form of
					}	// 0 U alphabet U V1 otherwise we ignore those rules
				}
			}
		}

		// Step 4 Create Dependency Graph and eliminate any nodes not reached by start
		// Variable

		boolean[] isVarReachable = new boolean[newRulesList.length];	// creates an array of size equal to that of the # of variables
		isVarReachable[0] = true; // our first variable is reachable as it is our start symbol
		//System.out.println("looking for reachables");
		for (int k = 0; k < newRulesList.length; k++) {	// for every variable
			for (int j = 0; j < newRulesList[k].length; j++) {	// for every variable's rules
				if (newRulesList[k][j] == null) {	// if the rule is null
					continue;		// skip it 
				} else {
					for (int i = 1; i < newRulesList.length; i++) {	// otherwise for every variable
						if (newRulesList[0][j].contains(newRulesList[i][0])) {	// if the start variable contains another variable
							isVarReachable[i] = true;	// set that variable's index to true
						}
					}
				}
			}

		}
		/*for (int i = 0; i < newRulesList.length; i++) {
			System.out.println("Var: " + newRulesList[i][0] + " is " + isVarReachable[i] + " reachable");
		}*/
		//Delete Un-Reachables
		for(int i = 0; i < newRulesList.length; i++) {	// for every variable
			for(int j = 1; j < newRulesList[i].length; j++) {	// for every variable's rules
				if(isVarReachable[i] == false) {	// if the variable is unreachable
						newRulesList[i][j] = null;	// set the rule to null
				}
			}
		}
		return newRulesList;
	}

	// This method checks and returns the number of occurrences of a character in a string
	public static int numOccurrences(String s, char check) {
		int count = 1;	// automatically set the count to 1
		for (int i = 0; i < s.length(); i++) {	// check the whole string
			if (s.charAt(i) == check) {	// if the character equals the character we want to check for
				count++;	// increment the counter
			}
		}
		return count;
	}
}
