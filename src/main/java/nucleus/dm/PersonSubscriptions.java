/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import javafx.beans.property.SimpleStringProperty;

public class PersonSubscriptions {
    private SimpleStringProperty userName;
    private SimpleStringProperty activeSubscription;
    private SimpleStringProperty pendingSubscription;
    private SimpleStringProperty pausedSubscription;
    private SimpleStringProperty cancelledSubscription;
    private SimpleStringProperty expiredSubscription;
    private SimpleStringProperty terminatedSubscription;
    private SimpleStringProperty pendingRequest;

    public PersonSubscriptions(SimpleStringProperty userName, SimpleStringProperty activeSubscription, SimpleStringProperty pendingSubscription, SimpleStringProperty pausedSubscription, SimpleStringProperty cancelledSubscription, SimpleStringProperty expiredSubscription, SimpleStringProperty terminatedSubscription, SimpleStringProperty pendingRequest) {
        this.userName = userName;
        this.activeSubscription = activeSubscription;
        this.pendingSubscription = pendingSubscription;
        this.pausedSubscription = pausedSubscription;
        this.cancelledSubscription = cancelledSubscription;
        this.expiredSubscription = expiredSubscription;
        this.terminatedSubscription = terminatedSubscription;
        this.pendingRequest = pendingRequest;
    }

    public SimpleStringProperty getUserName() {
        return userName;
    }

    public void setUserName(SimpleStringProperty userName) {
        this.userName = userName;
    }

    public SimpleStringProperty getActiveSubscription() {
        return activeSubscription;
    }

    public void setActiveSubscription(SimpleStringProperty activeSubscription) {
        this.activeSubscription = activeSubscription;
    }

    public SimpleStringProperty getPendingSubscription() {
        return pendingSubscription;
    }

    public void setPendingSubscription(SimpleStringProperty pendingSubscription) {
        this.pendingSubscription = pendingSubscription;
    }

    public SimpleStringProperty getPausedSubscription() {
        return pausedSubscription;
    }

    public void setPausedSubscription(SimpleStringProperty pausedSubscription) {
        this.pausedSubscription = pausedSubscription;
    }

    public SimpleStringProperty getCancelledSubscription() {
        return cancelledSubscription;
    }

    public void setCancelledSubscription(SimpleStringProperty cancelledSubscription) {
        this.cancelledSubscription = cancelledSubscription;
    }

    public SimpleStringProperty getExpiredSubscription() {
        return expiredSubscription;
    }

    public void setExpiredSubscription(SimpleStringProperty expiredSubscription) {
        this.expiredSubscription = expiredSubscription;
    }

    public SimpleStringProperty getTerminatedSubscription() {
        return terminatedSubscription;
    }

    public void setTerminatedSubscription(SimpleStringProperty terminatedSubscription) {
        this.terminatedSubscription = terminatedSubscription;
    }

    public SimpleStringProperty getPendingRequest() {
        return pendingRequest;
    }

    public void setPendingRequest(SimpleStringProperty pendingRequest) {
        this.pendingRequest = pendingRequest;
    }
    
}
