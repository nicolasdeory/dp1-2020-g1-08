package org.springframework.samples.petclinic.validation;

public class ToDoLimitMilestoneException extends Exception{
	public ToDoLimitMilestoneException() {
		super("Many toDos in one milestone");
	}
	
}
