//Zhuoyang Hao 1255309
package client;

import Communication.ClientRequest;
import Communication.RequestMethod;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ClientGUI {
    private Client client;
    private JTextField inputField;
    private JTextArea existingMeaningArea, newMeaningArea, responseTextArea;

    public ClientGUI(String serverAddress, int serverPort) {
        this.client = new Client(serverAddress, serverPort);
        initializeUI();
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Dictionary Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);  // Adjusted size for additional text area

        // First row - command and words
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel wordLabel = new JLabel("Word");
        inputField = new JTextField(20);
        JButton createButton = new JButton("Create");
        JButton addButton = new JButton("Add");
        JButton retrieveButton = new JButton("Retrieve");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearAllButton = new JButton("Clear All");

        topPanel.add(wordLabel);
        topPanel.add(inputField);
        topPanel.add(createButton);
        topPanel.add(addButton);
        topPanel.add(retrieveButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(clearAllButton);


        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

        // Second row - existing meaning
        JPanel existingMeaningPanel = new JPanel(new BorderLayout());
        JLabel existingMeaningLabel = new JLabel("Existing Meaning (for update only):");
        existingMeaningArea = new JTextArea(5, 30);
        existingMeaningPanel.add(existingMeaningLabel, BorderLayout.NORTH);
        existingMeaningPanel.add(new JScrollPane(existingMeaningArea), BorderLayout.CENTER);
        middlePanel.add(existingMeaningPanel);

        // Third row - new meaning for update
        JPanel newMeaningPanel = new JPanel(new BorderLayout());
        JLabel newMeaningLabel = new JLabel("New Meaning (for create, one meaning per line; for add and update, put one meaning only):");
        newMeaningArea = new JTextArea(5, 30);
        newMeaningPanel.add(newMeaningLabel, BorderLayout.NORTH);
        newMeaningPanel.add(new JScrollPane(newMeaningArea), BorderLayout.CENTER);
        middlePanel.add(newMeaningPanel);

        // Fourth row - response
        JPanel responsePanel = new JPanel(new BorderLayout());
        JLabel responseLabel = new JLabel("Server Response");
        responseTextArea = new JTextArea(10, 30);
        responseTextArea.setEditable(false);
        responsePanel.add(responseLabel, BorderLayout.NORTH);
        responsePanel.add(new JScrollPane(responseTextArea), BorderLayout.CENTER);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(middlePanel, BorderLayout.CENTER);
        frame.add(responsePanel, BorderLayout.SOUTH);

        // Action listeners for CRUD buttons
        createButton.addActionListener(e -> sendRequest(RequestMethod.CREATE));
        retrieveButton.addActionListener(e -> sendRequest(RequestMethod.RETRIEVE));
        updateButton.addActionListener(e -> sendUpdateRequest());
        deleteButton.addActionListener(e -> sendRequest(RequestMethod.DELETE));
        addButton.addActionListener(e -> sendAddMeaningRequest());

        // Clear all fields
        clearAllButton.addActionListener(e -> {
            inputField.setText("");
            existingMeaningArea.setText("");
            newMeaningArea.setText("");
            responseTextArea.setText("");
        });

        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }

    private void sendUpdateRequest() {
        String newMeaning = newMeaningArea.getText().trim();
        String word = inputField.getText().trim().toLowerCase();

        // Check if the word is empty
        if (word.isEmpty()) {
            responseTextArea.setText("Error: The word field cannot be empty.");
            return;
        }

        // Check if new meaning contains more than one line
        if (newMeaning.contains("\n")) {
            responseTextArea.setText("Error: New meaning must be exactly one line.");
        } else {
            ArrayList<String> meanings = new ArrayList<>();
            meanings.add(existingMeaningArea.getText().trim().toLowerCase()); // Existing meaning to replace
            meanings.add(newMeaning.toLowerCase()); // New meaning to use

            ClientRequest request = new ClientRequest(RequestMethod.UPDATE, word, meanings);
            String response = client.sendRequestAndGetResponse(request);
            responseTextArea.setText(response);
        }
    }

    private void sendAddMeaningRequest() {
        String newMeaning = newMeaningArea.getText().trim();
        String word = inputField.getText().trim().toLowerCase();

        // Check if the word is empty
        if (word.isEmpty()) {
            responseTextArea.setText("Error: The word field cannot be empty.");
            return;
        }

        // Check if new meaning contains more than one line
        if (newMeaning.contains("\n")) {
            responseTextArea.setText("Error: New meaning must be exactly one line.");
        } else {
            ArrayList<String> meanings = new ArrayList<>();
            meanings.add(newMeaning.toLowerCase());

            ClientRequest request = new ClientRequest(RequestMethod.ADD, word, meanings);
            String response = client.sendRequestAndGetResponse(request);
            responseTextArea.setText(response);
        }
    }

    private void sendRequest(RequestMethod method) {
        ArrayList<String> meanings = new ArrayList<>();
        String word = inputField.getText().trim().toLowerCase();

        // Check if the word is empty
        if (word.isEmpty()) {
            responseTextArea.setText("Error: The word field cannot be empty.");
            return;
        }

        if (method == RequestMethod.CREATE) {
            HashSet<String> meaningSet = new HashSet<>(); // Use a set to track unique meanings
            for (String meaning : newMeaningArea.getText().split("\n")) {
                String trimmedMeaning = meaning.trim().toLowerCase();
                // Check for duplicate before adding
                if (!meaningSet.add(trimmedMeaning)) {
                    responseTextArea.setText("Error: Duplicate meanings detected.");
                    return; // Exit the method if any duplicates are found
                }
                meanings.add(trimmedMeaning);
            }
        }

        ClientRequest request = new ClientRequest(method, word, meanings);
        String response = client.sendRequestAndGetResponse(request);
        responseTextArea.setText(response);
    }
}