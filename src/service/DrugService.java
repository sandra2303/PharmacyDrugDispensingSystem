package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Drug;

public interface DrugService extends Remote {
    Drug save(Drug drug) throws RemoteException;
    Drug update(Drug drug) throws RemoteException;
    Drug delete(Drug drug) throws RemoteException;
    Drug findById(int id) throws RemoteException;
    Drug findByDrugCode(String drugCode) throws RemoteException;
    List<Drug> findAll() throws RemoteException;
    List<Drug> findLowStock() throws RemoteException;
    List<Drug> findExpiredDrugs() throws RemoteException;
    List<Drug> search(String keyword) throws RemoteException;
}
