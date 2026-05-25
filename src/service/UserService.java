package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.User;

public interface UserService extends Remote {
    User save(User user) throws RemoteException;
    User update(User user) throws RemoteException;
    User delete(User user) throws RemoteException;
    User findById(int id) throws RemoteException;
    User login(String username, String password) throws RemoteException;
    User findByUsername(String username) throws RemoteException;
    List<User> findAll() throws RemoteException;
}
