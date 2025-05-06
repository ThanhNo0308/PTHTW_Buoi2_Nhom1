/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.repository.UserRepository;
import jakarta.persistence.NoResultException;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRepositoryImp implements UserRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    @Autowired
    private BCryptPasswordEncoder passEncoder;

    @Override
    public User getUserByUsername(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        query.select(root);
        query.where(builder.equal(root.get("username"), username));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public User getUserByEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(builder.equal(root.get("email"), email));

            return session.createQuery(query).uniqueResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public User addUser(User u) {
        Session s = this.factory.getObject().getCurrentSession();
        s.save(u);

        return u;
    }

    @Override
    public boolean findEmail(String email) {
        Session s = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        query.select(root);
        query.where(builder.equal(root.get("email"), email));

        List<Student> students = s.createQuery(query).getResultList();

        return !students.isEmpty();
    }

    @Override
    public boolean isUsernameExists(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        query.select(builder.count(root));
        query.where(builder.equal(root.get("username"), username));

        Long count = session.createQuery(query).getSingleResult();
        return count > 0;
    }

    @Override
    public boolean isEmailExistsInUserTable(String email) {
        Session s = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        query.select(builder.count(root));
        query.where(builder.equal(root.get("email"), email));

        Long count = s.createQuery(query).getSingleResult();
        return count > 0;
    }

    @Override
    public boolean findTeacherEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Teacher> root = query.from(Teacher.class);

        query.select(builder.count(root));
        query.where(builder.equal(root.get("email"), email));

        Long count = session.createQuery(query).getSingleResult();

        return count > 0;
    }

    @Override
    public List<Teacher> getTeacherByEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Teacher> query = builder.createQuery(Teacher.class);
        Root<Teacher> root = query.from(Teacher.class);

        query.select(root);
        query.where(builder.equal(root.get("email"), email));

        return session.createQuery(query).getResultList();
    }

    @Override
    public User addTeacherUser(User user) {
        Session s = this.factory.getObject().getCurrentSession();
        s.save(user);

        return user;
    }

    @Override
    public boolean authAdminUser(String username, String password) {
        User user = this.getUserByUsername(username);
        if (user != null) {
            return user.getRole() == User.Role.Admin
                    && passEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    @Override
    public boolean authTeacherUser(String username, String password) {
        User user = this.getUserByUsername(username);
        if (user != null) {
            return user.getRole() == User.Role.Teacher
                    && passEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    @Override
    public boolean authStudentUser(String username, String password) {
        User user = this.getUserByUsername(username);
        if (user != null) {
            return user.getRole() == User.Role.Student
                    && passEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    @Override
    public List<User> getUsers() {
        Session s = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        query.select(root);
        query.orderBy(builder.asc(root.get("id")));

        return s.createQuery(query).getResultList();
    }

    @Override
    public User getUserById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(User.class, id);
    }

    @Override
    public boolean updateUser(User user) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            // Lấy người dùng hiện tại từ database
            User existingUser = this.getUserById(user.getId());
            if (existingUser == null) {
                return false;
            }

            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }

            if (user.getGender() != null) {
                existingUser.setGender(user.getGender());
            }

            if (user.getHometown() != null) {
                existingUser.setHometown(user.getHometown());
            }

            if (user.getBirthdate() != null) {
                existingUser.setBirthdate(user.getBirthdate());
            }

            if (user.getPhone() != null) {
                existingUser.setPhone(user.getPhone());
            }

            if (user.getIdentifyCard() != null) {
                existingUser.setIdentifyCard(user.getIdentifyCard());
            }

            if (user.getImage() != null && !user.getImage().isEmpty()) {
                existingUser.setImage(user.getImage());
            }

            if (user.getPassword() != null) {
                existingUser.setPassword(user.getPassword());
            }

            // Nếu thực hiện cập nhật trạng thái active
            if (user.getActive() != null) {
                existingUser.setActive(user.getActive());
            }

            // Lưu đối tượng đã cập nhật vào cơ sở dữ liệu
            s.saveOrUpdate(existingUser);
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteUser(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            User user = s.get(User.class, id);
            if (user != null) {
                s.delete(user);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveUser(User user) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            session.saveOrUpdate(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getUsersByRole(String role) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            // Chuyển đổi chuỗi role thành Enum User.Role
            User.Role roleEnum = User.Role.valueOf(role);

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
            Root<User> root = query.from(User.class);

            query.multiselect(
                    root.get("id"),
                    root.get("username"),
                    root.get("email")
            );
            query.where(builder.equal(root.get("role"), roleEnum));

            List<Object[]> results = session.createQuery(query).getResultList();
            List<Map<String, Object>> users = new ArrayList<>();

            for (Object[] row : results) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", row[0]);
                user.put("username", row[1]);
                user.put("email", row[2]);

                users.add(user);
            }

            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
