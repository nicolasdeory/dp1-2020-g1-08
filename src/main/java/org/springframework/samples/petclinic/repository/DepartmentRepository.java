package org.springframework.samples.petclinic.repository;

import java.util.Collection;

import org.springframework.dao.DataAccessException;

import org.springframework.data.repository.Repository;
import org.springframework.samples.petclinic.model.*;

public interface DepartmentRepository extends Repository<Department, String> {
	    /**
	     * Save a <code>Department</code> to the data store, either inserting or updating it.
	     * @param departments the <code>Department</code> to save
	     * @see BaseEntity#isNew
	     */
	    void save(Department departments) throws DataAccessException;
	    void deleteById(String name) throws DataAccessException;
	    public Department findById(String name);

	    Collection<Department> findAll() throws DataAccessException;;
	    


	}


