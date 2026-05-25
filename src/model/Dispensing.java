package model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "dispensings")
public class Dispensing implements Serializable {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String dispensingDate; // yyyy-MM-dd HH:mm:ss

    @Column(nullable = false)
    private int quantityDispensed;

    @Column(nullable = false)
    private double totalCost;

    @Column(nullable = false)
    private String paymentMethod; // CASH, INSURANCE, MOBILE_MONEY

    private String insuranceProvider;
    private String insurancePolicyNo;

    @Column(nullable = false)
    private String status; // COMPLETED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // One Dispensing -> One Prescription
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_id", nullable = false, unique = true)
    private Prescription prescription;

    // Many Dispensings -> One User (Pharmacist)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dispensed_by", nullable = false)
    private User dispensedBy;

    public Dispensing() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDispensingDate() { return dispensingDate; }
    public void setDispensingDate(String dispensingDate) { this.dispensingDate = dispensingDate; }

    public int getQuantityDispensed() { return quantityDispensed; }
    public void setQuantityDispensed(int quantityDispensed) { this.quantityDispensed = quantityDispensed; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public String getInsurancePolicyNo() { return insurancePolicyNo; }
    public void setInsurancePolicyNo(String insurancePolicyNo) { this.insurancePolicyNo = insurancePolicyNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }

    public User getDispensedBy() { return dispensedBy; }
    public void setDispensedBy(User dispensedBy) { this.dispensedBy = dispensedBy; }
}
