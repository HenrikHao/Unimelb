//Zhuoyang Hao 1255309
package server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


import Communication.ServerResponse;

public class Dictionary {
    private JSONObject dict;
    private String path;

    public Dictionary(String path) {
        this.path = path;
        loadDictionary();
    }

    private void loadDictionary() {
        try (FileReader fileReader = new FileReader(path)) {
            StringBuilder stringBuilder = new StringBuilder();
            int data;
            while ((data = fileReader.read()) != -1) {
                stringBuilder.append((char) data);
            }
            dict = new JSONObject(stringBuilder.toString());
        } catch (IOException e) {
            System.err.println("IO exception: " + path);
            System.err.println(path + " not found, creating an empty dict now");
            dict = new JSONObject(); // Initialize an empty JSON object if file read fails
            writeToJsonFile(); // Create a new file with an empty JSON object.
        } catch (JSONException e) {
            System.err.println("Please double check your json file format: " + path);
            dict = new JSONObject(); // Initialize an empty JSON object if parsing fails
            writeToJsonFile();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            System.exit(1);
        }
    }

    public synchronized ServerResponse create(String word, ArrayList<String> meanings) {
        ServerResponse response = new ServerResponse();
        // if the dictionary already contain the word
        if (dict.has(word)) {
            response.setSuccessful(false);
            response.setMessage(word + " already exists");
            return response;
        }
        // check the meaning
        if (meanings == null || meanings.stream().anyMatch(String::isEmpty)) {
            response.setSuccessful(false);
            response.setMessage("Please enter meanings for " + word);
            return response;
        }
        // put the word into the dictionary and set the response message
        dict.put(word, meanings);
        response.setSuccessful(true);
        response.setMessage(word + " created");
        writeToJsonFile();
        return response;
    }

    public synchronized ServerResponse retrieve(String word) {
        ServerResponse response = new ServerResponse();
        // if the word not found
        if (!dict.has(word)) {
            response.setSuccessful(false);
            response.setMessage(word + " not found");
            return response;
        }
        // otherwise retrieve the meaning
        ArrayList<String> meanings = new ArrayList<>();
        JSONArray jsonArray = dict.getJSONArray(word);
        for (int i = 0; i < jsonArray.length(); i++) {
            meanings.add(jsonArray.getString(i));
        }
        response.setSuccessful(true);
        response.setMessage(word + " retrieved");
        response.setMeanings(meanings);
        return response;
    }

    public synchronized ServerResponse addMeaning(String word, String newMeaning) {
        ServerResponse response = new ServerResponse();
        // if the word not found
        if (!dict.has(word)) {
            response.setSuccessful(false);
            response.setMessage(word + " not found");
            return response;
        }
        // if the new meaning is blank
        if (newMeaning == null || newMeaning.isEmpty()) {
            response.setSuccessful(false);
            response.setMessage("Please enter a new meaning for " + word);
            return response;
        }
        ArrayList<String> meanings = retrieve(word).getMeanings();
        if (!meanings.contains(newMeaning)) { // Check if the new meaning already exists
            meanings.add(newMeaning);
            dict.put(word, meanings);
            response.setSuccessful(true);
            response.setMessage(word + " with new meaning " + newMeaning + " added");
            writeToJsonFile();
            return response;
        }
        response.setSuccessful(false);
        response.setMessage(word + " with meaning " + newMeaning+ " already exists");
        return response;
    }

    public synchronized ServerResponse update(String word, ArrayList<String> meanings) {
        ServerResponse response = new ServerResponse();
        // if the word not found
        if (!dict.has(word)) {
            response.setSuccessful(false);
            response.setMessage(word + " not found");
            return response;
        }
        if (meanings == null || meanings.size() != 2 || meanings.get(0).isEmpty() || meanings.get(1).isEmpty()) {
            response.setSuccessful(false);
            response.setMessage("Please ensure the Existing Meanings and New Meanings are not blank and exactly two are provided.");
            return response;
        }
        ArrayList<String> existingMeanings = retrieve(word).getMeanings();
        if (existingMeanings.contains(meanings.get(1))) {
            response.setSuccessful(false);
            response.setMessage(word + " with new meaning " + meanings.get(1) + " already exists");
            return response;
        }
        if (existingMeanings.contains(meanings.get(0))) { // meanings.get(0) is the existing meaning
            if (meanings.get(0).equals(meanings.get(1))) {
                response.setSuccessful(false);
                response.setMessage("Error: The new meaning is the same as the existing one.");
                return response;
            }
            existingMeanings.remove(meanings.get(0)); // Remove the old meaning
            existingMeanings.add(meanings.get(1)); // Add the new meaning
            dict.put(word, existingMeanings);
            response.setSuccessful(true);
            response.setMessage("Meaning update successful");
            writeToJsonFile();
            return response;
        }
        response.setSuccessful(false);
        response.setMessage(word + " with meaning '" + meanings.get(0) + "' not found");
        return response;
    }

    public synchronized ServerResponse delete(String word) {
        ServerResponse response = new ServerResponse();
        if (!dict.has(word)) {
            response.setSuccessful(false);
            response.setMessage(word + " not found");
            return response;
        }
        dict.remove(word);
        response.setSuccessful(true);
        response.setMessage(word + " deleted");
        writeToJsonFile();
        return response;
    }

    private void writeToJsonFile() {
        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(dict.toString(4)); // Indentation for better readability
            fileWriter.flush();
            System.out.println("Dictionary updated and written to file.");
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while writing to file: " + e.getMessage());
        }
    }
}