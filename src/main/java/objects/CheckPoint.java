package objects;

/**
 * CheckPoint class that is the saved point during the preprocessing process, represent a specific point
 * represented by the unique code for both the From file and the To file
 */
public class CheckPoint {
    private String from;
    private String to;
    private String inputFilePath;
    private String graphName;
    private int fileIndex;
    private boolean useKdTree;

    public CheckPoint () {}

    public CheckPoint(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    public String getFrom()
    {
        return from;
    }

    public String getTo()
    {
        return to;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public void setKdTree(boolean useKdTree) {
        this.useKdTree = useKdTree;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public String getGraphName() {
        return graphName;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public boolean getKdTree() {
        return useKdTree;
    }
}
