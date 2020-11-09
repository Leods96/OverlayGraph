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
    static final String TERMINAL_COLOR = "white";
    static final String ERROR_COLOR = "red";
    static final String MENU_COLOR = "cyan";

    static final int EXIT = 0;
    static TextIO textIO = TextIoFactory.getTextIO();
    static TextTerminal textTerminal = textIO.getTextTerminal();
    static Controller controller;

    private MenuState nextState;
    private boolean printingMenu = false;

    public void menu() {
        textTerminal.getProperties().setPromptColor(TERMINAL_COLOR);
        controller = new Controller();
        int choice = 1;
        while(choice != EXIT) {
            switchColorMenu();
            textTerminal.println("\n\n");
            textTerminal.println("################");
            textTerminal.println("##### MENU #####");
            textTerminal.println("################");
            textTerminal.println("   1 - Compute distances");
            textTerminal.println("   2 - Configuration");
            textTerminal.println("   3 - Overlay-Graph management");
            textTerminal.println("   4 - Help usage");
            textTerminal.println("exit - to close the program");
            switchColorMenu();

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
        switchColorMenu();
        textTerminal.println("\n\n");
        textTerminal.println("#################");
        textTerminal.println("##### USAGE #####");
        textTerminal.println("#################");
        textTerminal.println("\n* This is a Command Line Interface for the use of the overlay-graph " +
                "framework \n" +
                "* This CLI is composed by different menus and is possible to navigate into these menus " +
                "following the instruction of each of them \n" +
                "* Depending on the type of menu the possible inputs are: \n" +
                "\t - Number: to select a choice between a list of actions\n" +
                "\t - YES/NO: to select a choice between a pair (possible input: y/yes/n/no) \n" +
                "\t - Path: to specify a file to be read, this must be an absolute path containing" +
                " also the name of the file and the extension\n" +
                "\t - the \"exit\" keyword: to close a menu or stop the execution of a process\n" +
                "* You can find more explaination on the framework use into the documentation\n" +
                "ENJOY!\n");
        switchColorMenu();
        textTerminal.println("Press enter to continue..");
        textIO.newStringInputReader().withMinLength(0).read();
    }

    private void configuration() {
        int choice = 1;

        while (choice != EXIT) {
            switchColorMenu();
            textTerminal.println("\n\n");
            textTerminal.println("#########################");
            textTerminal.println("##### CONFIGURATION #####");
            textTerminal.println("#########################\n");
            textTerminal.println("   1  - Set-up of the Graph-Hopper graph");
            textTerminal.println("   2  - Overlay-graphs configuration");
            textTerminal.println("   3  - Reset Overlay-graphs configuration");
            textTerminal.println("   4  - Clean the dumps files");
            textTerminal.println("   5  - Clean the output files");
            textTerminal.println("exit  - Return to home menu");
            switchColorMenu();
            choice = UtilCLI.inputParser(5);
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
                    case 5:
                        try {
                            ConfigurationCLI.flushOutputs();
                            textTerminal.println("\nOutputs flushed");
                        } catch (IOException e) {
                            printError("Problem during the clean of the outputs");
                        }
                        break;
                }
        }
    }

    private void overlay_management() {
        int choice = 1;
        while (choice != EXIT) {
            switchColorMenu();
            textTerminal.println("\n\n");
            textTerminal.println("##############################");
            textTerminal.println("##### OVERLAY MANAGEMENT #####");
            textTerminal.println("##############################\n");
            textTerminal.println("   1 - Create new overlay graph");
            textTerminal.println("   2 - Delete graph");
            textTerminal.println("exit - Return to home menu");
            switchColorMenu();
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

    private void switchColorMenu() {
        this.printingMenu = !this.printingMenu;
        UtilCLI.switchColorMenu(printingMenu);
    }
}
