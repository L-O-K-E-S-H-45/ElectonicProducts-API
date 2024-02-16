package com.flipkart_clone.requestdtos;

import com.flipkart_clone.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
	
	@NotNull(message = "User Email cannot be null!!!")
	@NotBlank(message = "User Email cannot be blank!!!")
	@Email(regexp = "[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}", message = "Invalid Email ")
	private String email;
	
	@NotNull(message = "User password cannot be null!!!")
	@NotBlank(message = "User password cannot be blank!!!")
	@Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters") 
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must"
			+ " contain at least one uppercase letter, one lowercase letter, one number, one special character")
	private String password;
	
	@NotNull(message = "UserRole cannot be null!!!")
	private String userRole;

}
