package com.efimchick.ifmo.web.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.efimchick.ifmo.web.jdbc.domain.*;

public class SetMapperFactory
{

    public SetMapper<Set<Employee>> employeesSetMapper()
    {

        SetMapper<Set<Employee>> resultMap = new SetMapper<Set<Employee>>()
        {
            @Override
            public Set<Employee> mapSet(ResultSet resultSet)
            {
                Set<Employee> lilSet = new HashSet<Employee>();
                try
                {
                    while (resultSet.next())
                    {
                        Employee newOne = EmployeeBorn(resultSet);
                        lilSet.add(newOne);

                    }
                } catch (SQLException e)
                {
                    System.out.println(e.getMessage());
                }
                return lilSet;
            }
        };

        return resultMap;
    }

    private Employee EmployeeBorn(ResultSet resultSet)
    {
        try
        {
            BigInteger id = new BigInteger(resultSet.getString("ID"));
            FullName fullName = new FullName(resultSet.getString("FIRSTNAME"), resultSet.getString("LASTNAME"), resultSet.getString("MIDDLENAME"));
            Position position = Position.valueOf(resultSet.getString("POSITION"));
            LocalDate hireDate = LocalDate.parse(resultSet.getString("HIREDATE"));
            BigDecimal salary = resultSet.getBigDecimal("SALARY");
            Employee manager = null;
            if (resultSet.getString("MANAGER") != null)
            {
                int managerId = Integer.valueOf(resultSet.getString("MANAGER"));

                int curRow = resultSet.getRow();
                if (!resultSet.isBeforeFirst())
                    resultSet.beforeFirst();

                while (resultSet.next())
                {
                    if (Integer.valueOf(resultSet.getString("ID")) == managerId)
                        manager = EmployeeBorn(resultSet);
                }
                resultSet.absolute(curRow);
            }

            return new Employee(id, fullName, position, hireDate, salary, manager);
        } catch (SQLException e)
        {
            return null;
        }

    }
}
