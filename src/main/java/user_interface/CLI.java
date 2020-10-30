package user_interface;

import controllers.Controller;
import controllers.exceptions.GraphLoadingException;
import controllers.exceptions.OutputFileException;
import controllers.exceptions.ResultEmptyException;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

import java.io.IOException;

public class CLI {
    static final int EXIT = 0;
    static TextIO textIO = TextIoFactory.getTextIO();

    static Controller controller;

    private MenuState current_state;
    private MenuState next_state;

    private void menu() {
        int choice = 1;
        while(choice != EXIT) {
            System.out.println("MENU");
            System.out.println("1 - Compute distances");
            System.out.println("2 - Configuration");
            System.out.println("3 - Overlay-Graph computation");
            System.out.println("4 - Help usage");
            System.out.println("exit - to close the program");

            choice = UtilCLI.inputParser(4);
            if(choice != EXIT) {
                next_state = MenuState.getStateFromNum(choice);
                switch_state();
            }
        }
    }

    private void switch_state() {
        switch(next_state) {
            case HELP:
                print_usage();
                break;
            case CONFIGURATION:
                configuration();
                break;
            case OVERLAY_MANAGEMENT:
                overlay_management();
                break;
            case DISTANCE_COMPUTATION:
                distance_computation();
                break;
        }
    }

    private void print_usage() {
        //TODO usage...premere un tasto qualsiasi per tornare al menu
    }

    private void configuration() {
        int choice = 1;

        while (choice != EXIT) {
            System.out.println("1 - Set-up of the Graph-Hopper graph");
            System.out.println("2 - Set the graphs path"); //modificare in path della cartella di lavoro
            System.out.println("exit - Return to home menu");
            choice = UtilCLI.inputParser(2);

            if (choice != EXIT)
                switch(choice) {
                    case 1:
                        System.out.println("Graph-Hopper - graph creation..");
                        ConfigurationCLI.graphHopperSetUp();
                        break;
                    case 2:
                        ConfigurationCLI.graphsFolderSetUp();
                        break;
                }
        }
    }

    private void overlay_management() {
        int choice = 1;
        while (choice != EXIT) {
            System.out.println("1 - Create new overlay graph");
            System.out.println("2 - Delete graph");
            System.out.println("exit - Return to home menu");
            choice = UtilCLI.inputParser(2);

            if (choice != EXIT)
                switch(choice) {
                    case 1:
                        OverlayManagementCLI.overlayCreation();
                        break;
                    case 2:
                        OverlayManagementCLI.overlayDelete();
                        break;
                }
        }
    }

    private void distance_computation() {
        if (DistanceComputationCLI.setInputFile()) return;

        if (DistanceComputationCLI.setInputFileSheetIndex()) return;

        if (DistanceComputationCLI.setGraph()) return;

        if (DistanceComputationCLI.setOutputFormat()) return;

        if (DistanceComputationCLI.setOutputPath()) return;

        System.out.println("Computation!, wait for final result");
        try {
            controller.startComputation();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Selected sheet ("+ e.getMessage() +") is not present in the workbook!\n");
            return;
        } catch (IOException e) {
            System.err.println("Impossible to perform the computation!\n" +
                    "Problem in the manage of the input file, this file must be an excel file in the correct format");
            return;
        } catch (ResultEmptyException e) {
            System.err.println("The result matrix is an empty matrix \n Output aborted");
            return;
        } catch (GraphLoadingException e) {
            System.err.println("Is not possible to load the selected graph");
            e.printStackTrace();
            return;
        } catch (OutputFileException e) {
            System.err.println("Is not possible to write the output file");
            return;
        }
        System.out.println("Computation completed! The final result has been written to the file");
    }


}
