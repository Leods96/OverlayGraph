package user_interface;

import controllers.exceptions.GraphCreationException;
import controllers.exceptions.InputFileException;
import input_output.exceptions.CheckPointException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static user_interface.CLI.*;
import static user_interface.UtilCLI.exitCondition;
import static user_interface.UtilCLI.printError;

public interface OverlayManagementCLI {

    static void overlayCreation() {
        if(controller.checkOverlayCreationInProgress()) {
            textTerminal.println("\nA saved partial graph pre-computation has been find");
            textTerminal.println("1 - Continue from this check-point");
            textTerminal.println("2 - Create new overlay graph (this saved work will be deleted)");
            textTerminal.println("exit - Return to previous menu");
            int choice = UtilCLI.inputParser(2);

            if (choice == EXIT) return;
            if (choice == 1) {
                textTerminal.println("\nComputation restarted..");
                try {
                    controller.reStartOverlayCreation();
                } catch (FileNotFoundException e) {
                    printError("Check Point file corrupted \n computation aborted");
                    return;
                }  catch (IOException e) {
                    printError("Unable to create the dumps in the creation of the overlay graph \n creation aborted");
                    return;
                } catch (InputFileException e) {
                    printError("Is not possible to read the specified input file, check point could be corrupted \n creation aborted");
                    return;
                } catch (GraphCreationException e) {
                    printError("Unable to write the overlay graph on the disk \n creation aborted");
                    return;
                } catch (CheckPointException e) {
                    printError("Unable to create the check-point \n creation aborted");
                    return;
                }
                textTerminal.println("\nGraph completed");
                return;
            }
        }
        createNewGraph();
    }

    static void overlayDelete() {
       List<String> graphs = controller.getExistingGraphs();
       UtilCLI.printChoiceAmongList(graphs);
       textTerminal.println("Choose the graph to be deleted");
       int choice = UtilCLI.inputParser(graphs.size());
       if(choice == EXIT) return;
       controller.setGraphToUse(graphs.get(choice - 1));
       try {
           controller.deleteGraph();
           textTerminal.println( graphs.get(choice - 1) + " graph deleted");
       } catch (IOException e) {
           printError("Is not possible to delete this graph");
       }
    }

    static void createNewGraph() {
        String input;
        boolean condition;

        //Set of the overlay points' file
        textTerminal.println("\nInsert the absolute path of the file containing the overlay points: ");
        do {
            condition = true;
            input = textIO.newStringInputReader().read("Path: ");
            if (exitCondition(input)) return;
            try {
                controller.setInputAddressFile(input);
            } catch (Exception e) {
                printError(e.getMessage());
                condition = false;
            }
        } while(!condition);

        //Set of the file's index
        textTerminal.println("\nInsert the index of the sheet to read in the excel file");
        int choice = UtilCLI.inputParser(-1);
        if(choice == EXIT) return;
        controller.setSheetIndex(choice);

        //Set graph's name
        textTerminal.println("\nInsert the name of the overlay graph: ");
        do {
            condition = true;
            input = textIO.newStringInputReader().read("Name");
            if (exitCondition(input)) return;
            try {
                controller.setOverlayGraphName(input);
            } catch (GraphCreationException e) {
                textTerminal.println("Name already in use");
                condition = false;
            }
        } while(!condition);

        //Enable check point
        textTerminal.println("\nDo you want to enable the check-point? ");
        choice = UtilCLI.parseInputYesOrNo();
        if (choice == EXIT) return;
        controller.useCheckPoint(choice > 0);

        //Enable kdTree
        textTerminal.println("\nDo you want to use a KdTree supporter ? (Yes = KdTree supporter/No = Linear supporter)? ");
        choice = UtilCLI.parseInputYesOrNo();
        if (choice == EXIT) return;
        controller.useKdTree(choice > 0);

        textTerminal.println("\nOverlay pre-computation started..");
        try {
            controller.startOverlayPreComputation();
        } catch (IOException e) {
            printError("Unable to create the dumps in the creation of the overlay graph \n creation aborted");
            return;
        } catch (InputFileException e) {
            printError("Is not possible to read the specified input file \n creation aborted");
            return;
        } catch (GraphCreationException e) {
            printError("Unable to write the overlay graph on the disk \n creation aborted");
            return;
        } catch (CheckPointException e) {
            printError("Unable to create the check-point \n creation aborted");
            return;
        }
        textTerminal.println("\nOverlay graph computed");
    }

}
