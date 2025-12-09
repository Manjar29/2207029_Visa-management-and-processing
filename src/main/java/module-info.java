module com.visa.management {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.visa.management to javafx.fxml;
    opens com.visa.management.controllers to javafx.fxml;
    
    exports com.visa.management;
    exports com.visa.management.controllers;
}
