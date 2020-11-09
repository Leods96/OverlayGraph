package controllers.exceptions;

public class GraphCreationException extends Exception {

    public GraphCreationException (Exception message) {
        super(message);
    }

    public GraphCreationException () {
        super();
    }

}
