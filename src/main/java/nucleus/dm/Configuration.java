/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

/**
 *
 * @author Administrator
 */
public class Configuration {
    private String csaIdmUser;
    private String csaIdmPassword;
    private String csaTransportUser;
    private String csaTransportPassword;
    private String csaConsumer;
    private String csaConsumerPassword;
    private String csaConsumerTenant;
    private String csaOnBehalfConsumer;
    private String csaAdminUser;
    private String csaAdminPassword;
    private String csaProviderOrg;
    private String csaServer;
    private String csaProtocol;
    private int csaPort;
    private Decryptor decryptor;

    public Configuration() throws Exception {
    }

    public String getCsaIdmUser() {
        return csaIdmUser;
    }

    public void setCsaIdmUser(String csaIdmUser) {
        this.csaIdmUser = csaIdmUser;
    }

    public String getCsaIdmPassword() {
        return csaIdmPassword;
    }

    public void setCsaIdmPassword(String csaIdmPassword) throws Exception {
        Decryptor decryptor = new Decryptor();
        this.csaIdmPassword = decryptor.decryptPassword(csaIdmPassword);
    }

    public String getCsaTransportUser() {
        return csaTransportUser;
    }

    public void setCsaTransportUser(String csaTransportUser) {
        this.csaTransportUser = csaTransportUser;
    }

    public String getCsaTransportPassword() {
        return csaTransportPassword;
    }

    public void setCsaTransportPassword(String csaTransportPassword) throws Exception {
        Decryptor decryptor = new Decryptor();      
        this.csaTransportPassword = decryptor.decryptPassword(csaTransportPassword);
    }

    public String getCsaConsumer() {
        return csaConsumer;
    }

    public void setCsaConsumer(String csaConsumer) {
        this.csaConsumer = csaConsumer;
    }

    public String getCsaConsumerPassword() {
        return csaConsumerPassword;
    }

    public void setCsaConsumerPassword(String csaConsumerPassword) throws Exception {
        Decryptor decryptor = new Decryptor();        
        this.csaConsumerPassword = decryptor.decryptPassword(csaConsumerPassword);
    }

    public String getCsaConsumerTenant() {
        return csaConsumerTenant;
    }

    public void setCsaConsumerTenant(String csaConsumerTenant) {
        this.csaConsumerTenant = csaConsumerTenant;
    }

    public String getCsaOnBehalfConsumer() {
        return csaOnBehalfConsumer;
    }

    public void setCsaOnBehalfConsumer(String csaOnBehalfConsumer) {
        this.csaOnBehalfConsumer = csaOnBehalfConsumer;
    }

    public String getCsaAdminUser() {
        return csaAdminUser;
    }

    public void setCsaAdminUser(String csaAdminUser) {
        this.csaAdminUser = csaAdminUser;
    }

    public String getCsaAdminPassword() {
        return csaAdminPassword;
    }

    public void setCsaAdminPassword(String csaAdminPassword) throws Exception {
        Decryptor decryptor = new Decryptor(); 
        this.csaAdminPassword = decryptor.decryptPassword(csaAdminPassword);
    }

    public String getCsaProviderOrg() {
        return csaProviderOrg;
    }

    public void setCsaProviderOrg(String csaProviderOrg) {
        this.csaProviderOrg = csaProviderOrg;
    }

    public String getCsaServer() {
        return csaServer;
    }

    public void setCsaServer(String csaServer) {
        this.csaServer = csaServer;
    }

    public String getCsaProtocol() {
        return csaProtocol;
    }

    public void setCsaProtocol(String csaProtocol) {
        this.csaProtocol = csaProtocol;
    }

    public int getCsaPort() {
        return csaPort;
    }

    public void setCsaPort(int csaPort) {
        this.csaPort = csaPort;
    }
    
    
    
}
