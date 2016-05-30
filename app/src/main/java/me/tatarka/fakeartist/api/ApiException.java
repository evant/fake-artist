package me.tatarka.fakeartist.api;

public class ApiException extends Exception {
    private final int result;

    public ApiException(int result) {
        super("Api Error: " + result);
        this.result = result;
    }
    
    public ApiException(String message, int result) {
        super(message);
        this.result = result;
    }
    
    public int getResult() {
        return result;
    }
}
