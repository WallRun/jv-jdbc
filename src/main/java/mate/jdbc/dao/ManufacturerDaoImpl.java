package mate.jdbc.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mate.jdbc.exception.DataProcessingException;
import mate.jdbc.lib.Dao;
import mate.jdbc.model.Manufacturer;
import mate.jdbc.util.ConnectionUtil;

@Dao
public class ManufacturerDaoImpl implements ManufacturerDao {
    @Override
    public Manufacturer create(Manufacturer manufacturer) {
        String query = "INSERT INTO manufacturers "
                + "(name, country, is_deleted) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement
                 = ConnectionUtil.getConnection()
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, manufacturer.getName());
            preparedStatement.setString(2, manufacturer.getCountry());
            preparedStatement.setBoolean(3, false);

            preparedStatement.execute();
            ResultSet keys = preparedStatement.getGeneratedKeys();
            if (keys.next()) {
                manufacturer.setId(keys.getLong(1));
            }
            return manufacturer;
        } catch (Exception e) {
            throw new DataProcessingException("Can't create manufacturer", e);
        }
    }

    @Override
    public Optional<Manufacturer> get(Long id) {
        String query = "SELECT * FROM manufacturers "
                + "WHERE id = ? AND is_deleted = FALSE";
        try (PreparedStatement preparedStatement = ConnectionUtil.getConnection()
                .prepareStatement(query)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getManufacturerFromResultSet(resultSet));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new DataProcessingException("Can't get manufacturer by id: " + id, e);
        }
    }

    @Override
    public List<Manufacturer> getAll() {
        String query = "SELECT * FROM manufacturers "
                + "WHERE is_deleted = FALSE";
        List<Manufacturer> manufacturers = new ArrayList<>();
        try (PreparedStatement preparedStatement = ConnectionUtil.getConnection()
                .prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                manufacturers.add(getManufacturerFromResultSet(resultSet));
            }
        } catch (Exception e) {
            throw new DataProcessingException("Can't get all manufacturers", e);
        }

        return manufacturers;
    }

    @Override
    public Manufacturer update(Manufacturer manufacturer) {
        String query = "UPDATE manufacturers SET name = ?, country = ? WHERE id = ?";
        try (PreparedStatement preparedStatement
                 = ConnectionUtil.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, manufacturer.getName());
            preparedStatement.setString(2, manufacturer.getCountry());
            preparedStatement.setLong(3, manufacturer.getId());
            preparedStatement.executeUpdate();

            return manufacturer;
        } catch (Exception e) {
            throw new DataProcessingException("error updating manufacturer with id: "
                + manufacturer.getId(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String query = "UPDATE manufacturers SET is_deleted = TRUE WHERE id = ?";
        try (PreparedStatement preparedStatement
                 = ConnectionUtil.getConnection().prepareStatement(query)) {
            preparedStatement.setLong(1, id);
            int result = preparedStatement.executeUpdate();
            return result != 0;
        } catch (Exception e) {
            throw new DataProcessingException("Can't delete manufacturer by id: " + id, e);
        }
    }

    private Manufacturer getManufacturerFromResultSet(ResultSet resultSet) {
        Manufacturer manufacturer = new Manufacturer();
        try {
            manufacturer.setId(resultSet.getObject("id", Long.class));
            manufacturer.setName(resultSet.getString("name"));
            manufacturer.setCountry(resultSet.getString("country"));
        } catch (Exception e) {
            throw new DataProcessingException("Can't get manufacturer from result set", e);
        }

        return manufacturer;
    }
}