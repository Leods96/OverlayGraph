package input_output;


import org.beryx.textio.TextTerminal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OutputFormat {
    EXCEL_FILE_LIST,
    SQUARE_DISTANCE_MATRIX,
    TRIANGULAR_DISTANCE_MATRIX;

    public static OutputFormat getOutputFromNum(int choice) {
        return OutputFormat.values()[choice - 1];
    }

    public static List<String> getOutputFormatList() {
        return Arrays.stream(OutputFormat.values()).map(OutputFormat::toString).collect(Collectors.toList());
    }

    public static int printFormats(TextTerminal textTerminal) {
        int count = 0;
        for(OutputFormat elem : OutputFormat.values())
            textTerminal.println(++count + " - " + elem);
        return count;
    }

}
