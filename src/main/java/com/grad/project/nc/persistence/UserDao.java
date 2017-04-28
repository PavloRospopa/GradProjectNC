package com.grad.project.nc.persistence;

import com.grad.project.nc.model.Role;
import com.grad.project.nc.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
public class UserDao extends AbstractDao<User> {

    CrudDao<Role> rolesDao;

    @Autowired
    UserDao(JdbcTemplate jdbcTemplate, CrudDao<Role> rolesDao) {
        super(jdbcTemplate);

        this.rolesDao = rolesDao;
    }

    @Override
    public User add(User user){

        //TODO add roles saving

        KeyHolder keyHolder = executeInsert(connection -> {
            String statement = "INSERT INTO \"user\" (email, password, first_name, last_name, phone_number)" +
                    " VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getFirstName());
            preparedStatement.setString(4, user.getLastName());
            preparedStatement.setString(5, user.getPhoneNumber());

            return preparedStatement;
        });

        user.setUserId(getLongValue(keyHolder, "user_id"));

        return user;
    }

    @Override
    public User update(User user) {

        executeUpdate(connection -> {
            String query = "UPDATE \"user\" SET email = ?, password = ?," +
                "first_name = ? , last_name = ? , phone_number = ? WHERE user_id = ? ";

            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getFirstName());
            preparedStatement.setString(4, user.getLastName());
            preparedStatement.setString(5, user.getPhoneNumber());

            return preparedStatement;
        });

        return user;
    }

    @Override
    public User find(long id) {
        return findOne(connection -> {
            String statement = "SELECT user_id, email, password, first_name, last_name, phone_number FROM \"user\" WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            preparedStatement.setLong(1, id);

            return preparedStatement;
        }, new UserRowMapper());
    }

    @Override
    @Transactional
    public Collection<User> findAll() {
        return findMultiple(connection -> {
            String statement = "SELECT user_id, email, password, first_name, last_name, phone_number FROM \"user\"";
            return connection.prepareStatement(statement);
        }, new UserRowMapper());
    }

    @Override
    public void delete(User user) {
        executeUpdate(connection -> {
            String statement = "DELETE FROM \"user\" WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            preparedStatement.setLong(1, user.getUserId());
            return preparedStatement;
        });
    }

    @Transactional
    public Optional<User> findByEmail(String email) {
        User result = findOne(connection -> {
            String statement = "SELECT user_id, email, password, first_name, last_name, phone_number FROM \"user\" WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(statement);

            preparedStatement.setString(1, email);

            return preparedStatement;
        }, new UserRowMapper());

        return Optional.of(result);
    }
/*
    @Transactional
    public void addUserRole(User user, Role role){

        SimpleJdbcInsert insertUserQuery = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_role");

        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("user_id", user.getUser_id());
        parameters.put("role_id", role.getRoleId());

        insertUserQuery.execute(parameters);
    }
*/
    private class UserProxy extends User {
        @Override
        public List<Role> getRoles() {
            if (super.getRoles() == null) {
                Collection<Role> roles = findMultiple(connection -> {
                    String query = "SELECT role.role_id, role.role_name FROM role " +
                            "INNER JOIN user_role ON role.role_id = user_role.role_id WHERE user_role.user_id = ?";

                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setLong(1, getUserId());

                    return statement;
                }, new RoleDao.RoleRowMapper());

                super.setRoles(new LinkedList<>(roles));
            }

            return super.getRoles();
        }
    }

    private final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new UserProxy();

            user.setUserId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setPhoneNumber(rs.getString("phone_number"));

            return user;
        }
    }
}
