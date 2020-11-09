package user_interface;

import java.util.List;

import static user_interface.CLI.*;

public interface UtilCLI {

    /**
     * Return true if the input is the string exit
     */
    static boolean exitCondition(String input) {
        return input.compareTo("exit") == 0;
    }

    /**
     * Given a list print all his element as
     * @param list
     */
    static void printChoiceAmongList(List<String> list) {
        int count = 0;
        textTerminal.println("\nList: ");
        for(Object l : list)
            textTerminal.println(++count + " - " + l);
    }

    /**
     * This method manage the input, if the user insert something without sense the function is recalled
     * until the input fit the condition
     * @param max define the range into which the user can choose, this range in always from 1 to max, if max is -1 means that there is no a maximum value
     * @return 0 if the command corresponds to the exit, the command if satisfy the condition
     */
    static int inputParser(int max) {
        String choice = textIO.newStringInputReader().read("input: ");
        try {
            int result = Integer.parseInt(choice);
            if(max == -1 || (result > 0 && result <= max))
                return result;
        } catch (Exception e) {
            if(exitCondition(choice))
                return 0;
        }
        printError("Unacceptable input");
        return inputParser(max);
    }

    static double inputParserDouble(int max) {
        String choice = textIO.newStringInputReader().read("input: ");
        try {
            double result = Double.parseDouble(choice);
            if(max == -1 || (result > 0 && result <= max))
                return result;
        } catch (Exception e) {
            if(exitCondition(choice))
                return 0;
        }
        printError("Unacceptable input");
        return inputParserDouble(max);
    }

    /**
     * This method manage the input between a boolean choice, if the user insert something
     * without sense the function is recalled until the input fit the condition
     * @return 1 if the choice is positive, -1 if the choice is negative and 0
     * if the user wants to exit
     */
    static int parseInputYesOrNo() {
        String choice = textIO.newStringInputReader().read("Choose between yes or no: ");
        if (choice.compareToIgnoreCase("yes") == 0 || choice.compareToIgnoreCase("y") == 0)
            return 1;
        if (choice.compareToIgnoreCase("no") == 0 || choice.compareToIgnoreCase("n") == 0)
            return -1;
        if(exitCondition(choice))
            return 0;
        printError("Unacceptable input");
        return parseInputYesOrNo();
    }

    static void printError(String message) {
        textTerminal.getProperties().setPromptColor(ERROR_COLOR);
        textTerminal.println(message);
        textTerminal.getProperties().setPromptColor(TERMINAL_COLOR);
    }

}
