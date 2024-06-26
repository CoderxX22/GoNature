package gui;

import command.Message;
import data.MessagesToSend;
import data.Order;
import data.RegisteredAccount;
import handler.ClientHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.converter.LocalTimeStringConverter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class OrderVisit extends Login implements Initializable {
    @FXML
    private Spinner<Integer> numberOfVisitors;
    @FXML
    private DatePicker dateToVisit;
    @FXML
    private Spinner<LocalTime> timeOfVisit;
    @FXML
    private TextField customer_id;
    @FXML
    private RadioButton waitingList;
    @FXML
    private TextField customer_email;
    @FXML
    private TextField customer_phone_number;
    @FXML
    private SplitMenuButton location_pick;
    @FXML
    private MenuItem menuItem_karmiel;
    @FXML
    private MenuItem menuItem_Haifa;
    @FXML
    private MenuItem menuItem_Tel_aviv;
    private RegisteredAccount registeredAccount;
    private Order newOrder;
    private int accID;
    private String accType;
    private float priceCalculation(int numberOfVisitors,double discount){
        return 0;
    }

    /**
     * Initializes the OrderVisit controller.
     * Sets up initial values and configurations for the form fields.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ClientHandler.getAccount().account_type.equals("ParkWorker")){
            customer_id.setDisable(false);
        }else {
            customer_id.setText(String.valueOf(ClientHandler.getAccount().account_id_pk));
        }
        if (ClientHandler.getAccount().account_type.equals("TourGuide") || ClientHandler.getAccount().account_type.equals("Client") ){
            blockFields();
            if (ClientHandler.getAccount().account_type.equals("TourGuide")) {
                numberOfVisitors.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15));
            }else{
                numberOfVisitors.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5));
            }
        }else {
            numberOfVisitors.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5));
        }

        SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<LocalTime>() {
            {
                setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));
            }

            @Override
            public void decrement(int steps) {
                if (getValue() == null)
                    setValue(LocalTime.of(7,0));
                else {
                    LocalTime time = (LocalTime) getValue();
                    setValue(time.minusMinutes(30));
                }
            }

            @Override
            public void increment(int steps) {
                if (this.getValue() == null)
                    setValue(LocalTime.of(7,0));
                else {
                    LocalTime time = (LocalTime) getValue();
                    setValue(time.plusMinutes(30));                }
            }
        };

        timeOfVisit.setValueFactory(valueFactory);
        timeOfVisit.setEditable(true);

        menuItem_karmiel.setOnAction(event -> location_pick.setText(menuItem_karmiel.getText()));
        menuItem_Haifa.setOnAction(event -> location_pick.setText(menuItem_Haifa.getText()));
        menuItem_Tel_aviv.setOnAction(event -> location_pick.setText(menuItem_Tel_aviv.getText()));

    }
    /**
     * Handles the order visit action.
     * Validates user input and creates a new order based on the provided information.
     *
     * @param actionEvent The event that triggered the action.
     */
    @FXML
    public void OrderVisit(ActionEvent actionEvent) {
        newOrder = new Order();
        if(ClientHandler.getAccount().account_type.equals("TourGuide")) {
            newOrder.guided_order = true;
        }else{
            newOrder.guided_order = false;
        }
            try {
                String email = String.valueOf(customer_email.getText());
                newOrder.email = email;
            } catch (NullPointerException e) {
                ClientUI.popupNotification("Please enter your Email");
            }

            try {
                String phoneNumber = String.valueOf(customer_phone_number.getText());
                newOrder.phone = phoneNumber;
            } catch (NullPointerException e) {
                ClientUI.popupNotification("Please enter your Phone Number");
            }
            int numOfVisitors = Integer.parseInt(String.valueOf(numberOfVisitors.getValue()));
            newOrder.number_of_visitors = numOfVisitors;

            try {
                LocalTime visitTime = timeOfVisit.getValue();
                newOrder.visit_time = visitTime;
            } catch (NullPointerException e) {
                ClientUI.popupNotification("Enter visit time");
            }

            try {
                LocalDate visitDate = dateToVisit.getValue();
                newOrder.visit_date = visitDate;
            } catch (NullPointerException e) {
                ClientUI.popupNotification("Please enter visit Date");
            }

            newOrder.order_id_pk = UUID.randomUUID();
            newOrder.account_id = ClientHandler.getAccount().account_id_pk;
            newOrder.cancelled = false;
//            try {
//                newOrder.exit_time = newOrder.visit_time.plusHours(4);
//            }catch (NullPointerException e){
//                ClientUI.popupNotification("Choose Time between 7:00 - 20:00");
//            }
            newOrder.park_id_fk = String.valueOf(location_pick.getText());
            if (ClientHandler.getAccount().account_type.equals("ParkWorker")){
                newOrder.on_arrival_order = true;
            }else{
                newOrder.on_arrival_order = false;
            }
//            if(!newOrder.on_arrival_order){
//                ClientHandler.request(new Message("CheckParkCapacity",newOrder));
//                Message response = ClientHandler.getLastResponse();
//                if (response.getCommand().equals("ParkFull")) {
//                    // Show a popup window with an option for the user
//                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                    alert.setTitle("Park Full");
//                    alert.setHeaderText("The park is full at the specified date and time.");
//                    alert.setContentText("Do you want to be placed on the waiting list?");
//
//                    ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
//                    ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
//                    alert.getButtonTypes().setAll(yesButton, noButton);
//
//                    Optional<ButtonType> result = alert.showAndWait();
//                    if (result.isPresent() && result.get() == yesButton) {
//                        newOrder.on_waiting_list = true;
//                    } else {
//                        // User chose not to be placed on the waiting list, handle this accordingly
//                        newOrder.on_waiting_list = false;
//                    }
//                }
//            }

        ClientHandler.request(new Message("CreateNewOrder", newOrder));

            if (ClientHandler.getLastResponse().getCommand().equals("OrderEmail")){
                ClientUI.popupNotification("Enter your Email please");
            }
            if (ClientHandler.getLastResponse().getCommand().equals("OrderPhone")){
                ClientUI.popupNotification("Enter your Phone,please");
            }
            if (ClientHandler.getLastResponse().getCommand().equals("OrderDate")){
                ClientUI.popupNotification("Choose Date is after "+LocalDate.now()+",please");
            }
            if (ClientHandler.getLastResponse().getCommand().equals("OrderTime")){
                ClientUI.popupNotification("Choose Time between 7:00 - 20:00");
            }
            if (ClientHandler.getLastResponse().getCommand().equals("OnWaitingList")){
                ClientUI.popupNotification("Unfortunately, The park is fully booked.\nYour order Moved to waiting List.\nWe will let you know when there will be open spot");
            }
            if (ClientHandler.getLastResponse().getCommand().equals("LocationError")){
                ClientUI.popupNotification("Please choose Location");
            }
            if (ClientHandler.getLastResponse().getCommand().equals("OrderCreated")) {
                ClientUI.popupNotification("Congratulations!!\nYour Order Created Successfully.\nWe'll be Happy To see you!");
                MessagesToSend msg = ClientHandler.getMessageToSend();
                ClientUI.popupNotification("To: "+msg.Email+"\nTitle: Email title\n"+"Content:"+msg.MessageText);
                ClientUI.popupNotification("To: "+msg.phone+"\nTitle: "+msg.MessageTitle+"Content:"+msg.MessageText);
            }
        if (ClientHandler.getLastResponse().getCommand().equals("NotOnWaitingList")) {
            ClientUI.popupNotification("order was cancelled and not added to waiting-list");
        }
    }


    private void blockFields(){
        registeredAccount = (RegisteredAccount) ClientHandler.getAccount();
        customer_email.setDisable(true);
        customer_email.setText(registeredAccount.email);
        customer_phone_number.setDisable(true);
        customer_phone_number.setText(registeredAccount.phone);
    }

}
