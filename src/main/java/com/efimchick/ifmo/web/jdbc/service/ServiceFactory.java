package com.efimchick.ifmo.web.jdbc.service;

import com.efimchick.ifmo.web.jdbc.ConnectionSource;
import com.efimchick.ifmo.web.jdbc.domain.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class ServiceFactory
{
    public EmployeeService employeeService()
    {
        return new EmployeeService()
        {
            private List<Employee> writePage(List<Employee> someEmp, Paging paging)
            {
                return someEmp.subList(paging.itemPerPage * (paging.page - 1),
                        min(someEmp.size(), paging.itemPerPage * paging.page));
            }

            private ResultSet drawResultList(String request)
            {
                try
                {
                    return ConnectionSource.instance().createConnection().createStatement().executeQuery(request);
                } catch (SQLException e)
                {
                    return null;
                }
            }

            private List<Employee> groupSomeEmps(ResultSet RS)
            {
                List<Employee> someEmp = new ArrayList<>();
                try
                {
                    while (RS.next())
                        someEmp.add(bornEmployee(RS, false, false));
                } catch (SQLException e)
                {
                }
                return someEmp;
            }

            private Department buildDepartment(ResultSet RS)
            {
                try
                {
                    return new Department(
                            new BigInteger(RS.getString("ID")),
                            RS.getString("NAME"),
                            RS.getString("LOCATION"));

                } catch (SQLException e)
                {
                    return null;
                }
            }

            private Employee bornEmployee(ResultSet RS, boolean isManager, boolean isFullChain)
            {
                try
                {
                    Employee man = null;
                    if (!isManager)
                    {
                        ResultSet manRS = drawResultList("SELECT * FROM employee WHERE id = " + RS.getInt("MANAGER"));

                        if (manRS.next())
                            man = bornEmployee(manRS, !isFullChain, isFullChain);
                    }

                    Department dep = null;

                    ResultSet depRS = drawResultList("SELECT * FROM department WHERE id = " + RS.getInt("DEPARTMENT"));

                    if (depRS.next())
                        dep = buildDepartment(depRS);


                    return new Employee(
                            new BigInteger(RS.getString("ID")),
                            new FullName(
                                    RS.getString("FIRSTNAME"),
                                    RS.getString("LASTNAME"),
                                    RS.getString("MIDDLENAME")),
                            Position.valueOf(RS.getString("POSITION")),
                            LocalDate.parse(RS.getString("HIREDATE")),
                            RS.getBigDecimal("SALARY"),
                            man,
                            dep);

                } catch (SQLException e)
                {
                    return null;
                }
            }


            @Override
            public List<Employee> getAllSortByHireDate(Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee ORDER BY hiredate")), paging);
            }

            @Override
            public List<Employee> getAllSortByLastname(Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee ORDER BY lastname")), paging);
            }

            @Override
            public List<Employee> getAllSortBySalary(Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee ORDER BY salary")), paging);
            }

            @Override
            public List<Employee> getAllSortByDepartmentNameAndLastname(Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee ORDER BY department, lastname")), paging);
            }

            @Override
            public List<Employee> getByDepartmentSortByHireDate(Department department, Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY hiredate")), paging);
            }

            @Override
            public List<Employee> getByDepartmentSortBySalary(Department department, Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY salary")), paging);
            }

            @Override
            public List<Employee> getByDepartmentSortByLastname(Department department, Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY lastname")), paging);
            }

            @Override
            public List<Employee> getByManagerSortByLastname(Employee manager, Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee WHERE manager = " + manager.getId() + "ORDER BY lastname")), paging);
            }

            @Override
            public List<Employee> getByManagerSortByHireDate(Employee manager, Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee WHERE manager = " + manager.getId() + "ORDER BY hiredate")), paging);
            }

            @Override
            public List<Employee> getByManagerSortBySalary(Employee manager, Paging paging)
            {
                return writePage(groupSomeEmps(drawResultList("SELECT * FROM employee WHERE manager = " + manager.getId() + "ORDER BY salary")), paging);
            }

            @Override
            public Employee getWithDepartmentAndFullManagerChain(Employee employee)
            {
                try
                {
                    ResultSet RS = drawResultList("SELECT * FROM employee WHERE id = " + employee.getId());

                    if (RS.next())
                        return bornEmployee(RS, false, true);
                } catch (SQLException e)
                {
                }
                return null;
            }

            @Override
            public Employee getTopNthBySalaryByDepartment(int salaryRank, Department department)
            {
                return groupSomeEmps(drawResultList("SELECT * FROM employee WHERE department = " + department.getId() + "ORDER BY salary DESC"))
                        .get(salaryRank - 1);
            }
        };
    }
}