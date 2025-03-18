package Data;

public record Result(int status, String content) {

    public boolean isOK() {
        return status == 200;
    }
}
