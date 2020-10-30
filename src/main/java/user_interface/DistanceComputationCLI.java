package user_interface;

import input_output.OutputFormat;

import java.util.List;

import static user_interface.CLI.*;
import static user_interface.UtilCLI.exitCondition;

/**
 * This interface contains all the support functions of the CLI that are used to manage the setup of the
 * distance matrix computation.
 * Each function menage one aspect of the setup:
 * - setInputFile: Manage the file used as an input that contains the addresses
 * - setInputFileSheetIndex: Take the excel file sheet's index
 * - setGraph: Used to select the graph to use for the computation
 * - setOutputFormat: Used to choose the distance matrix format
 * - setOutputPath: Used to choose the path of the output file
 * - setDoubleComputation: Used to choose the precision in the computation between one-way or roundtrip
 * Each function have a boolean return value that is used to manage the EXIT condition, if the parser scans the
 * exit keyword the function return a true otherwise return a false.
 * The return of the true will be managed as an interrupt of the distance computation setup process
 */
public interface DistanceComputationCLI {

    static boolean setInputFile() {
        boolean condition;
        do {
            condition = true;
            System.out.println("Absolute path of the file containing the input addresses on which work");
            String path = textIO.newStringInputReader().read("Path: ");
            if(exitCondition(path)) return true;
            try {
                controller.setInputAddressFile(path);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                condition = false;
            }
        } while (!condition);
        return false;
    }

    static boolean setInputFileSheetIndex() {
        System.out.println("Insert the index of the sheet to read in the excel file");
        int choice = UtilCLI.inputParser(-1);
        if(choice == EXIT) return true;
        controller.setSheetIndex(choice);
        return false;
    }

    static boolean setGraph() {
        List<String> graphs = controller.getExistingGraphs();
        UtilCLI.printChoiceAmongList(graphs);
        int choice = UtilCLI.inputParser(graphs.size());
        if(choice == EXIT) return true;
        controller.setGraphToUse(graphs.get(choice - 1));
        return false;
    }

    static boolean setOutputFormat() {
        int choice = UtilCLI.inputParser(OutputFormat.printFormats());
        if(choice == EXIT) return true;
        controller.setOutputFormat(OutputFormat.getOutputFromNum(choice));
        if(OutputFormat.getOutputFromNum(choice) == OutputFormat.EXCEL_FILE_LIST)
            return setDoubleComputation();
        return false;
    }

    static boolean setDoubleComputation() {
        System.out.println("Select the computation option of the distance matrix: ");
        System.out.println("1 - One way approximation");
        System.out.println("2 - Differentiation between direction ");
        int choice = UtilCLI.inputParser(2);
        if (choice == EXIT) return true;
        controller.setRoundTripOption(choice == 2);
        return false;
    }

    static boolean setOutputPath() {
        System.out.println("Absolute path where write the output: ");
        String name = textIO.newStringInputReader().read("Path: ");
        if(exitCondition(name)) return true;
        controller.setOutputName(name);
        return false;
    }
}
