package user_interface;

import controllers.exceptions.GraphCreationException;
import controllers.exceptions.InputFileException;
import input_output.exceptions.CheckPointException;

import java.io.IOException;
import java.util.List;

import static user_interface.CLI.*;
import static user_interface.UtilCLI.exitCondition;

public interface OverlayManagementCLI {

    static void overlayCreation() {
        if(controller.checkOverlayCreationInProgress()) {
            System.out.println("A saved partial graph pre-computation has been find");
            System.out.println("1 - Continue from this check-point");
            System.out.println("2 - Create new overlay graph (this saved work will be deleted)");
            System.out.println("exit - Return to previous menu");
            int choice = UtilCLI.inputParser(2);

            if (choice == EXIT) return;
            if (choice == 1) {
                System.out.println("Computation restarted..");
                controller.reStartOverlayCreation();
                System.out.println("Graph completed");
                return;
            }
        }
        createNewGraph();
    }

    static void overlayDelete() {
       List<String> graphs = controller.getExistingGraphs();
       UtilCLI.printChoiceAmongList(graphs);
       int choice = UtilCLI.inputParser(graphs.size());
       if(choice == EXIT) return;
       controller.setGraphToUse(graphs.get(choice - 1));
       controller.deleteGraph();
    }

    static void createNewGraph() {
        String input;
        boolean condition;

        //Set of the overlay points' file
        do {
            condition = true;
            System.out.println("Insert the absolute path of the file containing the overlay points: ");
            input = textIO.newStringInputReader().withPattern("/^\\\\(?!\\\\)/").read();
            if (exitCondition(input)) return;
            try {
                controller.setInputAddressFile(input);
            } catch (Exception e) {
                condition = false;
            }
        } while(!condition);

        //Set of the file's index
        System.out.println("Insert the index of the sheet to read in the excel file");
        int choice = UtilCLI.inputParser(-1);
        if(choice == EXIT) return;
        controller.setSheetIndex(choice);

        //Set graph's name
        do {
            condition = true;
            System.out.println("Insert the name of the overlay graph: ");
            input = textIO.newStringInputReader().read();
            if (exitCondition(input)) return;
            try {
                controller.setOverlayGraphName(input);
            } catch (Exception e) {
                System.err.println("Name already in use");
                condition = false;
            }
        } while(!condition);

        //Enable check point
        System.out.println("Do you want to enable the check-point? ");
        choice = UtilCLI.parseInputYesOrNo();
        if (choice == EXIT) return;
        if (choice > 0) controller.enableCheckPoint();
        else controller.disableCheckPoint();

        System.out.println("Overlay pre-computation started..");
        try {
            controller.startOverlayPreComputation();
        } catch (IOException e) {
            System.err.println("Unable to create the dumps in the creation of the overlay graph \n creation aborted");
            return;
        } catch (InputFileException e) {
            System.err.println("Is not possible to read the specified input file \n creation aborted");
            return;
        } catch (GraphCreationException e) {
            System.err.println("Unable to write the overlay graph on the disk \n creation aborted");
            return;
        } catch (CheckPointException e) {
            System.err.println("Unable to create the check-point \n creation aborted");
            return;
        }
        System.out.println("Overlay graph computed");
    }

}
