package location_iq.Exceptions;

public class BadResponseException extends Exception {

    private int code;
    private String responseMessage;
    public BadResponseException(String message, int code){
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
