package worker.aims.service.itf;

import org.apache.ibatis.annotations.Param;
import worker.aims.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface UserService {

    Map<String, Object> register(String username, String password, String email, String phone, String fullName, String tempToken);

    Map<String, Object> login(String username, String password);

    User getUserByUid(Integer uid);

    User getUserByUidAndFactoryId(Integer uid, String factoryId);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    void changeUsername(Integer uid, String username);

    void changePassword(Integer uid, String opassword, String npassword);

    void resetPassword(Integer uid, String password);

    void changeEmail(Integer uid, String email);

    void updateFullName(Integer uid, String fullName);

    void updatePhone(Integer uid, String phone);

    void updateDepartment(Integer uid, String department);

    void updateRole(Integer uid, String role);

    void updatePermissions(Integer uid, String permissions);

    void updateIsActive(Integer uid, boolean isActive);

    void updatePosition(Integer uid, String position);

    void updateFactoryId(Integer uid, String factoryId);

    void deleteUserByUid(Integer uid);

    List<User> getAllUsers();

    List<User> getPendingUsers(Boolean isActive, String factoryId);

    Map<String, Object> getUserStats(String factoryId);
}

