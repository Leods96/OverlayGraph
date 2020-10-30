package input_output;

import user_interface.MenuState;

public enum OutputFormat {
    EXCEL_FILE_LIST,
    SQUARE_DISTANCE_MATRIX,
    TRIANGULAR_DISTANCE_MATRIX;

    public static OutputFormat getOutputFromNum(int choice) {
        return OutputFormat.values()[choice - 1];
    }

    public static OutputFormat[] getOutputFormatList() {
        return  OutputFormat.values();
    }

    public static int printFormats() {
        int count = 0;
        for(OutputFormat elem : OutputFormat.values())
            System.out.println(++count + " - " + elem);
        return count;
    }

}
