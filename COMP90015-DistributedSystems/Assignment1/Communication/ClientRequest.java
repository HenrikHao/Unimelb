//Zhuoyang Hao 1255309
package Communication;

import java.io.Serializable;
import java.util.ArrayList;

public class ClientRequest implements Serializable {
    private RequestMethod requestMethod;
    private String word;
    private ArrayList<String> meanings; // This will contain both existing and new meanings.

    public ClientRequest(RequestMethod requestMethod, String word, ArrayList<String> meanings) {
        this.requestMethod = requestMethod;
        this.word = word;
        this.meanings = meanings;
    }

    // Getter methods
    public RequestMethod getRequest() { return requestMethod; }
    public String getWord() { return word; }
    public ArrayList<String> getMeanings() { return meanings; }
}