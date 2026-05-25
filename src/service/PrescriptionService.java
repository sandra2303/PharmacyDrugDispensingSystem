package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Prescription;

public interface PrescriptionService extends Remote {
    Prescription save(Prescription prescription) throws RemoteException;
    Prescription update(Prescription prescription) throws RemoteException;
    Prescription delete(Prescription prescription) throws RemoteException;
    Prescription findById(int id) throws RemoteException;
    Prescription findByPrescriptionNumber(String number) throws RemoteException;
    List<Prescription> findAll() throws RemoteException;
    List<Prescription> findByPatientId(int patientId) throws RemoteException;
    List<Prescription> findPending() throws RemoteException;
}
