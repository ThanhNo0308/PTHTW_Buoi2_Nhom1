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
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private Environment env;
    @Autowired
    private BCryptPasswordEncoder passEncoder;
    @Autowired
    private UserRepository userRepository;

    @Override
    public User getUserByUsername(String username) {
        Session session = this.factory.getObject().getCurrentSession();
        String hql = "FROM User u WHERE u.username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);

        try {
            return (User) query.getSingleResult();
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
    public boolean authUser(String username, String password) {
        User u = this.getUserByUsername(username);

        return this.passEncoder.matches(password, u.getPassword());
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
        Query q = s.createQuery("FROM Student WHERE email=:email");
        q.setParameter("email", email);

        // Lấy danh sách kết quả
        List<Student> students = q.getResultList();

        // Kiểm tra xem danh sách có phần tử nào không
        return !students.isEmpty();
    }

    @Override
    public boolean findTeacherEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();
        Query query = session.createQuery("FROM Teacher WHERE email = :email", Teacher.class);
        query.setParameter("email", email);

        List<Teacher> teachers = query.getResultList();

        return !teachers.isEmpty();

    }

    @Override
    public List<Teacher> getTeacherByEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();
        Query query = session.createQuery("FROM Teacher WHERE email = :email", Teacher.class);
        query.setParameter("email", email);

        List<Teacher> teachers = query.getResultList();

        return teachers;
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
        jakarta.persistence.TypedQuery<User> q = s.createQuery("FROM User ORDER BY id", User.class);
        return q.getResultList();
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
            this.userRepository.saveUser(existingUser);
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

}
