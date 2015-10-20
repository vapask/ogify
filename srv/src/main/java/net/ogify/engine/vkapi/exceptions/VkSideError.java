package net.ogify.engine.vkapi.exceptions;

import java.rmi.RemoteException;

/**
 * Exception which indicates about error returned from vk servers or about errors on working with vk servers.
 *
 * @author Morgen Matvey
 */
public class VkSideError extends RemoteException {
    /**
     * Returned http code.
     */
    private int httpCode = 0;

    /**
     * Default constructor.
     */
    public VkSideError() {
    }

    /**
     * Construct object with description, but without http code. Useful when it is not able to detect http code (if
     * we can't connect to vk, for example).
     * @param s errors description.
     */
    public VkSideError(String s) {
        super(s);
    }

    public VkSideError(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * Construct error with description and returned http code from vk.
     * @param s error description.
     * @param httpCode returned http code.
     */
    public VkSideError(String s, int httpCode) {
        super(s);
        this.httpCode = httpCode;
    }

    public VkSideError(String s, Throwable cause, int httpCode) {
        super(s, cause);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
