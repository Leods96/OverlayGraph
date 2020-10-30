package user_interface;

import static user_interface.CLI.textIO;
import static user_interface.CLI.controller;
import static user_interface.UtilCLI.exitCondition;

public interface ConfigurationCLI {

    static void graphHopperSetUp() {
        boolean condition;
        do {
            condition = true;
            System.out.println("Insert the OSM file's absolute path: ");
            String input = textIO.newStringInputReader().withPattern("/^\\\\(?!\\\\)/").read();
            if (exitCondition(input)) return;
            try {
                controller.setOSMPath(input);
            } catch (Exception e) {
                condition = false;
            }
        } while(!condition);

        if(controller.getGraphPath() != null)
            System.out.println("The default graph folder will be used");
        else
            if(graphsFolderSetUp()) return;
        try {
            controller.graphHopperGraphCreation();
        } catch (Exception e) {
            System.err.println("Problem in the creation of graph-hopper");
            e.printStackTrace();
            return;
        }
        System.out.println("Graph-Hopper graph created and saved");
    }

    static boolean graphsFolderSetUp() {
        boolean condition;
        do {
            condition = true;
            System.out.println("Set the path of the graph folder: ");
            String input = textIO.newStringInputReader().read();
            if (exitCondition(input)) return true;
            try {
                controller.setGraphFolderPath(input);
            } catch (Exception e) {
                condition = false;
            }
        } while (!condition);
        return false;
    }

}
