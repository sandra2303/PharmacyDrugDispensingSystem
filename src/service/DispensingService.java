package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Dispensing;

public interface DispensingService extends Remote {
    Dispensing save(Dispensing dispensing) throws RemoteException;
    Dispensing update(Dispensing dispensing) throws RemoteException;
    Dispensing delete(Dispensing dispensing) throws RemoteException;
    Dispensing findById(int id) throws RemoteException;
    List<Dispensing> findAll() throws RemoteException;
    List<Dispensing> findByPharmacist(int userId) throws RemoteException;
    List<Dispensing> findByDateRange(String startDate, String endDate) throws RemoteException;
    double getTotalRevenue() throws RemoteException;
}
