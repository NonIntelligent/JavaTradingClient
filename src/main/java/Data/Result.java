package Data;

/**
 * Record representation of an HTTP request result.
 */
public record Result(int status, String content) {

    /**
     * @return If the HTTP request status is OK (200).
     */
    public boolean isOK() {
        return status == 200;
    }
}
