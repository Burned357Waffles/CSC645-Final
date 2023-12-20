import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class TextServer {
    static BufferedReader inFromClient;
    static DataOutputStream outToClient;
    static HashMap<String, String> users = new HashMap<>();
    static HashMap<String, List<String>> messages = new HashMap<>();
    static String currentUser;
    
    
    // Handle communication with a connected client
    private static void handleClientConnection(Socket connectionSocket) throws IOException {
        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        
        boolean running = true;
        do {
            String option = inFromClient.readLine();
            
            System.out.println("\nUser's choice is " + option);
            switch (option) {
                case "0" -> validateCredentials(connectionSocket);
                case "1" -> listUsers();
                case "2" -> sendMessage();
                case "3" -> readMessages();
                case "4" -> {
                    System.out.println("Exiting\n");
                    connectionSocket.close();
                    running = false;
                }
            }
        } while (running);
    }
    
    
    // Validate user credentials
    private static void validateCredentials(Socket connectionSocket) throws IOException {
        while (true) {
            String username = inFromClient.readLine();
            String password = inFromClient.readLine();
            System.out.println("username = " + username + " password = " + password);
            if (!Objects.equals(users.get(username), password)) {
                System.out.println("Access Denied – Username/Password Incorrect");
                outToClient.write(0);
            } else {
                currentUser = username;
                System.out.println("Access Granted");
                outToClient.write(1);
                return;
            }
        }
    }
    
    
    // Send list of users to the client
    private static void listUsers() throws IOException {
        System.out.println("Returning List of users...");
        outToClient.writeBytes(String.valueOf(users.size()) + '\n');
        for (String user : users.keySet()) {
            System.out.println(user);
            outToClient.writeBytes(user + '\n');
        }
    }
    
    
    // Send a message from the client to another user
    private static void sendMessage() throws IOException {
        String receiver = inFromClient.readLine();
        
        // Check if the user is valid
        if (!messages.containsKey(receiver)) {
            System.out.println("Invalid user");
            outToClient.write(0);
            return;
        } else
            outToClient.write(1);
        
        // Store the received message for the recipient
        System.out.println("Receive a message for " + receiver);
        messages.get(receiver).add(currentUser + ": " + inFromClient.readLine());
    }
    
    
    // Send user's messages to the client
    private static void readMessages() throws IOException {
        List<String> userMessages = messages.get(currentUser);
        
        System.out.println("Returning messages for " + currentUser);
        outToClient.writeBytes(String.valueOf(userMessages.size()) + '\n');
        
        if (userMessages.isEmpty()) {
            System.out.println("*** No messages for " + currentUser + " ***");
            return;
        }
        
        // Display all messages for the current user
        for (String message : userMessages) {
            outToClient.writeBytes(message + '\n');
        }
    }
    
    
    // Initialize user data
    private static void initializeUsers() {
        users.put("Alice", "1234");
        users.put("Bob", "5678");
        
        messages.put("Alice", new ArrayList<>());
        messages.put("Bob", new ArrayList<>());
    }
    
    
    public static void main(String[] argv) throws Exception {
        initializeUsers();
        ServerSocket welcomeSocket = new ServerSocket(6789);
        System.out.println("SERVER is running ... ");
        
        // Accept incoming client connections and handle them
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println("Client connected");
            try {
                handleClientConnection(connectionSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
