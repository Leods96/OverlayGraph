package user_interface;

import input_output.ExternalFileManager;
import objects.ParamsObject;

import java.io.IOException;

import static controllers.Controller.dumpsPath;
import static controllers.Controller.outputPath;
import static user_interface.CLI.*;
import static user_interface.UtilCLI.*;

public interface ConfigurationCLI {

    static void graphHopperSetUp() {
        boolean condition;
        textTerminal.println("\nInsert the OSM file's absolute path: ");
        do {
            condition = true;
            String input = textIO.newStringInputReader().read("Path");
            if (exitCondition(input)) return;
            try {
                controller.setOSMPath(input);
            } catch (Exception e) {
                printError("Unacceptable OSM input file");
                condition = false;
            }
        } while (!condition);

        if (!controller.graphsFolderExist() && !controller.graphsFolderSetUp()) {
            printError("Impossible to create the graph folder");
            return;
        }
        try {
            textTerminal.println("\nCreation of the graph hopper graph, wait..");
            controller.graphHopperGraphCreation();
        } catch (Exception e) {
            printError("Problem in the creation of graph-hopper");
            return;
        }
        textTerminal.println("\nGraph-Hopper graph created and saved");
    }

    static ParamsObject graphParamsSetup(ParamsObject po) {
        textTerminal.println("\n\n");
        textTerminal.println("######################");
        textTerminal.println("##### PARAMETERS #####");
        textTerminal.println("######################\n");
        textTerminal.println("   1 - ANGLE_NEIGHBOURS_HINT \n" +
                "   2 - BEST_PATH_CHOSE_OVER_OVERLAY_ONLY \n" +
                "   3 - NEIGHBOURS_THRESHOLD \n" +
                "   4 - THRESHOLD_NEIGHBOUR_DISTANCE \n" +
                "   5 - SPLIT_LATITUDE\n" +
                "   6 - NUMBER_OF_NN\n" +
                "exit - To save the configuration and return to previous menu");
        int choice = inputParser(6);
        if(choice == EXIT)
            return po;
        switch (choice) {
            case 1:
                textTerminal.println("\n######################");
                textTerminal.println("\nActivation of the ANGLE_NEIGHBOURS_HINT parameter \n" +
                        "This parameter define the use of the angle hint research during the NNR \n" +
                        "yes - will activate it \n" +
                        "no - will deactivate it");
                int a = parseInputYesOrNo();
                if (a != EXIT)
                    po.setAngleHint(a == 1);
                break;
            case 2:
                textTerminal.println("\n######################");
                textTerminal.println("\nActivation of the BEST_PATH_CHOSE_OVER_OVERLAY_ONLY parameter \n" +
                        "yes - will activate it \n" +
                        "no - will deactivate it");
                int b = parseInputYesOrNo();
                if (b != EXIT)
                    po.setOverlayOnly(b == 1);
                break;
            case 3:
                textTerminal.println("\n######################");
                textTerminal.println("\nTuning of the NEIGHBOURS_THRESHOLD parameter \n" +
                        "This threshold represents the maximum allowed distance (in meters) " +
                        "between the nearest neighbour and the other possible neighbours");
                int c = inputParser(-1);
                if (c != EXIT)
                    po.setNeighbourThreshold(c);
                break;
            case 4:
                textTerminal.println("\n######################");
                textTerminal.println("\nTuning of the THRESHOLD_NEIGHBOUR_DISTANCE parameter \n" +
                        "This threshold represents the maximum allowed distance (in meters) between the" +
                        " overlay point and his neighbours");
                int d = inputParser(-1);
                if (d != EXIT)
                    po.setNeighbourDistance(d);
                break;
            case 5:
                textTerminal.println("\n######################");
                textTerminal.println("\nChoose of the SPLIT_LATITUDE parameter \n" +
                        "This params says if the first split of a KdTree will be done by a latitude split\n" +
                        "or a longitude split" +
                        "yes - will split on latitude \n" +
                        "no - will split on longitude");
                int e = parseInputYesOrNo();
                if (e != EXIT)
                    po.setSplitLatitude(e == 1);
                break;
            case 6:
                textTerminal.println("\n######################");
                textTerminal.println("\nTuning of the NUMBER_OF_NN parameter \n" +
                        "This param defines the number of neighbours for each NN research");
                int f = inputParser(-1);
                if (f != EXIT)
                    po.setNumberNN(f);
                break;
        }
        return graphParamsSetup(po);
    }


    static void flushDumps() throws IOException {
        new ExternalFileManager().deleteInnerFiles(dumpsPath);
    }

    static void flushOutputs() throws IOException {
        new ExternalFileManager().deleteInnerFiles(outputPath);
    }

}
