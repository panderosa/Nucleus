/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nucleus.dm;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

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

    public void setCsaIdmPassword(String csaIdmPassword) {
        try {
            Decryptor decryptor = new Decryptor();
            this.csaIdmPassword = decryptor.decryptPassword(csaIdmPassword);
        }
        catch(Exception exp) {
            System.out.println(processException(exp));
        }
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

    public void setCsaTransportPassword(String csaTransportPassword) {
        try {
            Decryptor decryptor = new Decryptor();
            this.csaTransportPassword = decryptor.decryptPassword(csaTransportPassword);
        }
        catch(Exception exp) {
            System.out.println(processException(exp));
        }
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

    public void setCsaConsumerPassword(String csaConsumerPassword) {
        try {
            Decryptor decryptor = new Decryptor();
            this.csaConsumerPassword = decryptor.decryptPassword(csaConsumerPassword);
        }
        catch(Exception exp) {
            System.out.println(processException(exp));
        }
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

    public void setCsaAdminPassword(String csaAdminPassword) {
        try {
            Decryptor decryptor = new Decryptor();
            this.csaAdminPassword = decryptor.decryptPassword(csaAdminPassword);
        }
        catch(Exception exp) {
            System.out.println(processException(exp));
        }
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("csaIdmUser: ").append(csaIdmUser).append("\n");
        sb.append("csaIdmPassword: ").append(csaIdmPassword).append("\n");
        sb.append("csaTransportUser: ").append(csaTransportUser).append("\n");
        sb.append("csaTransportPassword: ").append(csaTransportPassword).append("\n");
        sb.append("csaConsumer: ").append(csaConsumer).append("\n");
        sb.append("csaConsumerPassword: ").append(csaConsumerPassword).append("\n");
        sb.append("csaConsumerTenant: ").append(csaConsumerTenant).append("\n");
        sb.append("csaOnBehalfConsumer: ").append(csaOnBehalfConsumer).append("\n");
        sb.append("csaAdminUser: ").append(csaAdminUser).append("\n");
        sb.append("csaAdminPassword: ").append(csaAdminPassword).append("\n");
        sb.append("csaProviderOrg: ").append(csaProviderOrg).append("\n");
        sb.append("csaServer: ").append(csaServer).append("\n");
        sb.append("csaProtocol: ").append(csaProtocol).append("\n");
        sb.append("csaPort: ").append(csaPort).append("\n");
        return(sb.toString());   
    }
    
    String processException(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter pw = new PrintWriter(baos,true);
        e.printStackTrace(pw);
        String out = baos.toString();
        return out;
    }
    
}
