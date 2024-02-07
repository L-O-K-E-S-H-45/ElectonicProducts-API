package com.flipkart_clone.serviceimplementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.flipkart_clone.entities.Customer;
import com.flipkart_clone.entities.Seller;
import com.flipkart_clone.entities.User;
import com.flipkart_clone.enums.UserRole;
import com.flipkart_clone.exception.IllegalRequestException;
import com.flipkart_clone.exception.UserNotFoundByEmailException;
import com.flipkart_clone.repository.CustomerRepository;
import com.flipkart_clone.repository.SellerRepository;
import com.flipkart_clone.repository.UserRepository;
import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.service.AuthService;
import com.flipkart_clone.util.ResponseStructure;

import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {

//	@Autowired // (this will do field injection, not good practice, better use constructor injection)
//	private UserRepository userRepo;
	
	private UserRepository userRepo;
	
	private SellerRepository sellerRepo;
	
	private CustomerRepository customerRepo;
	
	private ResponseStructure<UserResponse> structure;
	
	public <T extends User>T mapUserRequestToUserObject(UserRequest userRequest){
		User user=null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER ->{user = new Customer();}
		case SELLER -> {user = new Seller();}
		}
		
		user.setUserName(userRequest.getEmail().substring(0,userRequest.getEmail().indexOf('@')));
//		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
		user.setUserRole(userRequest.getUserRole());
		
		return (T) user;
		
	}
	
	public UserResponse mapUserObjectToUserResponse(User user){
		return UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.email(user.getEmail().toLowerCase())
				.userRole(user.getUserRole())
				.build();
	}
	
	private User saveUser(User user) {
		System.out.println(user.getUserId()+" ---------");
		switch (user.getUserRole()) {
		case CUSTOMER -> {customerRepo.save((Customer) user);}
		case SELLER -> {sellerRepo.save((Seller)user);}
		default -> throw new IllegalRequestException("Falied to register User b/z Invalid UserRole : "+user.getUserRole());
		}
		System.out.println(user.getUserId()+" ++++++++++");
		return user;
	}
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest userRequest) {
		if (userRepo.existsByEmail(userRequest.getEmail().toLowerCase())) 
			throw new UserNotFoundByEmailException("Failed to register User");
		
//		if (!userRequest.getUserRole().equals(UserRole.SELLER) && !userRequest.getUserRole().equals(UserRole.CUSTOMER))
//			throw new IllegalRequestException("Falied to register User b/z Invalid UserRole : "+userRequest.getUserRole());     
		
		User user = mapUserRequestToUserObject(userRequest);
		user = saveUser(user);
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(
				structure.setStatus(HttpStatus.ACCEPTED.value())
				.setMessage(userRequest.getUserRole()+" registered successfully, kindly verify your email by "
						+ "OTP sent to your email")
				.setData(mapUserObjectToUserResponse(user)), HttpStatus.ACCEPTED);
		
	}

}









