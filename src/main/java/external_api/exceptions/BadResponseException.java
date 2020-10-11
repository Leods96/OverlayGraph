package external_api.exceptions;

public class BadResponseException extends Exception {

    private int code;

    public BadResponseException(String message, int code){
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
