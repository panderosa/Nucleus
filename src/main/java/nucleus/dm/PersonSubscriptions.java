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

    public PersonSubscriptions(String userName, String activeSubscription, String pendingSubscription, String pausedSubscription, String cancelledSubscription, String expiredSubscription, String terminatedSubscription, String pendingRequest) {
        this.userName = new SimpleStringProperty(userName);
        this.activeSubscription = new SimpleStringProperty(activeSubscription);
        this.pendingSubscription = new SimpleStringProperty(pendingSubscription);
        this.pausedSubscription = new SimpleStringProperty(pausedSubscription);
        this.cancelledSubscription = new SimpleStringProperty(cancelledSubscription);
        this.expiredSubscription = new SimpleStringProperty(expiredSubscription);
        this.terminatedSubscription = new SimpleStringProperty(terminatedSubscription);
        this.pendingRequest = new SimpleStringProperty(pendingRequest);
    }

    public String getUserName() {
        return userName.get();
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getActiveSubscription() {
        return activeSubscription.get();
    }

    public void setActiveSubscription(String activeSubscription) {
        this.activeSubscription.set(activeSubscription);
    }

    public String getPendingSubscription() {
        return pendingSubscription.get();
    }

    public void setPendingSubscription(String pendingSubscription) {
        this.pendingSubscription.set(pendingSubscription);
    }

    public String getPausedSubscription() {
        return pausedSubscription.get();
    }

    public void setPausedSubscription(String pausedSubscription) {
        this.pausedSubscription.set(pausedSubscription);
    }

    public String getCancelledSubscription() {
        return cancelledSubscription.get();
    }

    public void setCancelledSubscription(String cancelledSubscription) {
        this.cancelledSubscription.set(cancelledSubscription);
    }

    public String getExpiredSubscription() {
        return expiredSubscription.get();
    }

    public void setExpiredSubscription(String expiredSubscription) {
        this.expiredSubscription.set(expiredSubscription);
    }

    public String getTerminatedSubscription() {
        return terminatedSubscription.get();
    }

    public void setTerminatedSubscription(String terminatedSubscription) {
        this.terminatedSubscription.set(terminatedSubscription);
    }

    public String getPendingRequest() {
        return pendingRequest.get();
    }

    public void setPendingRequest(String pendingRequest) {
        this.pendingRequest.set(pendingRequest);
    }
    
}
