//Zhuoyang Hao 1255309
package Communication;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerResponse implements Serializable {
    // a response contains if the request is successful and the word meanings if applicable
    private boolean successful;

    // meanings of word
    private ArrayList<String> meanings;

    // message of response
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;
    public ServerResponse() {
    }
    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public ArrayList<String> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<String> meanings) {
        this.meanings = meanings;
    }
}