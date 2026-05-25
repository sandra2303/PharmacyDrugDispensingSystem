package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Patient;

public interface PatientService extends Remote {
    Patient save(Patient patient) throws RemoteException;
    Patient update(Patient patient) throws RemoteException;
    Patient delete(Patient patient) throws RemoteException;
    Patient findById(int id) throws RemoteException;
    Patient findByNationalId(String nationalId) throws RemoteException;
    List<Patient> findAll() throws RemoteException;
    List<Patient> search(String keyword) throws RemoteException;
}
