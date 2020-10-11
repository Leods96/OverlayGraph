package input_output;

/**
 * CheckPoint class that is the saved point during the preprocessing process, represent a specific point
 * represented by the unique code for both the From file and the To file
 */
public class CheckPoint {
    private final String from;
    private final String to;

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
}
