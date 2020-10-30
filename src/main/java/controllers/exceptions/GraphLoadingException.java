package controllers.exceptions;

public class GraphLoadingException extends Exception{
    public GraphLoadingException (Exception message) {
        super(message);
    }
}
