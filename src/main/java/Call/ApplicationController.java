package Call;

import Audio.AudioFileThread;
import Audio.AudioSinusoidalThread;
import Audio.AudioThread;
import Audio.OutputAudio;
import VoIP.Request;

import VoIP.Response;
import VoIP.Session;
import VoIP.UserAgent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class ApplicationController implements Initializable {

    /**
     * MAIN TAB ATTRIBUTES
     */
    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane anchorMain;

    @FXML
    private Tab mainTab;

    @FXML
    private ImageView logo;

    @FXML
    private Label connectionLabel;

    @FXML
    private Label receiverLabel;

    @FXML
    private Button callButton;

    @FXML
    private Button hangUpButton;

    /**
     * AUDIO TAB
     */
    @FXML
    private Tab audioTab;

    private Thread callThread;

    @FXML
    private AnchorPane anchorAudio;

    @FXML
    private ImageView audio;

    @FXML
    private Label audioControlLabel;

    @FXML
    private Button spoiledAudioButton;

    @FXML
    private Button fileAudioButton;

    @FXML
    private Button sinusoidalAudioButton;

    @FXML
    private Button stopButton;

    private Thread currentThread;

    /**
     * LOGS TAB
     */
    @FXML
    private Tab logsTab;

    @FXML
    private AnchorPane anchorLogs;

    @FXML
    private TextFlow logsTextFlow;

    @FXML
    private ScrollPane logsScrollPane;

    @FXML
    private ImageView logsButton;

    @FXML
    private Button saveLogsButton;

    @FXML
    private Button updateButton;

    /**
     * SETTINGS TAB
     */
    @FXML
    private Tab settingsTab;

    @FXML
    private AnchorPane anchorSettings;

    @FXML
    private TextField userNameTextBox;

    @FXML
    private Label call_idLabel;

    @FXML
    private Label receiverTagLabel;

    @FXML
    private TextField frequencyTextBox;

    @FXML
    private TextField amplitudeTextBox;

    @FXML
    private Button saveSettingsButton;

    /**
     * Define the three threads sending audio (RTP) packets.
     */
    AudioThread spoiledThread = new AudioThread();
    AudioFileThread imperialThread = new AudioFileThread();
    AudioSinusoidalThread sinusoidalThread = new AudioSinusoidalThread();

    /**
     * Initialize the page
     */
    @Override
    public void initialize(URL url, ResourceBundle rb){
        StringBuilder callID = new StringBuilder();
        for(int index = 0; index < 12; index++){
            callID.append(Request.getCallId().charAt(index));
        }
        this.call_idLabel.setText(callID.toString());
        this.receiverTagLabel.setText(Request.getReceiverTag());
        logsScrollPane.setContent(logsTextFlow);
    }

    /**
     * Set the Connection status in the relative label                          // MAIN TAB
     *
     * @param value the status, in a string
     */
    public synchronized void setConnectionLabel(String value){
        Platform.runLater(()->{this.connectionLabel.setText(value);});
    }

    /**
     * Call the mjUA "Bob", read the response to the Invite Request and send
     *  ACK if the mJUA sends a 200OK Response
     *
     * @param event press on Call button
     */
    @FXML
    void call(ActionEvent event) {                           // Start the call if the Session is not active yet

        if(!Session.isActive() && (Response.getServerAnswer() == null ||Response.getServerAnswer().equals("200"))) {
            callThread = null;
            Session.clear();
            callThread = new Thread(new UserAgent());
            callThread.start();
        }
    }

    /**
     * Hang Up the call sending a Bye Request is the Session is active and read
     *  the ACK Response sent by the mjUA "Bob"
     *
     * @param event press on Hang Up button
     */
    @FXML
    void hangUp(ActionEvent event) {
        if (Session.isActive()) {
            UserAgent.closeCall();
        }
    }

    /**
     * Receive mjUA "Bob" RTP Packets, edit the packet's payload with randomly      // AUDIO
     *  generated bytes and send the packet back to him
     *
     * @param event press on Spoiled Audio button
     */
    @FXML
    void sendSpoiledAudio(ActionEvent event) {
        if (!OutputAudio.isRunning()) {
            new Thread(spoiledThread).start();
        }
    }

    /**
     * Send the mjUA "Bob" the Imperial_March.wav in resources/audio folder
     *
     * @param event press on Spoiled Audio button
     */
    @FXML
    void sendImperialMarch(ActionEvent event) {
        if (!OutputAudio.isRunning()) {
            new Thread(imperialThread).start();
        }
    }

    /**
     * Start sending a sine wave in RTP Packets to the UserAgent "Bob"
     *
     * @param event press on Sinusoidal Audio button
     */
    @FXML
    void sendSinusoidalAudio(ActionEvent event) {
        if (!OutputAudio.isRunning()) {
            new Thread(sinusoidalThread).start();
        }
    }

    /**
     * Stop sending audio
     *
     * @param event press on Stop button
     */
    @FXML
    synchronized void stopAudio(ActionEvent event) {
        OutputAudio.setRunning(false);
    }

    /**
     * Save logs on a external file in resources/requests/logs.txt              //  LOGS TAB
     *
     * @param event press on Save button
     */
    @FXML
    void saveLogs(ActionEvent event) {
        Session.save();
    }

    /**
     * Update the Text Flow in the Scroll Pane with the updated logs
     *
     * @param event press on Update button
     */
    @FXML
    void updateLogs(ActionEvent event) {
        Text text = new Text(Session.logsMessage());
        this.logsTextFlow.getChildren().clear();
        this.logsTextFlow.getChildren().add(text);
        logsScrollPane.setContent(logsTextFlow);
    }

    /**
     * Save the new settings of the UserAgent                                   // SETTINGS TAB
     *
     * @param event press on Save button
     */
    @FXML
    void saveSettings(ActionEvent event) {
        String newName = userNameTextBox.getText();                                 // Take the string in the TextBox
        String newFrequency = frequencyTextBox.getText();
        String newAmplitude = amplitudeTextBox.getText();

        if(!Session.isActive() && !newName.equals(""))
            Request.setSenderName(newName);

        if(tryParseDouble(newFrequency))                                            // Set the new Frequency
            AudioSinusoidalThread.setFrequency(Double.parseDouble(newFrequency));

        if(tryParseDouble(newAmplitude))
            AudioSinusoidalThread.setAmplitude(Double.parseDouble(newAmplitude));   // Set the new Amplitude
    }

    /**
     * Set the Receiver Tag in the relative label
     *
     */
    public synchronized void setReceiverTagLabel(){
        StringBuilder receiverTag = new StringBuilder();
        for(int index = 4; index < Request.getReceiverTag().length(); index++){
            receiverTag.append(Request.getReceiverTag().charAt(index));
        }
        Platform.runLater(()->{this.receiverTagLabel.setText(receiverTag.toString());});
    }

    /**
     * Try Parse Double method
     *
     * @param value the value to convert to double
     * @return true if the string can be converted to double,
     *  false otherwise
     */
    public boolean tryParseDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
