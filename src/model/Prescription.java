package model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "prescriptions")
public class Prescription implements Serializable {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String prescriptionNumber;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private String doctorLicenseNo;

    @Column(nullable = false)
    private String issuedDate; // yyyy-MM-dd

    @Column(nullable = false)
    private String expiryDate; // yyyy-MM-dd (prescriptions expire)

    @Column(nullable = false)
    private String status; // PENDING, DISPENSED, CANCELLED, EXPIRED

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Many Prescriptions -> One Patient
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Many Prescriptions <-> Many Drugs
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "prescription_drugs",
        joinColumns = @JoinColumn(name = "prescription_id"),
        inverseJoinColumns = @JoinColumn(name = "drug_id")
    )
    private List<Drug> drugs;

    // One Prescription -> One Dispensing
    @OneToOne(mappedBy = "prescription", fetch = FetchType.LAZY)
    private Dispensing dispensing;

    public Prescription() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPrescriptionNumber() { return prescriptionNumber; }
    public void setPrescriptionNumber(String prescriptionNumber) { this.prescriptionNumber = prescriptionNumber; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorLicenseNo() { return doctorLicenseNo; }
    public void setDoctorLicenseNo(String doctorLicenseNo) { this.doctorLicenseNo = doctorLicenseNo; }

    public String getIssuedDate() { return issuedDate; }
    public void setIssuedDate(String issuedDate) { this.issuedDate = issuedDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public List<Drug> getDrugs() { return drugs; }
    public void setDrugs(List<Drug> drugs) { this.drugs = drugs; }

    public Dispensing getDispensing() { return dispensing; }
    public void setDispensing(Dispensing dispensing) { this.dispensing = dispensing; }
}
