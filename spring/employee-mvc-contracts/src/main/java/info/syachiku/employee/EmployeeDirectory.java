package info.syachiku.employee;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeDirectory {
    private final ConcurrentHashMap<Long, Employee> employees = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(10);

    public EmployeeDirectory() {
        employees.put(10L, new Employee(10, "田中", "ACTIVE", "staff-only"));
    }

    public List<Employee> findAll() {
        return employees.values().stream().sorted(Comparator.comparingLong(Employee::id)).toList();
    }

    public Employee find(long id) {
        var employee = employees.get(id);
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return employee;
    }

    public Employee create(String name) {
        long id = nextId.incrementAndGet();
        var employee = new Employee(id, name, "ACTIVE", "staff-only");
        employees.put(id, employee);
        return employee;
    }

    public void delete(long id) {
        if (employees.remove(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

record Employee(long id, String name, String status, String internalNote) {
}
