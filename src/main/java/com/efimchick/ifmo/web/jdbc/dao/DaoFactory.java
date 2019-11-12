package com.efimchick.ifmo.web.jdbc.dao;

import com.efimchick.ifmo.web.jdbc.domain.Department;

import com.efimchick.ifmo.web.jdbc.domain.Employee;
import com.efimchick.ifmo.web.jdbc.ConnectionSource;
import com.efimchick.ifmo.web.jdbc.domain.FullName;
import com.efimchick.ifmo.web.jdbc.domain.Position;

import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

public class DaoFactory
{

    private Connection EstablishConnection()
    {

        try
        {
            ConnectionSource connectionSource = ConnectionSource.instance();
            return connectionSource.createConnection();
        } catch (SQLException e)
        {
            return null;
        }

    }

    private Employee EmployeeBorn(ResultSet RS) throws SQLException
    {
        return new Employee(
                BigInteger.valueOf(RS.getInt("ID")),
                new FullName(RS.getString("FIRSTNAME"),
                        RS.getString("LASTNAME"),
                        RS.getString("MIDDLENAME")),
                Position.valueOf(RS.getString("POSITION")),
                LocalDate.parse(RS.getString("HIREDATE")),
                RS.getBigDecimal("SALARY"),
                BigInteger.valueOf(RS.getInt("MANAGER")),
                BigInteger.valueOf(RS.getInt("DEPARTMENT"))
        );

    }

    private Department DepartmentBuild(ResultSet RS) throws SQLException
    {
        return new Department(
                new BigInteger(RS.getString("ID")),
                RS.getString("NAME"),
                RS.getString("LOCATION")
        );

    }

    public EmployeeDao employeeDAO()
    {
        return new EmployeeDao()
        {
            @Override
            public List<Employee> getByDepartment(Department department)
            {
                List<Employee> someEmp = new ArrayList<>();

                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("SELECT * FROM employee WHERE department = ?");
                    PS.setInt(1, department.getId().intValue());
                    ResultSet RS = PS.executeQuery();
                    while (RS.next())
                    {
                        someEmp.add(EmployeeBorn(RS));
                    }

                } catch (SQLException e)
                {
                }
                return someEmp;
            }

            @Override
            public List<Employee> getByManager(Employee employee)
            {
                List<Employee> someEmp = new ArrayList<>();

                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("SELECT * FROM employee WHERE manager = ?");
                    PS.setInt(1, employee.getId().intValue());
                    ResultSet RS = PS.executeQuery();
                    while (RS.next())
                    {
                        someEmp.add(EmployeeBorn(RS));
                    }

                } catch (SQLException e)
                {

                }
                return someEmp;
            }

            @Override
            public Optional<Employee> getById(BigInteger id)
            {
                Employee someEmp = null;

                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("SELECT * FROM employee WHERE id = ?");
                    PS.setInt(1, id.intValue());
                    ResultSet RS = PS.executeQuery();
                    while (RS.next())
                    {
                        someEmp = EmployeeBorn(RS);
                    }

                } catch (SQLException e)
                {

                }
                if (someEmp != null)
                    return Optional.of(someEmp);
                else
                    return Optional.empty();
            }

            @Override
            public List<Employee> getAll()
            {
                List<Employee> someEmp = new ArrayList<>();

                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("SELECT * FROM employee ");
                    ResultSet RS = PS.executeQuery();
                    while (RS.next())
                    {
                        someEmp.add(EmployeeBorn(RS));
                    }

                } catch (SQLException e)
                {

                }
                return someEmp;
            }

            @Override
            public Employee save(Employee employee)
            {
                Employee emp = null;
                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("INSERT INTO employee VALUES (?,?,?,?,?,?,?,?,?)");

                    PS.setInt(1, employee.getId().intValue());
                    PS.setString(2, employee.getFullName().getFirstName());
                    PS.setString(3, employee.getFullName().getLastName());
                    PS.setString(4, employee.getFullName().getMiddleName());
                    PS.setString(5, employee.getPosition().toString());
                    PS.setInt(6, employee.getManagerId().intValue());
                    PS.setDate(7, Date.valueOf(employee.getHired()));
                    PS.setDouble(8, employee.getSalary().doubleValue());
                    PS.setInt(9, employee.getDepartmentId().intValue());

                    PS.executeUpdate();
                    emp = employee;
                } catch (SQLException e)
                {
                }

                return emp;
            }

            @Override
            public void delete(Employee employee)
            {
                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("DELETE FROM employee WHERE ID = ?");
                    PS.setInt(1, employee.getId().intValue());
                    PS.executeUpdate();

                } catch (SQLException e)
                {
                }

            }
        };
    }

    public DepartmentDao departmentDAO()
    {
        return new DepartmentDao()
        {
            @Override
            public Optional<Department> getById(BigInteger id)
            {
                Department someDep = null;
                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("SELECT * FROM department WHERE id = ?");
                    PS.setInt(1, id.intValue());
                    ResultSet RS = PS.executeQuery();
                    while (RS.next())
                    {
                        someDep = DepartmentBuild(RS);
                    }

                } catch (SQLException e)
                {
                }
                if (someDep != null)
                    return Optional.of(someDep);
                else
                    return Optional.empty();
            }

            @Override
            public List<Department> getAll()
            {
                List<Department> someDep = new ArrayList<>();

                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("SELECT * FROM department ");
                    ResultSet RS = PS.executeQuery();
                    while (RS.next())
                    {
                        someDep.add(DepartmentBuild(RS));
                    }

                } catch (SQLException e)
                {

                }
                return someDep;
            }

            @Override
            public Department save(Department department)
            {
                Department dep = null;
                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS;
                    if (!getById(department.getId()).equals(Optional.empty()))
                    {
                        PS = connection.prepareStatement("UPDATE DEPARTMENT SET NAME = ?, LOCATION = ? WHERE ID = ?");
                        PS.setInt(3, department.getId().intValue());
                        PS.setString(1, department.getName());
                        PS.setString(2, department.getLocation());

                    } else
                    {
                        PS = connection.prepareStatement("INSERT INTO department VALUES (?,?,?)");
                        PS.setInt(1, department.getId().intValue());
                        PS.setString(2, department.getName());
                        PS.setString(3, department.getLocation());
                    }

                    PS.executeUpdate();
                    dep = department;

                } catch (SQLException e)
                {
                }

                return dep;
            }

            @Override
            public void delete(Department department)
            {
                Connection connection = EstablishConnection();
                try
                {
                    PreparedStatement PS = connection.prepareStatement("DELETE FROM department WHERE ID = ?");
                    PS.setInt(1, department.getId().intValue());
                    PS.executeUpdate();

                } catch (SQLException e)
                {
                }
            }
        };
    }
}
