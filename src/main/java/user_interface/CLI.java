package user_interface;

import controllers.Controller;
import controllers.exceptions.GraphLoadingException;
import controllers.exceptions.OutputFileException;
import controllers.exceptions.ResultEmptyException;
import objects.ParamsObject;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.io.IOException;

import static user_interface.UtilCLI.printError;

public class CLI {
    static final String TERMINAL_COLOR = "cyan";
    static final String ERROR_COLOR = "red";

    static final int EXIT = 0;
    static TextIO textIO = TextIoFactory.getTextIO();
    static TextTerminal textTerminal = TextIoFactory.getTextTerminal();

    static Controller controller;

    private MenuState nextState;

    public void menu() {
        textTerminal.getProperties().setPromptColor(TERMINAL_COLOR);
        controller = new Controller();
        int choice = 1;
        while(choice != EXIT) {
            textTerminal.println("\nMENU");
            textTerminal.println("1 - Compute distances");
            textTerminal.println("2 - Configuration");
            textTerminal.println("3 - Overlay-Graph computation");
            textTerminal.println("4 - Help usage");
            textTerminal.println("exit - to close the program");

            choice = UtilCLI.inputParser(4);
            if(choice != EXIT) {
                nextState = MenuState.getStateFromNum(choice);
                switch_state();
            }
        }
        textTerminal.println("\nSee you");
        textTerminal.abort();
    }

    private void switch_state() {
        switch(nextState) {
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
            textTerminal.println("\n1 - Set-up of the Graph-Hopper graph");
            textTerminal.println("2 - Overlay-graphs configuration");
            textTerminal.println("3 - Reset configuration");
            textTerminal.println("4 - Clean the dumps files");
            textTerminal.println("exit - Return to home menu");
            choice = UtilCLI.inputParser(4);
            if (choice != EXIT)
                switch(choice) {
                    case 1:
                        ConfigurationCLI.graphHopperSetUp();
                        textTerminal.println("\nGraph-Hopper - graph creation..");
                        break;
                    case 2:
                        try {
                            controller.createConfigurationFile(ConfigurationCLI.graphParamsSetup(new ParamsObject()));
                        } catch (IOException e) {
                            printError("Error during the creation of the configuration file \n" +
                                "creation aborted");
                        }
                        break;
                    case 3:
                        try {
                            controller.deleteConfigurationFile();
                            textTerminal.println("\nConfiguration parameters resetted");
                        } catch (IOException e) {
                            printError("Impossible to reset the configuration");
                        }
                        break;
                    case 4:
                        try {
                            ConfigurationCLI.flushDumps();
                            textTerminal.println("\nDumps flushed");
                        } catch (IOException e) {
                            printError("Problem during the clean of the dumps");
                        }
                        break;
                }
        }
    }

    private void overlay_management() {
        int choice = 1;
        while (choice != EXIT) {
            textTerminal.println("\n1 - Create new overlay graph");
            textTerminal.println("2 - Delete graph");
            textTerminal.println("exit - Return to home menu");
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

        textTerminal.println("\nComputation!, wait for final result");
        try {
            controller.startComputation();
        } catch (ArrayIndexOutOfBoundsException e) {
            printError("Selected sheet ("+ e.getMessage() +") is not present in the workbook!\n");
            return;
        } catch (IOException e) {
            printError("Impossible to perform the computation!\n" +
                    "Problem in the manage of the input file, this file must be an excel file in the correct format");
            return;
        } catch (ResultEmptyException e) {
            printError("The result matrix is an empty matrix \n Output aborted");
            return;
        } catch (GraphLoadingException e) {
            printError("Is not possible to load the selected graph");
            e.printStackTrace();
            return;
        } catch (OutputFileException e) {
            printError("Is not possible to write the output file");
            return;
        }
        textTerminal.println("\nComputation completed! The final result has been written to the file");
    }
}
